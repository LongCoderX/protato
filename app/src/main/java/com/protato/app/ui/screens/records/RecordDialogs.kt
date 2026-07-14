package com.protato.app.ui.screens.records

import com.protato.app.*
import com.protato.app.domain.record.suggestRecordTodoId

import com.protato.app.ai.*
import com.protato.app.domain.timer.*
import com.protato.app.platform.*
import com.protato.app.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.ai.LlmChatMessage
import com.protato.app.domain.todo.sortedForAction
import com.protato.app.ui.components.CelebrationCanvas
import com.protato.app.ui.components.ChatBubble
import com.protato.app.ui.screens.records.RecordAnswerFields
import com.protato.app.ui.screens.records.RecordTodoSelector

@Composable
internal fun RecordChatDialog(
    record: PomodoroRecord,
    template: RecordTemplate,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val requestController = remember(scope) { RecordChatController(scope) }
    val requestState by requestController.state.collectAsState()
    val selectableAgents = agents.filter { it.enabled }.ifEmpty { agents }
    var selectedAgentId by remember(record.id, agents) {
        mutableStateOf(selectRecordAgent(agents)?.id ?: selectableAgents.firstOrNull()?.id)
    }
    var agentMenuExpanded by remember { mutableStateOf(false) }
    var input by remember(record.id) { mutableStateOf("") }
    val loading = requestState is AsyncRequestState.Loading
    val errorText = (requestState as? AsyncRequestState.Failed)?.error?.message
    var messages by remember(record.id) {
        mutableStateOf(
            listOf(
                LlmChatMessage(
                    "assistant",
                    "我在这里。你可以问这次番茄做得怎么样、下一步怎么拆，或者让我帮你从记录里找一个继续推进的角度。"
                )
            )
        )
    }
    val selectedAgent = selectableAgents.firstOrNull { it.id == selectedAgentId }
    val selectedProvider = selectedAgent?.providerId?.let { providerId ->
        providers.firstOrNull { it.id == providerId }
    } ?: providers.firstOrNull { it.isConfiguredForChat() }
    val canSend = selectedAgent != null &&
        selectedProvider?.isConfiguredForChat() == true &&
        input.isNotBlank() &&
        !loading

    AlertDialog(
        onDismissRequest = {
            requestController.cancel()
            onDismiss()
        },
        confirmButton = {
            TextButton(
                enabled = canSend,
                onClick = {
                    val agent = selectedAgent ?: return@TextButton
                    val provider = selectedProvider ?: return@TextButton
                    val userMessage = LlmChatMessage("user", input.trim())
                    val nextMessages = messages + userMessage
                    input = ""
                    messages = nextMessages
                    requestController.submit(
                        record = record,
                        template = template,
                        agent = agent,
                        provider = provider,
                        nickname = nickname,
                        messages = nextMessages
                    ) { reply ->
                        messages = messages + LlmChatMessage("assistant", reply)
                    }
                }
            ) {
                Text(if (loading) "发送中" else "发送")
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
        title = { Text("记录 Chat") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    OutlinedButton(
                        enabled = selectableAgents.isNotEmpty(),
                        onClick = { agentMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedAgent?.name ?: "选择 Agent",
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = agentMenuExpanded,
                        onDismissRequest = { agentMenuExpanded = false }
                    ) {
                        selectableAgents.forEach { agent ->
                            DropdownMenuItem(
                                text = { Text(agent.name) },
                                onClick = {
                                    selectedAgentId = agent.id
                                    agentMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    selectedProvider?.name?.let { "Provider：$it" } ?: "先在设置里配置 Provider",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedAgent != null && !selectedAgent.permissions.dailyRecords) {
                    Text("当前 Agent 未开启每日记录权限，本次对话不会读取记录详情。", color = MaterialTheme.colorScheme.error)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    messages.forEach { message ->
                        ChatBubble(message)
                    }
                    if (loading) {
                        Text("AI 正在思考...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                errorText?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(800) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入问题") },
                    minLines = 2
                )
            }
        }
    )
}

@Composable
internal fun RecordDialog(
    pendingRecord: PendingRecord,
    template: RecordTemplate,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    todos: List<TodoItem>,
    recordPolishProviderId: String,
    onDismiss: () -> Unit,
    onSave: (PendingRecord, List<FieldAnswer>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val requestController = remember(scope) { RecordCompletionController(scope) }
    val encouragementState by requestController.encouragementState.collectAsState()
    val polishState by requestController.polishState.collectAsState()
    val answers = remember(template.id) { mutableStateMapOf<String, String>() }
    val missingRequired = hasMissingRequiredAnswers(template, answers)
    val recordDayKey = dayKey(pendingRecord.endedAt)
    val recordDayTodos = remember(todos, recordDayKey) {
        todos
            .filter { it.dueDate == recordDayKey }
            .sortedForAction(recordDayKey)
    }
    val suggestedTodoId = remember(pendingRecord.todoId, pendingRecord.todoTitle, recordDayTodos) {
        pendingRecord.todoId ?: suggestRecordTodoId(
            pendingRecord = pendingRecord,
            answers = emptyList(),
            todos = recordDayTodos
        )
    }
    var selectedTodoId by rememberSaveable(pendingRecord.startedAt) {
        mutableStateOf(suggestedTodoId)
    }
    val selectedTodo = recordDayTodos.firstOrNull { it.id == selectedTodoId }
    var encouragement by remember(pendingRecord.startedAt) {
        mutableStateOf(localEncouragement(pendingRecord, nickname))
    }
    val loadingEncouragement = encouragementState is AsyncRequestState.Loading
    var polishPreview by remember { mutableStateOf<String?>(null) }
    val polishing = polishState is AsyncRequestState.Loading
    var polishFieldId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pendingRecord.startedAt, agents, providers) {
        val agent = selectEncouragerAgent(agents)
        val provider = agent?.providerId?.let { providerId ->
            providers.firstOrNull { it.id == providerId }
        } ?: providers.firstOrNull { it.isConfiguredForChat() }
        if (agent != null && provider != null && provider.isConfiguredForChat()) {
            requestController.loadEncouragement(
                pendingRecord = pendingRecord,
                agent = agent,
                provider = provider,
                nickname = nickname
            ) { result ->
                encouragement = result
            }
        }
        playEncouragementSound()
    }

    val polishProvider = recordPolishProviderId.takeIf { it.isNotBlank() }?.let { providerId ->
        providers.firstOrNull { it.id == providerId }
    } ?: providers.firstOrNull { it.isConfiguredForChat() }

    AlertDialog(
        onDismissRequest = {
            requestController.cancel()
            onDismiss()
        },
        confirmButton = {
            TextButton(
                enabled = !missingRequired,
                onClick = {
                    requestController.cancel()
                    val finalAnswers = template.fields.map {
                        FieldAnswer(it.id, answers[it.id].orEmpty().trim())
                    }
                    val finalTodo = selectedTodo ?: recordDayTodos.firstOrNull {
                        it.id == suggestRecordTodoId(pendingRecord, finalAnswers, recordDayTodos)
                    }
                    onSave(
                        pendingRecord.copy(
                            todoId = finalTodo?.id ?: pendingRecord.todoId,
                            todoTitle = finalTodo?.title ?: pendingRecord.todoTitle
                        ),
                        finalAnswers
                    )
                }
            ) {
                Text("保存记录")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                requestController.cancel()
                onDismiss()
            }) { Text("稍后跳过") }
        },
        title = { Text("记录本轮番茄") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CelebrationCanvas()
                Text(
                    "${pendingRecord.todoTitle} · ${pendingRecord.focusMinutes} 分钟",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pendingRecord.todoId == null) {
                    RecordTodoSelector(
                        todos = recordDayTodos,
                        selectedTodoId = selectedTodoId,
                        fallbackTodoTitle = null,
                        dayLabel = formatDayLabel(recordDayKey),
                        tagColor = ::todoTagColor,
                        onSelected = { selectedTodoId = it }
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("AI 复盘提醒", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (loadingEncouragement) "正在认真组织一句足够贴心的话..." else encouragement
                        )
                        Text(
                            "可以按三点写：完成了什么、遇到什么阻力、下一步最小动作是什么。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                RecordAnswerFields(
                    template = template,
                    answers = answers,
                    onPolish = { fieldId, rawText ->
                        val provider = polishProvider
                        if (provider == null || !provider.isConfiguredForChat()) return@RecordAnswerFields
                        requestController.polish(
                            rawText = rawText,
                            pendingRecord = pendingRecord,
                            template = template,
                            answers = answers,
                            provider = provider,
                            nickname = nickname
                        ) { polished ->
                            polishFieldId = fieldId
                            polishPreview = polished
                        }
                    },
                    polishing = polishing,
                    showPolishButton = true
                )
            }
        }
    )

    polishPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { polishPreview = null; polishFieldId = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        polishFieldId?.let { fieldId ->
                            answers[fieldId] = preview
                        }
                        polishPreview = null
                        polishFieldId = null
                    }
                ) {
                    Text("替换")
                }
            },
            dismissButton = {
                TextButton(onClick = { polishPreview = null; polishFieldId = null }) {
                    Text("保留原文")
                }
            },
            title = { Text("确认替换") },
            text = { Text(preview) }
        )
    }
}

@Composable
internal fun RecordEditDialog(
    record: PomodoroRecord,
    template: RecordTemplate,
    todos: List<TodoItem>,
    onDismiss: () -> Unit,
    onSave: (PomodoroRecord) -> Unit
) {
    var selectedTodoId by rememberSaveable(record.id) { mutableStateOf(record.todoId) }
    val answers = remember(record.id, template.id) {
        mutableStateMapOf<String, String>().apply {
            record.answers.forEach { answer -> put(answer.fieldId, answer.value) }
        }
    }
    val editableFields = template.fields.ifEmpty {
        record.answers.mapIndexed { index, answer ->
            TemplateField(
                id = answer.fieldId,
                title = "字段 ${index + 1}",
                type = FieldType.ShortAnswer
            )
        }
    }
    val editableTemplate = template.copy(fields = editableFields)
    val missingRequired = hasMissingRequiredAnswers(editableTemplate, answers)
    val selectedTodo = todos.firstOrNull { it.id == selectedTodoId }
    val originalTodoStillSelected = selectedTodoId == record.todoId && selectedTodoId != null
    val recordDayKey = dayKey(record.endedAt)
    val recordDayTodos = remember(todos, recordDayKey) {
        todos
            .filter { it.dueDate == recordDayKey }
            .sortedForAction(recordDayKey)
    }
    val selectedTodoShownInDayList = recordDayTodos.any { it.id == selectedTodoId }
    val updatedTodoTitle = when {
        selectedTodo != null -> selectedTodo.title
        originalTodoStillSelected -> record.todoTitle
        else -> "未绑定任务"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !missingRequired,
                onClick = {
                    onSave(
                        record.copy(
                            todoId = selectedTodo?.id ?: selectedTodoId.takeIf { originalTodoStillSelected },
                            todoTitle = updatedTodoTitle,
                            answers = editableTemplate.fields.map {
                                FieldAnswer(it.id, answers[it.id].orEmpty().trim())
                            }
                        )
                    )
                }
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("保存修改")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("编辑记录") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "${record.focusMinutes} 分钟 · ${formatTime(record.endedAt)} · ${record.templateName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RecordTodoSelector(
                    todos = recordDayTodos,
                    selectedTodoId = selectedTodoId,
                    fallbackTodoTitle = updatedTodoTitle.takeIf {
                        originalTodoStillSelected && !selectedTodoShownInDayList
                    },
                    dayLabel = formatDayLabel(recordDayKey),
                    tagColor = ::todoTagColor,
                    onSelected = { selectedTodoId = it }
                )
                HorizontalDivider()
                RecordAnswerFields(
                    template = editableTemplate,
                    answers = answers,
                    onPolish = { _, _ -> },
                    polishing = false,
                    showPolishButton = false
                )
            }
        }
    )
}
