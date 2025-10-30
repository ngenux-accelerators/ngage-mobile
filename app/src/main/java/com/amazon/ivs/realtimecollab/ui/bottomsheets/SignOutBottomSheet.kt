package com.amazon.ivs.realtimecollab.ui.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.AuthHandler
import com.amazon.ivs.realtimecollab.ui.components.BoringAvatar
import com.amazon.ivs.realtimecollab.ui.components.ButtonText
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenPreview
import com.amazon.ivs.realtimecollab.ui.components.isPhoneLandscape
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.RedPrimary

@Composable
fun SignOutBottomSheet() {
    val isLoading by AuthHandler.isLoading.collectAsStateWithLifecycle()
    val user by AuthHandler.user.collectAsStateWithLifecycle()

    SignOutBottomSheetContent(
        username = user.username,
        isLoading = isLoading,
    )
}

@Composable
private fun SignOutBottomSheetContent(
    username: String,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = if (isPhoneLandscape()) 20.dp else 32.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoringAvatar(
            name = username,
            avatarSize = 100.dp,
        )
        Text(
            text = username,
            style = InterPrimary.copy(fontSize = 24.sp),
        )
        ButtonText(
            modifier = Modifier.padding(
                top = if (isPhoneLandscape()) 20.dp else 40.dp,
                bottom = if (isPhoneLandscape()) 20.dp else 40.dp
            ),
            text = stringResource(R.string.log_out),
            background = RedPrimary,
            isLoading = isLoading,
            onClick = AuthHandler::signOut,
        )
    }
}

@MultiPreview
@Composable
private fun SignOutPreview() {
    SignOutBottomSheetPreview()
}

@ScreenPreview
@Composable
private fun SignOutLoadingPreview() {
    SignOutBottomSheetPreview(isLoading = true)
}

@Composable
private fun SignOutBottomSheetPreview(
    username: String = "username",
    isLoading: Boolean = false,
) {
    PreviewSurface {
        BottomSheetContainer {
            SignOutBottomSheetContent(
                username = username,
                isLoading = isLoading,
            )
        }
    }
}
