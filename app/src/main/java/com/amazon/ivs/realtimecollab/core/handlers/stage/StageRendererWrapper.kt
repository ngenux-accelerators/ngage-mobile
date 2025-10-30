package com.amazon.ivs.realtimecollab.core.handlers.stage

import com.amazon.ivs.realtimecollab.core.common.getMockParticipants
import com.amazon.ivs.realtimecollab.core.handlers.AuthHandler
import com.amazon.ivs.realtimecollab.core.handlers.networking.NetworkHandler
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.BroadcastConfiguration.AspectMode
import com.amazonaws.ivs.broadcast.BroadcastException
import com.amazonaws.ivs.broadcast.ImageLocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.amazonaws.ivs.broadcast.StageRenderer
import com.amazonaws.ivs.broadcast.StageStream
import com.amazonaws.ivs.broadcast.SurfaceSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class Participant(
    val id: String,
    val name: String,
    val isViewer: Boolean = false,
    val isMicOn: Boolean = false,
    val isCameraOn: Boolean = false,
    val isCameraFlipped: Boolean = false,
    val isScreenSharing: Boolean = false,
    val isSpeaker: Boolean = false,
    val isSpeaking: Boolean = false,
    val isFilled: Boolean = true,
    val stream: StageStream? = null,
) {
    val isSelf get() = id == SELF_PARTICIPANT_ID
    val preview get() = try {
        if (stream?.muted == true || !isCameraOn) {
            null
        } else {
            val surface = stream?.device as? SurfaceSource
            Timber.d("Getting preview: $isScreenSharing, $isFilled, stream valid: ${surface?.isValid}")
            surface?.getPreviewView(if (isFilled) AspectMode.FILL else AspectMode.FIT)
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to get video preview")
        null
    }
}

internal object StageRendererWrapper {
    private val _participants = MutableStateFlow(emptyList<Participant>())
    private val _members = MutableStateFlow(emptyList<Participant>())
    private val _stageRenderer = object : StageRenderer {
        override fun onStreamsAdded(stage: Stage, participantInfo: ParticipantInfo, streams: List<StageStream?>) {
            super.onStreamsAdded(stage, participantInfo, streams)
            if (participantInfo.isLocal) return
            if (participantInfo.participantId == ScreenShareStage.screenShareId) return
            Timber.d("Streams added: ${streams.size} for participant: ${participantInfo.participantId}")
            val videoStream = streams.getVideoStream()
            val audioStream = streams.getAudioStream()
            _participants.updateParticipant(
                id = participantInfo.participantId,
                stream = videoStream,
                isCameraOn = videoStream?.muted?.not(),
                isMicOn = audioStream?.muted?.not(),
            )
            audioStream.handleSpeaker { isSpeaking ->
                _participants.setSpeaking(
                    id = participantInfo.participantId,
                    isSpeaking = isSpeaking,
                )
            }
        }

        override fun onStreamsRemoved(stage: Stage, participantInfo: ParticipantInfo, streams: List<StageStream?>) {
            super.onStreamsRemoved(stage, participantInfo, streams)
            if (participantInfo.isLocal) return
            if (participantInfo.participantId == ScreenShareStage.screenShareId) return
            Timber.d("Streams removed: ${streams.size} for participant: ${participantInfo.participantId}")
            val videoStream = streams.getVideoStream()
            val audioStream = streams.getAudioStream()
            _participants.updateParticipant(
                id = participantInfo.participantId,
                removeStream = videoStream != null,
                isCameraOn = if (videoStream != null) false else null,
                isMicOn = if (audioStream != null) false else null,
            )
        }

        override fun onStreamsMutedChanged(
            stage: Stage,
            participantInfo: ParticipantInfo,
            streams: List<StageStream?>
        ) {
            super.onStreamsMutedChanged(stage, participantInfo, streams)
            if (participantInfo.isLocal) return
            if (participantInfo.participantId == ScreenShareStage.screenShareId) return
            Timber.d("Streams muted changed: ${streams.size} for participant: ${participantInfo.participantId}")
            val videoStream = streams.getVideoStream()
            val audioStream = streams.getAudioStream()
            _participants.updateParticipant(
                id = participantInfo.participantId,
                stream = videoStream,
                isCameraOn = videoStream?.muted?.not(),
                isMicOn = audioStream?.muted?.not(),
            )
        }

        override fun onConnectionStateChanged(
            stage: Stage,
            state: Stage.ConnectionState,
            exception: BroadcastException?
        ) {
            super.onConnectionStateChanged(stage, state, exception)
            Timber.d("Connection state changed: $state")
        }

        override fun onParticipantJoined(stage: Stage, participantInfo: ParticipantInfo) {
            super.onParticipantJoined(stage, participantInfo)
            if (participantInfo.isLocal) return
            if (participantInfo.participantId == ScreenShareStage.screenShareId) return
            Timber.d("Participant joined: ${participantInfo.participantId}")
            _participants.addParticipant(participantInfo = participantInfo)
            if (!(participantInfo.isScreenSharing())) {
                _members.addParticipant(participantInfo = participantInfo)
            }
        }

        override fun onParticipantLeft(stage: Stage, participantInfo: ParticipantInfo) {
            super.onParticipantLeft(stage, participantInfo)
            if (participantInfo.isLocal) return
            if (participantInfo.participantId == ScreenShareStage.screenShareId) return
            Timber.d("Participant left: ${participantInfo.participantId}")
            _participants.removeParticipant(id = participantInfo.participantId)
        }

        override fun onError(exception: BroadcastException) {
            super.onError(exception)
            Timber.w(exception, "Stage error: ${exception.code}, ${exception.error}, ${exception.message}")
        }
    }

    val participants = _participants.asStateFlow()
    val members = _members.asStateFlow()

    fun setPreviewParticipants(count: Int) {
        _participants.update {
            getMockParticipants(count = count)
        }
    }

    fun join(stage: Stage) {
        stage.removeRenderer(_stageRenderer)
        stage.addRenderer(_stageRenderer)
        stage.refreshStrategy()
        stage.join()
    }

    fun leave(stage: Stage?) {
        Timber.d("Removing stage renderer")
        _participants.update { emptyList() }
        _members.update { emptyList() }
        stage?.removeRenderer(_stageRenderer)
        stage?.leave()
    }

    fun addScreenShareParticipant(stream: ImageLocalStageStream?) {
        val name = AuthHandler.user.value.username
        _participants.addParticipant(
            id = SCREEN_SHARE_ID,
            name = name,
            stream = stream,
            isMicOn = true,
            isCameraOn = stream != null,
            isCameraFlipped = false,
        )
    }

    fun removeScreenShareParticipant() {
        _participants.removeParticipant(id = SCREEN_SHARE_ID)
    }

    fun removeSelfParticipant() {
        _participants.removeParticipant(id = SELF_PARTICIPANT_ID)
    }

    fun addSelfParticipant(
        videoStream: ImageLocalStageStream?,
        audioStream: AudioLocalStageStream?,
        isMicOn: Boolean,
        isCameraOn: Boolean,
        isCameraFlipped: Boolean,
    ) {
        val name = AuthHandler.user.value.username
        audioStream.handleSpeaker { isSpeaking ->
            _participants.setSpeaking(
                id = SELF_PARTICIPANT_ID,
                isSpeaking = isSpeaking,
            )
        }
        _participants.addParticipant(
            id = SELF_PARTICIPANT_ID,
            name = name,
            stream = videoStream,
            isMicOn = isMicOn,
            isCameraOn = isCameraOn,
            isCameraFlipped = isCameraFlipped,
        )
        _members.addParticipant(
            id = SELF_PARTICIPANT_ID,
            name = name,
            stream = videoStream,
            isMicOn = isMicOn,
            isCameraOn = isCameraOn,
            isCameraFlipped = isCameraFlipped,
        )
    }

    fun toggleVideoFilled(id: String) {
        val isFilled = _participants.value.find { it.id == id }?.isFilled?.not() ?: true
        Timber.d("Toggling video filled: $isFilled for participant: $id")
        _participants.updateParticipant(
            id = id,
            isFilled = isFilled,
        )
    }

    fun updateSelfParticipant(
        name: String? = null,
        isMicOn: Boolean? = null,
        isCameraOn: Boolean? = null,
        isCameraFlipped: Boolean? = null,
        isScreenSharing: Boolean? = null,
        isSpeaker: Boolean? = null,
        isSpeaking: Boolean? = null,
        isFilled: Boolean? = null,
        stream: StageStream? = null
    ) {
        _participants.updateParticipant(
            id = SELF_PARTICIPANT_ID,
            name = name,
            isMicOn = isMicOn,
            isCameraOn = isCameraOn,
            isCameraFlipped = isCameraFlipped,
            isScreenSharing = isScreenSharing,
            isSpeaker = isSpeaker,
            isSpeaking = isSpeaking,
            isFilled = isFilled,
            stream = stream,
        )
    }

    suspend fun getMeetingParticipants(meetingId: String) {
        val members = NetworkHandler.getMeetingParticipants(
            meetingId = meetingId,
        ) ?: return
        Timber.d("Members: ${members.map { "{${it.name}, ${it.isViewer}}" }}")
        _members.update { members }
    }
}
