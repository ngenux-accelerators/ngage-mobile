package com.amazon.ivs.realtimecollab.core.handlers

import android.Manifest
import android.media.projection.MediaProjection
import androidx.annotation.RequiresPermission
import com.amazon.ivs.realtimecollab.core.common.launchDefault
import com.amazon.ivs.realtimecollab.core.common.launchMain
import com.amazon.ivs.realtimecollab.core.handlers.networking.MeetingConfig
import com.amazon.ivs.realtimecollab.core.handlers.networking.NetworkHandler
import com.amazon.ivs.realtimecollab.core.handlers.stage.ScreenShareStage
import com.amazon.ivs.realtimecollab.core.handlers.stage.StageRendererWrapper
import com.amazon.ivs.realtimecollab.core.handlers.stage.StageWrapper
import com.amazonaws.ivs.broadcast.Stage.SubscribeType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

enum class StageType {
    Regular,
    Viewer,
    OnTheGo,
}

/**
 * Might as well be a singleton ViewModel that you inject in all composables with hilt, but given the nature
 * of the project and the fact that there won't be any unit tests - this is much more convenient.
 */
object StageHandler {
    private val _isMembersOpen = MutableStateFlow(false)
    private val _isChatOpen = MutableStateFlow(false)
    private val _isSettingsOpen = MutableStateFlow(false)
    private val _isMicOn = MutableStateFlow(false)
    private val _isCameraOn = MutableStateFlow(false)
    private val _isScreenSharing = MutableStateFlow(false)
    private val _isIncomingVideoOn = MutableStateFlow(true)
    private val _isSelfVideoMirrored = MutableStateFlow(true)
    private val _isCameraFlipped = MutableStateFlow(false)
    private val _meetingConfig = MutableStateFlow(MeetingConfig())
    private var _getMeetingParticipantsJob: Job? = null

    val isMembersOpen = _isMembersOpen.asStateFlow()
    val isChatOpen = _isChatOpen.asStateFlow()
    val isSettingsOpen = _isSettingsOpen.asStateFlow()
    val isMicOn = _isMicOn.asStateFlow()
    val isCameraOn = _isCameraOn.asStateFlow()
    val isScreenSharing = _isScreenSharing.asStateFlow()
    val isIncomingVideoOn = _isIncomingVideoOn.asStateFlow()
    val isSelfVideoMirrored = _isSelfVideoMirrored.asStateFlow()
    val meetingConfig = _meetingConfig.asStateFlow()

    val stageType = StageWrapper.stageType
    val participants = StageRendererWrapper.participants
    val members = StageRendererWrapper.members

    init {
        launchDefault {
            StageRendererWrapper.participants.collect { participants ->
                val self = participants.find { it.isSelf } ?: return@collect
                val isMicOn = _isMicOn.value
                val isCameraOn = _isCameraOn.value
                if (self.isMicOn != isMicOn) {
                    Timber.d("Self Mic state updated: ${self.isMicOn}")
                    _isMicOn.update { self.isMicOn }
                }
                if (self.isCameraOn != isCameraOn) {
                    Timber.d("Self Camera state updated: ${self.isCameraOn}")
                    _isCameraOn.update { self.isCameraOn }
                }
            }
        }
    }

    fun setPreviewParticipants(count: Int) {
        StageRendererWrapper.setPreviewParticipants(count = count)
    }

    fun joinMeeting(meetingId: String? = null) = launchMain {
        Timber.d("Joining meeting: $meetingId")
        NavigationHandler.showLoading(loadingState = LoadingState.Loading)
        val meeting = NetworkHandler.joinMeeting(meetingId = meetingId)
        Timber.d("Meeting joined: $meeting")
        NavigationHandler.hideLoading()

        if (meeting != null) {
            _meetingConfig.update { meeting }
            StageWrapper.startPreview()
        }
    }

    fun joinStage(stageType: StageType) = launchMain {
        val meeting = _meetingConfig.value
        Timber.d("Joining stage: $stageType, $meeting")
        NavigationHandler.showLoading(loadingState = LoadingState.Loading)
        val isJoined = StageWrapper.joinStage(
            token = meeting.userToken,
            stageType = stageType
        )
        Timber.d("Stage joined: $isJoined")

        delay(1000)
        NavigationHandler.hideLoading()
        if (isJoined) {
            NavigationHandler.goTo(Destination.StageScreen)
            StageWrapper.startPreview(
                isMicOn = _isMicOn.value,
                isCameraOn = _isCameraOn.value,
            )
            delay(1000)
            StageRendererWrapper.getMeetingParticipants(
                meetingId = meeting.meetingId
            )
        }
    }

