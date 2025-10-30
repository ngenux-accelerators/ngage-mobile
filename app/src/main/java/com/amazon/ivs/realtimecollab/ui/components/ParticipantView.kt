package com.amazon.ivs.realtimecollab.ui.components

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.core.handlers.stage.SELF_PARTICIPANT_ID
import com.amazon.ivs.realtimecollab.ui.theme.BlackSecondary
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.InterHint
import com.amazon.ivs.realtimecollab.ui.theme.OrangeSecondary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary
import com.amazonaws.ivs.broadcast.ImagePreviewView
import timber.log.Timber

const val PARTICIPANT_ASPECT_RATIO = 1.5f

@Composable
fun ParticipantView(
    participant: Participant,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    setAspectRatio: Boolean = true,
    showBackground: Boolean = true,
    showContent: Boolean = true,
    textSize: TextUnit = 14.sp,
    avatarSize: Dp = 48.dp,
    aspectRatio: Float = PARTICIPANT_ASPECT_RATIO,
) {
    val shape = RoundedCornerShape(30.dp)

    BoxWithConstraints(
        modifier = modifier
            .thenOptional(enabled = fillMaxWidth) {
                fillMaxWidth()
            }
            .thenOptional(enabled = setAspectRatio) {
                aspectRatio(ratio = aspectRatio)
            }
            .thenOptional(enabled = showBackground) {
                background(
                    color = BlackSecondary,
                    shape = shape,
                )
            }
            .clip(shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val width = density.run { constraints.minWidth.toDp() }
        val height = density.run { constraints.minHeight.toDp() }
        val showVideo = showContent && participant.isCameraOn

        @Composable
        fun Username(
            modifier: Modifier = Modifier,
            showShadow: Boolean = false,
        ) {
            var style = InterHint.copy(
                color = WhitePrimary,
                fontSize = textSize,
            )
            if (showShadow) {
                style = style.copy(
                    shadow = Shadow(
                        color = BlackTertiary.copy(alpha = 0.9f),
                        offset = Offset(0f, 2f),
                        blurRadius = 2f
                    )
                )
            }

            Text(
                modifier = modifier,
                text = participant.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = style,
                textAlign = TextAlign.Center,
            )
        }

        CrossfadeBox(
            modifier = Modifier
                .size(size = DpSize(width = width, height = height))
                .thenOptional(participant.isSpeaking) {
                    border(
                        width = 4.dp,
                        color = OrangeSecondary,
                        shape = shape,
                    )
                },
            isFirstContent = showVideo,
            firstContent = {
                if (!showVideo) return@CrossfadeBox
                VideoView(participant = participant)
                if (participant.isScreenSharing) return@CrossfadeBox
                Username(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    showShadow = true,
                )
            },
            secondContent = {
                ColumnRow(
                    isColumn = showContent,
                ) {
                    if (participant.isScreenSharing) {
                        val size = if (showContent) 40.dp else 28.dp

                        Icon(
                            modifier = Modifier.size(size),
                            painter = painterResource(R.drawable.ic_screenshare_disabled),
                            contentDescription = null,
                            tint = WhitePrimary,
                        )
                        return@ColumnRow
                    }
                    FadeBox(
                        isVisible = showContent
                    ) {
                        BoringAvatar(
                            name = participant.name,
                            avatarSize = avatarSize,
                        )
                    }
                    Username(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = when {
                                    showContent -> 0.dp
                                    else -> 16.dp
                                },
                                end = when {
                                    showContent -> 0.dp
                                    participant.isMicOn -> 16.dp
                                    else -> 34.dp
                                }
                            )
                    )
                }
            }
        )
        FadeBox(
            modifier = Modifier
                .align(if (showContent) Alignment.TopEnd else Alignment.CenterEnd)
                .padding(if (showContent) 20.dp else 8.dp),
            isVisible = !participant.isMicOn && !participant.isScreenSharing
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_mic_off),
                contentDescription = null,
                tint = WhitePrimary,
            )
        }
    }
}

