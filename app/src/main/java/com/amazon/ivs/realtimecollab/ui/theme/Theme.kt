package com.amazon.ivs.realtimecollab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    primary = BlackPrimary,
    secondary = BlackPrimary,
    tertiary = BlackPrimary,
    background = BlackPrimary,
    surface = BlackPrimary,
    onPrimary = WhitePrimary,
    onSecondary = WhitePrimary,
    onTertiary = WhitePrimary,
    onSurface = WhitePrimary
)

@Composable
fun RealtimeCollabTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
