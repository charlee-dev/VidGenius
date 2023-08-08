package com.charleex.vidgenius.ui.features.process.section.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import com.charleex.vidgenius.datasource.db.Video
import com.charleex.vidgenius.ui.components.AppOutlinedButton
import com.charleex.vidgenius.ui.components.LocalImage
import com.charleex.vidgenius.ui.components.SectionContainer
import com.charleex.vidgenius.ui.features.process.section.local.ContentText
import com.charleex.vidgenius.ui.util.pretty

@Composable
internal fun CompletedVideo(
    modifier: Modifier = Modifier,
    video: Video,
    isOnYt: Boolean = true,
    onDeleteClicked: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    SectionContainer(
        name = "${video.youtubeId} | ${video.contentInfo.enUS.title}",
        openInitially = false,
        modifier = modifier,
        extra = {
            AnimatedVisibility(!isOnYt) {
                AppOutlinedButton(
                    label = "Not on YT",
                    icon = Icons.Default.Check,
                    enabled = false,
                    onClick = {}
                )
            }
            SelectionContainer {
                Text(
                    text = video.modifiedAt.pretty(),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                )
            }
            if (showDeleteConfirm) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    AppOutlinedButton(
                        label = "YES",
                        icon = null,
                        bgColor = Color.Green,
                        labelColor = Color.Black,
                        iconTint = Color.Black,
                        onClick = {
                            onDeleteClicked()
                            showDeleteConfirm = false
                        },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    AppOutlinedButton(
                        label = "NO",
                        bgColor = Color.Red,
                        labelColor = Color.Black,
                        iconTint = Color.Black,
                        icon = null,
                        onClick = { showDeleteConfirm = false },
                    )
                }
            } else {
                AppOutlinedButton(
                    label = "Delete",
                    icon = Icons.Default.PlayArrow,
                    onClick = { showDeleteConfirm = true },
                )
            }
        }
    ) {
        ContentText(
            video = video,
            modifier = Modifier.padding(32.dp),
        )
    }
}