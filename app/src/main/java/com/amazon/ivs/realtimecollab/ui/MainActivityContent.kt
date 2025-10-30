package com.amazon.ivs.realtimecollab.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.screens.HomeScreen
import com.amazon.ivs.realtimecollab.ui.screens.JoinScreen
import com.amazon.ivs.realtimecollab.ui.screens.SignInScreen
import com.amazon.ivs.realtimecollab.ui.screens.SignUpScreen
import com.amazon.ivs.realtimecollab.ui.screens.StageScreen

@Composable
fun MainActivityContent(
    destination: Destination,
    innerPadding: PaddingValues,
) {
    MainActivityContentContent(
        modifier = Modifier,
        innerPadding = innerPadding,
        destination = destination,
    )
}

@Composable
private fun MainActivityContentContent(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    destination: Destination = Destination.None,
) {
    Box(
        modifier = modifier.padding(innerPadding),
    ) {
        HomeScreen(destination = destination)
        JoinScreen(destination = destination)
        SignInScreen(destination = destination)
        SignUpScreen(destination = destination)
        StageScreen(destination = destination)
    }
}

@MultiPreview
@Composable
private fun SignInPortraitPreview() {
    MainActivityContentPreview(destination = Destination.SignInScreen)
}

@MultiPreview
@Composable
private fun SignUpPortraitPreview() {
    MainActivityContentPreview(destination = Destination.SignUpScreen)
}

@MultiPreview
@Composable
private fun HomePortraitPreview() {
    MainActivityContentPreview(destination = Destination.HomeScreen)
}

@MultiPreview
@Composable
private fun JoinPortraitPreview() {
    MainActivityContentPreview(destination = Destination.JoinScreen)
}

@Composable
private fun MainActivityContentPreview(
    destination: Destination,
) {
    PreviewSurface {
        MainActivityContentContent(
            destination = destination,
            innerPadding = PaddingValues(),
        )
    }
}
