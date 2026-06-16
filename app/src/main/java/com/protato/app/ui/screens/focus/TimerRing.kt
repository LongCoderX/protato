package com.protato.app.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TimerRing(progress: Float) {
    val color = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.secondary
    val track = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        val innerStroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke
        )
        drawArc(
            color = accent.copy(alpha = 0.85f),
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = innerStroke
        )
    }
}
