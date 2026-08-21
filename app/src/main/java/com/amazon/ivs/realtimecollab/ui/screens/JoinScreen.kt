package com.amazon.ivs.realtimecollab.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.core.handlers.stage.getSelfParticipant
import com.amazon.ivs.realtimecollab.ui.components.ButtonIcon
import com.amazon.ivs.realtimecollab.ui.components.ButtonText
import com.amazon.ivs.realtimecollab.ui.components.ColumnWithConstraints
import com.amazon.ivs.realtimecollab.ui.components.FadeBox
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.ParticipantView
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenBox
import com.amazon.ivs.realtimecollab.ui.components.StageIdBar
import com.amazon.ivs.realtimecollab.ui.components.fillMaxPortraitWidth
import com.amazon.ivs.realtimecollab.ui.components.isMultiWindow
import com.amazon.ivs.realtimecollab.ui.components.isPhoneLandscape
import com.amazon.ivs.realtimecollab.ui.components.isSquare
import com.amazon.ivs.realtimecollab.ui.components.isSquareOrPortrait
import com.amazon.ivs.realtimecollab.ui.components.thenOptional
import com.amazon.ivs.realtimecollab.ui.theme.BlackQuaternary
import com.amazon.ivs.realtimecollab.ui.theme.BlackSecondary
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.GrayTertiary
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary
import timber.log.Timber

@Composable
fun JoinScreen(
    destination: Destination,
) {
    val isVisible = destination == Destination.JoinScreen
    val meetingConfig by StageHandler.meetingConfig.collectAsStateWithLifecycle()
    val isMicOn by StageHandler.isMicOn.collectAsStateWithLifecycle()
    val isCameraOn by StageHandler.isCameraOn.collectAsStateWithLifecycle()
    val isSettingsOpen by StageHandler.isSettingsOpen.collectAsStateWithLifecycle()
    val isVoiceOnly by StageHandler.isVoiceOnly.collectAsStateWithLifecycle()
    val participants by StageHandler.participants.collectAsStateWithLifecycle()
    val participant = participants.getSelfParticipant()
    val stageId = meetingConfig.meetingId

    FadeBox(
        isVisible = isVisible,
    ) {
        JoinScreenContent(
            stageId = stageId,
            participant = participant,
            isMicOn = isMicOn,
            isCameraOn = isCameraOn,
            isSettingsOpen = isSettingsOpen,
            isVoiceOnly = isVoiceOnly,
        )
    }
}

