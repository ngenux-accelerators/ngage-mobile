package com.amazon.ivs.realtimecollab.ui.screens.previews

import androidx.compose.runtime.Composable
import com.amazon.ivs.realtimecollab.core.common.getMockParticipants
import com.amazon.ivs.realtimecollab.core.common.onTheGoParticipant
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.screens.StageScreenContent

@MultiPreview
@Composable
private fun StageScreenPortraitRegular1Preview() {
    StageScreenPreview(
        stageType = StageType.Regular,
        participants = getMockParticipants(count = 1),
    )
}

@MultiPreview
@Composable
private fun StageScreenPortraitRegular3Preview() {
    StageScreenPreview(
        stageType = StageType.Regular,
        participants = getMockParticipants(count = 3),
    )
}

@MultiPreview
@Composable
private fun StageScreenPortraitRegular5Preview() {
    StageScreenPreview(
        stageType = StageType.Regular,
        participants = getMockParticipants(count = 4),
    )
}

@MultiPreview
@Composable
private fun StageScreenPortraitRegularPreview() {
    StageScreenPreview(
        stageType = StageType.Regular,
        participants = getMockParticipants(count = 7, screenShareIndexes = listOf(4)),
    )
}

@MultiPreview
@Composable
private fun StageScreenPortraitViewerPreview() {
    StageScreenPreview(stageType = StageType.Viewer)
}

@MultiPreview
@Composable
private fun StageScreenPortraitOnTheGoPreview() {
    StageScreenPreview(
        stageType = StageType.OnTheGo,
        participants = listOf(onTheGoParticipant),
    )
}

@MultiPreview
@Composable
private fun ScreenSharePreview() {
    StageScreenPreview(
        stageType = StageType.Regular,
        participants = getMockParticipants(
            count = 4,
            screenShareIndexes = listOf(0, 1),
            cameraOnIndexes = listOf(0, 1, 2, 3)
        ),
    )
}

@Composable
private fun StageScreenPreview(
    stageType: StageType,
    participants: List<Participant> = getMockParticipants(),
) {
    PreviewSurface {
        StageScreenContent(
            stageType = stageType,
            stageId = "otiw-osrl-7xgb",
            participants = participants,
        )
    }
}
