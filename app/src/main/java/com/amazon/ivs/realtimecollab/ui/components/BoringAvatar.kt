package com.amazon.ivs.realtimecollab.ui.components

import android.graphics.Matrix
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import com.amazon.ivs.realtimecollab.ui.theme.BlackSecondary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

private const val SIZE = 36

private fun hashCode(name: String): Int {
    var hash = 0
    for (c in name) {
        val character = c.hashCode()
        hash = ((hash shl 5) - hash) + character
        hash = hash and 0xFFFFFFFF.toInt()
    }
    return abs(hash)
}

private fun getDigit(number: Int, ntn: Int): Int {
    return floor((number / 10.0.pow(ntn.toDouble())) % 10).toInt()
}

private fun getUnit(number: Int, range: Int, index: Int? = null): Int {
    val value = number % range
    val modValue = if (value < 0) value + range else value
    return if (index != null && (getDigit(number, index) % 2 == 0)) {
        -modValue
    } else {
        modValue
    }
}

private fun getBoolean(number: Int, ntn: Int): Boolean {
    return getDigit(number, ntn) % 2 == 0
}

private fun getRandomColor(number: Int, colors: List<Color>, range: Int): Color {
    if (range == 0) return Color.Gray
    val idx = number % range
    return colors[idx]
}

private fun getContrast(color: Color): Color {
    val yiq = ((color.red * 299) + (color.green * 587) + (color.blue * 114)) / 1000f
    return if (yiq >= 0.5f) Color.Black else Color.White
}

private fun Color.toHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

private data class AvatarData(
    val wrapperColor: Color,
    val faceColor: Color,
    val backgroundColor: Color,
    val wrapperTranslateX: Float,
    val wrapperTranslateY: Float,
    val wrapperRotate: Float,
    val wrapperScale: Float,
    val isMouthOpen: Boolean,
    val isCircle: Boolean,
    val eyeSpread: Float,
    val mouthSpread: Float,
    val faceRotate: Float,
    val faceTranslateX: Float,
    val faceTranslateY: Float,
) {
    override fun toString(): String {
        return """
        {
          "wrapperColor": "${wrapperColor.toHex()}",
          "faceColor": "${faceColor.toHex()}",
          "backgroundColor": "${backgroundColor.toHex()}",
          "wrapperTranslateX": $wrapperTranslateX,
          "wrapperTranslateY": $wrapperTranslateY,
          "wrapperRotate": $wrapperRotate,
          "wrapperScale": $wrapperScale,
          "isMouthOpen": $isMouthOpen,
          "isCircle": $isCircle,
          "eyeSpread": $eyeSpread,
          "mouthSpread": $mouthSpread,
          "faceRotate": $faceRotate,
          "faceTranslateX": $faceTranslateX,
          "faceTranslateY": $faceTranslateY
        }
        """.trimIndent()
    }
}

private fun generateData(
    name: String?,
    colors: List<Color>,
): AvatarData {
    val numFromName = hashCode(name = name ?: "")
    val range = colors.size
    val wrapperColor = getRandomColor(number = numFromName, colors = colors, range = range)
    val preTranslateX = getUnit(number = numFromName, range = 10, index = 1).toFloat()
    val preTranslateY = getUnit(number = numFromName, range = 10, index = 2).toFloat()
    val wrapperTranslateX = if (preTranslateX < 5) preTranslateX + SIZE / 9f else preTranslateX
    val wrapperTranslateY = if (preTranslateY < 5) preTranslateY + SIZE / 9f else preTranslateY

    return AvatarData(
        wrapperColor = wrapperColor,
        faceColor = getContrast(color = wrapperColor),
        backgroundColor = getRandomColor(number = numFromName + 13, colors = colors, range = range),
        wrapperTranslateX = wrapperTranslateX,
        wrapperTranslateY = wrapperTranslateY,
        wrapperRotate = getUnit(number = numFromName, range = 360).toFloat(),
        wrapperScale = 1f + (getUnit(number = numFromName, range = SIZE / 12) / 10f),
        isMouthOpen = getBoolean(number = numFromName, ntn = 2),
        isCircle = getBoolean(number = numFromName, ntn = 1),
        eyeSpread = getUnit(number = numFromName, range = 5).toFloat(),
        mouthSpread = getUnit(number = numFromName, range = 3).toFloat(),
        faceRotate = getUnit(number = numFromName, range = 10, index = 3).toFloat(),
        faceTranslateX = if (wrapperTranslateX > (SIZE / 6f)) {
            wrapperTranslateX / 2f
        } else {
            getUnit(number = numFromName, range = 8, index = 1).toFloat()
        },
        faceTranslateY = if (wrapperTranslateY > (SIZE / 6f)) {
            wrapperTranslateY / 2f
        } else {
            getUnit(number = numFromName, range = 7, index = 2).toFloat()
        },
    )
}

