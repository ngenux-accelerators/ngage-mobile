package com.amazon.ivs.realtimecollab.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.appContext
import com.amazon.ivs.realtimecollab.core.common.ScreenCaptureService
import com.amazon.ivs.realtimecollab.core.common.ShareServiceHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.GrayTertiary
import com.amazon.ivs.realtimecollab.ui.theme.RedPrimary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary
import timber.log.Timber

@Composable
fun StageControls(
    stageType: StageType,
    modifier: Modifier = Modifier,
) {
    val isMicOn by StageHandler.isMicOn.collectAsStateWithLifecycle()
    val isCameraOn by StageHandler.isCameraOn.collectAsStateWithLifecycle()
    val isScreenSharing by StageHandler.isScreenSharing.collectAsStateWithLifecycle()
    val isSettingsOpen by StageHandler.isSettingsOpen.collectAsStateWithLifecycle()
    val isVoiceOnly by StageHandler.isVoiceOnly.collectAsStateWithLifecycle()
    val isServiceReady by ShareServiceHandler.isReady.collectAsStateWithLifecycle(false)
    val mediaProjectionManager: MediaProjectionManager? = if (LocalInspectionMode.current) null else remember {
        appContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    var shareIntent by remember { mutableStateOf<Intent?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContract<Unit, Pair<Int, Intent?>?>() {
            override fun createIntent(context: Context, input: Unit) =
                mediaProjectionManager?.createScreenCaptureIntent() ?: Intent()
            override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, Intent?>? =
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    Pair(resultCode, intent)
                } else {
                    null
                }
        }
    ) { result ->
        result?.let { (resultCode, intent) ->
            if (resultCode == Activity.RESULT_OK && intent != null) {
                try {
                    shareIntent = intent
                    val serviceIntent = Intent(appContext, ScreenCaptureService::class.java)
                    ContextCompat.startForegroundService(appContext, serviceIntent)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get media projection")
                    StageHandler.stopScreenShare()
                }
            } else {
                StageHandler.stopScreenShare()
            }
        }
    }

    LaunchedEffect(key1 = isServiceReady) {
        val intent = shareIntent ?: return@LaunchedEffect
        if (!isServiceReady) return@LaunchedEffect
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) return@LaunchedEffect
        Timber.d("Service is ready - starting media projection")
        val mediaProjection = mediaProjectionManager?.getMediaProjection(Activity.RESULT_OK, intent)
        StageHandler.startScreenShare(mediaProjection = mediaProjection)
    }

    StageControlsContent(
        modifier = modifier,
        stageType = stageType,
        isMicOn = isMicOn,
        isCameraOn = isCameraOn,
        isScreenSharing = isScreenSharing,
        isSettingsOpen = isSettingsOpen,
        isVoiceOnly = isVoiceOnly,
        toggleScreenSharing = {
            Timber.d("Toggle screen sharing: $isScreenSharing")
            if (isScreenSharing) {
                StageHandler.stopScreenShare()
            } else {
                launcher.launch(Unit)
            }
        }
    )
}

@Composable
private fun StageControlsContent(
    stageType: StageType,
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isScreenSharing: Boolean,
    isSettingsOpen: Boolean,
    isVoiceOnly: Boolean,
    modifier: Modifier = Modifier,
    toggleScreenSharing: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxPortraitWidth(),
    ) {
        when (stageType) {
            StageType.Regular -> StageControlsRegular(
                isMicOn = isMicOn,
                isCameraOn = isCameraOn,
                isScreenSharing = isScreenSharing,
                isVoiceOnly = isVoiceOnly,
                toggleScreenSharing = toggleScreenSharing,
            )
            StageType.Viewer -> StageControlsViewer()
            StageType.OnTheGo -> StageControlsOnTheGo(
                isMicOn = isMicOn,
                isCameraOn = isCameraOn,
                isScreenSharing = isScreenSharing,
                isSettingsOpen = isSettingsOpen,
                isVoiceOnly = isVoiceOnly,
                toggleScreenSharing = toggleScreenSharing,
            )
        }
    }
}

@Composable
private fun StageControlsRegular(
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isScreenSharing: Boolean,
    isVoiceOnly: Boolean,
    toggleScreenSharing: () -> Unit,
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
    val screenSharingBackground by animateColorAsState(
        targetValue = if (isScreenSharing) GrayTertiary else GraySecondary,
    )
    val screenSharingIcon by animateColorAsState(
        targetValue = if (isScreenSharing) BlackTertiary else WhitePrimary,
    )

    BoxWithConstraints(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val buttonPadding = 10.dp
        val otherButtonCount = if (isVoiceOnly) 2 else 4
        val totalPadding = buttonPadding * otherButtonCount
        val minCloseButtonWidth = 90.dp
        val availableOtherButtonSpace = this.maxWidth - minCloseButtonWidth - totalPadding
        val otherButtonWidth = (availableOtherButtonSpace / otherButtonCount).coerceIn(48.dp .. 80.dp)
        val closeButtonWidth = (this.maxWidth - (otherButtonWidth * otherButtonCount) - totalPadding)
            .coerceIn(minCloseButtonWidth .. 200.dp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(space = buttonPadding),
        ) {
            ButtonIcon(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(otherButtonWidth),
                icon = if (isMicOn) R.drawable.ic_mic_on else R.drawable.ic_mic_off,
                background = micBackground,
                tint = micIcon,
                buttonSize = null,
                onClick = StageHandler::toggleMic,
            )
            if (!isVoiceOnly) {
                ButtonIcon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(otherButtonWidth),
                    icon = if (isCameraOn) R.drawable.ic_camera_on else R.drawable.ic_camera_off,
                    background = cameraBackground,
                    tint = cameraIcon,
                    buttonSize = null,
                    onClick = StageHandler::toggleCamera,
                )
                ButtonIcon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(otherButtonWidth),
                    icon = if (isScreenSharing) R.drawable.ic_screenshare_on else R.drawable.ic_screenshare_off,
                    background = screenSharingBackground,
                    tint = screenSharingIcon,
                    buttonSize = null,
                    onClick = toggleScreenSharing,
                )
            }
            ButtonIcon(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(otherButtonWidth),
                icon = R.drawable.ic_settings,
                buttonSize = null,
                onClick = StageHandler::toggleSettingsOpen,
            )
            ButtonIcon(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(closeButtonWidth),
                icon = R.drawable.ic_phone,
                background = RedPrimary,
                buttonSize = null,
                onClick = StageHandler::leaveStage,
            )
        }
    }
}

