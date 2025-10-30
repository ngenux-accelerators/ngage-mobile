package com.amazon.ivs.realtimecollab.ui.components

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary
import kotlin.math.abs

private const val SQUARE_SHAPE_DELTA_PX = 100
const val MIN_WIDTH = 300
const val MAX_WIDTH = 568

enum class ScreenType {
    PhonePortrait,
    TabletPortrait,
    PhoneLandscape,
    TabletLandscape,
    DesktopLandscape,
}

@Composable
fun isSquare(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    val isSquare = abs(size.width - size.height) < SQUARE_SHAPE_DELTA_PX
    return isSquare
}

@Composable
fun isLessThanSquare(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    val isSquare = size.height < size.width
    return isSquare
}

@Composable
fun isLandscape(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    val isLandscape = size.width >= size.height
    return isLandscape && !isMultiWindow()
}

@Composable
fun isSquareOrLandscape() = isSquare() || isLandscape()

@Composable
fun isSquareOrPortrait() = isSquare() || isPortrait()

@Composable
fun isPortrait() = !isSquareOrLandscape() || isMultiWindow()

@Composable
fun getScreenType(): ScreenType {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val width = density.run { size.width.toDp() }
    val height = density.run { size.height.toDp() }
    return if (isPortrait()) {
        when (width) {
            in 0.dp .. 600.dp -> ScreenType.PhonePortrait
            else -> ScreenType.TabletPortrait
        }
    } else {
        when (height) {
            in 0.dp .. 600.dp -> ScreenType.PhoneLandscape
            in 601.dp..1000.dp -> ScreenType.TabletLandscape
            else -> ScreenType.DesktopLandscape
        }
    }
}

@Composable
fun isMultiWindow() = LocalActivity.current?.isInMultiWindowMode ?: false

@Composable
fun isPhoneLandscape() = getScreenType() == ScreenType.PhoneLandscape

@Composable
fun isDesktopLandscape() = getScreenType() == ScreenType.DesktopLandscape

@Composable
fun isTabletPortrait() = getScreenType() == ScreenType.TabletPortrait

@Composable
fun screenHeight(): Dp {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return density.run { size.height.toDp() }
}

@Composable
fun SetSoftInputMode(mode: Int = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) {
    val activity = LocalActivity.current

    DisposableEffect(key1 = Unit) {
        val previousMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(mode)
        onDispose {
            val modeValue = previousMode ?: return@onDispose
            activity.window?.setSoftInputMode(modeValue)
        }
    }
}

@Composable
inline fun Modifier.onClick(
    isClickable: Boolean = true,
    rippleColor: Color = OrangePrimary,
    indication: Indication = ripple(color = rippleColor),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    crossinline onClick: () -> Unit,
) = clickable(
    enabled = isClickable,
    interactionSource = interactionSource,
    indication = indication,
    onClick = {
        onClick()
    },
)

inline fun Modifier.thenOptional(
    enabled: Boolean,
    apply: Modifier.() -> Modifier
) = if (enabled) {
    apply()
} else {
    this
}

inline fun Modifier.thenOptional(
    enabled: Boolean,
    ifEnabled: Modifier.() -> Modifier,
    ifDisabled: Modifier.() -> Modifier,
) = if (enabled) {
    ifEnabled()
} else {
    ifDisabled()
}

fun Modifier.fillMaxPortraitWidth(
    minWidth: Dp = MIN_WIDTH.dp,
    maxWidth: Dp = MAX_WIDTH.dp,
) = composed {
    if (isPortrait()) {
        widthIn(max = maxWidth)
    } else {
        val size = LocalWindowInfo.current.containerSize
        val width = size.width
        val portraitWidth = LocalDensity.current.run { width.toDp() }.coerceIn(
            minimumValue = if (minWidth < maxWidth) minWidth else (maxWidth - 1.dp).coerceAtLeast(0.dp),
            maximumValue = if (maxWidth >= minWidth) maxWidth else minWidth + 1.dp,
        )
        width(portraitWidth)
    }
}

fun Modifier.fillMaxPortraitSize(
    minWidth: Dp = MIN_WIDTH.dp,
    maxWidth: Dp = MAX_WIDTH.dp,
) = fillMaxPortraitWidth(
    minWidth = minWidth,
    maxWidth = maxWidth,
).fillMaxHeight()

fun ScreenType.isPhone() = this == ScreenType.PhonePortrait || this == ScreenType.PhoneLandscape
fun ScreenType.isLandscape() = this == ScreenType.PhoneLandscape ||
        this == ScreenType.TabletLandscape ||
        this == ScreenType.DesktopLandscape
fun ScreenType.isPortrait() = this == ScreenType.PhonePortrait || this == ScreenType.TabletPortrait