@Composable
private fun JoinScreenContent(
    stageId: String,
    participant: Participant,
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isSettingsOpen: Boolean,
    isVoiceOnly: Boolean = false,
) {
    ScreenBox(
        contentAlignment = Alignment.Center,
    ) {
        val isPreview = LocalInspectionMode.current && isSquare()
        val density = LocalDensity.current
        val padding = 24.dp
        var screenHeight by remember {
            mutableStateOf(
                value = if (isPreview) 400.dp else 0.dp
            )
        }
        var participantContainerHeight by remember {
            mutableStateOf(
                value = if (isPreview) 200.dp else 0.dp
            )
        }
        var buttonContainerHeight by remember {
            mutableStateOf(
                value = if (isPreview) 200.dp else 0.dp
            )
        }
        val totalContentHeight = participantContainerHeight + buttonContainerHeight + padding
        val showPreview = if (isMultiWindow() || isPreview) {
            totalContentHeight < screenHeight
        } else true
        Timber.d("Show preview: $showPreview, height: $screenHeight, participant: $participantContainerHeight, buttons: $buttonContainerHeight")

        if (isSquareOrPortrait()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxPortraitWidth(maxWidth = 500.dp)
                    .onSizeChanged { size ->
                        screenHeight = density.run { size.height.toDp() }
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = buttonContainerHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    JoinScreenCameraPreview(
                        participant = participant,
                        isMicOn = isMicOn,
                        isCameraOn = isCameraOn,
                        isSettingsOpen = isSettingsOpen,
                        isVoiceOnly = isVoiceOnly,
                        showPreview = showPreview,
                        onHeightSet = { height ->
                            if (height > participantContainerHeight) {
                                participantContainerHeight = height
                            }
                        }
                    )
                }
                JoinScreenButtons(
                    modifier = Modifier
                        .fillMaxPortraitWidth(maxWidth = 446.dp)
                        .verticalScroll(state = rememberScrollState()),
                    isMicOn = isMicOn,
                    isCameraOn = isCameraOn,
                    isSettingsOpen = isSettingsOpen,
                    isVoiceOnly = isVoiceOnly,
                    showControls = !showPreview,
                    onHeightSet = { height ->
                        buttonContainerHeight = height
                    }
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(top = if (isPhoneLandscape()) 68.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(56.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxPortraitWidth(maxWidth = 500.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JoinScreenCameraPreview(
                        participant = participant,
                        isMicOn = isMicOn,
                        isCameraOn = isCameraOn,
                        isSettingsOpen = isSettingsOpen,
                        isVoiceOnly = isVoiceOnly,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxPortraitWidth(maxWidth = 335.dp)
                        .padding(bottom = 68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JoinScreenButtons(
                        isMicOn = isMicOn,
                        isCameraOn = isCameraOn,
                        isSettingsOpen = isSettingsOpen,
                        isVoiceOnly = isVoiceOnly,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ButtonIcon(
                modifier = Modifier.align(Alignment.CenterStart),
                icon = R.drawable.ic_back,
                background = Color.Transparent,
                onClick = NavigationHandler::goBack,
            )
            StageIdBar(
                stageId = stageId,
            )
        }
    }
}

@Composable
private fun VoiceOnlyPlaceholder(
    username: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(30.dp))
            .background(BlackSecondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = username,
            style = InterPrimary.copy(color = WhitePrimary),
        )
    }
}

@Composable
private fun JoinScreenCameraPreview(
    participant: Participant,
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isSettingsOpen: Boolean,
    isVoiceOnly: Boolean = false,
    showPreview: Boolean = true,
    onHeightSet: (Dp) -> Unit = {},
) {
    if (!showPreview) return
    ColumnWithConstraints(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        onSizeChanged = { _, maxHeight ->
            onHeightSet(maxHeight)
        },
    ) {
        if (isVoiceOnly) {
            VoiceOnlyPlaceholder(
                username = participant.name,
                modifier = Modifier.thenOptional(enabled = isPhoneLandscape()) {
                    weight(1f)
                        .aspectRatio(1.5f)
                },
            )
        } else {
            ParticipantView(
                modifier = Modifier.thenOptional(enabled = isPhoneLandscape()) {
                    weight(1f)
                        .aspectRatio(1.5f)
                },
                participant = participant,
                setAspectRatio = !isPhoneLandscape(),
            )
        }
        JoinStageControls(
            isMicOn = isMicOn,
            isCameraOn = isCameraOn,
            isSettingsOpen = isSettingsOpen,
            isVoiceOnly = isVoiceOnly,
        )
    }
}

@Composable
private fun JoinStageControls(
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isSettingsOpen: Boolean,
    isVoiceOnly: Boolean = false,
) {
    val micBackground by animateColorAsState(
        targetValue = if (isMicOn) GrayTertiary else GraySecondary,
    )
    val micIcon by animateColorAsState(
        targetValue = if (isMicOn) BlackTertiary else WhitePrimary,
    )
    val cameraBackground by animateColorAsState(
        targetValue = if (isCameraOn) GrayTertiary else GraySecondary,
    )
    val cameraIcon by animateColorAsState(
        targetValue = if (isCameraOn) BlackTertiary else WhitePrimary,
    )
    val settingsBackground by animateColorAsState(
        targetValue = if (isSettingsOpen) GrayTertiary else GraySecondary,
    )
    val settingsIcon by animateColorAsState(
        targetValue = if (isSettingsOpen) BlackTertiary else WhitePrimary,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterHorizontally
        ),
    ) {
        ButtonIcon(
            icon = if (isMicOn) R.drawable.ic_mic_on else R.drawable.ic_mic_off,
            background = micBackground,
            tint = micIcon,
            onClick = StageHandler::toggleMic,
        )
        if (!isVoiceOnly) {
            ButtonIcon(
                icon = if (isCameraOn) R.drawable.ic_camera_on else R.drawable.ic_camera_off,
                background = cameraBackground,
                tint = cameraIcon,
                onClick = StageHandler::toggleCamera,
            )
            ButtonIcon(
                icon = R.drawable.ic_settings,
                background = settingsBackground,
                tint = settingsIcon,
                onClick = StageHandler::toggleSettingsOpen,
            )
        }
    }
}

@Composable
private fun JoinScreenButtons(
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isSettingsOpen: Boolean,
    modifier: Modifier = Modifier,
    isVoiceOnly: Boolean = false,
    showControls: Boolean = false,
    onHeightSet: (Dp) -> Unit = {},
) {
    ColumnWithConstraints(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        onSizeChanged = { _, maxHeight ->
            onHeightSet(maxHeight)
        },
    ) {
        if (showControls) {
            JoinStageControls(
                isMicOn = isMicOn,
                isCameraOn = isCameraOn,
                isSettingsOpen = isSettingsOpen,
                isVoiceOnly = isVoiceOnly,
            )
        }
        /*
        ButtonText(
            text = stringResource(R.string.join_as_a_viewer),
            onClick = {
                StageHandler.joinStage(StageType.Viewer)
            }
        )

        ButtonText(
            text = stringResource(R.string.join_on_the_go),
            onClick = {
                StageHandler.joinStage(StageType.OnTheGo)
            }
        )
        */
        ButtonText(
            text = stringResource(R.string.join_now),
            background = OrangePrimary,
            textColor = BlackQuaternary,
            rippleColor = GraySecondary,
            onClick = {
                StageHandler.joinStage(StageType.Regular)
            }
        )
    }
}

@MultiPreview
@Composable
private fun JoinScreenPreview(
    stageId: String = "otiw-osrl-7xgb",
    participant: Participant = Participant(
        id = "1",
        name = "username",
    ),
    isMicOn: Boolean = false,
    isCameraOn: Boolean = false,
    isSettingsOpen: Boolean = false,
) {
    PreviewSurface {
        JoinScreenContent(
            stageId = stageId,
            participant = participant,
            isMicOn = isMicOn,
            isCameraOn = isCameraOn,
            isSettingsOpen = isSettingsOpen,
            isVoiceOnly = false,
        )
    }
}
