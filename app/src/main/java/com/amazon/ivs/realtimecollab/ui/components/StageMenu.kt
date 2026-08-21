package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.GrayTertiary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

@Composable
fun StageMenu(
    modifier: Modifier = Modifier,
) {
    val isMembersOpen by StageHandler.isMembersOpen.collectAsStateWithLifecycle()
    val isChatOpen by StageHandler.isChatOpen.collectAsStateWithLifecycle()

    StageMenuContent(
        modifier = modifier,
        isMembersOpen = isMembersOpen,
        isChatOpen = isChatOpen,
    )
}

@Composable
private fun StageMenuContent(
    isMembersOpen: Boolean,
    isChatOpen: Boolean,
    modifier: Modifier = Modifier,
) {
    val membersBackground by animateColorAsState(
        targetValue = if (isMembersOpen) GrayTertiary else GraySecondary,
    )
    val membersIcon by animateColorAsState(
        targetValue = if (isMembersOpen) BlackTertiary else WhitePrimary,
    )
    val chatBackground by animateColorAsState(
        targetValue = if (isChatOpen) GrayTertiary else GraySecondary,
    )
    val chatIcon by animateColorAsState(
        targetValue = if (isChatOpen) BlackTertiary else WhitePrimary,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ButtonIcon(
            icon = R.drawable.ic_participants,
            background = membersBackground,
            tint = membersIcon,
            onClick = StageHandler::toggleMembersOpen,
        )
        // ButtonIcon(
        //     icon = R.drawable.ic_chat,
        //     background = chatBackground,
        //     tint = chatIcon,
        //     onClick = StageHandler::toggleChatOpen,
        // )
    }
}

@Preview
@Composable
private fun StageMenuAllClosed() {
    StageMenuPreview()
}

@Preview
@Composable
private fun StageMenuMembersOpen() {
    StageMenuPreview(
        isMembersOpen = true,
    )
}

@Preview
@Composable
private fun StageMenuChatOpen() {
    StageMenuPreview(
        isChatOpen = true,
    )
}

@Composable
private fun StageMenuPreview(
    isMembersOpen: Boolean = false,
    isChatOpen: Boolean = false,
) {
    PreviewSurface {
        StageMenuContent(
            isMembersOpen = isMembersOpen,
            isChatOpen = isChatOpen,
        )
    }
}
