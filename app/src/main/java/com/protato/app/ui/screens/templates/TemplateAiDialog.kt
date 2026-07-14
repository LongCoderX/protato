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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.protato.app.FieldType
import com.protato.app.RecordTemplate
import com.protato.app.ai.AsyncRequestState
import com.protato.app.ai.TemplateSuggestionController

@Composable
internal fun TemplateAiDialog(
    controller: TemplateSuggestionController,
    generateSuggestion: (String) -> Boolean,
    fieldTypeLabel: (FieldType) -> String,
    onDismiss: () -> Unit,
    onAddTemplate: (RecordTemplate) -> Unit
) {
    var rawInput by rememberSaveable { mutableStateOf("") }
    val requestState by controller.state.collectAsState()
    val outcome = (requestState as? AsyncRequestState.Success)?.value
    val suggestion = outcome?.suggestion
    val loading = requestState is AsyncRequestState.Loading
    val errorText = outcome?.errorText
        ?: (requestState as? AsyncRequestState.Failed)?.error?.message

    AlertDialog(
        onDismissRequest = {
            controller.cancel()
            onDismiss()
        },
        confirmButton = {
            TextButton(
                enabled = suggestion != null,
                onClick = {
                    controller.cancel()
                    suggestion?.let(onAddTemplate)
                }
            ) {
                Text("添加模板")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                controller.cancel()
                onDismiss()
            }) {
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
                        generateSuggestion(rawInput)
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
