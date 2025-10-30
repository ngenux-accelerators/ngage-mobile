package com.amazon.ivs.realtimecollab.ui.screens.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.core.common.getMockParticipants
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.SplitScreenPreview
import com.amazon.ivs.realtimecollab.ui.screens.StageScreenContent

@SplitScreenPreview
@Composable
private fun Stage1Participant() {
    StageScreenPreview(participantCount = 1)
}

@SplitScreenPreview
@Composable
private fun Stage2Participant() {
    StageScreenPreview(participantCount = 2)
}

@SplitScreenPreview
@Composable
private fun Stage3Participant() {
    StageScreenPreview(participantCount = 3)
}

@SplitScreenPreview
@Composable
private fun Stage4Participant() {
    StageScreenPreview(participantCount = 4)
}

@SplitScreenPreview
@Composable
private fun Stage5Participant() {
    StageScreenPreview(participantCount = 5)
}

@SplitScreenPreview
@Composable
private fun StageScreenShare2Participant() {
    StageScreenPreview(
        participantCount = 2,
        isScreenSharing = true,
    )
}

@SplitScreenPreview
@Composable
private fun StageScreenShare3Participant() {
    StageScreenPreview(
        participantCount = 3,
        isScreenSharing = true,
    )
}

@SplitScreenPreview
@Composable
private fun StageScreenShare7Participant() {
    StageScreenPreview(
        participantCount = 7,
        isScreenSharing = true,
    )
}

@Composable
private fun StageScreenPreview(
    participantCount: Int = 7,
    isScreenSharing: Boolean = false,
    participants: List<Participant> = getMockParticipants(
        count = participantCount,
        screenShareIndexes = if (isScreenSharing) listOf(0) else emptyList(),
    ),
) {
    PreviewSurface {
        val density = LocalDensity.current
        val size = LocalWindowInfo.current.containerSize
        val height = density.run { size.height.toDp() }

        StageScreenContent(
            stageType = StageType.Regular,
            stageId = "otiw-osrl-7xgb",
            participants = participants,
            isPortrait = true,
            isMultiWindow = height <= 600.dp,
            isLandscape = false,
            isPhoneLandscape = false,
            isSquareOrLandscape = false,
        )
    }
}
