package com.charleex.vidgenius.datasource

import co.touchlab.kermit.Logger
import com.benasher44.uuid.uuid4
import com.charleex.vidgenius.datasource.db.Video
import com.charleex.vidgenius.datasource.model.Screenshot
import com.hackathon.cda.repository.db.VidGeniusDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import src.charleex.vidgenius.processor.file.FileProcessor
import src.charleex.vidgenius.processor.screenshot.ScreenshotCapturing
import java.io.File

interface VideoRepository {
    suspend fun filterVideos(files: List<*>)
    fun getVideoDuration(videoId: String): Long
    fun flowOfVideo(videoId: String): Flow<Video>
    fun flowOfVideos(): Flow<List<Video>>
    fun deleteVideo(videoId: String)

    suspend fun captureScreenshots(videoId: String, timestamps: List<Long>): Flow<Float>
    fun deleteScreenshot(videoId: String, screenshotPath: String)
}

internal class VideoRepositoryImpl(
    private val logger: Logger,
    private val fileProcessor: FileProcessor,
    private val screenshotCapturing: ScreenshotCapturing,
    private val database: VidGeniusDatabase,
) : VideoRepository {
    override suspend fun filterVideos(files: List<*>) {
        logger.d("Getting videos from files")
        val videos = fileProcessor.filterVideoFiles(files)
        val localVideos = database.videoQueries.getAll().executeAsList()
        val filteredVideos = videos.filter {
            it.path !in localVideos.map { video -> video.path }
        }
        filteredVideos.forEach { video ->
            logger.d("Adding video ${video.absolutePath}")
            database.videoQueries.upsert(
                Video(
                    id = uuid4().toString(),
                    path = video.absolutePath,
                    duration = 0,
                    screenshots = emptyList(),
                    title = null,
                    description = null,
                    tags = emptyList(),
                    youtubeVideoId = null,
                    createdAt = Clock.System.now(),
                    modifiedAt = Clock.System.now(),
                )
            )
        }
    }

    override suspend fun captureScreenshots(videoId: String, timestamps: List<Long>): Flow<Float> = flow {
        logger.d("Getting screenshots from file")
        val video = getVideoById(videoId)
        val file = File(video.path)

        val screenshots = timestamps.mapIndexed { index: Int, timestamp: Long ->
            val screenshotFile = screenshotCapturing.captureScreenshot(file, timestamp, index)
            val screenshot = Screenshot(
                path = screenshotFile.absolutePath,
                videoId = video.id,
                description = video.description,
                createdAt = video.createdAt,
                modifiedAt = video.createdAt,
            )
            emit(index.toFloat() / timestamps.size)
            screenshot
        }
        val videoToUpdate = video.copy(
            screenshots = screenshots,
            modifiedAt = Clock.System.now(),
        )
        logger.d("Total screenshots: ${screenshots.size}")
        database.videoQueries.upsert(videoToUpdate)
    }

    override fun getVideoDuration(videoId: String): Long {
        val video = getVideoById(videoId)
        val file = File(video.path)
        if (!file.exists()) error("File does not exist")
        return screenshotCapturing.getVideoDuration(file)
    }

    override fun flowOfVideo(videoId: String): Flow<Video> {
        logger.d("Getting flow of video $videoId")
        return database.videoQueries.getById(videoId).asFlow()
            .map { it.executeAsOne() }
            .catch { logger.e(it) { "Error getting video $videoId: ${it.message}" } }
    }

    override fun flowOfVideos(): Flow<List<Video>> {
        logger.d("Getting flow of all videos")
        return database.videoQueries.getAll().asFlow()
            .map { it.executeAsList() }
            .catch { logger.e(it) { "Error getting videos: ${it.message}" } }
            .onEach { logger.d("Total videos: ${it.size}") }
    }

    override fun deleteVideo(videoId: String) {
        val video = getVideoById(videoId)
        logger.d("Deleting video ${video.path}")
        if (video.screenshots.isNotEmpty()) {
            logger.d("Video ${video.path} has screenshots")
            video.screenshots.forEach { screenshot ->
                deleteScreenshot(video.id, screenshot.id)
            }
        } else {
            logger.d("Video ${video.path} has no screenshots")
        }
        database.videoQueries.delete(video.id)
    }

    override fun deleteScreenshot(videoId: String, screenshotPath: String) {
        val video = getVideoById(videoId)
        val screenshot = video.screenshots.find { it.path == screenshotPath }
        if (screenshot == null) {
            logger.d("Screenshot $screenshotPath not found")
            return
        }
        logger.d("Deleting screenshot ${screenshot.id}")
        val screenshots = video.screenshots.ifEmpty { return }
        val updatedScreenshots = screenshots - screenshot
        val updatedVideo = video.copy(
            screenshots = updatedScreenshots,
            modifiedAt = Clock.System.now()
        )
        database.videoQueries.upsert(updatedVideo)
        fileProcessor.deleteFile(screenshot.path)
    }

    private fun getVideoById(videoId: String): Video {
        return database.videoQueries.getById(videoId).executeAsOne()
    }
}
