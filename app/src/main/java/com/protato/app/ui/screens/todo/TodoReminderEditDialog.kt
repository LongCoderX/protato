package com.protato.app.ui.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var selectedHour by rememberSaveable(initialReminderAt) {
        mutableIntStateOf(SimpleDateFormat("HH", Locale.getDefault()).format(Date(initialMillis)).toInt())
    }
    var selectedMinute by rememberSaveable(initialReminderAt) {
        mutableIntStateOf(SimpleDateFormat("mm", Locale.getDefault()).format(Date(initialMillis)).toInt())
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    val selectedDay = state.selectedDateMillis ?: return@TextButton
                    onSelected(
                        dateKeyToLocalMillis(
                            utcDayKey(selectedDay),
                            selectedHour,
                            selectedMinute
                        )
                    )
                }
            ) { Text("设置提醒") }
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
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReminderTimeDropdown(
                    value = selectedHour,
                    options = (0..23).toList(),
                    label = "时",
                    onSelected = { selectedHour = it },
                    modifier = Modifier.weight(1f)
                )
                ReminderTimeDropdown(
                    value = selectedMinute,
                    options = (0..59).toList(),
                    label = "分",
                    onSelected = { selectedMinute = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ReminderTimeDropdown(
    value: Int,
    options: List<Int>,
    label: String,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "%02d $label".format(value),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold
            )
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("%02d $label".format(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
