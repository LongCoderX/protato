package com.protato.app.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.protato.app.FieldType
import com.protato.app.RecordTemplate

@Composable
fun RecordAnswerFields(
    template: RecordTemplate,
    answers: SnapshotStateMap<String, String>,
    onPolish: (String, String) -> Unit,
    polishing: Boolean,
    showPolishButton: Boolean
) {
    template.fields.forEach { field ->
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (field.required) "${field.title} *" else field.title,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (showPolishButton && field.id == "field-summary-draft") {
                    TextButton(
                        enabled = answers[field.id].orEmpty().isNotBlank() && !polishing,
                        onClick = { onPolish(field.id, answers[field.id].orEmpty()) }
                    ) {
                        Text(if (polishing) "润色中" else "AI 润色")
                    }
                }
            }
            when (field.type) {
                FieldType.SingleChoice -> field.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = answers[field.id] == option,
                            onClick = { answers[field.id] = option }
                        )
                        Text(option)
                    }
                }

                FieldType.ShortAnswer -> OutlinedTextField(
                    value = answers[field.id].orEmpty(),
                    onValueChange = { answers[field.id] = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                FieldType.Attachment -> OutlinedTextField(
                    value = answers[field.id].orEmpty(),
                    onValueChange = { answers[field.id] = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("粘贴语音/图片链接或文件路径") },
                    singleLine = true
                )
            }
        }
    }
}
