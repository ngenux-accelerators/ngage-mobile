package com.amazon.ivs.realtimecollab.core.handlers.stage

import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import aws.smithy.kotlin.runtime.InternalApi
import com.amazon.ivs.realtimecollab.appContext
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.Device.Descriptor.DeviceType
import com.amazonaws.ivs.broadcast.Device.Descriptor.Position
import com.amazonaws.ivs.broadcast.ImageLocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.amazonaws.ivs.broadcast.Stage.Strategy
import com.amazonaws.ivs.broadcast.Stage.SubscribeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

const val SCREEN_SHARE_ID = "ScreenShareStream"
const val SELF_PARTICIPANT_ID = "SelfParticipant"

data class ScreenShareState(
    val mediaProjection: MediaProjection? = null,
    val virtualDisplay: VirtualDisplay? = null,
    val videoStream: ImageLocalStageStream? = null,
)

data class SelfDeviceState(
    val videoDevice: ImageLocalStageStream? = null,
    val audioDevice: AudioLocalStageStream? = null
) {
    val streams get() = listOf(videoDevice, audioDevice)
}

internal object StageWrapper {
    private var _stage: Stage? = null
    private val _stageType = MutableStateFlow(StageType.Regular)
    private var _cameraFacing = Position.FRONT
    private var _selfDeviceState = SelfDeviceState()
    private var _subscribeType = SubscribeType.AUDIO_VIDEO
    private val _stageStrategy = object : Strategy {
        override fun stageStreamsToPublishForParticipant(stage: Stage, info: ParticipantInfo) =
            if (_stageType.value == StageType.Viewer) emptyList() else _selfDeviceState.streams
        override fun shouldPublishFromParticipant(stage: Stage, info: ParticipantInfo) =
            _stageType.value != StageType.Viewer
        override fun shouldSubscribeToParticipant(stage: Stage, info: ParticipantInfo) = _subscribeType
    }

    val stageType = _stageType.asStateFlow()

    fun joinStage(token: String, stageType: StageType): Boolean {
        if (_stage != null) return false
        if (token.isBlank()) return false

        return try {
            val stage = Stage(appContext, token, _stageStrategy)
            StageRendererWrapper.join(stage)
            _stage = stage
            _stageType.update { stageType }
            true
        } catch (e: Exception) {
            Timber.d(e, "Failed to join stage: $token")
            _stage = null
            false
        }
    }

    @OptIn(InternalApi::class)
    fun leaveStage() {
        Timber.d("Removing stage devices")
        _selfDeviceState = SelfDeviceState()
        StageRendererWrapper.leave(stage = _stage)
        _stageType.update { StageType.Regular }
        _stage = null
    }

    fun startPreview(
        isMicOn: Boolean = true,
        isCameraOn: Boolean = true,
    ) {
        val audioDevice = DeviceDiscoveryWrapper.getDevice(
            deviceType = DeviceType.MICROPHONE,
            stageType = _stageType.value,
        )?.let { device ->
            AudioLocalStageStream(device).apply {
                muted = !isMicOn
            }
        }
        val videoDevice = DeviceDiscoveryWrapper.getDevice(
            deviceType = DeviceType.CAMERA,
            stageType = _stageType.value,
            position = _cameraFacing
        )?.let { device ->
            ImageLocalStageStream(device).apply {
                muted = !isCameraOn
            }
        }
        Timber.d("Self device state: $audioDevice, $videoDevice")
        _selfDeviceState = SelfDeviceState(
            audioDevice = audioDevice,
            videoDevice = videoDevice
        )
        if (_stageType.value != StageType.Viewer) {
            StageRendererWrapper.addSelfParticipant(
                audioStream = audioDevice,
                videoStream = videoDevice,
                isMicOn = isMicOn,
                isCameraOn = isCameraOn,
                isCameraFlipped = _cameraFacing == Position.BACK,
            )
        } else {
            StageRendererWrapper.removeSelfParticipant()
        }
        _stage?.refreshStrategy()
    }

    fun flipCamera() {
        val facing = if (_cameraFacing == Position.FRONT) Position.BACK else Position.FRONT
        _cameraFacing = facing
        Timber.d("Flipping camera: $facing")
        DeviceDiscoveryWrapper.getDevice(
            deviceType = DeviceType.CAMERA,
            stageType = _stageType.value,
            position = facing
        )?.let { device ->
            val videoDevice = ImageLocalStageStream(device)
            _selfDeviceState = _selfDeviceState.copy(videoDevice = videoDevice)
            StageRendererWrapper.updateSelfParticipant(
                stream = videoDevice,
                isCameraFlipped = _cameraFacing == Position.BACK,
            )
            _stage?.refreshStrategy()
        }
    }

    fun setMic(isOn: Boolean) {
        Timber.d("Set mic: $isOn")
        _selfDeviceState.audioDevice?.muted = !isOn
        StageRendererWrapper.updateSelfParticipant(isMicOn = isOn)
        _stage?.refreshStrategy()
    }

    fun setCamera(isOn: Boolean) {
        Timber.d("Set camera: $isOn")
        _selfDeviceState.videoDevice?.muted = !isOn
        StageRendererWrapper.updateSelfParticipant(isCameraOn = isOn)
        _stage?.refreshStrategy()
    }

    fun setSubscribeType(subscribeType: SubscribeType) {
        Timber.d("Set subscribe type: $subscribeType")
        _subscribeType = subscribeType
        _stage?.refreshStrategy()
    }
}
