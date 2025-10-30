package com.amazon.ivs.realtimecollab.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.core.handlers.BottomSheetDestination
import com.amazon.ivs.realtimecollab.ui.bottomsheets.BottomSheetContainer
import com.amazon.ivs.realtimecollab.ui.bottomsheets.ChatBottomSheet
import com.amazon.ivs.realtimecollab.ui.bottomsheets.MembersBottomSheet
import com.amazon.ivs.realtimecollab.ui.bottomsheets.SettingsBottomSheet
import com.amazon.ivs.realtimecollab.ui.bottomsheets.SignOutBottomSheet
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenPreview
import com.amazon.ivs.realtimecollab.ui.components.isDesktopLandscape

@Composable
fun MainBottomSheetContent(
    bottomSheetDestination: BottomSheetDestination,
    innerPadding: PaddingValues = PaddingValues(),
) {
    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = bottomSheetDestination
    ) { destination ->
        if (destination == BottomSheetDestination.None) return@Crossfade
        val isSideBar = destination != BottomSheetDestination.None
                && destination != BottomSheetDestination.Settings
                && isDesktopLandscape()
        val padding = if (isSideBar) {
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 76.dp,
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
            )
        } else innerPadding

        BottomSheetContainer(
            innerPadding = padding,
            contentAlignment = if (isSideBar) Alignment.BottomEnd else Alignment.BottomCenter
        ) {
            when (destination) {
                BottomSheetDestination.None -> return@BottomSheetContainer
                BottomSheetDestination.Members -> MembersBottomSheet()
                BottomSheetDestination.Chat -> ChatBottomSheet()
                BottomSheetDestination.Settings -> SettingsBottomSheet()
                BottomSheetDestination.SignOut -> SignOutBottomSheet()
            }
        }
    }
}

@MultiPreview
@Composable
private fun MainBottomSheetMembersPreview() {
    MainBottomSheetContentPreview(
        bottomSheetDestination = BottomSheetDestination.Members,
    )
}

@ScreenPreview
@Composable
private fun MainBottomSheetChatPreview() {
    MainBottomSheetContentPreview(
        bottomSheetDestination = BottomSheetDestination.Chat,
    )
}

@ScreenPreview
@Composable
private fun MainBottomSheetSettingsPreview() {
    MainBottomSheetContentPreview(
        bottomSheetDestination = BottomSheetDestination.Settings,
    )
}

@ScreenPreview
@Composable
private fun MainBottomSheetSignOutPreview() {
    MainBottomSheetContentPreview(
        bottomSheetDestination = BottomSheetDestination.SignOut,
    )
}

@Composable
private fun MainBottomSheetContentPreview(
    bottomSheetDestination: BottomSheetDestination
) {
    PreviewSurface {
        MainBottomSheetContent(bottomSheetDestination = bottomSheetDestination)
    }
}
