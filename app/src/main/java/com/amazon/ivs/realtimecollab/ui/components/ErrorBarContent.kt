package com.amazon.ivs.realtimecollab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.ErrorDestination
import com.amazon.ivs.realtimecollab.ui.theme.InterError
import com.amazon.ivs.realtimecollab.ui.theme.RedSecondary
import com.amazon.ivs.realtimecollab.ui.theme.RedTertiary

@Composable
fun ErrorBarContent(
    errorDestination: ErrorDestination,
    innerPadding: PaddingValues = PaddingValues(),
) {
    FadeBox(
        isVisible = errorDestination != ErrorDestination.None,
    ) {
        val shape = RoundedCornerShape(10.dp)

        Row(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .padding(20.dp)
                .defaultMinSize(minHeight = 48.dp)
                .background(color = RedSecondary, shape = shape),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            var errorInt by remember { mutableIntStateOf(errorDestination.error) }
            var hasAction by remember { mutableStateOf(errorDestination.hasAction) }
            val errorString = if (errorInt != -1) stringResource(errorInt) else ""

            LaunchedEffect(key1 = errorDestination) {
                if (errorDestination == ErrorDestination.None) return@LaunchedEffect
                errorInt = errorDestination.error
                hasAction = errorDestination.hasAction
            }

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                text = errorString,
                style = InterError,
            )

            if (hasAction) {
                ButtonText(
                    text = "Retry",
                    fillMaxWidth = false,
                    background = Color.Transparent,
                    style = InterError.copy(color = RedTertiary),
                    padding = PaddingValues(horizontal = 20.dp),
                    shape = shape,
                    onClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun ErrorBarPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ErrorBarContent(
                errorDestination = ErrorDestination.Toast(
                    error = R.string.err_sign_in,
                    hasAction = true,
                )
            )
            ErrorBarContent(
                errorDestination = ErrorDestination.Toast(
                    error = R.string.err_passwords_dont_match,
                    hasAction = false,
                )
            )
        }
    }
}
