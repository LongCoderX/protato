package com.protato.app.ui.screens.agent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.protato.app.ai.AiDiagnostics
import com.protato.app.ui.components.ProtatoPanel

@Composable
internal fun AiDiagnosticsCard() {
    var showingLog by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf(AiDiagnostics.readText()) }
    val latestLine = logText.lineSequence().lastOrNull().orEmpty()

    ProtatoPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("网络诊断", style = MaterialTheme.typography.titleMedium)
            Text(
                latestLine.ifBlank { "暂无 AI 请求日志。" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            Text(
                "日志仅保存在应用私有目录，不记录 API Key、提示词或完整回复。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(
                onClick = {
                    logText = AiDiagnostics.readText()
                    showingLog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看本地日志")
            }
        }
    }

    if (showingLog) {
        AlertDialog(
            onDismissRequest = { showingLog = false },
            confirmButton = {
                TextButton(onClick = {
                    logText = AiDiagnostics.readText()
                }) {
                    Text("刷新")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    AiDiagnostics.clear()
                    logText = AiDiagnostics.readText()
                }) {
                    Text("清空")
                }
            },
            title = { Text("AI 请求日志") },
            text = {
                SelectionContainer {
                    Text(
                        logText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        )
    }
}