@Composable
private fun StageControlsViewer() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ButtonIcon(
            modifier = Modifier.size(48.dp),
            icon = R.drawable.ic_settings,
            buttonSize = null,
            onClick = StageHandler::toggleSettingsOpen,
        )
        ButtonIcon(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            icon = R.drawable.ic_phone,
            background = RedPrimary,
            buttonSize = null,
            onClick = StageHandler::leaveStage,
        )
    }
}

@Composable
private fun StageControlsOnTheGo(
    isMicOn: Boolean,
    isCameraOn: Boolean,
    isScreenSharing: Boolean,
    isSettingsOpen: Boolean,
    isVoiceOnly: Boolean,
    toggleScreenSharing: () -> Unit,
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
    val screenSharingBackground by animateColorAsState(
        targetValue = if (isScreenSharing) GrayTertiary else GraySecondary,
    )
    val screenSharingIcon by animateColorAsState(
        targetValue = if (isScreenSharing) BlackTertiary else WhitePrimary,
    )
    val settingsBackground by animateColorAsState(
        targetValue = if (isSettingsOpen) GrayTertiary else GraySecondary,
    )
    val settingsIcon by animateColorAsState(
        targetValue = if (isSettingsOpen) BlackTertiary else WhitePrimary,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ButtonIcon(
            modifier = Modifier.fillMaxWidth(),
            buttonSize = null,
            innerPadding = PaddingValues(vertical = 20.dp),
            text = stringResource(if (isMicOn) R.string.mute else R.string.unmute),
            icon = if (isMicOn) R.drawable.ic_mic_on else R.drawable.ic_mic_off,
            background = micBackground,
            tint = micIcon,
            onClick = StageHandler::toggleMic,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!isVoiceOnly) {
                ButtonIcon(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    icon = if (isCameraOn) R.drawable.ic_camera_on else R.drawable.ic_camera_off,
                    background = cameraBackground,
                    tint = cameraIcon,
                    onClick = StageHandler::toggleCamera,
                )
                ButtonIcon(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    icon = if (isScreenSharing) R.drawable.ic_screenshare_on else R.drawable.ic_screenshare_off,
                    background = screenSharingBackground,
                    tint = screenSharingIcon,
                    onClick = toggleScreenSharing,
                )
            }
            ButtonIcon(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                icon = R.drawable.ic_settings,
                background = settingsBackground,
                tint = settingsIcon,
                onClick = StageHandler::toggleSettingsOpen,
            )
        }
        ButtonIcon(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            icon = R.drawable.ic_phone,
            background = RedPrimary,
            buttonSize = null,
            onClick = StageHandler::leaveStage,
        )
    }
}

@MultiPreview
@Composable
private fun StageControlsRegularOffPreview() {
    StageControlsPreview(
        stageType = StageType.Regular,
    )
}

@ScreenPreview
@Composable
private fun StageControlsViewerOffPreview() {
    StageControlsPreview(
        stageType = StageType.Viewer,
    )
}

@ScreenPreview
@Composable
private fun StageControlsOnTheGoOffPreview() {
    StageControlsPreview(
        stageType = StageType.OnTheGo,
    )
}

@ScreenPreview
@Composable
private fun StageControlsRegularOnPreview() {
    StageControlsPreview(
        stageType = StageType.Regular,
        isMicOn = true,
        isCameraOn = true,
        isScreenSharing = true,
    )
}

@ScreenPreview
@Composable
private fun StageControlsViewerOnPreview() {
    StageControlsPreview(
        stageType = StageType.Viewer,
        isMicOn = true,
        isCameraOn = true,
        isScreenSharing = true,
    )
}

@ScreenPreview
@Composable
private fun StageControlsOnTheGoOnPreview() {
    StageControlsPreview(
        stageType = StageType.OnTheGo,
        isMicOn = true,
        isCameraOn = true,
        isScreenSharing = true,
    )
}

@Composable
private fun StageControlsPreview(
    stageType: StageType,
    isMicOn: Boolean = false,
    isCameraOn: Boolean = false,
    isScreenSharing: Boolean = false,
    isSettingsOpen: Boolean = false,
) {
    PreviewSurface {
        ScreenBox(
            contentAlignment = Alignment.BottomCenter,
        ) {
            StageControlsContent(
                stageType = stageType,
                isMicOn = isMicOn,
                isCameraOn = isCameraOn,
                isScreenSharing = isScreenSharing,
                isSettingsOpen = isSettingsOpen,
                isVoiceOnly = false,
                toggleScreenSharing = {},
            )
        }
    }
}
