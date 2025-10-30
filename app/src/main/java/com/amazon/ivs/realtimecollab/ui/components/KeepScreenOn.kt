package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn(keepScreenOn: Boolean) {
    val currentView = LocalView.current

    DisposableEffect(Unit) {
        currentView.keepScreenOn = keepScreenOn
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}
