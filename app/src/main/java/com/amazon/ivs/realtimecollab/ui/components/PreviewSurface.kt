package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.amazon.ivs.realtimecollab.ui.theme.BlackPrimary
import com.amazon.ivs.realtimecollab.ui.theme.RealtimeCollabTheme

@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    background: Color = BlackPrimary,
    content: @Composable () -> Unit
) {
    RealtimeCollabTheme {
        Surface(
            color = background
        ) {
            Box(
                modifier = modifier
            ) {
                content()
            }
        }
    }
}

@Preview(
    name = "Pixel 10 Pro",
    apiLevel = 35,
    widthDp = 414,
    heightDp = 923,
)
@Preview(
    name = "Pixel 10 Pro Landscape",
    apiLevel = 35,
    widthDp = 923,
    heightDp = 414,
)
@Preview(
    name = "Samsung Flip 7",
    apiLevel = 35,
    widthDp = 406,
    heightDp = 1016,
)
@Preview(
    name = "Samsung Galaxy Tab S7",
    apiLevel = 35,
    widthDp = 934,
    heightDp = 1494,
)
@Preview(
    name = "Samsung Galaxy Tab S11 Ultra",
    apiLevel = 35,
    widthDp = 1237,
    heightDp = 1982,
)
@Preview(
    name = "Google Pixel 10 Pro Fold - Unfolded",
    apiLevel = 35,
    widthDp = 900,
    heightDp = 920,
)
@Preview(
    name = "Desktop",
    apiLevel = 35,
    widthDp = 2000,
    heightDp = 1126,
)
@Preview(
    name = "Split Screen",
    apiLevel = 35,
    widthDp = 400,
    heightDp = 400,
)
annotation class MultiPreview

@Preview(
    name = "Pixel 10 Pro Preview",
    apiLevel = 35,
    widthDp = 414,
    heightDp = 923,
)
@Preview(
    name = "Pixel 10 Pro Landscape",
    apiLevel = 35,
    widthDp = 923,
    heightDp = 414,
)
annotation class ScreenPreview

@Preview(
    name = "Pixel 10 Pro Preview",
    apiLevel = 35,
    widthDp = 414,
    heightDp = 923,
)
@Preview(
    name = "Pixel 10 Pro Landscape",
    apiLevel = 35,
    widthDp = 923,
    heightDp = 414,
)
@Preview(
    name = "Desktop",
    apiLevel = 35,
    widthDp = 2000,
    heightDp = 1126,
)
annotation class StageScreenPreview

@Preview(
    name = "1. Full Screen",
    apiLevel = 35,
    widthDp = 400,
    heightDp = 923,
)
@Preview(
    name = "2. Split Screen Big",
    apiLevel = 35,
    widthDp = 400,
    heightDp = 550,
)
@Preview(
    name = "3. Split Screen Square",
    apiLevel = 35,
    widthDp = 400,
    heightDp = 400,
)
@Preview(
    name = "4. Split Screen Small",
    apiLevel = 35,
    widthDp = 400,
    heightDp = 250,
)
annotation class SplitScreenPreview
