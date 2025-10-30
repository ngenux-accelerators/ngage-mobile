package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.ui.theme.BlackSecondary
import com.amazon.ivs.realtimecollab.ui.theme.BlackTertiary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.InterDescription
import com.amazon.ivs.realtimecollab.ui.theme.InterHint
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary
import com.amazon.ivs.realtimecollab.ui.theme.RedPrimary
import com.amazon.ivs.realtimecollab.ui.theme.RobotoPrimary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

@Composable
fun ButtonText(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = GraySecondary,
    textColor: Color = WhitePrimary,
    rippleColor: Color = OrangePrimary,
    padding: PaddingValues = PaddingValues(),
    shape: RoundedCornerShape = RoundedCornerShape(100),
    style: TextStyle = RobotoPrimary.copy(color = textColor),
    isClickable: Boolean = true,
    isLoading: Boolean = false,
    fillMaxWidth: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .thenOptional(fillMaxWidth) {
                fillMaxWidth()
            }
            .height(48.dp)
            .background(
                color = background,
                shape = shape
            )
            .padding(padding)
            .clip(shape)
            .onClick(
                isClickable = isClickable,
                rippleColor = rippleColor,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        FadeBox(isVisible = isLoading) {
            LoadingSpinner(
                size = 32.dp,
                color = textColor,
            )
        }
        FadeBox(isVisible = !isLoading) {
            Text(
                text = text,
                style = style,
            )
        }
    }
}

@Composable
fun ButtonIcon(
    icon: Int,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    buttonSize: DpSize? = DpSize(width = 48.dp, height = 48.dp),
    iconSize: Dp = 24.dp,
    background: Color = GraySecondary,
    tint: Color = WhitePrimary,
    rippleColor: Color = OrangePrimary,
    text: String? = null,
    onClick: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(100)

    Box(
        modifier = modifier
            .thenOptional(buttonSize != null) {
                size(buttonSize!!)
            }
            .background(
                color = background,
                shape = buttonShape
            )
            .clip(buttonShape)
            .onClick(
                rippleColor = rippleColor,
                onClick = onClick,
            )
            .padding(paddingValues = innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(icon),
                contentDescription = null,
                tint = tint,
            )
            if (text != null) {
                Text(
                    text = text,
                    style = InterHint.copy(color = tint),
                )
            }
        }
    }
}

@Composable
fun ButtonSwitch(
    text: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
        ) {
            Box(
                modifier = Modifier.height(48.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = text,
                    style = InterPrimary,
                )
            }
            if (description != null) {
                Text(
                    text = description,
                    style = InterDescription,
                )
            }
        }
        Switch(
            modifier = Modifier.height(48.dp),
            checked = isChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WhitePrimary,
                checkedTrackColor = OrangePrimary,
                uncheckedThumbColor = WhitePrimary,
                uncheckedTrackColor = BlackSecondary,
                uncheckedBorderColor = BlackSecondary,
            ),
            onCheckedChange = onCheckedChange,
            thumbContent = {
                Box(
                    modifier = Modifier
                        .size(23.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                        )
                        .background(
                            color = WhitePrimary,
                            shape = CircleShape,
                        )
                )
            }
        )
    }
}

@Preview
@Composable
private fun ButtonPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ButtonText(
                text = "Primary",
                background = OrangePrimary,
                textColor = BlackTertiary,
                onClick = {}
            )
            ButtonText(
                text = "Secondary",
                onClick = {}
            )
            ButtonText(
                text = "Loading primary",
                background = OrangePrimary,
                textColor = BlackTertiary,
                isLoading = true,
                onClick = {}
            )
            ButtonIcon(
                icon = R.drawable.ic_arrow,
                onClick = {}
            )
            ButtonIcon(
                modifier = Modifier.fillMaxWidth()
                    .height(48.dp),
                buttonSize = null,
                background = RedPrimary,
                icon = R.drawable.ic_phone,
                onClick = {},
            )
            ButtonIcon(
                modifier = Modifier.fillMaxWidth(),
                buttonSize = null,
                innerPadding = PaddingValues(vertical = 20.dp),
                text = "Unmute",
                icon = R.drawable.ic_mic_off,
                onClick = {},
            )
            ButtonSwitch(
                text = "Switch with title",
                isChecked = true,
                onCheckedChange = {}
            )
            ButtonSwitch(
                text = "Switch with description",
                description = "Description text that is very very long, maybe even multiple lines long",
                isChecked = false,
                onCheckedChange = {}
            )
        }
    }
}
