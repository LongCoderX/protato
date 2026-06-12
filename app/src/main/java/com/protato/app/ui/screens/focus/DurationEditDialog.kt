package com.protato.app.ui.screens.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DurationEditDialog(
    modeKey: Any,
    modeLabel: String,
    durationRange: IntRange,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var minutesText by rememberSaveable(modeKey, initialMinutes) {
        mutableStateOf(initialMinutes.toString())
    }
    val minutes = minutesText.toIntOrNull()
    val validMinutes = minutes?.coerceIn(durationRange.first, durationRange.last)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = validMinutes != null,
                onClick = { validMinutes?.let(onSave) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("调整${modeLabel}时长") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { value ->
                        minutesText = value.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("分钟") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    "${durationRange.first}-${durationRange.last} 分钟",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
