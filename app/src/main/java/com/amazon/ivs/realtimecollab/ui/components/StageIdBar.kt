package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.ui.theme.BlackSecondary
import com.amazon.ivs.realtimecollab.ui.theme.GrayPrimary
import com.amazon.ivs.realtimecollab.ui.theme.InterHint

@Composable
fun StageIdBar(
    stageId: String,
    modifier: Modifier = Modifier,
    isViewer: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FadeBox(
            modifier = Modifier
                .background(color = BlackSecondary, shape = RoundedCornerShape(100f))
                .padding(
                    horizontal = 14.dp,
                    vertical = 4.5.dp,
                ),
            isVisible = isViewer,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_eye),
                contentDescription = null,
                tint = GrayPrimary,
            )
        }
        Text(
            modifier = Modifier
                .background(
                    color = BlackSecondary,
                    shape = RoundedCornerShape(100f),
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 8.dp,
                ),
            text = stageId,
            style = InterHint,
        )
    }
}

@Preview
@Composable
private fun StageIdBarViewerPreview() {
    StageIdBarPreview(isViewer = true)
}

@Preview
@Composable
private fun StageIdBarRegularPreview() {
    StageIdBarPreview(isViewer = false)
}

@Composable
private fun StageIdBarPreview(
    isViewer: Boolean,
) {
    PreviewSurface {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            StageIdBar(
                stageId = "otiw-osrl-7xgb",
                isViewer = isViewer,
            )
        }
    }
}