@Composable
private fun BoringAvatarSVG(
    data: AvatarData,
    avatarSizePx: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val maskID = "mask-${data.hashCode()}"
    val stringBuilder = StringBuilder()
    stringBuilder.append("<svg viewBox=\"0 0 $SIZE $SIZE\" fill=\"none\" role=\"img\" " +
            "xmlns=\"http://www.w3.org/2000/svg\" width=\"$avatarSizePx\" height=\"$avatarSizePx\">")
    stringBuilder.append("<mask id=\"$maskID\" maskUnits=\"userSpaceOnUse\" x=\"0\" y=\"0\" " +
            "width=\"$SIZE\" height=\"$SIZE\">")
    stringBuilder.append("<rect width=\"$SIZE\" height=\"$SIZE\" rx=\"${SIZE * 2}\" fill=\"#FFFFFF\"/>")
    stringBuilder.append("</mask>")
    stringBuilder.append("<g mask=\"url(#$maskID)\">")
    stringBuilder.append("<rect width=\"$SIZE\" height=\"$SIZE\" fill=\"${data.backgroundColor.toHex()}\"/>")
    stringBuilder.append("<rect x=\"0\" y=\"0\" width=\"$SIZE\" height=\"$SIZE\" " +
            "transform=\"translate(${data.wrapperTranslateX} ${data.wrapperTranslateY}) " +
            "rotate(${data.wrapperRotate} ${SIZE / 2} ${SIZE / 2}) " +
            "scale(${data.wrapperScale})\" " +
            "fill=\"${data.wrapperColor.toHex()}\" " +
            "rx=\"${if (data.isCircle) SIZE else SIZE / 6}\"/>")
    stringBuilder.append("<g transform=\"translate(${data.faceTranslateX} ${data.faceTranslateY}) " +
            "rotate(${data.faceRotate} ${SIZE / 2} ${SIZE / 2})\">")
    if (data.isMouthOpen) {
        stringBuilder.append("<path d=\"M15 ${19 + data.mouthSpread}c2 1 4 1 6 0\" " +
                "stroke=\"${data.faceColor.toHex()}\" fill=\"none\" stroke-linecap=\"round\"/>")
    } else {
        stringBuilder.append("<path d=\"M13,${19 + data.mouthSpread} a1,0.75 0 0,0 10,0\" " +
                "fill=\"${data.faceColor.toHex()}\"/>")
    }
    stringBuilder.append("<rect x=\"${14 - data.eyeSpread}\" y=\"14\" width=\"1.5\" height=\"2\" rx=\"1\" " +
            "stroke=\"none\" fill=\"${data.faceColor.toHex()}\"/>")
    stringBuilder.append("<rect x=\"${20 + data.eyeSpread}\" y=\"14\" width=\"1.5\" height=\"2\" rx=\"1\" " +
            "stroke=\"none\" fill=\"${data.faceColor.toHex()}\"/>")
    stringBuilder.append("</g></g></svg>")

    val svg = stringBuilder.toString()
    Timber.d("SVG: $svg")

    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(context)
            .data(data = svg.toByteArray())
            .decoderFactory(factory = SvgDecoder.Factory())
            .build(),
        contentDescription = null,
    )
}

@Composable
fun BoringAvatar(
    name: String?,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF92A1C6),
        Color(0xFF146A7C),
        Color(0xFFF0AB3D),
        Color(0xFFC271B4),
        Color(0xFFC20D90)
    ),
    avatarSize: Dp = 80.dp,
    showBorder: Boolean = false,
) {
    val avatarSizePx = LocalDensity.current.run { avatarSize.toPx() }
    val isPreview = LocalInspectionMode.current
    val data = generateData(
        name = name,
        colors = colors,
    )
    val avatarModifier = modifier
        .size(avatarSize)
        .thenOptional(enabled = showBorder) {
            border(
                width = 2.dp,
                color = BlackSecondary,
                shape = CircleShape,
            )
            .padding(2.dp)
        }
        .clip(CircleShape)

    if (isPreview) {
        BoringAvatarCanvas(
            avatarSizePx = avatarSizePx,
            data = data,
            modifier = avatarModifier,
        )
    } else {
        BoringAvatarSVG(
            data = data,
            avatarSizePx = avatarSizePx,
            modifier = avatarModifier,
        )
    }
}

