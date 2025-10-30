package com.amazon.ivs.realtimecollab.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.amazon.ivs.realtimecollab.R

private val Inter @Composable get() = if (LocalInspectionMode.current) {
    FontFamily.SansSerif
} else {
    FontFamily(
        Font(resId = R.font.inter, weight = FontWeight.Normal),
        Font(resId = R.font.inter_black, weight = FontWeight.Black),
        Font(resId = R.font.inter_bold, weight = FontWeight.Bold),
        Font(resId = R.font.inter_extrabold, weight = FontWeight.ExtraBold),
        Font(resId = R.font.inter_light, weight = FontWeight.Light),
        Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
        Font(resId = R.font.inter_semibold, weight = FontWeight.SemiBold),
    )
}

private val Roboto @Composable get() = if (LocalInspectionMode.current) {
    FontFamily.SansSerif
} else {
    FontFamily(
        Font(resId = R.font.roboto, weight = FontWeight.Normal),
        Font(resId = R.font.roboto_black, weight = FontWeight.Black),
        Font(resId = R.font.roboto_bold, weight = FontWeight.Bold),
        Font(resId = R.font.roboto_light, weight = FontWeight.Light),
        Font(resId = R.font.roboto_medium, weight = FontWeight.Medium),
    )
}

val InterHeader @Composable get() = TextStyle(
    color = WhitePrimary,
    fontFamily = Inter,
    fontSize = 40.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.Bold,
)

val InterPrimary @Composable get() = TextStyle(
    color = WhitePrimary,
    fontFamily = Inter,
    fontSize = 18.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.W500,
)

val InterSecondary @Composable get() = TextStyle(
    color = GrayPrimary,
    fontFamily = Inter,
    fontSize = 20.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.W300,
)

val InterError @Composable get() = TextStyle(
    color = WhitePrimary,
    fontFamily = Inter,
    fontSize = 16.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.W500,
)

val InterHint @Composable get() = TextStyle(
    color = GrayPrimary,
    fontFamily = Inter,
    fontSize = 14.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.W500,
)

val InterTitle @Composable get() = TextStyle(
    color = WhitePrimary,
    fontFamily = Inter,
    fontSize = 18.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.ExtraBold,
)

val InterDescription @Composable get() = TextStyle(
    color = GrayQuaternary,
    fontFamily = Inter,
    fontSize = 16.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.W400,
)

val RobotoPrimary @Composable get() = TextStyle(
    color = WhitePrimary,
    letterSpacing = 0.sp,
    fontFamily = Roboto,
    fontSize = 18.sp,
    fontWeight = FontWeight.W800
)
