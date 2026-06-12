package com.protato.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CelebrationCanvas() {
    val transition = rememberInfiniteTransition(label = "celebration")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti"
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFFC857)
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        val cupY = size.height * 0.58f
        drawRect(
            color = colors[0],
            topLeft = Offset(size.width * 0.33f, cupY),
            size = Size(size.width * 0.12f, size.height * 0.22f)
        )
        drawRect(
            color = colors[2],
            topLeft = Offset(size.width * 0.55f, cupY),
            size = Size(size.width * 0.12f, size.height * 0.22f)
        )
        drawLine(
            color = colors[1],
            start = Offset(size.width * 0.45f, cupY + size.height * 0.05f),
            end = Offset(size.width * 0.56f, cupY),
            strokeWidth = 5.dp.toPx()
        )
        repeat(18) { index ->
            val xSeed = ((index * 37) % 100) / 100f
            val ySeed = ((index * 19) % 100) / 100f
            val x = size.width * xSeed
            val y = size.height * ((ySeed + phase) % 1f) * 0.72f
            drawCircle(
                color = colors[index % colors.size],
                radius = (3 + index % 4).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
