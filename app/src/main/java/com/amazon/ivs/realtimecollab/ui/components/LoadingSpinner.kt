package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

@Composable
fun LoadingSpinner(
    color: Color = WhitePrimary,
    size: Dp = 32.dp,
) {
    if (LocalInspectionMode.current) {
        PreviewableSpinner(
            color = color,
            size = size,
        )
        return
    }

    CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = color,
    )
}

@Suppress("DEPRECATION")
@Composable
private fun PreviewableSpinner(
    color: Color = WhitePrimary,
    size: Dp = 32.dp,
) {
    CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = color,
        progress = 0.5f,
    )
}

@Preview
@Composable
private fun LoadingSpinnerPreview() {
    PreviewSurface {
        LoadingSpinner()
    }
}
