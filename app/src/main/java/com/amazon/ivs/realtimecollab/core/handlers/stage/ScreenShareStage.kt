package com.amazon.ivs.realtimecollab.core.handlers.stage

import android.Manifest
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.amazon.ivs.realtimecollab.appContext
import com.amazon.ivs.realtimecollab.core.common.ShareServiceHandler
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.amazonaws.ivs.broadcast.Stage.Strategy
import com.amazonaws.ivs.broadcast.Stage.SubscribeType
import com.amazonaws.ivs.broadcast.StageRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

object ScreenShareStage {
    private var _stage: Stage? = null
    private var _screenShareId: String? = null
    private var _screenShareState = ScreenShareState()
    private val _isSharingScreen = MutableStateFlow(false)
    private val _mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            val isSharing = _isSharingScreen.value
            Timber.d("Media projection stopped, is sharing: $isSharing")
            if (isSharing) return
            disposeScreenShare()
        }
    }
    private val _stageStrategy = object : Strategy {
        override fun stageStreamsToPublishForParticipant(stage: Stage, info: ParticipantInfo) =
            listOf(_screenShareState.videoStream)
        override fun shouldPublishFromParticipant(stage: Stage, info: ParticipantInfo) =
            _isSharingScreen.value
        override fun shouldSubscribeToParticipant(stage: Stage, info: ParticipantInfo) =
            SubscribeType.NONE
    }
    private val _stageRenderer = object : StageRenderer {
        override fun onParticipantJoined(stage: Stage, participantInfo: ParticipantInfo) {
            super.onParticipantJoined(stage, participantInfo)
            if (participantInfo.isLocal) {
                _screenShareId = participantInfo.participantId
            }
        }
    }

    val screenShareId get() = _screenShareId

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startScreenShare(
        token: String,
        mediaProjection: MediaProjection?
    ): Boolean {
        if (_isSharingScreen.value) return true
        val projection = mediaProjection ?: return false

        return try {
            projection.registerCallback(_mediaProjectionCallback, Handler(Looper.getMainLooper()))
            _screenShareState = projection.startScreenShare()
            StageRendererWrapper.addScreenShareParticipant(stream = _screenShareState.videoStream)
            _isSharingScreen.update { true }
            val stage = Stage(appContext, token, _stageStrategy)
            stage.removeRenderer(_stageRenderer)
            stage.addRenderer(_stageRenderer)
            stage.refreshStrategy()
            stage.join()
            _stage = stage
            true
        } catch (e: Exception) {
            Timber.w(e, "Failed to start screen share")
            _isSharingScreen.update { false }
            false
        }
    }

    fun stopScreenShare() {
        ShareServiceHandler.setReady(false)
        StageRendererWrapper.removeScreenShareParticipant()
        _screenShareState = _screenShareState.copy(videoStream = null)
        _isSharingScreen.update { false }
        _stage?.removeRenderer(_stageRenderer)
        _stage?.leave()
        _stage = null
        _screenShareId = null
    }

    fun disposeScreenShare() {
        _screenShareState.virtualDisplay?.release()
        _screenShareState.mediaProjection?.unregisterCallback(_mediaProjectionCallback)
        _screenShareState = ScreenShareState()
        Timber.d("Screen share disposed")
    }
}
