package com.protato.app.ui.screens.todo
import com.protato.app.domain.todo.*
import com.protato.app.domain.todo.TodoDraft

import com.protato.app.domain.todo.toggleSubtask
import com.protato.app.domain.todo.toggle

import com.protato.app.*

import com.protato.app.ai.*
import com.protato.app.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.ui.components.EmptyState
import com.protato.app.ui.components.Header
import com.protato.app.ui.screens.todo.TodoDraftMetadataRow
import com.protato.app.ui.screens.todo.TodoFilterControls
import com.protato.app.ui.screens.todo.TodoRow
import com.protato.app.ui.screens.todo.TodoSectionHeader
import com.protato.app.ui.screens.todo.TodoSelectionBar
import com.protato.app.ui.screens.todo.TodoTagFilterDialog
import java.util.Locale

@Composable
internal fun TodoScreen(
    todos: List<TodoItem>,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onAdd: (TodoItem) -> Unit,
    onToggle: (TodoItem) -> Unit,
    onCompleteWithReview: (TodoItem, TodoCompletionReview) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onUpdate: (TodoItem) -> Unit,
    onMoveManyToToday: (Set<String>) -> Unit,
    onDeleteMany: (Set<String>) -> Unit,
    onAddMany: (List<TodoItem>) -> Unit
) {
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var reviewingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var showingAiTodo by remember { mutableStateOf(false) }
    var showingDatePicker by rememberSaveable { mutableStateOf(false) }
    var showingTagFilter by rememberSaveable { mutableStateOf(false) }
    var selectedTodoIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var showIncompleteTodos by rememberSaveable { mutableStateOf(true) }
    var showCompletedTodos by rememberSaveable { mutableStateOf(false) }
    val selecting = selectedTodoIds.isNotEmpty()
    val todayKey = dayKey(System.currentTimeMillis())
    val canMoveSelectionToToday = selectedTodoIds.any { selectedId ->
        todos.any { todo ->
            todo.id == selectedId && !todo.completed && todo.dueDate != todayKey
        }
    }
    var selectedTodoDate by rememberSaveable { mutableStateOf<String?>(null) }
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
    val dateLabel = selectedTodoDate?.let(::formatDayLabel) ?: "收集箱"

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
                    canMoveToToday = canMoveSelectionToToday,
                    onCancel = { selectedTodoIds = emptySet() },
                    onMoveToToday = {
                        onMoveManyToToday(selectedTodoIds)
                        selectedTodoIds = emptySet()
                        selectedTodoDate = todayKey
                    },
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
                        onToggle = {
                            if (!todo.completed && todo.complexity == TodoComplexity.Complex) {
                                reviewingTodo = todo
                            } else {
                                onToggle(todo)
                            }
                        },
                        onToggleSubtask = { subtaskId ->
                            onUpdate(todo.toggleSubtask(subtaskId))
                        },
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
                            onToggleSubtask = { subtaskId ->
                                onUpdate(todo.toggleSubtask(subtaskId))
                            },
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

    reviewingTodo?.let { todo ->
        TodoCompletionReviewDialog(
            todo = todo,
            onDismiss = { reviewingTodo = null },
            onSave = { review ->
                onCompleteWithReview(todo, review)
                reviewingTodo = null
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
    val requestController = remember(scope) { TodoSuggestionController(scope) }
    var rawInput by remember { mutableStateOf("") }
    val requestState by requestController.state.collectAsState()
    val outcome = (requestState as? AsyncRequestState.Success)?.value
    val suggestions = outcome?.suggestions.orEmpty()
    val loading = requestState is AsyncRequestState.Loading
    val errorText = outcome?.errorText
        ?: (requestState as? AsyncRequestState.Failed)?.error?.message
    val agent = selectTodoAgent(agents)
    val provider = agent?.providerId?.let { id -> providers.firstOrNull { it.id == id } }
        ?: providers.firstOrNull { it.isConfiguredForChat() }

    AlertDialog(
        onDismissRequest = {
            requestController.cancel()
            onDismiss()
        },
        confirmButton = {
            TextButton(
                enabled = suggestions.isNotEmpty(),
                onClick = {
                    requestController.cancel()
                    onAddTodos(suggestions)
                }
            ) {
                Text("添加待办")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                requestController.cancel()
                onDismiss()
            }) {
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
                        requestController.submit(rawInput, agent, provider, nickname)
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