@Composable
private fun BoringAvatarCanvas(
    avatarSizePx: Float,
    data: AvatarData,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
    ) {
        val scaleFactor = avatarSizePx / SIZE
        val sizePx = SIZE * scaleFactor
        val pivot = Offset(
            x = sizePx / 2f,
            y = sizePx / 2f,
        )
        val radius = if (data.isCircle) sizePx else sizePx / 6f

        fun String.toPath(): Path {
            val scaleMatrix = Matrix()
            val rectF = RectF()
            val path = PathParser().parsePathString(this).toPath()
            val androidPath = path.asAndroidPath()
            scaleMatrix.setScale(scaleFactor, scaleFactor, rectF.centerX(), rectF.centerY())
            androidPath.computeBounds(rectF, true)
            androidPath.transform(scaleMatrix)
            return androidPath.asComposePath()
        }

        drawRect(
            color = data.backgroundColor,
            size = Size(width = sizePx, height = sizePx),
        )

        translate(
            left = data.wrapperTranslateX * scaleFactor,
            top = data.wrapperTranslateY * scaleFactor,
        ) {
            rotate(
                degrees = data.wrapperRotate,
                pivot = pivot,
            ) {
                scale(
                    scale = data.wrapperScale,
                    pivot = pivot,
                ) {
                    drawRoundRect(
                        color = data.wrapperColor,
                        size = Size(width = sizePx, height = sizePx),
                        topLeft = Offset(x = scaleFactor, y = scaleFactor),
                        cornerRadius = CornerRadius(
                            x = radius,
                            y = radius,
                        )
                    )
                }
            }
        }

        translate(
            left = data.faceTranslateX * scaleFactor,
            top = data.faceTranslateY * scaleFactor
        ) {
            rotate(
                degrees = data.faceRotate,
                pivot = pivot,
            ) {
                if (data.isMouthOpen) {
                    val path = "M15 ${19 + data.mouthSpread} c2 1 4 1 6 0"
                    drawPath(
                        path = path.toPath(),
                        color = data.faceColor,
                        style = Stroke(width = scaleFactor, cap = StrokeCap.Round)
                    )
                } else {
                    val path = "M13, ${19 + data.mouthSpread} a1,0.75 0 0,0 10,0"
                    drawPath(
                        path = path.toPath(),
                        color = data.faceColor
                    )
                }

                drawRoundRect(
                    color = data.faceColor,
                    topLeft = Offset(
                        x = 14f * scaleFactor - data.eyeSpread * scaleFactor,
                        y = 14f * scaleFactor,
                    ),
                    size = Size(
                        width = 1.5f * scaleFactor,
                        height = 2f * scaleFactor,
                    ),
                    cornerRadius = CornerRadius(x = scaleFactor, y = scaleFactor),
                )
                drawRoundRect(
                    color = data.faceColor,
                    topLeft = Offset(
                        x = 20f * scaleFactor + data.eyeSpread * scaleFactor,
                        y = 14f * scaleFactor,
                    ),
                    size = Size(
                        width = 1.5f * scaleFactor,
                        height = 2f * scaleFactor,
                    ),
                    cornerRadius = CornerRadius(x = scaleFactor, y = scaleFactor),
                )
            }
        }
    }
}

@Preview(apiLevel = 35)
@Composable
private fun BoringAvatarsPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = WhitePrimary)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BoringAvatar(
                    name = "username",
                )
                BoringAvatar(
                    name = "Eddy",
                )
                BoringAvatar(
                    name = "Uldis",
                )
                BoringAvatar(
                    name = "Max",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BoringAvatar(
                    name = "Maria Mitchell",
                )
                BoringAvatar(
                    name = "John Doe",
                )
                BoringAvatar(
                    name = "Jane Smith",
                )
                BoringAvatar(
                    name = "Alice Johnson",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BoringAvatar(
                    name = "Bob Wilson",
                )
                BoringAvatar(
                    name = "Carol Brown",
                )
                BoringAvatar(
                    name = "David Lee",
                )
                BoringAvatar(
                    name = "Emma Davis",
                )
            }
        }
    }
}
