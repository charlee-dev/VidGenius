package com.charleex.vidgenius.ui.features.process.section.local

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.charleex.vidgenius.datasource.db.Video
import com.charleex.vidgenius.datasource.db.YtVideo
import com.charleex.vidgenius.ui.components.AppOutlinedButton
import com.charleex.vidgenius.ui.components.SectionContainer

@Composable
fun LocalSection(
    videos: List<Video>,
    ytVideos: List<YtVideo>,
    onStartAll: () -> Unit,
    onStartOne: (Video) -> Unit,
    onDelete: (String) -> Unit,
) {
    SectionContainer(
        name = "Local videos: ${videos.size}",
        headerBgColor = Color.Green,
        isMainHeader = true,
        extra = {
            AnimatedVisibility(
                visible = videos.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AppOutlinedButton(
                    label = "Start All",
                    icon = Icons.Default.PlayArrow,
                    onClick = onStartAll,
                )
            }
        },
    ) {
        AnimatedVisibility(videos.none { !it.isCompleted }) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No local videos.",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(64.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            videos
                .filter { !it.isCompleted }
                .forEach { video ->
                    val isOnYT =
                        video.youtubeId in ytVideos.map { it.title }

                    LocalVideo(
                        video = video,
                        onDeleteClicked = {
                            onDelete(video.id)
                        },
                        onStartClicked = {
                            onStartOne(video)
                        },
                        isOnYt = isOnYT,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
        }
    }
}