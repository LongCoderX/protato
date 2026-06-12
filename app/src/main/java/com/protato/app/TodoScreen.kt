package com.protato.app

import com.protato.app.ai.*
import com.protato.app.util.*
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.protato.app.ai.LlmChatMessage
import com.protato.app.ai.fetchLlmModels
import com.protato.app.ai.requestLlmChat
import com.protato.app.ui.MainTab
import com.protato.app.ui.ProtatoBottomBar
import com.protato.app.ui.ProtatoTheme
import com.protato.app.ui.SecondaryDestination
import com.protato.app.ui.components.CelebrationCanvas
import com.protato.app.ui.components.ChatBubble
import com.protato.app.ui.components.EmptyState
import com.protato.app.ui.components.Header
import com.protato.app.ui.components.SecondaryHeader
import com.protato.app.ui.components.SettingActionRow
import com.protato.app.ui.components.TodoDateChip
import com.protato.app.ui.components.TodoPomodoroChip
import com.protato.app.ui.components.TodoReminderChip
import com.protato.app.ui.components.TodoTagChip
import com.protato.app.ui.screens.more.MoreScreen
import com.protato.app.ui.screens.records.DailySummaryDialog
import com.protato.app.ui.screens.records.DailySummaryCard
import com.protato.app.ui.screens.records.DaySwitcher
import com.protato.app.ui.screens.records.RecordAnswerFields
import com.protato.app.ui.screens.records.RecordDayOption
import com.protato.app.ui.screens.records.RecordTodoSelector
import com.protato.app.ui.screens.focus.DurationEditDialog
import com.protato.app.ui.screens.focus.TimerRing
import com.protato.app.ui.screens.records.RecordTodoChip
import com.protato.app.ui.screens.records.TimelineRecordItem
import com.protato.app.ui.screens.settings.FontScaleSettingsCard
import com.protato.app.ui.screens.settings.LlmProviderListCard
import com.protato.app.ui.screens.settings.NicknameSettingsCard
import com.protato.app.ui.screens.settings.ProjectVersionCard
import com.protato.app.ui.screens.settings.RecordPolishProviderCard
import com.protato.app.ui.screens.settings.WebDevSyncSettingsCard
import com.protato.app.ui.screens.templates.TemplateCard
import com.protato.app.ui.screens.templates.TemplateAiDialog
import com.protato.app.ui.screens.templates.TemplateEditorDialog
import com.protato.app.ui.screens.todo.TodoChoiceLabel
import com.protato.app.ui.screens.todo.TodoDraftMetadataRow
import com.protato.app.ui.screens.todo.TodoFilterControls
import com.protato.app.ui.screens.todo.TodoRow
import com.protato.app.ui.screens.todo.TodoSectionHeader
import com.protato.app.ui.screens.todo.TodoSelectionBar
import com.protato.app.ui.screens.todo.TodoTagFilterDialog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
internal fun TodoScreen(
    todos: List<TodoItem>,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onAdd: (TodoItem) -> Unit,
    onToggle: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onUpdate: (TodoItem) -> Unit,
    onDeleteMany: (Set<String>) -> Unit,
    onAddMany: (List<TodoItem>) -> Unit
) {
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var showingAiTodo by remember { mutableStateOf(false) }
    var showingDatePicker by rememberSaveable { mutableStateOf(false) }
    var showingTagFilter by rememberSaveable { mutableStateOf(false) }
    var selectedTodoIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var showIncompleteTodos by rememberSaveable { mutableStateOf(true) }
    var showCompletedTodos by rememberSaveable { mutableStateOf(false) }
    val selecting = selectedTodoIds.isNotEmpty()
    val todayKey = dayKey(System.currentTimeMillis())
    var selectedTodoDate by rememberSaveable { mutableStateOf<String?>(todayKey) }
    val availableTags = remember(todos) {
        todos.flatMap { it.tags }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sorted()
    }
    val availableDateKeys = remember(todos, todayKey, selectedTodoDate) {
        (todos.mapNotNull { it.dueDate } + todayKey + listOfNotNull(selectedTodoDate))
            .distinct()
            .sortedDescending()
    }
    val datedTodos = remember(todos, selectedTodoDate) {
        todos.filter { it.dueDate == selectedTodoDate }
    }
    val visibleTodos = remember(datedTodos, selectedTag) {
        selectedTag?.let { tag ->
            datedTodos.filter { todo -> todo.tags.any { it.equals(tag, ignoreCase = true) } }
        } ?: datedTodos
    }
    val visibleIncompleteTodos = remember(visibleTodos) { visibleTodos.filterNot { it.completed } }
    val visibleCompletedTodos = remember(visibleTodos) { visibleTodos.filter { it.completed } }
    val dateLabel = selectedTodoDate?.let(::formatDayLabel) ?: "无日期"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Header(title = "待办", subtitle = "给每一轮番茄一个明确的对象")
            if (selecting) {
                TodoSelectionBar(
                    selectedCount = selectedTodoIds.size,
                    onCancel = { selectedTodoIds = emptySet() },
                    onDelete = {
                        onDeleteMany(selectedTodoIds)
                        selectedTodoIds = emptySet()
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            editingTodo = TodoItem(
                                id = newId(),
                                title = "",
                                dueDate = selectedTodoDate
                            )
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "新建待办")
                    }
                    FilledIconButton(onClick = { showingAiTodo = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_robot_svg),
                            contentDescription = "AI 待办助手"
                        )
                    }
                    Text(
                        "${dateLabel} ${visibleIncompleteTodos.size}/${datedTodos.size}",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            TodoFilterControls(
                dateKeys = availableDateKeys,
                selectedDate = selectedTodoDate,
                selectedTag = selectedTag,
                tags = availableTags,
                undatedCount = todos.count { it.dueDate == null },
                onDateSelected = { selectedTodoDate = it },
                onOpenDatePicker = { showingDatePicker = true },
                onOpenTagFilter = { showingTagFilter = true }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (visibleTodos.isEmpty()) {
                item {
                    EmptyState(
                        if (selectedTag == null) {
                            "${dateLabel}还没有待办，点 + 添加一个明确的小目标。"
                        } else {
                            "${dateLabel}这个标签下还没有待办。"
                        }
                    )
                }
            }
            if (visibleIncompleteTodos.isNotEmpty()) {
                item {
                    TodoSectionHeader(
                        title = "未完成",
                        count = visibleIncompleteTodos.size,
                        expanded = showIncompleteTodos,
                        onToggle = { showIncompleteTodos = !showIncompleteTodos }
                    )
                }
            }
            if (showIncompleteTodos) {
                items(visibleIncompleteTodos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        selected = todo.id in selectedTodoIds,
                        selecting = selecting,
                        onToggle = { onToggle(todo) },
                        onEdit = { editingTodo = todo },
                        onDelete = { onDelete(todo) },
                        onToggleSelected = {
                            selectedTodoIds = selectedTodoIds.toggle(todo.id)
                        },
                        onLongPress = {
                            selectedTodoIds = selectedTodoIds + todo.id
                        }
                    )
                }
            }
            if (visibleCompletedTodos.isNotEmpty()) {
                item {
                    TodoSectionHeader(
                        title = "已完成",
                        count = visibleCompletedTodos.size,
                        expanded = showCompletedTodos,
                        onToggle = { showCompletedTodos = !showCompletedTodos }
                    )
                }
                if (showCompletedTodos) {
                    items(visibleCompletedTodos, key = { it.id }) { todo ->
                        TodoRow(
                            todo = todo,
                            selected = todo.id in selectedTodoIds,
                            selecting = selecting,
                            onToggle = { onToggle(todo) },
                            onEdit = { editingTodo = todo },
                            onDelete = { onDelete(todo) },
                            onToggleSelected = {
                                selectedTodoIds = selectedTodoIds.toggle(todo.id)
                            },
                            onLongPress = {
                                selectedTodoIds = selectedTodoIds + todo.id
                            }
                        )
                    }
                }
            }
        }
    }

    editingTodo?.let { todo ->
        TodoEditorDialog(
            todo = todo,
            availableTags = availableTags,
            onDismiss = { editingTodo = null },
            onSave = { updatedTodo ->
                if (todos.any { it.id == updatedTodo.id }) {
                    onUpdate(updatedTodo)
                } else {
                    onAdd(updatedTodo)
                }
                editingTodo = null
            }
        )
    }

    if (showingAiTodo) {
        TodoAiDialog(
            providers = providers,
            agents = agents,
            nickname = nickname,
            onDismiss = { showingAiTodo = false },
            onAddTodos = { draftTodos ->
                onAddMany(draftTodos.map { it.toTodoItem() })
                showingAiTodo = false
            }
        )
    }

    if (showingDatePicker) {
        TodoDatePickerDialog(
            initialDate = selectedTodoDate,
            onDismiss = { showingDatePicker = false },
            onClear = {
                selectedTodoDate = null
                showingDatePicker = false
            },
            onSelected = { dueDate ->
                selectedTodoDate = dueDate
                showingDatePicker = false
            }
        )
    }

    if (showingTagFilter) {
        TodoTagFilterDialog(
            tags = availableTags,
            selectedTag = selectedTag,
            onDismiss = { showingTagFilter = false },
            onSelected = { tag ->
                selectedTag = tag
                showingTagFilter = false
            }
        )
    }
}

