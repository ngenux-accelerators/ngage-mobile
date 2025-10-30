package com.amazon.ivs.realtimecollab.ui.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazon.ivs.realtimecollab.ui.components.ButtonIcon
import com.amazon.ivs.realtimecollab.ui.components.ButtonSwitch
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenPreview
import com.amazon.ivs.realtimecollab.ui.components.isPhoneLandscape
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GrayTertiary
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.InterTitle
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

@Composable
fun SettingsBottomSheet() {
    val isIncomingVideoOn by StageHandler.isIncomingVideoOn.collectAsStateWithLifecycle()
    val isSelfVideoMirrored by StageHandler.isSelfVideoMirrored.collectAsStateWithLifecycle()
    val stageType by StageHandler.stageType.collectAsStateWithLifecycle()

    SettingsBottomSheetContent(
        isIncomingVideoOn = isIncomingVideoOn,
        isSelfVideoMirrored = isSelfVideoMirrored,
        isViewer = stageType == StageType.Viewer,
    )
}

@Composable
private fun SettingsBottomSheetContent(
    isIncomingVideoOn: Boolean,
    isSelfVideoMirrored: Boolean,
    isViewer: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = InterTitle,
        )
        ButtonSwitch(
            modifier = Modifier.padding(bottom = if (isViewer) 30.dp else 0.dp),
            text = stringResource(R.string.incoming_video_title),
            description = stringResource(R.string.incoming_video_subtitle),
            isChecked = !isIncomingVideoOn,
            onCheckedChange = {
                StageHandler.toggleIncomingVideo()
            }
        )
        if (isViewer) return@Column

        ButtonSwitch(
            text = stringResource(R.string.mirror_my_video),
            isChecked = isSelfVideoMirrored,
            onCheckedChange = {
                StageHandler.toggleSelfVideoMirrored()
            }
        )
        Row(
            modifier = Modifier.padding(bottom = if (isPhoneLandscape()) 20.dp else 40.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(R.string.flip_camera),
                style = InterPrimary,
            )
            ButtonIcon(
                icon = R.drawable.ic_flip_camera,
                background = GrayTertiary,
                tint = BlackTertiary,
                rippleColor = WhitePrimary,
                onClick = StageHandler::toggleCameraFlip,
            )
        }
    }
}

@MultiPreview
@Composable
private fun SettingsBottomSheetVariant1Preview() {
    SettingsBottomSheetPreview()
}

@ScreenPreview
@Composable
private fun SettingsBottomSheetVariant2Preview() {
    SettingsBottomSheetPreview(
        isIncomingVideoOn = true,
        isSelfVideoMirrored = false,
    )
}

@ScreenPreview
@Composable
private fun SettingsBottomSheetViewerPreview() {
    SettingsBottomSheetPreview(
        isViewer = true,
    )
}

@Composable
private fun SettingsBottomSheetPreview(
    isIncomingVideoOn: Boolean = false,
    isSelfVideoMirrored: Boolean = true,
    isViewer: Boolean = false,
) {
    PreviewSurface {
        BottomSheetContainer {
            SettingsBottomSheetContent(
                isIncomingVideoOn = isIncomingVideoOn,
                isSelfVideoMirrored = isSelfVideoMirrored,
                isViewer = isViewer,
            )
        }
    }
}