@Composable
fun OtherParticipants(
    participants: List<Participant>,
    modifier: Modifier = Modifier,
    showContent: Boolean = true,
) {
    if (participants.size < 2) return
    val shape = RoundedCornerShape(30.dp)
    val isSpeaking = participants.any { it.isSpeaking }

    Box(
        modifier = modifier
            .background(
                color = BlackSecondary,
                shape = shape,
            )
            .clip(shape = shape)
            .thenOptional(enabled = isSpeaking) {
                border(
                    width = 4.dp,
                    color = OrangeSecondary,
                    shape = shape,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        ColumnRow(
            isColumn = showContent
        ) {
            FadeBox(
                isVisible = showContent,
                contentAlignment = Alignment.CenterStart,
            ) {
                participants.take(n = 5).forEachIndexed { index, participant ->
                    val showBorder = index != 0

                    BoringAvatar(
                        modifier = Modifier.padding(start = 16.dp * index),
                        name = participant.name,
                        avatarSize = if (showBorder) 28.dp else 23.dp,
                        showBorder = showBorder,
                    )
                }
            }
            Text(
                text = stringResource(R.string.other_participants, participants.size),
                style = InterHint.copy(color = WhitePrimary),
            )
        }
    }
}

@Composable
private fun VideoView(
    participant: Participant
) {
    val isPreview = LocalInspectionMode.current
    val isSelfVideoMirrored by StageHandler.isSelfVideoMirrored.collectAsStateWithLifecycle()
    var refreshVideo by remember { mutableStateOf(true) }
    var participantId by remember { mutableStateOf(participant.id) }
    var frameLayout by remember { mutableStateOf<FrameLayout?>(null) }

    fun getPreview() = frameLayout?.findViewById<ImagePreviewView?>(participantId.hashCode())

    fun dispose() {
        if (frameLayout == null) return
        Timber.d("Disposing video view for: $participantId")
        getPreview()?.run {
            Timber.d("Releasing preview: $id")
            surfaceTexture?.release()
            (parent as? ViewGroup)?.removeAllViews()
        }
        frameLayout?.removeAllViews()
        frameLayout = null
    }

    LaunchedEffect(key1 = participant.id) {
        participantId = participant.id
        Timber.d("Participant id updated: $participantId")
    }

    LaunchedEffect(key1 = isSelfVideoMirrored) {
        if (!participant.isSelf) return@LaunchedEffect
        Timber.d("Self video mirrored state updated: $isSelfVideoMirrored")
        getPreview()?.setMirrored(isSelfVideoMirrored)
    }

    LaunchedEffect(key1 = participant.isFilled) {
        Timber.d("Video fill mode updated: ${participant.isFilled}")
        refreshVideo = true
    }

    LaunchedEffect(key1 = participant.isCameraFlipped) {
        Timber.d("Video flip mode updated: ${participant.isCameraFlipped}")
        refreshVideo = true
    }

    LaunchedEffect(key1 = participant.stream) {
        Timber.d("Video stream updated: ${participant.stream}")
        refreshVideo = true
    }

    FadeBox(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        Timber.d("Double tap detected - toggling video fill mode for: $participantId")
                        StageHandler.toggleVideoFilled(id = participantId)
                    }
                )
            },
        isVisible = participant.isCameraOn || isPreview,
    ) {
        if (isPreview) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(
                    id = if (participant.isScreenSharing) {
                        R.drawable.bg_screenshare
                    } else {
                        R.drawable.bg_speaker
                    }
                ),
                contentDescription = null,
                contentScale = if (participant.isScreenSharing) {
                    if (participant.isFilled) {
                        ContentScale.Crop
                    } else {
                        ContentScale.Fit
                    }
                } else ContentScale.Crop,
            )
            return@FadeBox
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val layout = FrameLayout(context).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
                dispose()
                frameLayout = layout
                Timber.d("Creating video view for: $participantId")
                layout
            },
            update = { layout ->
                getPreview()?.run {
                    Timber.d("Preview view found for: $participantId, will refresh: $refreshVideo")
                    if (!refreshVideo) return@AndroidView
                }
                dispose()
                refreshVideo = false
                frameLayout = layout

                val preview = if (participant.isCameraOn) {
                    participant.preview ?: return@AndroidView
                } else {
                    return@AndroidView
                }
                preview.id = participantId.hashCode()

                Timber.d("Video view refreshed: ${preview.id} for: $participant")
                layout.addView(preview)
            }
        )
    }
}

@Preview
@Composable
private fun ParticipantViewPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val participant = Participant(
                id = "1",
                name = "username",
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ParticipantView(
                    modifier = Modifier.weight(1f),
                    participant = participant,
                    fillMaxWidth = false,
                )
                ParticipantView(
                    modifier = Modifier.weight(1f),
                    participant = participant.copy(
                        isCameraOn = true,
                        isSpeaker = true,
                        isMicOn = true,
                        id = SELF_PARTICIPANT_ID
                    ),
                    fillMaxWidth = false,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ParticipantView(
                    modifier = Modifier.weight(1f),
                    participant = participant.copy(
                        isScreenSharing = true,
                        isCameraOn = true,
                        isMicOn = false,
                        isFilled = false,
                    ),
                    fillMaxWidth = false,
                )
                ParticipantView(
                    modifier = Modifier.weight(1f),
                    participant = participant.copy(
                        isScreenSharing = true,
                        isFilled = false,
                        isCameraOn = false,
                    ),
                    fillMaxWidth = false,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ParticipantView(
                    modifier = Modifier.weight(1f),
                    participant = participant.copy(
                        isCameraOn = true,
                        isSpeaker = true,
                        isSpeaking = true,
                        isMicOn = true
                    ),
                    fillMaxWidth = false,
                )
                OtherParticipants(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(PARTICIPANT_ASPECT_RATIO),
                    participants = listOf(
                        participant.copy(name = "Eddy"),
                        participant.copy(name = "Uldis"),
                        participant.copy(name = "x"),
                        participant.copy(name = "y", isSpeaking = true),
                        participant.copy(name = "z"),
                    ),
                )
            }
            ParticipantView(
                participant = participant,
                avatarSize = 100.dp,
                textSize = 24.sp,
                showBackground = false,
                fillMaxWidth = false,
            )
            Row(
                modifier = Modifier.height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ParticipantView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    participant = participant.copy(
                        isScreenSharing = true,
                        isCameraOn = true,
                        isMicOn = false,
                        isFilled = false,
                    ),
                    fillMaxWidth = false,
                    showContent = false,
                    setAspectRatio = false,
                )
                ParticipantView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    participant = participant.copy(name = "eddy"),
                    fillMaxWidth = false,
                    showContent = false,
                    setAspectRatio = false,
                )
                OtherParticipants(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    participants = listOf(
                        participant.copy(name = "Eddy"),
                        participant.copy(name = "Uldis"),
                        participant.copy(name = "x"),
                        participant.copy(name = "y", isSpeaking = true),
                        participant.copy(name = "z"),
                    ),
                    showContent = false,
                )
                ParticipantView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    participant = participant.copy(isMicOn = true),
                    fillMaxWidth = false,
                    showContent = false,
                    setAspectRatio = false,
                )
            }
        }
    }
}
