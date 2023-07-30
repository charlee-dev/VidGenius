package com.charleex.vidgenius.ui.features.process.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charleex.vidgenius.feature.process_video.model.ProgressState
import com.charleex.vidgenius.ui.components.AppCard
import com.charleex.vidgenius.ui.components.AppFlexSpacer
import com.charleex.vidgenius.ui.util.openInBrowser


@Composable
internal fun SectionContainer(
    modifier: Modifier = Modifier,
    name: String,
    progressState: ProgressState,
    isOpen: Boolean = false,
    onOpenClicked: () -> Unit,
    enabled: Boolean = true,
    block: @Composable ColumnScope.() -> Unit,
) {
    val indicationSource = remember { MutableInteractionSource() }
    val headerBgColor by animateColorAsState(
        targetValue = when (isOpen) {
            true -> MaterialTheme.colors.background.copy(alpha = 0.3f)
            false -> MaterialTheme.colors.surface
        }
    )

    AppCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SectionHeader(
                name = name,
                progressState = progressState,
                bgColor = headerBgColor,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = indicationSource,
                        onClick = onOpenClicked,
                        enabled = enabled,
                        role = Role.Button,
                    )
            )
            AnimatedVisibility(isOpen) {
                block()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    name: String,
    progressState: ProgressState,
    bgColor: Color,
) {
    var isLinkHovered by remember { mutableStateOf(false) }

    Surface(
        color = bgColor,
        elevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .onPointerEvent(PointerEventType.Enter) {
                isLinkHovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isLinkHovered = false
            }
            .pointerHoverIcon(if (isLinkHovered) PointerIcon.Hand else PointerIcon.Default)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 20.dp,
                    horizontal = 48.dp
                )
        ) {
            Text(
                text = name,
                color = MaterialTheme.colors.onSurface,
            )
            AppFlexSpacer()
            when (progressState) {
                ProgressState.None -> {
                    // No-op
                }

                ProgressState.Queued -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                    ) {
                        Text(
                            text = "Queued",
                            color = MaterialTheme.colors.onSurface,
                        )
                        CircularIcon(
                            imageVector = Icons.Outlined.Pending,
                            bgColor = Color.LightGray,
                            iconColor = Color.Black,
                        )
                    }
                }

                is ProgressState.InProgress -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = progressState.progress,
                            modifier = Modifier
                                .fillMaxWidth(.8f)
                        )
                        Text(
                            text = "Running",
                            color = MaterialTheme.colors.onSurface,
                        )
                        CircularProgressIndicator(
                            strokeWidth = 4.dp,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .size(28.dp)
                        )
                    }
                }

                is ProgressState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                    ) {
                        Text(
                            text = progressState.message ?: "Success",
                            color = MaterialTheme.colors.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable(
                                    enabled = progressState.message?.contains("http://") ?: false,
                                    onClick = {
                                        progressState.message?.let {
                                            openInBrowser(it)
                                        }
                                    }
                                )
                        )
                        CircularIcon(
                            imageVector = Icons.Default.Check,
                            bgColor = Color.Green,
                            iconColor = Color.Black,
                        )
                    }
                }

                is ProgressState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        AppFlexSpacer()
                        Text(
                            text = "Failed: ${progressState.message}",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                        CircularIcon(
                            imageVector = Icons.Default.Close,
                            bgColor = Color.Red,
                            iconColor = Color.White,
                        )
                    }
                }

                ProgressState.Cancelled -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        AppFlexSpacer()
                        Text(
                            text = "Cancelled",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface,
                        )
                        CircularIcon(
                            imageVector = Icons.Default.Cancel,
                            bgColor = Color.LightGray,
                            iconColor = Color.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    bgColor: Color = MaterialTheme.colors.surface,
    iconColor: Color = MaterialTheme.colors.onSurface,
) {
    Surface(
        color = bgColor,
        shape = CircleShape,
        elevation = 0.dp,
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "",
            tint = iconColor,
            modifier = Modifier
                .padding(2.dp)
                .graphicsLayer {
                    scaleX = 0.8f
                    scaleY = 0.8f
                }
        )
    }
}
