package com.charleex.autoytvid.feature.videodetail

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.postInput
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import src.charleex.autoytvid.processor.FileProcessor
import src.charleex.autoytvid.processor.model.Video
import src.charleex.autoytvid.repository.YoutubeRepository

private typealias DragDropInputScope = InputHandlerScope<
        DragDropContract.Inputs,
        DragDropContract.Events,
        DragDropContract.State>

internal class DragDropInputHandler :
    KoinComponent,
    InputHandler<DragDropContract.Inputs, DragDropContract.Events, DragDropContract.State> {

    private val repository: YoutubeRepository by inject()
    private val fileProcessor: FileProcessor by inject()

    override suspend fun DragDropInputScope.handleInput(
        input: DragDropContract.Inputs,
    ) = when (input) {
        is DragDropContract.Inputs.GetFiles -> getFiles(input.anyList, fileProcessor)
        is DragDropContract.Inputs.Update -> when (input) {
            is DragDropContract.Inputs.Update.SetFiles -> updateState { it.copy(videos = input.videos) }
            is DragDropContract.Inputs.Update.SetLoading -> updateState { it.copy(loading = input.loading) }
        }

        is DragDropContract.Inputs.DeleteFile -> deleteFile(input.video)
    }
}

private suspend fun DragDropInputScope.deleteFile(video: Video) {
    val dragDropItems = getCurrentState().videos
    val newDragDropItems = dragDropItems - video
    postInput(DragDropContract.Inputs.Update.SetFiles(newDragDropItems))
}

private suspend fun DragDropInputScope.getFiles(anyList: List<*>, fileProcessor: FileProcessor) {
    try {
        val currentDragDropItems = getCurrentState().videos
        val videos = fileProcessor.processFileSystemItems(anyList)
        val combinedVideos = currentDragDropItems + videos
        postInput(DragDropContract.Inputs.Update.SetFiles(combinedVideos))
    } catch (e: Exception) {
        postEvent(DragDropContract.Events.ShowError(e.message ?: "Error while getting files"))
    }
}