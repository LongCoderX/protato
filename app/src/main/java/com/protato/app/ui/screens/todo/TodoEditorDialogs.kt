package com.protato.app.ui.screens.todo

import com.protato.app.domain.todo.summarizeTodoWithSubtasks

import com.protato.app.*

import com.protato.app.ai.*
import com.protato.app.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
internal fun TodoTagPickerButton(
    selectedTags: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.Label,
            contentDescription = "编辑标签",
            tint = selectedTags.firstOrNull()?.let(::todoTagColor) ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun TodoDatePickerButton(
    dueDate: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            Icons.Outlined.DateRange,
            contentDescription = "选择日期",
            tint = if (dueDate == dayKey(System.currentTimeMillis())) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
internal fun TodoTagEditDialog(
    availableTags: List<String>,
    selectedTags: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var draftTags by remember(selectedTags) { mutableStateOf(selectedTags) }
    var newTagText by rememberSaveable { mutableStateOf("") }
    val normalizedNewTag = parseTodoTags(newTagText).firstOrNull()
    val tags = remember(availableTags, draftTags) {
        (availableTags + draftTags)
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sorted()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(draftTags) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("标签") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTagText,
                        onValueChange = { newTagText = it.take(24) },
                        modifier = Modifier.weight(1f),
                        label = { Text("新标签") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null) }
                    )
                    FilledIconButton(
                        enabled = normalizedNewTag != null,
                        onClick = {
                            normalizedNewTag?.let { tag ->
                                draftTags = (draftTags + tag)
                                    .distinctBy { it.lowercase(Locale.getDefault()) }
                                newTagText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加标签")
                    }
                }
                if (tags.isEmpty()) {
                    Text("还没有标签。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    tags.forEach { tag ->
                        val selected = draftTags.any { it.equals(tag, ignoreCase = true) }
                        FilterChip(
                            selected = selected,
                            onClick = {
                                draftTags = if (selected) {
                                    draftTags.filterNot { it.equals(tag, ignoreCase = true) }
                                } else {
                                    (draftTags + tag).distinctBy { it.lowercase(Locale.getDefault()) }
                                }
                            },
                            label = { Text("#$tag") },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    color = todoTagColor(tag),
                                    shape = RoundedCornerShape(8.dp)
                                ) {}
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoDatePickerDialog(
    initialDate: String?,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSelected: (String) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.let(::dateKeyToUtcMillis)
            ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onSelected(utcDayKey(millis))
                    }
                }
            ) {
                Text("选择")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("清除")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
internal fun TodoEditorDialog(
    todo: TodoItem,
    availableTags: List<String>,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit
) {
    var title by rememberSaveable(todo.id) { mutableStateOf(todo.title) }
    var draftTags by rememberSaveable(todo.id) { mutableStateOf(todo.tags) }
    var draftDueDate by rememberSaveable(todo.id) { mutableStateOf(todo.dueDate) }
    var draftReminderAt by rememberSaveable(todo.id) { mutableStateOf(todo.reminderAt) }
    var draftComplexity by rememberSaveable(todo.id) { mutableStateOf(todo.complexity) }
    var draftSubtasks by remember(todo.id) { mutableStateOf(todo.subtasks) }
    var newSubtaskTitle by rememberSaveable(todo.id) { mutableStateOf("") }
    var plannedPomodorosText by rememberSaveable(todo.id) {
        mutableStateOf(todo.plannedPomodoros.coerceIn(1, 24).toString())
    }
    var editingTags by rememberSaveable(todo.id) { mutableStateOf(false) }
    var editingDate by rememberSaveable(todo.id) { mutableStateOf(false) }
    var editingReminder by rememberSaveable(todo.id) { mutableStateOf(false) }
    val plannedPomodoros = plannedPomodorosText.toIntOrNull()?.coerceIn(1, 24) ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val draftTodo = todo.copy(
                        title = title.trim(),
                        tags = draftTags,
                        dueDate = draftDueDate,
                        plannedPomodoros = plannedPomodoros,
                        reminderAt = draftReminderAt,
                        complexity = draftComplexity,
                        subtasks = draftSubtasks.filter { it.title.isNotBlank() }
                    )
                    onSave(
                        draftTodo.copy(summary = summarizeTodoWithSubtasks(draftTodo))
                    )
                }
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("保存修改")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text(if (todo.title.isBlank()) "新建待办" else "编辑待办") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("待办内容") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CompactTodoMetaAction(
                            icon = Icons.Outlined.DateRange,
                            value = draftDueDate?.let(::formatDayLabel) ?: "未安排",
                            onClick = { editingDate = true },
                            contentDescription = "设置日期",
                            modifier = Modifier.fillMaxWidth()
                        )
                        CompactTodoMetaAction(
                            icon = Icons.AutoMirrored.Outlined.Label,
                            value = if (draftTags.isEmpty()) "未添加" else draftTags.joinToString(" ") { "#$it" },
                            onClick = { editingTags = true },
                            contentDescription = "设置标签",
                            modifier = Modifier.fillMaxWidth()
                        )
                        CompactTodoMetaAction(
                            icon = Icons.Outlined.Notifications,
                            value = draftReminderAt?.let(::formatTime) ?: "不提醒",
                            onClick = { editingReminder = true },
                            contentDescription = "设置提醒",
                            modifier = Modifier.fillMaxWidth()
                        )
                        CompactPomodoroStepper(
                            value = plannedPomodoros,
                            onDecrease = {
                                plannedPomodorosText = (plannedPomodoros - 1).coerceAtLeast(1).toString()
                            },
                            onIncrease = {
                                plannedPomodorosText = (plannedPomodoros + 1).coerceAtMost(24).toString()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CompactComplexitySelector(
                        selected = draftComplexity,
                        onSelected = { draftComplexity = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("子任务", fontWeight = FontWeight.SemiBold)
                        draftSubtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = subtask.completed,
                                    onCheckedChange = {
                                        draftSubtasks = draftSubtasks.map {
                                            if (it.id == subtask.id) it.copy(completed = !it.completed) else it
                                        }
                                    }
                                )
                                Text(
                                    subtask.title,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None
                                )
                                IconButton(
                                    onClick = {
                                        draftSubtasks = draftSubtasks.filterNot { it.id == subtask.id }
                                    }
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "删除子任务")
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newSubtaskTitle,
                                onValueChange = { newSubtaskTitle = it.take(80) },
                                modifier = Modifier.weight(1f),
                                label = { Text("新的子任务") },
                                singleLine = true
                            )
                            FilledIconButton(
                                enabled = newSubtaskTitle.isNotBlank(),
                                onClick = {
                                    draftSubtasks = draftSubtasks + TodoSubtask(
                                        id = newId(),
                                        title = newSubtaskTitle.trim()
                                    )
                                    newSubtaskTitle = ""
                                }
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "添加子任务")
                            }
                        }
                    }
                }
            }
        }
    )

    if (editingDate) {
        TodoDatePickerDialog(
            initialDate = draftDueDate,
            onDismiss = { editingDate = false },
            onClear = {
                draftDueDate = null
                editingDate = false
            },
            onSelected = { dueDate ->
                draftDueDate = dueDate
                editingDate = false
            }
        )
    }

    if (editingTags) {
        TodoTagEditDialog(
            availableTags = availableTags,
            selectedTags = draftTags,
            onDismiss = { editingTags = false },
            onSave = { tags ->
                draftTags = tags
                editingTags = false
            }
        )
    }

    if (editingReminder) {
        TodoReminderEditDialog(
            initialReminderAt = draftReminderAt,
            onDismiss = { editingReminder = false },
            onClear = {
                draftReminderAt = null
                editingReminder = false
            },
            onSelected = { reminderAt ->
                draftReminderAt = reminderAt
                editingReminder = false
            }
        )
    }
}

@Composable
private fun CompactTodoMetaAction(
    icon: ImageVector,
    value: String,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            value,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CompactPomodoroStepper(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_tomato),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                "番茄数量",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                "$value 个番茄",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        IconButton(
            enabled = value > 1,
            onClick = onDecrease,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = "减少预计番茄")
        }
        IconButton(
            enabled = value < 24,
            onClick = onIncrease,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "增加预计番茄")
        }
    }
}

@Composable
private fun CompactComplexitySelector(
    selected: TodoComplexity,
    onSelected: (TodoComplexity) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Flag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            "任务类型",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
        CompactComplexityOption(
            label = "简单",
            selected = selected == TodoComplexity.Simple,
            onClick = { onSelected(TodoComplexity.Simple) }
        )
        CompactComplexityOption(
            label = "复杂",
            selected = selected == TodoComplexity.Complex,
            onClick = { onSelected(TodoComplexity.Complex) }
        )
    }
}

@Composable
private fun CompactComplexityOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