    fun leaveStage(resetToHomeScreen: Boolean = true) {
        stopScreenShare()
        ChatHandler.clearMessages()
        StageWrapper.leaveStage()

        Timber.d("Stage: ${_meetingConfig.value} left")
        _meetingConfig.update { MeetingConfig() }

        if (resetToHomeScreen) {
            NavigationHandler.reset()
            NavigationHandler.goTo(Destination.HomeScreen)
        }
    }

    fun toggleMembersOpen() {
        val isOpen = !_isMembersOpen.value
        Timber.d("Setting members open: $isOpen")
        _isMembersOpen.update { isOpen }
        if (isOpen) {
            if (_isChatOpen.value) _isChatOpen.update { false }
            if (_isSettingsOpen.value) _isSettingsOpen.update { false }
            NavigationHandler.showBottomSheet(BottomSheetDestination.Members)
            pollMeetingParticipants()
        } else {
            _getMeetingParticipantsJob?.cancel()
            _getMeetingParticipantsJob = null
            NavigationHandler.hideBottomSheet()
        }
    }

    fun toggleChatOpen() {
        val isOpen = !_isChatOpen.value
        Timber.d("Setting chat open: $isOpen")
        _isChatOpen.update { isOpen }
        if (isOpen) {
            if (_isMembersOpen.value) _isMembersOpen.update { false }
            if (_isSettingsOpen.value) _isSettingsOpen.update { false }
            NavigationHandler.showBottomSheet(BottomSheetDestination.Chat)
        } else {
            NavigationHandler.hideBottomSheet()
        }
    }

    fun toggleSettingsOpen() {
        val isOpen = !_isSettingsOpen.value
        Timber.d("Setting settings open: $isOpen")
        _isSettingsOpen.update { isOpen }
        if (isOpen) {
            if (_isMembersOpen.value) _isMembersOpen.update { false }
            if (_isChatOpen.value) _isChatOpen.update { false }
            NavigationHandler.showBottomSheet(BottomSheetDestination.Settings)
        } else {
            NavigationHandler.hideBottomSheet()
        }
    }

    fun closeBottomSheets() {
        Timber.d("Closing bottom sheets")
        _getMeetingParticipantsJob?.cancel()
        _getMeetingParticipantsJob = null
        _isMembersOpen.update { false }
        _isChatOpen.update { false }
        _isSettingsOpen.update { false }
    }

    fun toggleMic() {
        val isOn = !_isMicOn.value
        Timber.d("Setting mic on: $isOn")
        _isMicOn.update { isOn }
        StageWrapper.setMic(isOn)
    }

    fun toggleCamera() {
        val isOn = !_isCameraOn.value
        Timber.d("Setting camera on: $isOn")
        _isCameraOn.update { isOn }
        StageWrapper.setCamera(isOn)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startScreenShare(mediaProjection: MediaProjection?) {
        Timber.d("Starting screen share")
        val isScreenSharing = ScreenShareStage.startScreenShare(
            token = _meetingConfig.value.displayToken,
            mediaProjection = mediaProjection,
        )
        Timber.d("Screen share started: $isScreenSharing")
        _isScreenSharing.update { isScreenSharing }
    }

    fun stopScreenShare() {
        Timber.d("Stopping screen share")
        _isScreenSharing.update { false }
        ScreenShareStage.stopScreenShare()
    }

    fun toggleIncomingVideo() {
        val isOn = !_isIncomingVideoOn.value
        Timber.d("Setting incoming video on: $isOn")
        _isIncomingVideoOn.update { isOn }
        StageWrapper.setSubscribeType(
            subscribeType = if (isOn) SubscribeType.AUDIO_VIDEO else SubscribeType.AUDIO_ONLY
        )
    }

    fun toggleSelfVideoMirrored() {
        val isMirrored = !_isSelfVideoMirrored.value
        Timber.d("Setting self video mirrored: $isMirrored")
        _isSelfVideoMirrored.update { isMirrored }
    }

    fun toggleCameraFlip() {
        val isFlipped = !_isCameraFlipped.value
        Timber.d("Setting camera flip: $isFlipped")
        _isCameraFlipped.update { isFlipped }
        StageWrapper.flipCamera()
    }

    fun toggleVideoFilled(id: String) {
        Timber.d("Toggling video filled for: $id")
        StageRendererWrapper.toggleVideoFilled(id = id)
    }

    private fun pollMeetingParticipants() {
        Timber.d("Getting meeting participants")
        _getMeetingParticipantsJob?.cancel()
        _getMeetingParticipantsJob = launchDefault {
            delay(1000)
            StageRendererWrapper.getMeetingParticipants(
                meetingId = _meetingConfig.value.meetingId
            )
            if (_getMeetingParticipantsJob == null) return@launchDefault
            pollMeetingParticipants()
        }
    }
}