@Composable
internal fun TodoAiDialog(
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onDismiss: () -> Unit,
    onAddTodos: (List<TodoDraft>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var rawInput by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<TodoDraft>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val agent = selectTodoAgent(agents)
    val provider = agent?.providerId?.let { id -> providers.firstOrNull { it.id == id } }
        ?: providers.firstOrNull { it.isConfiguredForChat() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = suggestions.isNotEmpty(),
                onClick = { onAddTodos(suggestions) }
            ) {
                Text("添加待办")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("AI 待办助手") },
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
                    label = { Text("今天要做的事") },
                    minLines = 4
                )
                OutlinedButton(
                    enabled = rawInput.isNotBlank() && !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            errorText = null
                            suggestions = runCatching {
                                if (agent != null && provider != null && provider.isConfiguredForChat()) {
                                    generateTodoSuggestions(rawInput, agent, provider, nickname)
                                } else {
                                    parseTodoSuggestions(rawInput)
                                }
                            }.getOrElse { error ->
                                errorText = error.message ?: "生成失败"
                                parseTodoSuggestions(rawInput)
                            }
                            loading = false
                        }
                    }
                ) {
                    Text(if (loading) "整理中" else "整理待办")
                }
                errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                suggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                suggestion.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            TodoDraftMetadataRow(
                                plannedPomodoros = suggestion.plannedPomodoros,
                                dueDate = suggestion.dueDate,
                                reminderAt = suggestion.reminderAt,
                                tags = suggestion.tags,
                                dayLabel = ::formatDayLabel,
                                timeLabel = ::formatTime,
                                tagColor = ::todoTagColor
                            )
                        }
                    }
                }
            }
        }
    )
}

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
                    onSave(
                        todo.copy(
                            title = title.trim(),
                            tags = draftTags,
                            dueDate = draftDueDate,
                            plannedPomodoros = plannedPomodoros,
                            reminderAt = draftReminderAt
                        )
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
                    SettingActionRow(
                        icon = Icons.Outlined.DateRange,
                        title = "日期",
                        value = draftDueDate?.let(::formatDayLabel) ?: "未安排日期",
                        onClick = { editingDate = true }
                    )
                    SettingActionRow(
                        icon = Icons.AutoMirrored.Outlined.Label,
                        title = "标签",
                        value = if (draftTags.isEmpty()) "未添加标签" else draftTags.joinToString(" ") { "#$it" },
                        onClick = { editingTags = true }
                    )
                    SettingActionRow(
                        icon = Icons.Outlined.DateRange,
                        title = "提醒",
                        value = draftReminderAt?.let(::formatTime) ?: "不提醒",
                        onClick = { editingReminder = true }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tomato),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("预计番茄", fontWeight = FontWeight.SemiBold)
                            Text(
                                "用于估算这条待办需要几轮专注",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        OutlinedTextField(
                            value = plannedPomodorosText,
                            onValueChange = { value ->
                                plannedPomodorosText = value.filter { it.isDigit() }.take(2)
                            },
                            modifier = Modifier.width(84.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
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

internal fun Set<String>.toggle(itemId: String): Set<String> {
    return if (itemId in this) this - itemId else this + itemId
}

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
    var hourText by rememberSaveable(initialReminderAt) {
        mutableStateOf(SimpleDateFormat("HH", Locale.getDefault()).format(Date(initialMillis)))
    }
    var minuteText by rememberSaveable(initialReminderAt) {
        mutableStateOf(SimpleDateFormat("mm", Locale.getDefault()).format(Date(initialMillis)))
    }
    val hour = hourText.toIntOrNull()?.coerceIn(0, 23)
    val minute = minuteText.toIntOrNull()?.coerceIn(0, 59)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null && hour != null && minute != null,
                onClick = {
                    val selectedDay = state.selectedDateMillis ?: return@TextButton
                    val day = utcDayKey(selectedDay)
                    val reminderAt = dateKeyToLocalMillis(day, hour ?: 0, minute ?: 0)
                    onSelected(reminderAt)
                }
            ) {
                Text("设置提醒")
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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DatePicker(state = state)
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                    modifier = Modifier.weight(1f),
                    label = { Text("小时") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                    modifier = Modifier.weight(1f),
                    label = { Text("分钟") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}
