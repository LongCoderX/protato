package com.protato.app.ui.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.util.dateKeyToLocalMillis
import com.protato.app.util.utcDayKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoReminderEditDialog(
    initialReminderAt: Long?,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSelected: (Long) -> Unit
) {
    val initialMillis = initialReminderAt ?: System.currentTimeMillis()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val timeState = rememberTimePickerState(
        initialHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(initialMillis)).toInt(),
        initialMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date(initialMillis)).toInt(),
        is24Hour = true
    )
    var choosingTime by rememberSaveable(initialReminderAt) {
        mutableStateOf(false)
    }

    if (choosingTime) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = state.selectedDateMillis != null,
                    onClick = {
                        val selectedDay = state.selectedDateMillis ?: return@TextButton
                        onSelected(
                            dateKeyToLocalMillis(
                                utcDayKey(selectedDay),
                                timeState.hour,
                                timeState.minute
                            )
                        )
                    }
                ) { Text("设置提醒") }
            },
            dismissButton = {
                TextButton(onClick = { choosingTime = false }) { Text("返回日期") }
            },
            title = { Text("选择时间") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeInput(state = timeState)
                }
            }
        )
    } else {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = state.selectedDateMillis != null,
                    onClick = { choosingTime = true }
                ) { Text("下一步") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = onClear) { Text("清除") }
                    TextButton(onClick = onDismiss) { Text("取消") }
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DatePicker(state = state)
                Text(
                    text = "当前时间 %02d:%02d，点击下一步修改".format(timeState.hour, timeState.minute),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}
