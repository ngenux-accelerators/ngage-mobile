package com.amazon.ivs.realtimecollab.ui.screens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.AuthHandler
import com.amazon.ivs.realtimecollab.core.handlers.BottomSheetDestination
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.ui.components.BoringAvatar
import com.amazon.ivs.realtimecollab.ui.components.ButtonIcon
import com.amazon.ivs.realtimecollab.ui.components.ButtonText
import com.amazon.ivs.realtimecollab.ui.components.FadeBox
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenBox
import com.amazon.ivs.realtimecollab.ui.components.ScreenType
import com.amazon.ivs.realtimecollab.ui.components.TextInput
import com.amazon.ivs.realtimecollab.ui.components.fillMaxPortraitWidth
import com.amazon.ivs.realtimecollab.ui.components.getScreenType
import com.amazon.ivs.realtimecollab.ui.components.isPortrait
import com.amazon.ivs.realtimecollab.ui.components.onClick
import com.amazon.ivs.realtimecollab.ui.components.thenOptional
import com.amazon.ivs.realtimecollab.ui.theme.BlackQuaternary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.InterHeader
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.InterSecondary
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

private val random_background = listOf(
    R.drawable.bg_home_1,
    R.drawable.bg_home_2,
    R.drawable.bg_home_3,
    R.drawable.bg_home_4,
    R.drawable.bg_home_5,
).random()

@Composable
fun HomeScreen(
    destination: Destination,
) {
    val isVisible = destination == Destination.HomeScreen
    val user by AuthHandler.user.collectAsStateWithLifecycle()

    FadeBox(
        isVisible = isVisible,
    ) {
        HomeScreenContent(
            username = user.username,
            background = random_background,
        )
    }
}

@Composable
private fun HomeScreenContent(
    username: String,
    background: Int,
) {
    ScreenBox {
        if (isPortrait()) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .fillMaxHeight()
                    .verticalScroll(state = rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp),
            ) {
                HomeBanner(
                    username = username,
                    background = background,
                )
                Spacer(modifier = Modifier.weight(1f))
                HomeContent()
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                HomeContent(
                    modifier = Modifier
                        .verticalScroll(state = rememberScrollState())
                        .imePadding(),
                )
                HomeBanner(
                    username = username,
                    background = background,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
) {
    val maxWidth = when (getScreenType()) {
        ScreenType.PhonePortrait,
        ScreenType.TabletPortrait -> Dp.Unspecified
        ScreenType.PhoneLandscape,
        ScreenType.TabletLandscape,
        ScreenType.DesktopLandscape -> 335.dp
    }

    var isVoiceOnly by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxPortraitWidth(maxWidth = maxWidth)
            .fillMaxHeight()
            .then(modifier),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.amazon_ivs_real_time),
                style = InterHeader,
            )
            Text(
                text = stringResource(R.string.amazon_ivs_description),
                style = InterSecondary,
            )
            Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ButtonText(
                            modifier = Modifier.weight(1f),
                            fillMaxWidth = false,
                            text = stringResource(R.string.session_type_video),
                            background = if (!isVoiceOnly) OrangePrimary else GraySecondary,
                            textColor = if (!isVoiceOnly) BlackQuaternary else WhitePrimary,
                            rippleColor = GraySecondary,
                            onClick = { isVoiceOnly = false },
                        )
                        ButtonText(
                            modifier = Modifier.weight(1f),
                            fillMaxWidth = false,
                            text = stringResource(R.string.session_type_voice),
                            background = if (isVoiceOnly) OrangePrimary else GraySecondary,
                            textColor = if (isVoiceOnly) BlackQuaternary else WhitePrimary,
                            rippleColor = GraySecondary,
                            onClick = { isVoiceOnly = true },
                        )
                    }
                    ButtonText(
                        text = stringResource(R.string.new_stage),
                        background = OrangePrimary,
                        textColor = BlackQuaternary,
                        rippleColor = GraySecondary,
                        onClick = { StageHandler.joinMeeting(isVoiceOnly = isVoiceOnly) },
                    )
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val keyboard = LocalSoftwareKeyboardController.current
                var meetingId by remember { mutableStateOf(value = "") }

                fun joinStage() {
                    keyboard?.hide()
                    StageHandler.joinMeeting(meetingId)
                }

                TextInput(
                    containerModifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    icon = R.drawable.ic_keyboard,
                    hint = stringResource(R.string.enter_stage_code_hint),
                    text = meetingId,
                    capitalization = KeyboardCapitalization.None,
                    onValueChanged = { meetingId = it },
                    onImeAction = { joinStage() }
                )
                ButtonIcon(
                    icon = R.drawable.ic_arrow,
                    buttonSize = DpSize(width = 54.dp, height = 48.dp),
                    onClick = { joinStage() }
                )
            }
        }
    }
}

@Composable
private fun HomeBanner(
    username: String,
    background: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        val transition = rememberInfiniteTransition()
        val kenBurns by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 * 25,
                    easing = EaseInOut,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
        )

        Box(
            modifier = Modifier
                .thenOptional(isPortrait()) {
                    aspectRatio(1f)
                }
                .thenOptional(!isPortrait()) {
                    fillMaxSize()
                }
                .clip(shape = RoundedCornerShape(40.dp)),
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale = kenBurns),
                painter = painterResource(background),
                contentDescription = null,
                contentScale = if (isPortrait()) ContentScale.FillWidth else ContentScale.Crop,
            )
        }
        Row(
            modifier = Modifier
                .padding(10.dp)
                .background(color = GraySecondary, shape = RoundedCornerShape(100))
                .clip(shape = RoundedCornerShape(100))
                .onClick {
                    NavigationHandler.showBottomSheet(BottomSheetDestination.SignOut)
                }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoringAvatar(
                name = username,
                avatarSize = 24.dp,
            )
            Text(
                text = username,
                style = InterPrimary,
            )
        }
    }
}

@MultiPreview
@Composable
private fun HomeScreenPreview(
    username: String = "Eddy",
    background: Int = random_background,
) {
    PreviewSurface {
        HomeScreenContent(
            username = username,
            background = background,
        )
    }
}
