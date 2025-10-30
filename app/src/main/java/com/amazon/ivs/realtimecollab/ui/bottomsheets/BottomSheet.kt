package com.amazon.ivs.realtimecollab.ui.bottomsheets

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.core.handlers.BOTTOM_SHEET_ANIMATION_DURATION
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PortraitColumn
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.isDesktopLandscape
import com.amazon.ivs.realtimecollab.ui.components.thenOptional
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GrayPrimary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.composeunstyled.Text
import kotlin.math.roundToInt

enum class AnchorState { Hidden, Expanded }

@Composable
fun BottomSheetContainer(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(),
    contentAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable ColumnScope.() -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val isClosing by NavigationHandler.isBottomSheetClosing.collectAsStateWithLifecycle()
    var isOpen by remember { mutableStateOf(isPreview) }
    val yOffset = LocalDensity.current.run { 300.dp.toPx() }
    var maxOffset by remember { mutableIntStateOf(0) }
    val dragState = remember {
        AnchoredDraggableState(
            initialValue = AnchorState.Hidden,
            anchors = DraggableAnchors {
                AnchorState.Hidden at 0f
                AnchorState.Expanded at maxOffset.toFloat()
            },
        )
    }
    var dragAlpha by remember { mutableFloatStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    val offset by animateIntOffsetAsState(
        targetValue = if (isOpen) {
            IntOffset.Zero
        } else {
            IntOffset(
                x = 0,
                y = yOffset.roundToInt(),
            )
        },
        animationSpec = tween(
            durationMillis = BOTTOM_SHEET_ANIMATION_DURATION,
            easing = EaseOut,
        )
    )
    val alpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else if (isClosing) 0f else 0.5f,
        animationSpec = tween(
            durationMillis = BOTTOM_SHEET_ANIMATION_DURATION,
            easing = EaseOut,
        )
    )

    LaunchedEffect(key1 = isClosing) {
        isOpen = !isClosing
    }

    LaunchedEffect(key1 = dragState.offset) {
        val dragOffset = dragState.offset
        val startOffset = maxOffset / 2
        val margin = 5f
        val isCollapsed = dragOffset >= maxOffset - margin
        dragAlpha = when {
            dragOffset <= startOffset -> 0f
            isCollapsed -> 1f
            else -> (dragOffset - startOffset) / (maxOffset - startOffset)
        }
        if (isCollapsed) {
            NavigationHandler.hideBottomSheet()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .thenOptional(!isDesktopLandscape()) {
                background(color = BlackTertiary.copy(alpha = 0.4f - dragAlpha))
                    .alpha(alpha = alpha - dragAlpha)
            }
            .anchoredDraggable(
                state = dragState,
                orientation = Orientation.Vertical,
                interactionSource = interactionSource,
                enabled = true,
            )
            .offset {
                offset.copy(
                    y = offset.y + try {
                        dragState.requireOffset().roundToInt()
                    } catch (_: Exception) {
                        0
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        PortraitColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 80.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            innerModifier = Modifier
                .imePadding()
                .background(
                    color = GraySecondary,
                    shape = RoundedCornerShape(28.dp)
                )
                .onSizeChanged {
                    val expandedValue = it.height.toFloat()
                    maxOffset = it.height
                    dragState.updateAnchors(
                        newAnchors = DraggableAnchors {
                            AnchorState.Hidden at 0f
                            AnchorState.Expanded at expandedValue
                        }
                    )
                },
            contentAlignment = contentAlignment,
            verticalArrangement = Arrangement.Top,
            maxWidth = 420.dp,
        ) {
            DragIndicator()
            content()
        }
    }
}

@Composable
private fun ColumnScope.DragIndicator() {
    Box(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 8.dp)
            .size(
                width = 44.dp,
                height = 4.dp
            )
            .background(
                color = GrayPrimary,
                shape = RoundedCornerShape(100)
            )
    )
}

@MultiPreview
@Composable
private fun BottomSheetPreview() {
    PreviewSurface {
        BottomSheetContainer(
            contentAlignment = if (isDesktopLandscape()) Alignment.BottomEnd else Alignment.BottomCenter,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                text = "Content",
                style = InterPrimary,
            )
        }
    }
}
