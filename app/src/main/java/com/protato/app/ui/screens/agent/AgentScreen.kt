package com.protato.app.ui.screens.agent

import com.protato.app.domain.todo.*

import com.protato.app.*

import com.protato.app.ai.*
import com.protato.app.state.ProtatoEvent
import com.protato.app.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.ai.LlmChatMessage
import com.protato.app.ui.components.ChatBubble
import com.protato.app.ui.components.OutlineIconButton
import com.protato.app.ui.components.Header
import com.protato.app.ui.screens.todo.TodoDraftMetadataRow

@Composable
internal fun AgentScreen(
    appState: AppState,
    input: String,
    errorText: String?,
    sessionState: AgentSessionState,
    turnController: AgentTurnController,
    contextTodoId: String?,
    onInputChange: (String) -> Unit,
    onErrorTextChange: (String?) -> Unit,
    onSessionStateChange: (AgentSessionState) -> Unit,
    onContextTodoIdChange: (String?) -> Unit,
    onEvent: (ProtatoEvent) -> Unit
) {
    val requestState by turnController.state.collectAsState()
    val loading = requestState is AsyncRequestState.Loading
    val requestError = (requestState as? AsyncRequestState.Failed)?.error?.message
    DisposableEffect(turnController) {
        onDispose(turnController::cancel)
    }
    val nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME }
    val agent = selectUnifiedAgent(appState.agents)
    val provider = agent?.providerId?.let { providerId ->
        appState.llmProviders.firstOrNull { it.id == providerId }
    } ?: appState.llmProviders.firstOrNull { it.isConfiguredForChat() }
    val listState = rememberLazyListState()
    val canStartNewConversation = !loading &&
        (
            appState.agentMessages.isNotEmpty() ||
                sessionState.hasPendingActions ||
                errorText != null
            )

    LaunchedEffect(appState.agentMessages.size, loading) {
        val itemCount = appState.agentMessages.size + if (loading) 1 else 0
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Header(
            title = "助理",
            subtitle = "任务与复盘伙伴",
            action = {
                OutlineIconButton(
                    icon = Icons.Outlined.RestartAlt,
                    contentDescription = "新开对话",
                    onClick = {
                        if (canStartNewConversation) {
                            onInputChange("")
                            turnController.cancel()
                            onSessionStateChange(sessionState.reset())
                            onErrorTextChange(null)
                            onEvent(ProtatoEvent.ClearAgentMessages)
                        }
                    }
                )
            }
        )
        AgentTodoContextSelector(
            todos = appState.todos.filterNot { it.completed },
            selectedTodoId = contextTodoId,
            onSelectedTodoIdChange = onContextTodoIdChange
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appState.agentMessages, key = { it.id }) { message ->
                ChatBubble(LlmChatMessage(message.role, message.content))
            }
            if (loading) {
                item {
                    ChatBubble(LlmChatMessage("assistant", "正在整理..."))
                }
            }
        }

        if (sessionState.pendingTodos.isNotEmpty()) {
            AgentTodoDraftCard(
                drafts = sessionState.pendingTodos,
                onAdd = {
                    val drafts = sessionState.pendingTodos
                    onSessionStateChange(sessionState.acceptTodoDrafts())
                    onEvent(ProtatoEvent.AcceptAgentTodoDrafts(drafts))
                },
                onDismiss = {
                    val count = sessionState.pendingTodos.size
                    onSessionStateChange(sessionState.rejectTodoDrafts())
                    onEvent(ProtatoEvent.RejectAgentTodoDrafts(count))
                }
            )
        }

        if (sessionState.pendingTodoEdits.isNotEmpty()) {
            AgentTodoEditDraftCard(
                edits = sessionState.pendingTodoEdits,
                todos = appState.todos,
                onApply = {
                    val edits = sessionState.pendingTodoEdits
                    onSessionStateChange(sessionState.acceptTodoEdits())
                    onEvent(ProtatoEvent.AcceptAgentTodoEdits(edits))
                },
                onDismiss = {
                    val count = sessionState.pendingTodoEdits.size
                    onSessionStateChange(sessionState.rejectTodoEdits())
                    onEvent(ProtatoEvent.RejectAgentTodoEdits(count))
                }
            )
        }

        (errorText ?: requestError)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { onInputChange(it.take(1200)) },
                modifier = Modifier.weight(1f),
                label = { Text("和对话助手说话") },
                minLines = 1,
                maxLines = 4
            )
            FilledIconButton(
                enabled = input.isNotBlank() && !loading,
                onClick = {
                    val text = input.trim()
                    onInputChange("")
                    onErrorTextChange(null)
                    val userState = appState.copy(
                        agentMessages = appState.agentMessages.appendAgentMessage("user", text)
                    )
                    onEvent(ProtatoEvent.AppendAgentMessage("user", text))
                    val turnSessionState = sessionState
                    onSessionStateChange(sessionState.startTurn())
                    turnController.submit(
                        AgentTurnRequest(
                            text = text,
                            userState = userState,
                            agent = agent,
                            provider = provider,
                            nickname = nickname,
                            handledTodoDraftKeys = turnSessionState.handledTodoDraftKeySet,
                            handledTodoEditKeys = turnSessionState.handledTodoEditKeySet,
                            hasPendingActions = turnSessionState.hasPendingActions,
                            contextTodoId = contextTodoId
                        )
                    ) { result ->
                        onErrorTextChange(result.errorText)
                        onEvent(ProtatoEvent.AppendAgentMessage("assistant", result.reply))
                        onSessionStateChange(turnSessionState.completeTurn(result))
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "发送")
            }
        }
    }
}

