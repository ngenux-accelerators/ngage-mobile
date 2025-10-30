package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.ui.theme.BlackPrimary
import com.amazon.ivs.realtimecollab.ui.theme.BlackQuaternary
import com.amazon.ivs.realtimecollab.ui.theme.GrayPrimary
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary
import com.amazon.ivs.realtimecollab.ui.theme.RobotoPrimary

private const val FADE_IN_DURATION = 300
private const val FADE_OUT_DURATION = 300
private val easeIn = EaseIn
private val easeOut = EaseOut

@Composable
fun BoxContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BlackQuaternary,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 20.dp)
            .padding(top = 30.dp, bottom = 20.dp),
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
fun ColumnContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BlackQuaternary,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(20.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 20.dp)
            .padding(top = 30.dp, bottom = 20.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun PortraitColumn(
    modifier: Modifier = Modifier,
    innerModifier: Modifier = Modifier,
    scrollState: ScrollState? = null,
    minWidth: Dp = MIN_WIDTH.dp,
    maxWidth: Dp = MAX_WIDTH.dp,
    isOverlayVisible: Boolean = false,
    contentAlignment: Alignment = Alignment.BottomCenter,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(22.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    BackgroundOverlay(
        modifier = modifier,
        isVisible = isOverlayVisible,
        contentAlignment = contentAlignment,
    ) {
        Column(
            modifier = innerModifier
                .fillMaxPortraitWidth(
                    minWidth = minWidth,
                    maxWidth = maxWidth
                )
                .thenOptional(enabled = scrollState != null) {
                    verticalScroll(state = scrollState!!)
                },
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}

@Composable
fun ColumnRow(
    isColumn: Boolean,
    modifier: Modifier = Modifier,
    spacedBy: Dp = 10.dp,
    content: @Composable () -> Unit,
) {
    if (isColumn) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(spacedBy),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacedBy),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
fun BackgroundOverlay(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = contentAlignment,
    ) {
        FadeBox(
            modifier = Modifier.fillMaxSize(),
            isVisible = isVisible
        ) {
            Image(
                modifier = Modifier.fillMaxPortraitWidth(),
                painter = painterResource(R.drawable.bg_overlay),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }
        content()
    }
}

@Composable
fun ScreenBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 30.dp, top = 20.dp),
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
fun ScreenColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(20.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 30.dp, top = 20.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun FadeBox(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    innerModifier: Modifier = Modifier,
    fadeInDelay: Int = 0,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable() BoxScope.() -> Unit = {},
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = FADE_IN_DURATION,
                delayMillis = fadeInDelay,
                easing = easeIn
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = FADE_OUT_DURATION,
                easing = easeOut
            )
        ),
    ) {
        Box(
            modifier = innerModifier,
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

@Composable
fun CrossfadeBox(
    isFirstContent: Boolean,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    firstContent: @Composable () -> Unit = {},
    secondContent: @Composable () -> Unit = {},
) {
    Crossfade(
        modifier = modifier,
        targetState = isFirstContent,
        animationSpec = tween(
            durationMillis = FADE_IN_DURATION,
            easing = easeIn
        ),
    ) { state ->
        Box(
            modifier = modifier,
            contentAlignment = contentAlignment
        ) {
            if (state) {
                firstContent()
            } else {
                secondContent()
            }
        }
    }
}

@Composable
fun ColumnWithConstraints(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    onSizeChanged: (Dp, Dp) -> Unit = { _, _ -> },
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .onSizeChanged { size ->
                onSizeChanged(
                    with(density) { size.width.toDp() },
                    with(density) { size.height.toDp() },
                )
            },
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Preview
@Composable
private fun ContainerPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.background(BlackPrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BoxContainer(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Box container",
                    style = RobotoPrimary,
                )
            }
            ColumnContainer {
                ButtonText(
                    text = "Button 1",
                    background = OrangePrimary,
                    textColor = BlackQuaternary,
                    onClick = {}
                )
                ButtonText(
                    text = "Button 2",
                    onClick = {}
                )
            }
        }
    }
}

@MultiPreview
@Composable
private fun PortraitColumnPreview() {
    PreviewSurface {
        PortraitColumn {
            BoxContainer(
                backgroundColor = GrayPrimary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Content",
                    style = InterPrimary,
                )
            }
        }
    }
}
