package com.protato.app.ui.screens.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.protato.app.FieldType
import com.protato.app.RecordTemplate
import kotlinx.coroutines.launch

@Composable
fun TemplateAiDialog(
    generateSuggestion: suspend (String) -> RecordTemplate,
    fallbackSuggestion: (String) -> RecordTemplate,
    fieldTypeLabel: (FieldType) -> String,
    onDismiss: () -> Unit,
    onAddTemplate: (RecordTemplate) -> Unit
) {
    val scope = rememberCoroutineScope()
    var rawInput by rememberSaveable { mutableStateOf("") }
    var suggestion by remember { mutableStateOf<RecordTemplate?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = suggestion != null,
                onClick = { suggestion?.let(onAddTemplate) }
            ) {
                Text("添加模板")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("AI 生成模板") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it.take(1200) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("描述你想复盘什么") },
                    minLines = 4
                )
                OutlinedButton(
                    enabled = rawInput.isNotBlank() && !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            errorText = null
                            suggestion = runCatching {
                                generateSuggestion(rawInput)
                            }.getOrElse { error ->
                                errorText = error.message ?: "生成失败"
                                fallbackSuggestion(rawInput)
                            }
                            loading = false
                        }
                    }
                ) {
                    Text(if (loading) "生成中" else "生成模板")
                }
                errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                suggestion?.let { template ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(template.name, fontWeight = FontWeight.Bold)
                            template.fields.forEachIndexed { index, field ->
                                Text(
                                    "${index + 1}. ${field.title} · ${fieldTypeLabel(field.type)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
