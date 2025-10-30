package com.amazon.ivs.realtimecollab.core.handlers.stage

import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import androidx.window.layout.WindowMetricsCalculator
import com.amazon.ivs.realtimecollab.appContext
import com.amazon.ivs.realtimecollab.core.common.RMS_SPEAKING_THRESHOLD
import com.amazon.ivs.realtimecollab.core.handlers.AuthHandler
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.AudioStageStream
import com.amazonaws.ivs.broadcast.ImageLocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.StageStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localDateFormat get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

fun String.toDate(): Date = try {
    localDateFormat.parse(this)!!
} catch (_: Exception) {
    Date()
}

fun List<StageStream?>.getVideoStream() = getStream(type = StageStream.Type.VIDEO)
fun List<StageStream?>.getAudioStream() = getStream(type = StageStream.Type.AUDIO)
fun ParticipantInfo.isScreenSharing() = attributes.get(key = "participantGroup") == "display"

fun MutableStateFlow<List<Participant>>.addParticipant(
    participantInfo: ParticipantInfo,
) {
    val participants = value.toMutableList().apply {
        if (this.any { it.id == participantInfo.participantId }) return
        val isScreenSharing = participantInfo.isScreenSharing()

        add(
            Participant(
                id = participantInfo.participantId,
                name = participantInfo.attributes.getOrDefault(
                    key = "name",
                    defaultValue = "username"
                ),
                isScreenSharing = isScreenSharing,
                isFilled = !isScreenSharing,
            )
        )
    }
    Timber.d("Participant added: ${participantInfo.participantId}, ${participantInfo.attributes}, ${participantInfo.userInfo}, size: ${participants.size}")
    update { participants }
}

fun MutableStateFlow<List<Participant>>.addParticipant(
    id: String,
    name: String,
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isCameraFlipped: Boolean,
    stream: ImageLocalStageStream?,
) {
    val participants = value.filter { it.id != id }.toMutableList().apply {
        val isScreenSharing = id == SCREEN_SHARE_ID

        add(
            Participant(
                id = id,
                name = name,
                stream = stream,
                isCameraOn = isCameraOn,
                isMicOn = isMicOn,
                isScreenSharing = isScreenSharing,
                isFilled = !isScreenSharing,
                isCameraFlipped = isCameraFlipped,
            )
        )
    }
    Timber.d("Participant added: $id, size: ${participants.size}")
    update { participants }
}

fun MutableStateFlow<List<Participant>>.removeParticipant(id: String) {
    if (value.none { it.id == id }) return
    val participants = value.filter { it.id != id }
    Timber.d("Participant removed: $id, size: ${participants.size}")
    update { participants }
}

fun MutableStateFlow<List<Participant>>.updateParticipant(
    id: String,
    name: String? = null,
    isMicOn: Boolean? = null,
    isCameraOn: Boolean? = null,
    isCameraFlipped: Boolean? = null,
    isScreenSharing: Boolean? = null,
    isSpeaker: Boolean? = null,
    isSpeaking: Boolean? = null,
    isFilled: Boolean? = null,
    stream: StageStream? = null,
    removeStream: Boolean = false,
) {
    if (value.none { it.id == id }) return
    val participants = value.map { participant ->
        if (participant.id == id) {
            val updatedParticipant = participant.copy(
                name = name ?: participant.name,
                isMicOn = isMicOn ?: participant.isMicOn,
                isCameraOn = isCameraOn ?: participant.isCameraOn,
                isCameraFlipped = isCameraFlipped ?: participant.isCameraFlipped,
                isScreenSharing = isScreenSharing ?: participant.isScreenSharing,
                isSpeaker = isSpeaker ?: participant.isSpeaker,
                isSpeaking = isSpeaking ?: participant.isSpeaking,
                isFilled = isFilled ?: participant.isFilled,
                stream = if (removeStream) null else stream ?: participant.stream,
            )
            Timber.d("Participant updated: $id, $updatedParticipant")
            updatedParticipant
        } else {
            participant.copy()
        }
    }
    update { participants }
}

fun MutableStateFlow<List<Participant>>.setSpeaking(id: String, isSpeaking: Boolean) {
    val speaker = value.find { it.id == id } ?: return
    if (speaker.isSpeaking == isSpeaking) return
    if (!speaker.isMicOn && !speaker.isSpeaking && !speaker.isSpeaker) return
    val hasSpeaker = value.any { it.id != id && it.isSpeaker }

    val participants = value.map { participant ->
        if (!participant.isMicOn) {
            participant.copy(isSpeaker = false, isSpeaking = false)
        } else if (participant.id == id) {
            participant.copy(
                isSpeaker = if (!hasSpeaker) true else isSpeaking,
                isSpeaking = isSpeaking
            )
        } else if (isSpeaking) {
            participant.copy(isSpeaker = false)
        } else {
            participant.copy()
        }
    }
    Timber.d("Participant speaking: $id, $isSpeaking, speaker: ${participants.find { it.isSpeaker }?.id}, speaking: ${participants.find { it.isSpeaking }?.id}")
    update { participants }
}

fun List<Participant>.getSelfParticipant() = firstOrNull { it.isSelf } ?: Participant(
    id = SELF_PARTICIPANT_ID,
    name = AuthHandler.user.value.username,
)

fun List<Participant>.reorderSelfLast(): List<Participant> {
    if (this.size < 2) return this

    val participants = filter { !it.isSelf }.toMutableList()
    find { it.isSelf }?.run {
        participants.add(this)
    }
    return participants
}

fun List<Participant>.reorderSelfFirst(): List<Participant> {
    if (this.size < 2) return this

    val participants = filter { !it.isSelf }.toMutableList()
    find { it.isSelf }?.run {
        participants.add(0, this)
    }
    return participants
}

fun List<Participant>.reorderScreenShareFirst() = sortedByDescending { it.isScreenSharing }

fun StageStream?.handleSpeaker(
    onSpeaking: (Boolean) -> Unit,
) = (this as? AudioStageStream)?.setStatsCallback { _, rms ->
    onSpeaking(rms > RMS_SPEAKING_THRESHOLD)
} ?: (this as? AudioLocalStageStream)?.setStatsCallback { _, rms ->
    onSpeaking(rms > RMS_SPEAKING_THRESHOLD)
}

fun <T> MutableStateFlow<List<T>>.updateList(block: MutableList<T>.() -> Unit) = update {
    it.toMutableList().apply(block = block)
}

fun MediaProjection?.startScreenShare(): ScreenShareState {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(appContext)
    val bounds = metrics.bounds
    val screenWidth = bounds.width()
    val screenHeight = bounds.height()
    val densityDpi = appContext.resources.displayMetrics.densityDpi
    val imageStream = DeviceDiscoveryWrapper.getImageSource(
        width = screenWidth.toFloat(),
        height = screenHeight.toFloat()
    )
    var videoStream: ImageLocalStageStream? = null
    val virtualDisplay = if (imageStream != null) {
        videoStream = ImageLocalStageStream(imageStream)
        this?.createVirtualDisplay(
            SCREEN_SHARE_ID,
            screenWidth,
            screenHeight,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageStream.inputSurface,
            null,
            null
        )
    } else null

    return ScreenShareState(
        mediaProjection = this,
        virtualDisplay = virtualDisplay,
        videoStream = videoStream,
    )
}

private fun List<StageStream?>.getStream(type: StageStream.Type) = find { it?.streamType == type }
