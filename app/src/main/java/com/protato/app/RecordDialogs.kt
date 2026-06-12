package com.protato.app

import com.protato.app.ai.*
import com.protato.app.domain.timer.*
import com.protato.app.platform.*
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.style.TextDecoration
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
import com.protato.app.ui.screens.todo.TodoExpandedDetails
import com.protato.app.ui.screens.todo.TodoMetadataStrip
import com.protato.app.ui.screens.todo.TodoSectionHeader
import com.protato.app.ui.screens.todo.TodoSelectionBar
import com.protato.app.ui.screens.todo.TodoSwipeBackground
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
internal fun RecordChatDialog(
    record: PomodoroRecord,
    template: RecordTemplate,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val selectableAgents = agents.filter { it.enabled }.ifEmpty { agents }
    var selectedAgentId by remember(record.id, agents) {
        mutableStateOf(selectRecordAgent(agents)?.id ?: selectableAgents.firstOrNull()?.id)
    }
    var agentMenuExpanded by remember { mutableStateOf(false) }
    var input by remember(record.id) { mutableStateOf("") }
    var loading by remember(record.id) { mutableStateOf(false) }
    var errorText by remember(record.id) { mutableStateOf<String?>(null) }
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
        onDismissRequest = onDismiss,
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
                    errorText = null
                    scope.launch {
                        loading = true
                        val reply = runCatching {
                            requestRecordChat(
                                record = record,
                                template = template,
                                agent = agent,
                                provider = provider,
                                nickname = nickname,
                                messages = nextMessages
                            )
                        }.getOrElse { error ->
                            errorText = error.message ?: "发送失败"
                            null
                        }
                        if (reply != null) {
                            messages = messages + LlmChatMessage("assistant", reply)
                        }
                        loading = false
                    }
                }
            ) {
                Text(if (loading) "发送中" else "发送")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
    recordPolishProviderId: String,
    onDismiss: () -> Unit,
    onSave: (List<FieldAnswer>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val answers = remember(template.id) { mutableStateMapOf<String, String>() }
    val missingRequired = hasMissingRequiredAnswers(template, answers)
    var encouragement by remember(pendingRecord.startedAt) {
        mutableStateOf(localEncouragement(pendingRecord, nickname))
    }
    var loadingEncouragement by remember(pendingRecord.startedAt) { mutableStateOf(false) }
    var polishPreview by remember { mutableStateOf<String?>(null) }
    var polishing by remember { mutableStateOf(false) }
    var polishFieldId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pendingRecord.startedAt, agents, providers) {
        val agent = selectEncouragerAgent(agents)
        val provider = agent?.providerId?.let { providerId ->
            providers.firstOrNull { it.id == providerId }
        } ?: providers.firstOrNull { it.isConfiguredForChat() }
        if (agent != null && provider != null && provider.isConfiguredForChat()) {
            loadingEncouragement = true
            encouragement = runCatching {
                generateAiEncouragement(
                    pendingRecord = pendingRecord,
                    agent = agent,
                    provider = provider,
                    nickname = nickname
                )
            }.getOrElse {
                localEncouragement(pendingRecord, nickname)
            }
            loadingEncouragement = false
        }
        playEncouragementSound()
    }

    val polishProvider = recordPolishProviderId.takeIf { it.isNotBlank() }?.let { providerId ->
        providers.firstOrNull { it.id == providerId }
    } ?: providers.firstOrNull { it.isConfiguredForChat() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !missingRequired,
                onClick = {
                    onSave(template.fields.map { FieldAnswer(it.id, answers[it.id].orEmpty().trim()) })
                }
            ) {
                Text("保存记录")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("稍后跳过") } },
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("AI 鼓励", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (loadingEncouragement) "正在认真组织一句足够贴心的话..." else encouragement
                        )
                    }
                }
                RecordAnswerFields(
                    template = template,
                    answers = answers,
                    onPolish = { fieldId, rawText ->
                        val provider = polishProvider
                        if (provider == null || !provider.isConfiguredForChat()) return@RecordAnswerFields
                        polishing = true
                        scope.launch {
                            val polished = runCatching {
                                requestRecordPolish(
                                    rawText = rawText,
                                    pendingRecord = pendingRecord,
                                    template = template,
                                    answers = answers,
                                    provider = provider,
                                    nickname = nickname
                                )
                            }.getOrElse { rawText }
                            polishFieldId = fieldId
                            polishPreview = polished
                            polishing = false
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
        todos.filter { it.dueDate == recordDayKey }
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
