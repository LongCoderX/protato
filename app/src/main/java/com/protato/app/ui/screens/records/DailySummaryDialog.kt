package com.protato.app.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.DailySummary
import com.protato.app.ui.components.MarkdownText

@Composable
fun DailySummaryDialog(
    summary: DailySummary,
    generatedAtLabel: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text(summary.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${summary.pomodoroCount} 个番茄 · ${summary.focusMinutes} 分钟 · $generatedAtLabel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MarkdownText(summary.content)
            }
        }
    )
}