@Composable
internal fun AgentTodoContextSelector(
    todos: List<TodoItem>,
    selectedTodoId: String?,
    onSelectedTodoIdChange: (String?) -> Unit
) {
    if (todos.isEmpty()) return
    val selectedTodo = todos.firstOrNull { it.id == selectedTodoId }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "上下文待办",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selectedTodo != null) {
                TextButton(onClick = { onSelectedTodoIdChange(null) }) {
                    Text("取消选择")
                }
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(todos.take(12), key = { it.id }) { todo ->
                FilterChip(
                    selected = todo.id == selectedTodoId,
                    onClick = {
                        onSelectedTodoIdChange(
                            if (todo.id == selectedTodoId) null else todo.id
                        )
                    },
                    label = {
                        Text(
                            todo.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@Composable
internal fun AgentDayCard(
    todayKey: String,
    todos: List<TodoItem>,
    records: List<PomodoroRecord>,
    backingUp: Boolean,
    backupEnabled: Boolean,
    onSummary: () -> Unit,
    onBackup: () -> Unit
) {
    val totalMinutes = records.sumOf { it.focusMinutes }
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(formatDayLabel(todayKey), fontWeight = FontWeight.SemiBold)
                    Text(
                        "${todos.size} 个待办 · ${records.size} 个番茄 · $totalMinutes 分钟",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    enabled = backupEnabled && !backingUp,
                    onClick = onBackup
                ) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = "备份")
                }
            }
            if (todos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(todos.take(6), key = { it.id }) { todo ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    todo.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
            OutlinedButton(
                enabled = records.isNotEmpty(),
                onClick = onSummary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("生成今日总结")
            }
        }
    }
}

@Composable
internal fun AgentTodoDraftCard(
    drafts: List<TodoDraft>,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("待办工具请求", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onDismiss) {
                    Text("拒绝")
                }
                TextButton(onClick = onAdd) {
                    Text("确认添加")
                }
            }
            drafts.take(5).forEach { draft ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(draft.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        TodoDraftMetadataRow(
                            plannedPomodoros = draft.plannedPomodoros,
                            dueDate = draft.dueDate,
                            reminderAt = draft.reminderAt,
                            tags = draft.tags,
                            dayLabel = ::formatDayLabel,
                            timeLabel = ::formatTime,
                            tagColor = ::todoTagColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AgentTodoEditDraftCard(
    edits: List<TodoEditDraft>,
    todos: List<TodoItem>,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("待办编辑请求", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onDismiss) {
                    Text("拒绝")
                }
                TextButton(onClick = onApply) {
                    Text("确认更新")
                }
            }
            edits.take(5).forEach { edit ->
                val original = todos.firstOrNull { it.id == edit.id }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            original?.title ?: edit.id,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            edit.previewTitle(original),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val previewDraft = edit.toPreviewDraft(original)
                        TodoDraftMetadataRow(
                            plannedPomodoros = previewDraft.plannedPomodoros,
                            dueDate = previewDraft.dueDate,
                            reminderAt = previewDraft.reminderAt,
                            tags = previewDraft.tags,
                            dayLabel = ::formatDayLabel,
                            timeLabel = ::formatTime,
                            tagColor = ::todoTagColor
                        )
                        if (edit.subtasks != null) {
                            Text(
                                "子任务",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            edit.subtasks.take(6).forEachIndexed { index, subtask ->
                                Text(
                                    "${index + 1}. ${subtask.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
