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
import androidx.compose.ui.res.stringResource
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
fun SignUpScreen(
    destination: Destination,
) {
    val isVisible = destination == Destination.SignUpScreen
    val isLoading by AuthHandler.isLoading.collectAsStateWithLifecycle()
    val isError by AuthHandler.isError.collectAsStateWithLifecycle()

    FadeBox(
        isVisible = isVisible,
    ) {
        SignUpScreenContent(
            isLoading = isLoading,
            isError = isError,
        )
    }
}

@Composable
private fun SignUpScreenContent(
    isLoading: Boolean,
    isError: Boolean,
) {
    PortraitColumn(
        innerModifier = Modifier
            .padding(horizontal = 20.dp)
            .imePadding(),
        scrollState = rememberScrollState(),
        isOverlayVisible = true,
        maxWidth = 522.dp,
        verticalArrangement = Arrangement.spacedBy(
            space = 22.dp,
            alignment = Alignment.Bottom
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.sign_up),
            style = InterHeader,
        )
        ColumnContainer {
            var username by remember { mutableStateOf(value = "") }
            var email by remember { mutableStateOf(value = "") }
            var password1 by remember { mutableStateOf(value = "") }
            var password2 by remember { mutableStateOf(value = "") }

            fun signUp() {
                AuthHandler.signUp(
                    username = username,
                    email = email,
                    password1 = password1,
                    password2 = password2,
                )
            }

            TextInput(
                label = stringResource(R.string.username),
                hint = stringResource(R.string.username_hint),
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Text,
                isError = isError && username.isBlank(),
                text = username,
                onValueChanged = { username = it },
            )
            TextInput(
                label = stringResource(R.string.email),
                hint = stringResource(R.string.email_hint),
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Email,
                isError = isError && email.isBlank(),
                text = email,
                onValueChanged = { email = it },
            )
            TextInput(
                label = stringResource(R.string.password),
                hint = stringResource(R.string.password_hint),
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = isError && password1.isBlank(),
                text = password1,
                onValueChanged = { password1 = it },
            )
            TextInput(
                label = stringResource(R.string.confirm_password),
                hint = stringResource(R.string.confirm_password_hint),
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = isError && password2.isBlank(),
                text = password2,
                onValueChanged = { password2 = it },
                onImeAction = { signUp() }
            )
            ButtonText(
                text = stringResource(R.string.sign_up),
                background = OrangePrimary,
                textColor = BlackTertiary,
                rippleColor = GraySecondary,
                isLoading = isLoading,
                onClick = { signUp() },
            )
        }
        ButtonText(
            modifier = Modifier.padding(bottom = 30.dp),
            text = stringResource(R.string.sign_in),
            onClick = {
                NavigationHandler.goTo(destination = Destination.SignInScreen)
            },
        )
    }
}

@MultiPreview
@Composable
private fun SignUpPreview() {
    SignUpScreenPreview()
}

@ScreenPreview
@Composable
private fun SignUpLoadingPreview() {
    SignUpScreenPreview(
        isLoading = true,
    )
}

@ScreenPreview
@Composable
private fun SignUpErrorPreview() {
    SignUpScreenPreview(
        isError = true,
    )
}

@Composable
private fun SignUpScreenPreview(
    isLoading: Boolean = false,
    isError: Boolean = false,
) {
    PreviewSurface {
        SignUpScreenContent(
            isLoading = isLoading,
            isError = isError,
        )
    }
}
