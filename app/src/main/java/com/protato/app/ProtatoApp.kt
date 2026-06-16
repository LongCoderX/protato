package com.protato.app

import com.protato.app.domain.timer.*
import com.protato.app.platform.*
import com.protato.app.state.createProtatoStateHolder
import com.protato.app.state.ProtatoEffect
import com.protato.app.state.ProtatoEvent
import com.protato.app.state.ProtatoEventResult
import com.protato.app.util.*
import com.protato.app.sync.syncWebDevState
import android.Manifest
import android.content.Context
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
import com.protato.app.ai.LlmChatMessage
import com.protato.app.ai.AgentSessionState
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
fun ProtatoApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val stateHolder = remember { createProtatoStateHolder(context) }
    val effectRunner = remember { ProtatoEffectRunner(context) }
    val appState = stateHolder.appState
    var selectedTodoId by rememberSaveable { mutableStateOf<String?>(null) }
    var timerMode by rememberSaveable { mutableStateOf(TimerMode.Focus) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var syncing by rememberSaveable { mutableStateOf(false) }
    var agentInput by rememberSaveable { mutableStateOf("") }
    var agentLoading by rememberSaveable { mutableStateOf(false) }
    var agentErrorText by rememberSaveable { mutableStateOf<String?>(null) }
    var agentSessionState by remember { mutableStateOf(AgentSessionState()) }

    val todayKey = dayKey(System.currentTimeMillis())
    val selectedTodo = appState.todos.firstOrNull {
        it.id == selectedTodoId && !it.completed && it.dueDate == todayKey
    }
    val selectedTemplate = appState.templates.firstOrNull { it.id == appState.selectedTemplateId }
        ?: appState.templates.firstOrNull()
        ?: defaultTemplate()
    val durationMinutes = when (timerMode) {
        TimerMode.Focus -> appState.focusMinutes
        TimerMode.Break -> appState.restMinutes
    }
    val activeSession = appState.activeSession
    val isRunning = activeSession != null && activeSession.pausedRemainingSeconds == null
    val hasPausedSession = activeSession?.pausedRemainingSeconds != null
    val totalSeconds = activeSession?.totalSeconds ?: durationMinutes * 60
    val remainingSeconds = activeSession?.remainingSeconds(nowMillis) ?: totalSeconds
    val pendingRecord = appState.pendingRecord?.toPendingRecordUi()

    fun handleEventResult(result: ProtatoEventResult) {
        result.effects.forEach { effect ->
            if (effect is ProtatoEffect.RemoveTodoSelection && selectedTodoId == effect.todoId) {
                selectedTodoId = null
            }
        }
        effectRunner.runAll(result.effects)
    }

    fun dispatchEvent(event: ProtatoEvent) {
        handleEventResult(stateHolder.dispatch(event))
    }

    fun requestSync() {
        if (syncing) return
        val stateSnapshot = appState
        scope.launch {
            syncing = true
            val message = runCatching {
                syncWebDevState(stateSnapshot)
                "同步完成"
            }.getOrElse { error ->
                error.message ?: "同步失败"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            syncing = false
        }
    }

    LaunchedEffect(appState) {
        stateHolder.saveCurrentState()
    }

    LaunchedEffect(activeSession?.mode) {
        activeSession?.let { timerMode = it.mode }
    }

    LaunchedEffect(activeSession?.endsAt, activeSession?.pausedRemainingSeconds) {
        if (activeSession != null && isRunning) {
            effectRunner.runAll(
                listOf(
                    ProtatoEffect.StartTimerService,
                    ProtatoEffect.ScheduleTimerAlarm(activeSession)
                )
            )
        }
    }

    LaunchedEffect(activeSession?.endsAt, activeSession?.pausedRemainingSeconds) {
        while (activeSession != null && isRunning) {
            nowMillis = System.currentTimeMillis()
            if (activeSession.remainingSeconds(nowMillis) <= 0) {
                val nextState = completeSession(appState)
                stateHolder.setState(nextState)
                stateHolder.saveCurrentState()
                effectRunner.runAll(
                    listOf(
                        ProtatoEffect.CancelTimerNotification,
                        ProtatoEffect.CancelTimerAlarm,
                        ProtatoEffect.StopTimerService
                    )
                )
                break
            }
            delay(1000)
        }
    }

    ProtatoTheme(fontScale = appState.fontScale) {
        var currentTab by rememberSaveable { mutableStateOf(MainTab.Agent) }
        var secondaryDestination by rememberSaveable { mutableStateOf<SecondaryDestination?>(null) }
        fun selectTab(tab: MainTab) {
            currentTab = tab
            secondaryDestination = null
        }
        Scaffold(
            bottomBar = {
                ProtatoBottomBar(
                    currentTab = currentTab,
                    onTabSelected = ::selectTab
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                if (secondaryDestination != null) {
                    when (secondaryDestination) {
                        SecondaryDestination.Templates -> TemplateScreen(
                            templates = appState.templates,
                            selectedTemplateId = appState.selectedTemplateId,
                            providers = appState.llmProviders,
                            agents = appState.agents,
                            nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                            onSelectedTemplate = { templateId ->
                                dispatchEvent(ProtatoEvent.SelectTemplate(templateId))
                            },
                            onUpsert = { template ->
                                dispatchEvent(ProtatoEvent.UpsertTemplate(template))
                            },
                            onDelete = { template ->
                                dispatchEvent(ProtatoEvent.DeleteTemplate(template))
                            },
                            onBack = { secondaryDestination = null }
                        )

                        SecondaryDestination.Settings -> SettingsScreen(
                            appState = appState,
                            appVersionName = BuildConfig.VERSION_NAME,
                            syncing = syncing,
                            onSync = ::requestSync,
                            onEvent = ::dispatchEvent,
                            onBack = { secondaryDestination = null }
                        )

                        null -> Unit
                    }
                } else {
                    when (currentTab) {
                        MainTab.Agent -> AgentScreen(
                            appState = appState,
                            input = agentInput,
                            loading = agentLoading,
                            errorText = agentErrorText,
                            sessionState = agentSessionState,
                            turnScope = scope,
                            onInputChange = { agentInput = it },
                            onLoadingChange = { agentLoading = it },
                            onErrorTextChange = { agentErrorText = it },
                            onSessionStateChange = { agentSessionState = it },
                            onEvent = ::dispatchEvent
                        )

                        MainTab.Focus -> FocusScreen(
                            appState = appState,
                            selectedTodoId = selectedTodoId,
                            selectedTemplate = selectedTemplate,
                            providers = appState.llmProviders,
                            agents = appState.agents,
                            nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                            timerMode = timerMode,
                            remainingSeconds = remainingSeconds,
                            totalSeconds = totalSeconds,
                            isRunning = isRunning,
                            hasPausedSession = hasPausedSession,
                            pendingRecord = pendingRecord,
                            onEvent = ::dispatchEvent,
                            onSelectedTodo = { selectedTodoId = it },
                            onModeChange = { mode ->
                                timerMode = mode
                                dispatchEvent(ProtatoEvent.ClearActiveSession)
                            },
                            onStartPause = {
                                if (isRunning) {
                                    activeSession?.let { session ->
                                        dispatchEvent(
                                            ProtatoEvent.PauseActiveSession(
                                                remainingSeconds = session.remainingSeconds(nowMillis)
                                            )
                                        )
                                    }
                                } else {
                                    val startedAt = System.currentTimeMillis()
                                    val session = activeSession?.let { pausedSession ->
                                        pausedSession.pausedRemainingSeconds?.let { pausedSeconds ->
                                            pausedSession.copy(
                                                endsAt = startedAt + pausedSeconds * 1000L,
                                                pausedRemainingSeconds = null
                                            )
                                        }
                                    } ?: TimerSession(
                                        mode = timerMode,
                                        todoId = selectedTodo?.id,
                                        todoTitle = selectedTodo?.title ?: "未绑定任务",
                                        startedAt = startedAt,
                                        endsAt = startedAt + totalSeconds * 1000L,
                                        totalSeconds = totalSeconds,
                                        templateId = selectedTemplate.id
                                    )
                                    handleEventResult(stateHolder.dispatch(ProtatoEvent.SetActiveSession(session)))
                                    stateHolder.saveCurrentState()
                                    nowMillis = startedAt
                                }
                            },
                            onReset = {
                                dispatchEvent(ProtatoEvent.ClearActiveSession)
                            },
                            onDismissRecord = {
                                dispatchEvent(ProtatoEvent.ClearPendingRecord)
                            },
                            onSaveRecord = { record, answers ->
                                dispatchEvent(
                                    ProtatoEvent.SavePendingRecord(
                                        record = PendingPomodoroRecord(
                                            todoId = record.todoId,
                                            todoTitle = record.todoTitle,
                                            startedAt = record.startedAt,
                                            endedAt = record.endedAt,
                                            focusMinutes = record.focusMinutes,
                                            templateId = record.templateId
                                        ),
                                        answers = answers
                                    )
                                )
                            }
                        )

                        MainTab.Todo -> TodoScreen(
                            todos = appState.todos,
                            providers = appState.llmProviders,
                            agents = appState.agents,
                            nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                            onAdd = { todo ->
                                dispatchEvent(ProtatoEvent.AddTodo(todo))
                            },
                            onToggle = { item ->
                                dispatchEvent(ProtatoEvent.ToggleTodo(item))
                            },
                            onCompleteWithReview = { item, review ->
                                dispatchEvent(ProtatoEvent.CompleteTodoWithReview(item, review))
                            },
                            onDelete = { item ->
                                dispatchEvent(ProtatoEvent.DeleteTodo(item))
                            },
                            onUpdate = { updatedItem ->
                                dispatchEvent(ProtatoEvent.UpdateTodo(updatedItem))
                            },
                            onDeleteMany = { itemIds ->
                                dispatchEvent(ProtatoEvent.DeleteTodos(itemIds))
                            },
                            onAddMany = { newTodos ->
                                dispatchEvent(ProtatoEvent.AddTodos(newTodos))
                            }
                        )

                        MainTab.Records -> RecordsScreen(
                            records = appState.records,
                            summaries = appState.dailySummaries,
                            templates = appState.templates,
                            todos = appState.todos,
                            providers = appState.llmProviders,
                            agents = appState.agents,
                            nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                            onUpdateRecord = { updatedRecord ->
                                dispatchEvent(ProtatoEvent.UpdateRecord(updatedRecord))
                            },
                            onSaveSummary = { summary ->
                                dispatchEvent(ProtatoEvent.SaveDailySummary(summary))
                            }
                        )

                        MainTab.More -> MoreScreen(
                            onOpenSettings = { secondaryDestination = SecondaryDestination.Settings }
                        )
                    }
                }
            }
        }
    }
}
