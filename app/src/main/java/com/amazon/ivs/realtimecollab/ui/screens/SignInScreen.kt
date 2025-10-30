package com.amazon.ivs.realtimecollab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.AuthHandler
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.ui.components.ButtonText
import com.amazon.ivs.realtimecollab.ui.components.ColumnContainer
import com.amazon.ivs.realtimecollab.ui.components.FadeBox
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PortraitColumn
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenPreview
import com.amazon.ivs.realtimecollab.ui.components.TextInput
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.InterHeader
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary

@Composable
fun SignInScreen(
    destination: Destination,
) {
    val isVisible = destination == Destination.SignInScreen
    val isLoading by AuthHandler.isLoading.collectAsStateWithLifecycle()
    val isError by AuthHandler.isError.collectAsStateWithLifecycle()

    FadeBox(
        isVisible = isVisible,
    ) {
        SignInScreenContent(
            isLoading = isLoading,
            isError = isError,
        )
    }
}

@Composable
private fun SignInScreenContent(
    isLoading: Boolean,
    isError: Boolean,
) {
    PortraitColumn(
        innerModifier = Modifier
            .padding(horizontal = 20.dp)
            .imePadding(),
        isOverlayVisible = true,
        maxWidth = 522.dp,
        scrollState = rememberScrollState(),
        verticalArrangement = Arrangement.spacedBy(
            space = 22.dp,
            alignment = Alignment.Bottom
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.sign_in),
            style = InterHeader,
        )
        ColumnContainer {
            val focusManager = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            var username by remember { mutableStateOf(value = "") }
            var password by remember { mutableStateOf(value = "") }

            fun signIn() {
                keyboard?.hide()
                AuthHandler.signIn(
                    username = username,
                    password = password,
                )
            }

            TextInput(
                label = stringResource(R.string.username_or_email),
                hint = stringResource(R.string.username_or_email_hint),
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Email,
                text = username,
                isError = isError && username.isBlank(),
                onValueChanged = { username = it },
                onImeAction = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
            TextInput(
                label = stringResource(R.string.password),
                hint = stringResource(R.string.password_hint),
                imeAction = ImeAction.Go,
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = isError && password.isBlank(),
                text = password,
                onValueChanged = { password = it },
                onImeAction = { signIn() }
            )
            ButtonText(
                text = stringResource(R.string.sign_in),
                background = OrangePrimary,
                textColor = BlackTertiary,
                rippleColor = GraySecondary,
                isLoading = isLoading,
                onClick = { signIn() },
            )
        }
        ButtonText(
            modifier = Modifier.padding(bottom = 30.dp),
            text = stringResource(R.string.create_account),
            onClick = {
                NavigationHandler.goTo(Destination.SignUpScreen)
            },
        )
    }
}

@MultiPreview
@Composable
private fun SignInPreview() {
    SignInScreenPreview()
}

@ScreenPreview
@Composable
private fun SignInLoadingPreview() {
    SignInScreenPreview(
        isLoading = true,
    )
}

@ScreenPreview
@Composable
private fun SignInErrorPreview() {
    SignInScreenPreview(
        isError = true,
    )
}

@Composable
private fun SignInScreenPreview(
    isLoading: Boolean = false,
    isError: Boolean = false,
) {
    PreviewSurface {
        SignInScreenContent(
            isLoading = isLoading,
            isError = isError,
        )
    }
}
