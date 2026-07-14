package com.protato.app.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
internal fun CityFocusBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFAEB1CF), Color(0xFF7880A5), Color(0xFF30334C))
            )
        )
        val horizon = size.height * 0.35f
        drawRect(
            color = Color(0xFFE7EAF4).copy(alpha = 0.22f),
            topLeft = Offset(0f, horizon),
            size = Size(size.width, size.height * 0.11f)
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF111827).copy(alpha = 0.38f))
            )
        )
        drawSkyline(
            topRatio = 0.36f,
            alpha = 0.18f,
            buildings = floatArrayOf(
                0.02f, 0.22f, 0.11f, 0.34f, 0.15f, 0.27f, 0.24f, 0.44f,
                0.29f, 0.25f, 0.36f, 0.40f, 0.43f, 0.18f, 0.51f, 0.43f,
                0.56f, 0.28f, 0.64f, 0.47f, 0.70f, 0.21f, 0.78f, 0.42f,
                0.82f, 0.30f, 0.93f, 0.50f
            )
        )
        drawSkyline(
            topRatio = 0.48f,
            alpha = 0.30f,
            buildings = floatArrayOf(
                0.00f, 0.18f, 0.08f, 0.56f, 0.11f, 0.31f, 0.20f, 0.63f,
                0.23f, 0.20f, 0.33f, 0.60f, 0.36f, 0.34f, 0.45f, 0.68f,
                0.49f, 0.16f, 0.57f, 0.65f, 0.61f, 0.29f, 0.72f, 0.72f,
                0.76f, 0.24f, 0.87f, 0.65f, 0.90f, 0.38f, 1.00f, 0.76f
            )
        )
        val bridge = Path().apply {
            moveTo(size.width * 0.58f, size.height * 0.82f)
            lineTo(size.width, size.height * 0.70f)
            lineTo(size.width, size.height * 0.76f)
            lineTo(size.width * 0.58f, size.height * 0.88f)
            close()
        }
        drawPath(bridge, Color(0xFF0F172A).copy(alpha = 0.28f))
        drawRect(
            color = Color(0xFFE0E7FF).copy(alpha = 0.36f),
            topLeft = Offset(0f, size.height * 0.95f),
            size = Size(size.width, 1.4.dp.toPx())
        )
    }
}

private fun DrawScope.drawSkyline(topRatio: Float, alpha: Float, buildings: FloatArray) {
    val color = Color(0xFF111827).copy(alpha = alpha)
    var index = 0
    while (index + 3 < buildings.size) {
        val left = size.width * buildings[index]
        val top = size.height * (topRatio + buildings[index + 1] * 0.30f)
        val right = size.width * buildings[index + 2]
        val bottom = size.height * buildings[index + 3]
        drawRect(color, Offset(left, top), Size(right - left, bottom - top))
        drawWindowGrid(left, top, right, bottom, alpha)
        index += 4
    }
}

private fun DrawScope.drawWindowGrid(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    buildingAlpha: Float
) {
    val windowColor = Color(0xFFDDE7FF).copy(alpha = buildingAlpha * 0.75f)
    val windowWidth = 1.6.dp.toPx()
    val windowHeight = 1.2.dp.toPx()
    val gapX = 5.5.dp.toPx()
    val gapY = 6.dp.toPx()
    var y = top + gapY
    var row = 0
    while (y + windowHeight < bottom - gapY) {
        var x = left + gapX
        var column = 0
        while (x + windowWidth < right - gapX) {
            if ((row + column) % 3 != 0) {
                drawRect(windowColor, Offset(x, y), Size(windowWidth, windowHeight))
            }
            x += gapX
            column += 1
        }
        y += gapY
        row += 1
    }
}
