package com.amazon.ivs.realtimecollab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.LoadingState
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.ui.components.BackgroundOverlay
import com.amazon.ivs.realtimecollab.ui.components.ButtonIcon
import com.amazon.ivs.realtimecollab.ui.components.FadeBox
import com.amazon.ivs.realtimecollab.ui.components.LoadingSpinner
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenPreview
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary

@Composable
fun LoadingScreenContent(
    loadingState: LoadingState,
    innerPadding: PaddingValues,
) {
    FadeBox(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .background(color = BlackTertiary),
        isVisible = loadingState != LoadingState.None,
    ) {
        val canGoBack = loadingState == LoadingState.Loading

        if (canGoBack) {
            ButtonIcon(
                modifier = Modifier.padding(20.dp),
                icon = R.drawable.ic_back,
                background = Color.Transparent,
                onClick = NavigationHandler::goBack,
            )
        }
        BackgroundOverlay(
            isVisible = !canGoBack,
            contentAlignment = Alignment.Center,
        ) {
            LoadingSpinner()
        }
    }
}

@ScreenPreview
@Composable
private fun LoadingScreenSplashPreview() {
    LoadingScreenPreview(
        loadingState = LoadingState.Splash,
    )
}

@ScreenPreview
@Composable
private fun LoadingScreenLoadingPreview() {
    LoadingScreenPreview(
        loadingState = LoadingState.Loading,
    )
}

@Composable
private fun LoadingScreenPreview(
    loadingState: LoadingState,
) {
    PreviewSurface {
        LoadingScreenContent(
            loadingState = loadingState,
            innerPadding = PaddingValues(0.dp),
        )
    }
}
