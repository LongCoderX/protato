package com.protato.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun PaperThemeBackground(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFEF8), Color(0xFFF0ECE1))
                )
            )
            val gridColor = Color(0xFFB9B2A5).copy(alpha = 0.20f)
            val grid = 18.dp.toPx()
            var x = grid
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.8.dp.toPx())
                x += grid
            }
            var y = grid
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.8.dp.toPx())
                y += grid
            }
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.52f), Color.Transparent),
                    center = Offset(size.width * 0.32f, size.height * 0.12f),
                    radius = size.minDimension * 0.72f
                )
            )
            drawRect(
                color = Color(0xFF8EDFC8).copy(alpha = 0.16f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        content()
    }
}
