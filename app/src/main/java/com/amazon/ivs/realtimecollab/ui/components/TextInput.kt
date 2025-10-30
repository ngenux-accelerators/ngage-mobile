package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.ui.theme.BlackQuaternary
import com.amazon.ivs.realtimecollab.ui.theme.GrayPrimary
import com.amazon.ivs.realtimecollab.ui.theme.GraySecondary
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.OrangePrimary
import com.amazon.ivs.realtimecollab.ui.theme.RedPrimary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

@Composable
private fun textFieldColors(
    backgroundColor: Color = GraySecondary,
    textColor: Color = WhitePrimary,
    hintColor: Color = GrayPrimary,
) = TextFieldDefaults.colors(
    cursorColor = OrangePrimary,
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedLabelColor = textColor,
    unfocusedLabelColor = textColor,
    unfocusedPlaceholderColor = hintColor,
    focusedPlaceholderColor = hintColor,
    disabledPlaceholderColor = hintColor,
    focusedContainerColor = backgroundColor,
    unfocusedContainerColor = backgroundColor,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor =  Color.Transparent,
    selectionColors = TextSelectionColors(handleColor = OrangePrimary, backgroundColor = GraySecondary),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInput(
    hint: String,
    modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    text: String = "",
    label: String = "",
    maxLines: Int = 1,
    icon: Int? = null,
    isError: Boolean = false,
    isPassword: Boolean = false,
    fillMaxWidth: Boolean = true,
    singleLine: Boolean = true,
    backgroundColor: Color = GraySecondary,
    textColor: Color = WhitePrimary,
    labelColor: Color = WhitePrimary,
    hintColor: Color = GrayPrimary,
    colors: TextFieldColors = textFieldColors(
        backgroundColor = backgroundColor,
        textColor = textColor,
        hintColor = hintColor
    ),
    textStyle: TextStyle = InterPrimary.copy(color = textColor),
    hintStyle: TextStyle = InterPrimary.copy(color = hintColor),
    shape: Shape = RoundedCornerShape(100),
    imeAction: ImeAction = ImeAction.Done,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    onValueChanged: (String) -> Unit = {},
    onImeAction: (ImeAction) -> Unit = {}
) {
    val placeholder: (@Composable () -> Unit)? = if (text.isBlank()) {{
        Text(
            text = hint,
            style = hintStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }} else null
    val leadingIcon: (@Composable () -> Unit)? = if (icon != null) {{
        Icon(
            modifier = Modifier.padding(start = 10.dp),
            painter = painterResource(icon),
            contentDescription = null,
        )
    }} else null
    val borderWidth by animateDpAsState(targetValue = if (isError) 2.dp else 0.dp)
    val borderColor by animateColorAsState(targetValue = if (isError) RedPrimary else Color.Transparent)

    Column(
        modifier = containerModifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = InterPrimary.copy(color = labelColor),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        CompositionLocalProvider(
            LocalTextSelectionColors provides colors.textSelectionColors
        ) {
            BasicTextField(
                modifier = modifier
                    .thenOptional(fillMaxWidth) {
                        fillMaxWidth()
                    }
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = shape
                    ),
                value = text,
                maxLines = maxLines,
                singleLine = singleLine,
                textStyle = textStyle,
                onValueChange = onValueChanged,
                keyboardOptions = KeyboardOptions(
                    capitalization = capitalization,
                    imeAction = imeAction,
                    keyboardType = keyboardType
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction(ImeAction.Next) },
                    onDone = { onImeAction(ImeAction.Done) },
                    onSearch = { onImeAction(ImeAction.Search) },
                    onGo = { onImeAction(ImeAction.Go) },
                    onPrevious = { onImeAction(ImeAction.Previous) },
                    onSend = { onImeAction(ImeAction.Send) }
                ),
                cursorBrush = SolidColor(OrangePrimary),
                visualTransformation = if (isPassword) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = text,
                        innerTextField = innerTextField,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        shape = shape,
                        singleLine = singleLine,
                        enabled = true,
                        visualTransformation = if (isPassword) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        interactionSource = interactionSource,
                        colors = colors,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun TextInputPreview() {
    PreviewSurface(
        background = BlackQuaternary,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextInput(
                hint = "Text hint",
            )
            TextInput(
                text = "Text input",
                hint = "",
            )
            TextInput(
                label = "Text label",
                hint = "Enter your username or email",
            )
            TextInput(
                hint = "Text hint",
                fillMaxWidth = false,
                icon = R.drawable.ic_arrow,
            )
            TextInput(
                hint = "Text hint",
                isError = true,
            )
        }
    }
}
