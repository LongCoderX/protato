package com.protato.app

import com.protato.app.domain.timer.*
import com.protato.app.state.ProtatoEvent
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
internal fun FocusScreen(
    appState: AppState,
    selectedTodoId: String?,
    selectedTemplate: RecordTemplate,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    timerMode: TimerMode,
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    hasPausedSession: Boolean,
    pendingRecord: PendingRecord?,
    onEvent: (ProtatoEvent) -> Unit,
    onSelectedTodo: (String?) -> Unit,
    onModeChange: (TimerMode) -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onDismissRecord: () -> Unit,
    onSaveRecord: (PendingRecord, List<FieldAnswer>) -> Unit
) {
    val selectedTodo = appState.todos.firstOrNull { it.id == selectedTodoId }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(title = "Protato", subtitle = "番茄时钟、任务和复盘记录都在一条线上")
            TimerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                mode = timerMode,
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
                isRunning = isRunning,
                hasPausedSession = hasPausedSession,
                todos = appState.todos,
                selectedTodoId = selectedTodoId,
                selectedTodoTitle = selectedTodo?.title,
                templates = appState.templates,
                selectedTemplate = selectedTemplate,
                onModeChange = onModeChange,
                onDurationChange = { minutes ->
                    onEvent(ProtatoEvent.UpdateTimerDuration(timerMode, minutes))
                },
                onSelectedTodo = onSelectedTodo,
                onSelectedTemplate = { templateId ->
                    onEvent(ProtatoEvent.SelectTemplate(templateId))
                },
                onStartPause = onStartPause,
                onReset = onReset
            )
        }

        pendingRecord?.let { record ->
            val recordTemplate = appState.templates.firstOrNull { it.id == record.templateId }
                ?: selectedTemplate
            RecordDialog(
                pendingRecord = record,
                template = recordTemplate,
                providers = providers,
                agents = agents,
                nickname = nickname,
                recordPolishProviderId = appState.recordPolishProviderId,
                onDismiss = onDismissRecord,
                onSave = { answers ->
                    onSaveRecord(record, answers)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimerCard(
    modifier: Modifier = Modifier,
    mode: TimerMode,
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    hasPausedSession: Boolean,
    todos: List<TodoItem>,
    selectedTodoId: String?,
    selectedTodoTitle: String?,
    templates: List<RecordTemplate>,
    selectedTemplate: RecordTemplate,
    onModeChange: (TimerMode) -> Unit,
    onDurationChange: (Int) -> Unit,
    onSelectedTodo: (String?) -> Unit,
    onSelectedTemplate: (String) -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    var editingDuration by rememberSaveable { mutableStateOf(false) }
    var choosingTodo by rememberSaveable { mutableStateOf(false) }
    var choosingTemplate by rememberSaveable { mutableStateOf(false) }
    val durationMinutes = totalSeconds / 60
    val showFocusControls = mode == TimerMode.Focus

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val timerModes = listOf(TimerMode.Focus, TimerMode.Break)
                    SingleChoiceSegmentedButtonRow {
                        timerModes.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = mode == item,
                                onClick = { onModeChange(item) },
                                shape = SegmentedButtonDefaults.itemShape(index, timerModes.size),
                                label = { Text(item.label()) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val ringSize = minOf(minOf(maxWidth, maxHeight), 380.dp)
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
                            TimerRing(progress = remainingSeconds / totalSeconds.toFloat().coerceAtLeast(1f))
                            Column(
                                modifier = Modifier.padding(horizontal = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (showFocusControls) {
                                    TextButton(
                                        onClick = { choosingTemplate = true },
                                        modifier = Modifier.widthIn(max = ringSize * 0.78f)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ViewList,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "模板：${selectedTemplate.name}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "休息",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = remainingSeconds.asClock(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(enabled = !isRunning) {
                                        editingDuration = true
                                    }
                                )
                                if (showFocusControls) {
                                    Text(
                                        text = selectedTodoTitle ?: "选择一个待办开始",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable(enabled = !isRunning) {
                                            choosingTodo = true
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onStartPause) {
                        Icon(
                            if (isRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                isRunning -> "暂停"
                                hasPausedSession -> "继续"
                                else -> "开始"
                            }
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(onClick = onReset) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("重置")
                    }
                }
            }
        }
    }

    if (editingDuration) {
        DurationEditDialog(
            modeKey = mode,
            modeLabel = mode.label(),
            durationRange = mode.durationRange(),
            initialMinutes = durationMinutes,
            onDismiss = { editingDuration = false },
            onSave = { minutes ->
                onDurationChange(minutes)
                editingDuration = false
            }
        )
    }

    if (choosingTodo && showFocusControls) {
        val todayKey = dayKey(System.currentTimeMillis())
        TodoSelectDialog(
            todos = todos.filter { !it.completed && it.dueDate == todayKey },
            selectedTodoId = selectedTodoId,
            onDismiss = { choosingTodo = false },
            onSelected = { todoId ->
                onSelectedTodo(todoId)
                choosingTodo = false
            }
        )
    }

    if (choosingTemplate && showFocusControls) {
        TemplateSelectDialog(
            templates = templates,
            selectedTemplateId = selectedTemplate.id,
            onDismiss = { choosingTemplate = false },
            onSelected = { templateId ->
                onSelectedTemplate(templateId)
                choosingTemplate = false
            }
        )
    }
}

@Composable
internal fun TodoSelectDialog(
    todos: List<TodoItem>,
    selectedTodoId: String?,
    onDismiss: () -> Unit,
    onSelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("选择本轮任务") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTodoId == null,
                        onClick = { onSelected(null) },
                        label = { Text("不绑定") }
                    )
                }
                if (todos.isEmpty()) {
                    item {
                        Text("今天还没有未完成待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(todos, key = { it.id }) { todo ->
                        FilterChip(
                            selected = selectedTodoId == todo.id,
                            onClick = { onSelected(todo.id) },
                            label = {
                                TodoChoiceLabel(
                                    todo = todo,
                                    dateLabel = todo.dueDate?.let(::formatDayLabel)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

@Composable
internal fun TemplateSelectDialog(
    templates: List<RecordTemplate>,
    selectedTemplateId: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("选择记录模板") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    FilterChip(
                        selected = selectedTemplateId == template.id,
                        onClick = { onSelected(template.id) },
                        label = {
                            Text(
                                "${template.name} · ${template.fields.size} 个字段",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}
