package com.amazon.ivs.realtimecollab.ui.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.common.getMockMessages
import com.amazon.ivs.realtimecollab.core.handlers.ChatHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageMessage
import com.amazon.ivs.realtimecollab.ui.components.BoringAvatar
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.SetSoftInputMode
import com.amazon.ivs.realtimecollab.ui.components.TextInput
import com.amazon.ivs.realtimecollab.ui.components.isDesktopLandscape
import com.amazon.ivs.realtimecollab.ui.components.isTabletPortrait
import com.amazon.ivs.realtimecollab.ui.components.screenHeight
import com.amazon.ivs.realtimecollab.ui.components.thenOptional
import com.amazon.ivs.realtimecollab.ui.theme.BlackPentanary
import com.amazon.ivs.realtimecollab.ui.theme.BlackPrimary
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.InterTitle

@Composable
fun ChatBottomSheet() {
    val messages by ChatHandler.messages.collectAsStateWithLifecycle()

    SetSoftInputMode()
    ChatBottomSheetContent(
        messages = messages,
    )
}

@Composable
private fun ChatBottomSheetContent(
    messages: List<StageMessage>,
) {
    val maxHeight = screenHeight() / 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .thenOptional(
                enabled = isTabletPortrait(),
                ifEnabled = {
                    height(maxHeight)
                },
                ifDisabled = {
                    fillMaxHeight()
                }
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp),
        horizontalAlignment = if (isDesktopLandscape()) Alignment.End else Alignment.CenterHorizontally,
    ) {
        var message by remember { mutableStateOf(value = "") }
        val listState = rememberLazyListState()
        val imeInsets = WindowInsets.ime
        val imeVisible = imeInsets.getBottom(LocalDensity.current) > 0

        LaunchedEffect(key1 = messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }

        LaunchedEffect(imeVisible) {
            if (imeVisible && messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }

        Text(
            text = stringResource(R.string.chat),
            style = InterTitle,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(space = 5.dp),
        ) {
            items(
                items = messages,
            ) { message ->
                ChatMessageItem(
                    message = message,
                )
            }
        }
        TextInput(
            hint = stringResource(R.string.say_something),
            text = message,
            onValueChanged = { message = it },
            backgroundColor = BlackPrimary.copy(alpha = 0.5f),
            onImeAction = {
                ChatHandler.sendMessage(message)
                message = ""
            }
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: StageMessage,
) {
    val shape = RoundedCornerShape(26.dp)

    Row(
        modifier = Modifier
            .background(color = BlackPentanary, shape = shape)
            .clip(shape = shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoringAvatar(
            name = message.username,
            avatarSize = 24.dp,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.W500)) {
                    append("${message.username} ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.W400)) {
                    append(message.message)
                }
            },
            style = InterPrimary,
        )
    }
}

@MultiPreview
@Composable
private fun ChatBottomSheetPreview(
    messages: List<StageMessage> = getMockMessages(),
) {
    PreviewSurface {
        BottomSheetContainer(
            contentAlignment = if (isDesktopLandscape()) Alignment.BottomEnd else Alignment.BottomCenter,
        ) {
            ChatBottomSheetContent(
                messages = messages,
            )
        }
    }
}
