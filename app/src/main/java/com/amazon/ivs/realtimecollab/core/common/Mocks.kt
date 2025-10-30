package com.amazon.ivs.realtimecollab.core.common

import com.amazon.ivs.realtimecollab.core.handlers.StageMessage
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.core.handlers.stage.SELF_PARTICIPANT_ID

fun getMockParticipants(
    count: Int = 7,
    selfIndex: Int = 1,
    screenShareIndexes: List<Int> = emptyList(),
    viewerIndexes: List<Int> = emptyList(),
    cameraOnIndexes: List<Int> = listOf(2),
) = (0 until count).map {
    val isScreenSharing = screenShareIndexes.contains(it)
    val isViewer = viewerIndexes.contains(it)
    val isCameraOn = cameraOnIndexes.contains(it)

    Participant(
        id = if (it == selfIndex) SELF_PARTICIPANT_ID else "$it",
        name = if (it == selfIndex) "Eddy" else "username${it.takeIf { it > 0 } ?: ""}",
        isViewer = isViewer,
        isSpeaker = it == 1,
        isSpeaking = it == 3,
        isScreenSharing = isScreenSharing,
        isFilled = !isScreenSharing,
        isCameraOn = isCameraOn,
        isMicOn = it == 2,
    )
}

val onTheGoParticipant = Participant(
    id = "id",
    name = "username",
    isSpeaker = true,
    isMicOn = true,
)

fun getMockMessages() = listOf(
    StageMessage(messageId = "0", username = "username", message = "lorem ipsum"),
    StageMessage(messageId = "1", username = "username", message = "test"),
    StageMessage(messageId = "2", username = "username", message = "test test test test test test test test test test test test test test test test test test"),
)
