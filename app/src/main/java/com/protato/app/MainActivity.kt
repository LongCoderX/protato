package com.protato.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.Label
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
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

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        setContent {
            ProtatoApp()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    Agent("对话", Icons.AutoMirrored.Outlined.Chat),
    Focus("专注", Icons.Outlined.Timer),
    Todo("待办", Icons.AutoMirrored.Outlined.List),
    Templates("模板", Icons.AutoMirrored.Outlined.ViewList),
    Records("记录", Icons.Outlined.WorkHistory),
    Settings("设置", Icons.Outlined.Settings)
}

@Composable
fun ProtatoApp() {
    val context = LocalContext.current
    val store = remember { ProtatoStore(context) }
    var appState by remember { mutableStateOf(store.load()) }
    var selectedTodoId by rememberSaveable { mutableStateOf<String?>(null) }
    var timerMode by rememberSaveable { mutableStateOf(TimerMode.Focus) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

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

    fun setAppState(nextState: AppState) {
        appState = nextState.withNextRevisionFrom(appState)
    }

    LaunchedEffect(appState) {
        store.save(appState)
    }

    LaunchedEffect(activeSession?.mode) {
        activeSession?.let { timerMode = it.mode }
    }

    LaunchedEffect(activeSession?.endsAt, activeSession?.pausedRemainingSeconds) {
        if (activeSession != null && isRunning) {
            context.startPomodoroService(ACTION_START_TIMER_SERVICE)
            PomodoroAlarmScheduler(context).schedule(activeSession)
        }
    }

    LaunchedEffect(activeSession?.endsAt, activeSession?.pausedRemainingSeconds) {
        while (activeSession != null && isRunning) {
            nowMillis = System.currentTimeMillis()
            if (activeSession.remainingSeconds(nowMillis) <= 0) {
                val nextState = completeSession(appState)
                val revisedState = nextState.withNextRevisionFrom(appState)
                appState = revisedState
                store.save(revisedState)
                PomodoroNotifications(context).cancelTimer()
                PomodoroAlarmScheduler(context).cancel()
                context.stopService(Intent(context, PomodoroTimerService::class.java))
                break
            }
            delay(1000)
        }
    }

    ProtatoTheme(fontScale = appState.fontScale) {
        var currentTab by rememberSaveable { mutableStateOf(MainTab.Agent) }
        Scaffold(
            bottomBar = {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentTab) {
                    MainTab.Agent -> AgentScreen(
                        appState = appState,
                        onStateChange = { setAppState(it) }
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
                        onStateChange = { setAppState(it) },
                        onSelectedTodo = { selectedTodoId = it },
                        onModeChange = { mode ->
                            timerMode = mode
                            setAppState(appState.copy(activeSession = null))
                            PomodoroAlarmScheduler(context).cancel()
                            context.startPomodoroService(ACTION_STOP_TIMER_SERVICE)
                        },
                        onStartPause = {
                            if (isRunning) {
                                activeSession?.let { session ->
                                    setAppState(
                                        appState.copy(
                                            activeSession = session.copy(
                                                pausedRemainingSeconds = session.remainingSeconds(nowMillis)
                                            )
                                        )
                                    )
                                }
                                PomodoroAlarmScheduler(context).cancel()
                                context.stopService(Intent(context, PomodoroTimerService::class.java))
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
                                val nextState = appState.copy(activeSession = session)
                                val revisedState = nextState.withNextRevisionFrom(appState)
                                appState = revisedState
                                store.save(revisedState)
                                nowMillis = startedAt
                                PomodoroAlarmScheduler(context).schedule(session)
                                context.startPomodoroService(ACTION_START_TIMER_SERVICE)
                            }
                        },
                        onReset = {
                            setAppState(appState.copy(activeSession = null))
                            PomodoroAlarmScheduler(context).cancel()
                            context.startPomodoroService(ACTION_STOP_TIMER_SERVICE)
                        },
                        onDismissRecord = {
                            setAppState(appState.copy(pendingRecord = null))
                            PomodoroNotifications(context).cancelCompleted()
                        },
                        onSaveRecord = { record, answers ->
                            val template = appState.templates.firstOrNull { it.id == record.templateId }
                                ?: selectedTemplate
                            val newRecord = PomodoroRecord(
                                id = newId(),
                                todoId = record.todoId,
                                todoTitle = record.todoTitle,
                                templateId = template.id,
                                templateName = template.name,
                                startedAt = record.startedAt,
                                endedAt = record.endedAt,
                                focusMinutes = record.focusMinutes,
                                answers = answers
                            )
                            setAppState(appState.copy(
                                records = listOf(newRecord) + appState.records,
                                pendingRecord = null
                            ))
                            PomodoroNotifications(context).cancelCompleted()
                        }
                    )

                    MainTab.Todo -> TodoScreen(
                        todos = appState.todos,
                        providers = appState.llmProviders,
                        agents = appState.agents,
                        nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                        onAdd = { todo ->
                            setAppState(appState.copy(
                                todos = listOf(todo) + appState.todos
                            ))
                        },
                        onToggle = { item ->
                            setAppState(appState.copy(
                                todos = appState.todos.map {
                                    if (it.id == item.id) it.copy(completed = !it.completed) else it
                                }
                            ))
                        },
                        onDelete = { item ->
                            setAppState(appState.copy(todos = appState.todos.filterNot { it.id == item.id }))
                            if (selectedTodoId == item.id) selectedTodoId = null
                        },
                        onUpdate = { updatedItem ->
                            setAppState(appState.copy(
                                todos = appState.todos.map { item ->
                                    if (item.id == updatedItem.id) updatedItem else item
                                }
                            ))
                        },
                        onDeleteMany = { itemIds ->
                            setAppState(appState.copy(
                                todos = appState.todos.filterNot { it.id in itemIds }
                            ))
                            if (selectedTodoId in itemIds) selectedTodoId = null
                        },
                        onAddMany = { newTodos ->
                            if (newTodos.isNotEmpty()) {
                                setAppState(appState.copy(todos = newTodos + appState.todos))
                            }
                        }
                    )

                    MainTab.Templates -> TemplateScreen(
                        templates = appState.templates,
                        selectedTemplateId = appState.selectedTemplateId,
                        providers = appState.llmProviders,
                        agents = appState.agents,
                        nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                        onSelectedTemplate = { templateId ->
                            setAppState(appState.copy(selectedTemplateId = templateId))
                        },
                        onUpsert = { template ->
                            val exists = appState.templates.any { it.id == template.id }
                            val templates = if (exists) {
                                appState.templates.map { if (it.id == template.id) template else it }
                            } else {
                                listOf(template) + appState.templates
                            }
                            setAppState(appState.copy(
                                templates = templates,
                                selectedTemplateId = template.id
                            ))
                        },
                        onDelete = { template ->
                            val nextTemplates = appState.templates.filterNot { it.id == template.id }.ifEmpty {
                                listOf(defaultTemplate())
                            }
                            setAppState(appState.copy(
                                templates = nextTemplates,
                                selectedTemplateId = nextTemplates.first().id
                            ))
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
                            setAppState(appState.copy(
                                records = appState.records.map { record ->
                                    if (record.id == updatedRecord.id) updatedRecord else record
                                }
                            ))
                        },
                        onSaveSummary = { summary ->
                            val summaries = listOf(summary) + appState.dailySummaries.filterNot {
                                it.dayKey == summary.dayKey
                            }
                            setAppState(appState.copy(dailySummaries = summaries))
                        }
                    )

                    MainTab.Settings -> SettingsScreen(
                        appState = appState,
                        appVersionName = BuildConfig.VERSION_NAME,
                        onStateChange = { setAppState(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtatoTheme(fontScale: Float, content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Color(0xFFE94F37),
        onPrimary = Color.White,
        secondary = Color(0xFF2F9E44),
        tertiary = Color(0xFF456990),
        background = Color(0xFFFAFAF8),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF0EEE9),
        outline = Color(0xFFD1CCC1)
    )
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale)
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}

@Composable
private fun AgentScreen(
    appState: AppState,
    onStateChange: (AppState) -> Unit
) {
    val scope = rememberCoroutineScope()
    val nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME }
    val agent = selectUnifiedAgent(appState.agents)
    val provider = agent?.providerId?.let { providerId ->
        appState.llmProviders.firstOrNull { it.id == providerId }
    } ?: appState.llmProviders.firstOrNull { it.isConfiguredForChat() }
    var input by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingTodos by remember { mutableStateOf<List<TodoDraft>>(emptyList()) }
    var pendingTodoEdits by remember { mutableStateOf<List<TodoEditDraft>>(emptyList()) }
    var handledTodoDraftKeys by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var handledTodoEditKeys by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val listState = rememberLazyListState()
    val canStartNewConversation = !loading &&
        (
            appState.agentMessages.isNotEmpty() ||
                pendingTodos.isNotEmpty() ||
                pendingTodoEdits.isNotEmpty() ||
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

        if (pendingTodos.isNotEmpty()) {
            AgentTodoDraftCard(
                drafts = pendingTodos,
                onAdd = {
                    val newTodos = pendingTodos.map { it.toTodoItem() }
                    handledTodoDraftKeys = (handledTodoDraftKeys + pendingTodos.map(::todoDraftKey))
                        .distinct()
                        .takeLast(120)
                    onStateChange(
                        appState.copy(
                            todos = newTodos + appState.todos,
                            agentMessages = appState.agentMessages.appendAgentMessage(
                                role = "assistant",
                                content = "已添加 ${newTodos.size} 个待办。"
                            )
                        )
                    )
                    pendingTodos = emptyList()
                },
                onDismiss = {
                    handledTodoDraftKeys = (handledTodoDraftKeys + pendingTodos.map(::todoDraftKey))
                        .distinct()
                        .takeLast(120)
                    onStateChange(
                        appState.copy(
                            agentMessages = appState.agentMessages.appendAgentMessage(
                                role = "assistant",
                                content = "已拒绝本次待办草案。"
                            )
                        )
                    )
                    pendingTodos = emptyList()
                }
            )
        }

        if (pendingTodoEdits.isNotEmpty()) {
            AgentTodoEditDraftCard(
                edits = pendingTodoEdits,
                todos = appState.todos,
                onApply = {
                    handledTodoEditKeys = (handledTodoEditKeys + pendingTodoEdits.map(::todoEditDraftKey))
                        .distinct()
                        .takeLast(120)
                    val editsById = pendingTodoEdits.associateBy { it.id }
                    val updatedTodos = appState.todos.map { todo ->
                        editsById[todo.id]?.applyTo(todo) ?: todo
                    }
                    onStateChange(
                        appState.copy(
                            todos = updatedTodos,
                            agentMessages = appState.agentMessages.appendAgentMessage(
                                role = "assistant",
                                content = "已更新 ${pendingTodoEdits.size} 个待办。"
                            )
                        )
                    )
                    pendingTodoEdits = emptyList()
                },
                onDismiss = {
                    handledTodoEditKeys = (handledTodoEditKeys + pendingTodoEdits.map(::todoEditDraftKey))
                        .distinct()
                        .takeLast(120)
                    onStateChange(
                        appState.copy(
                            agentMessages = appState.agentMessages.appendAgentMessage(
                                role = "assistant",
                                content = "已拒绝本次待办编辑草案。"
                            )
                        )
                    )
                    pendingTodoEdits = emptyList()
                }
            )
        }

        errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.take(1200) },
                modifier = Modifier.weight(1f),
                label = { Text("和对话助手说话") },
                minLines = 1,
                maxLines = 4
            )
            IconButton(
                enabled = canStartNewConversation,
                onClick = {
                    input = ""
                    pendingTodos = emptyList()
                    pendingTodoEdits = emptyList()
                    handledTodoDraftKeys = emptyList()
                    handledTodoEditKeys = emptyList()
                    errorText = null
                    onStateChange(appState.copy(agentMessages = emptyList()))
                }
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = "新开对话")
            }
            FilledIconButton(
                enabled = input.isNotBlank() && !loading,
                onClick = {
                    val text = input.trim()
                    input = ""
                    errorText = null
                    val userState = appState.copy(
                        agentMessages = appState.agentMessages.appendAgentMessage("user", text)
                    )
                    onStateChange(userState)
                    pendingTodos = emptyList()
                    pendingTodoEdits = emptyList()
                    scope.launch {
                        loading = true
                        val shouldPlan = text.looksLikeTodoPlanningRequest()
                        val todoDrafts = if (shouldPlan) {
                            runCatching {
                                if (agent != null && provider != null && provider.isConfiguredForChat()) {
                                    generateAgentTodoDrafts(
                                        rawInput = text,
                                        state = userState,
                                        agent = agent,
                                        provider = provider,
                                        nickname = nickname,
                                        planningMode = true
                                    )
                                } else {
                                    localPlanTodoDrafts(text)
                                }
                            }.getOrElse {
                                localPlanTodoDrafts(text)
                            }
                        } else {
                            emptyList()
                        }
                        val rawReply = runCatching {
                            if (agent != null && provider != null && provider.isConfiguredForChat()) {
                                requestUnifiedAgentChat(
                                    state = userState,
                                    agent = agent,
                                    provider = provider,
                                    nickname = nickname
                                )
                            } else {
                                localUnifiedAgentReply(text, userState, nickname)
                            }
                        }.getOrElse { error ->
                            errorText = error.message ?: "发送失败"
                            localUnifiedAgentReply(text, userState, nickname)
                        }
                        val toolResponse = parseAgentToolResponse(rawReply)
                        val finalTodoDrafts = toolResponse.todoDrafts
                            .ifEmpty { todoDrafts }
                            .filterNewTodoDrafts(
                                existingTodos = userState.todos,
                                handledKeys = handledTodoDraftKeys.toSet()
                            )
                        val finalTodoEdits = toolResponse.todoEdits.filterNewTodoEdits(
                            existingTodos = userState.todos,
                            handledKeys = handledTodoEditKeys.toSet()
                        )
                        val reply = if (
                            (
                                toolResponse.todoDrafts.isNotEmpty() ||
                                    todoDrafts.isNotEmpty() ||
                                    toolResponse.todoEdits.isNotEmpty()
                                ) &&
                            finalTodoDrafts.isEmpty() &&
                            finalTodoEdits.isEmpty()
                        ) {
                            "这些待办草案或编辑草案已经确认、拒绝或不再需要处理，我不会重复提交。"
                        } else {
                            toolResponse.visibleReply
                        }
                        onStateChange(
                            userState.copy(
                                agentMessages = userState.agentMessages.appendAgentMessage(
                                    "assistant",
                                    reply
                                )
                            )
                        )
                        pendingTodos = finalTodoDrafts
                        pendingTodoEdits = finalTodoEdits
                        loading = false
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "发送")
            }
        }
    }
}

@Composable
private fun AgentDayCard(
    todayKey: String,
    todos: List<TodoItem>,
    records: List<PomodoroRecord>,
    syncing: Boolean,
    syncEnabled: Boolean,
    onSummary: () -> Unit,
    onSync: () -> Unit
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
                    enabled = syncEnabled && !syncing,
                    onClick = onSync
                ) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = "同步")
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
private fun AgentTodoDraftCard(
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
                        TodoDraftMetadataRow(draft)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentTodoEditDraftCard(
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
                        TodoDraftMetadataRow(edit.toPreviewDraft(original))
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusScreen(
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
    onStateChange: (AppState) -> Unit,
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
                    onStateChange(
                        when (timerMode) {
                            TimerMode.Focus -> appState.copy(focusMinutes = minutes)
                            TimerMode.Break -> appState.copy(restMinutes = minutes)
                        }
                    )
                },
                onSelectedTodo = onSelectedTodo,
                onSelectedTemplate = { templateId ->
                    onStateChange(appState.copy(selectedTemplateId = templateId))
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
private fun TimerCard(
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
            mode = mode,
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
private fun DurationEditDialog(
    mode: TimerMode,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var minutesText by rememberSaveable(mode, initialMinutes) { mutableStateOf(initialMinutes.toString()) }
    val range = mode.durationRange()
    val minutes = minutesText.toIntOrNull()
    val validMinutes = minutes?.coerceIn(range.first, range.last)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = validMinutes != null,
                onClick = { validMinutes?.let(onSave) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("调整${mode.label()}时长") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { value ->
                        minutesText = value.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("分钟") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    "${range.first}-${range.last} 分钟",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun TodoSelectDialog(
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
                                TodoChoiceLabel(todo)
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
private fun TemplateSelectDialog(
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

@Composable
private fun TodoChoiceLabel(todo: TodoItem) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            todo.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val metadata = buildList {
            add("${todo.plannedPomodoros.coerceAtLeast(1)} 个番茄")
            todo.dueDate?.let { add(formatDayLabel(it)) }
            addAll(todo.tags.map { "#$it" })
        }.joinToString(" · ")
        if (metadata.isNotBlank()) {
            Text(
                metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TimerRing(progress: Float) {
    val color = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke
        )
    }
}

@Composable
private fun TodoScreen(
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
private fun TodoAiDialog(
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
                            TodoDraftMetadataRow(suggestion)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TodoDraftMetadataRow(draft: TodoDraft) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item { TodoPomodoroChip(draft.plannedPomodoros) }
        draft.dueDate?.let { dueDate ->
            item { TodoDateChip(dueDate) }
        }
        items(draft.tags, key = { it }) { tag ->
            TodoTagChip(tag)
        }
    }
}

@Composable
private fun TodoTagPickerButton(
    selectedTags: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            Icons.Outlined.Label,
            contentDescription = "编辑标签",
            tint = selectedTags.firstOrNull()?.let(::todoTagColor) ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodoDatePickerButton(
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
private fun TodoTagEditDialog(
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
                        leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) }
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
private fun TodoDatePickerDialog(
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
private fun TodoFilterControls(
    dateKeys: List<String>,
    selectedDate: String?,
    selectedTag: String?,
    tags: List<String>,
    undatedCount: Int,
    onDateSelected: (String?) -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenTagFilter: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onOpenTagFilter, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Label, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedTag?.let { "#$it" } ?: "全部标签",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOpenDatePicker) {
                Icon(Icons.Outlined.DateRange, contentDescription = "选择日期")
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dateKeys, key = { it }) { key ->
                FilterChip(
                    selected = selectedDate == key,
                    onClick = { onDateSelected(key) },
                    label = {
                        Text(
                            formatDayLabel(key),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
            item {
                FilterChip(
                    selected = selectedDate == null,
                    onClick = { onDateSelected(null) },
                    label = { Text("无日期 · $undatedCount") }
                )
            }
        }
        if (tags.isEmpty() && selectedTag != null) {
            Text(
                "当前没有可用标签",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TodoTagFilterDialog(
    tags: List<String>,
    selectedTag: String?,
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
        title = { Text("筛选标签") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTag == null,
                        onClick = { onSelected(null) },
                        label = { Text("全部标签") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (tags.isEmpty()) {
                    item {
                        Text("还没有标签。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(tags, key = { it }) { tag ->
                        val tagColor = todoTagColor(tag)
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { onSelected(if (selectedTag == tag) null else tag) },
                            label = { Text("#$tag") },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    color = tagColor,
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

@Composable
private fun TodoSectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.width(44.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
        Text("$title · $count", fontWeight = FontWeight.SemiBold)
        Icon(
            if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TodoRow(
    todo: TodoItem,
    selected: Boolean,
    selecting: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelected: () -> Unit,
    onLongPress: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    var expanded by rememberSaveable(todo.id) { mutableStateOf(false) }

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.StartToEnd -> {
                onEdit()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !selecting,
        enableDismissFromEndToStart = !selecting,
        backgroundContent = {
            TodoSwipeBackground(dismissValue = dismissState.targetValue)
        }
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (selecting) {
                            onToggleSelected()
                        } else {
                            expanded = !expanded
                        }
                    },
                    onLongClick = onLongPress
                ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = if (selecting) selected else todo.completed,
                    onCheckedChange = {
                        if (selecting) onToggleSelected() else onToggle()
                    }
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = todo.title,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (todo.completed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    TodoMetadataStrip(todo = todo, includeTags = !expanded)
                    if (expanded) {
                        TodoExpandedDetails(todo = todo)
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoMetadataStrip(
    todo: TodoItem,
    includeTags: Boolean = true
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item { TodoPomodoroChip(todo.plannedPomodoros) }
        todo.dueDate?.let { dueDate ->
            item { TodoDateChip(dueDate) }
        }
        if (includeTags) {
            items(todo.tags, key = { it }) { tag ->
                TodoTagChip(tag)
            }
        }
    }
}

@Composable
private fun TodoExpandedDetails(todo: TodoItem) {
    if (todo.tags.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (todo.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(todo.tags, key = { it }) { tag ->
                    TodoTagChip(tag)
                }
            }
        }
    }
}

@Composable
private fun TodoPomodoroChip(count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tomato),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                "x ${count.coerceAtLeast(1)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TodoDateChip(dueDate: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.DateRange,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                formatDayLabel(dueDate),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TodoTagChip(tag: String) {
    val chipColor = todoTagColor(tag)
    Surface(
        color = chipColor.copy(alpha = 0.18f),
        contentColor = chipColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.Label,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                "#$tag",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TodoSwipeBackground(dismissValue: SwipeToDismissBoxValue) {
    val deleting = dismissValue == SwipeToDismissBoxValue.EndToStart
    val editing = dismissValue == SwipeToDismissBoxValue.StartToEnd
    val editContentColor = if (editing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = when {
        deleting -> MaterialTheme.colorScheme.error
        editing -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 18.dp),
        horizontalArrangement = if (deleting) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!deleting) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "编辑待办",
                tint = editContentColor
            )
            Spacer(Modifier.width(8.dp))
            Text("编辑", color = editContentColor, fontWeight = FontWeight.SemiBold)
        } else {
            Text("删除", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "删除待办",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun TodoSelectionBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancel) {
            Icon(Icons.Outlined.Close, contentDescription = "退出多选")
        }
        Text(
            text = "已选择 $selectedCount 项",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "删除所选待办",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodoEditorDialog(
    todo: TodoItem,
    availableTags: List<String>,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit
) {
    var title by rememberSaveable(todo.id) { mutableStateOf(todo.title) }
    var draftTags by rememberSaveable(todo.id) { mutableStateOf(todo.tags) }
    var draftDueDate by rememberSaveable(todo.id) { mutableStateOf(todo.dueDate) }
    var plannedPomodorosText by rememberSaveable(todo.id) {
        mutableStateOf(todo.plannedPomodoros.coerceIn(1, 24).toString())
    }
    var editingTags by rememberSaveable(todo.id) { mutableStateOf(false) }
    var editingDate by rememberSaveable(todo.id) { mutableStateOf(false) }
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
                            plannedPomodoros = plannedPomodoros
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
                        icon = Icons.Outlined.Label,
                        title = "标签",
                        value = if (draftTags.isEmpty()) "未添加标签" else draftTags.joinToString(" ") { "#$it" },
                        onClick = { editingTags = true }
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
}

private fun Set<String>.toggle(itemId: String): Set<String> {
    return if (itemId in this) this - itemId else this + itemId
}

@Composable
private fun TemplateScreen(
    templates: List<RecordTemplate>,
    selectedTemplateId: String,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onSelectedTemplate: (String) -> Unit,
    onUpsert: (RecordTemplate) -> Unit,
    onDelete: (RecordTemplate) -> Unit
) {
    var editingTemplate by remember { mutableStateOf<RecordTemplate?>(null) }
    var showingAiTemplate by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(title = "记录模板", subtitle = "自定义番茄结束后要回答的问题")
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(onClick = { editingTemplate = emptyTemplate() }) {
                    Icon(Icons.Outlined.Add, contentDescription = "新建模板")
                }
                FilledIconButton(onClick = { showingAiTemplate = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_robot_svg),
                        contentDescription = "AI 生成模板"
                    )
                }
                Text(
                    "${templates.size} 个模板",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        items(templates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                selected = template.id == selectedTemplateId,
                canDelete = templates.size > 1,
                onSelect = { onSelectedTemplate(template.id) },
                onEdit = { editingTemplate = template },
                onDelete = { onDelete(template) }
            )
        }
    }

    editingTemplate?.let { template ->
        TemplateEditorDialog(
            initialTemplate = template,
            onDismiss = { editingTemplate = null },
            onSave = {
                onUpsert(it)
                editingTemplate = null
            }
        )
    }

    if (showingAiTemplate) {
        TemplateAiDialog(
            providers = providers,
            agents = agents,
            nickname = nickname,
            onDismiss = { showingAiTemplate = false },
            onAddTemplate = { template ->
                onUpsert(template)
                showingAiTemplate = false
            }
        )
    }
}

@Composable
private fun TemplateCard(
    template: RecordTemplate,
    selected: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        template.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${template.fields.size} 个步骤",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (selected) {
                    AssistChip(onClick = onSelect, label = { Text("默认") }, leadingIcon = {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                    })
                } else {
                    OutlinedButton(onClick = onSelect) {
                        Text("设为默认")
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                template.fields.forEachIndexed { index, field ->
                    Text(
                        text = "${index + 1}. ${field.title} · ${field.type.label()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("编辑")
                }
                if (canDelete) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("删除")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditorDialog(
    initialTemplate: RecordTemplate,
    onDismiss: () -> Unit,
    onSave: (RecordTemplate) -> Unit
) {
    var name by rememberSaveable(initialTemplate.id) { mutableStateOf(initialTemplate.name) }
    var fields by remember(initialTemplate.id) { mutableStateOf(initialTemplate.fields) }
    var fieldTitle by rememberSaveable(initialTemplate.id) { mutableStateOf("") }
    var fieldType by rememberSaveable(initialTemplate.id) { mutableStateOf(FieldType.ShortAnswer) }
    var optionsText by rememberSaveable(initialTemplate.id) { mutableStateOf("") }
    var required by rememberSaveable(initialTemplate.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && fields.isNotEmpty()) {
                        onSave(initialTemplate.copy(name = name.trim(), fields = fields))
                    }
                }
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("编辑模板") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("模板名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                Text("字段", fontWeight = FontWeight.SemiBold)
                fields.forEach { field ->
                    FieldEditorRow(
                        field = field,
                        onDelete = { fields = fields.filterNot { it.id == field.id } }
                    )
                }
                OutlinedTextField(
                    value = fieldTitle,
                    onValueChange = { fieldTitle = it },
                    label = { Text("字段标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow {
                    FieldType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = fieldType == type,
                            onClick = { fieldType = type },
                            shape = SegmentedButtonDefaults.itemShape(index, FieldType.entries.size),
                            label = { Text(type.label()) }
                        )
                    }
                }
                if (fieldType == FieldType.SingleChoice) {
                    OutlinedTextField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = { Text("选项，用逗号分隔") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = required, onCheckedChange = { required = it })
                    Text("必填")
                }
                OutlinedButton(
                    onClick = {
                        val options = optionsText.split(",", "，")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        val canAdd = fieldTitle.isNotBlank() &&
                            (fieldType != FieldType.SingleChoice || options.isNotEmpty())
                        if (canAdd) {
                            fields = fields + TemplateField(
                                id = newId(),
                                title = fieldTitle.trim(),
                                type = fieldType,
                                options = options,
                                required = required
                            )
                            fieldTitle = ""
                            optionsText = ""
                            required = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("添加字段")
                }
            }
        }
    )
}

@Composable
private fun TemplateAiDialog(
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onDismiss: () -> Unit,
    onAddTemplate: (RecordTemplate) -> Unit
) {
    val scope = rememberCoroutineScope()
    var rawInput by remember { mutableStateOf("") }
    var suggestion by remember { mutableStateOf<RecordTemplate?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val agent = selectTemplateAgent(agents)
    val provider = agent?.providerId?.let { id -> providers.firstOrNull { it.id == id } }
        ?: providers.firstOrNull { it.isConfiguredForChat() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = suggestion != null,
                onClick = { suggestion?.let(onAddTemplate) }
            ) {
                Text("添加模板")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("AI 生成模板") },
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
                    label = { Text("描述你想复盘什么") },
                    minLines = 4
                )
                OutlinedButton(
                    enabled = rawInput.isNotBlank() && !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            errorText = null
                            suggestion = runCatching {
                                if (agent != null && provider != null && provider.isConfiguredForChat()) {
                                    generateTemplateSuggestion(rawInput, agent, provider, nickname)
                                } else {
                                    localTemplateSuggestion(rawInput)
                                }
                            }.getOrElse { error ->
                                errorText = error.message ?: "生成失败"
                                localTemplateSuggestion(rawInput)
                            }
                            loading = false
                        }
                    }
                ) {
                    Text(if (loading) "生成中" else "生成模板")
                }
                errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                suggestion?.let { template ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(template.name, fontWeight = FontWeight.Bold)
                            template.fields.forEachIndexed { index, field ->
                                Text(
                                    "${index + 1}. ${field.title} · ${field.type.label()}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun FieldEditorRow(field: TemplateField, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(field.title, fontWeight = FontWeight.Medium)
            Text(
                text = buildString {
                    append(field.type.label())
                    if (field.required) append(" · 必填")
                    if (field.options.isNotEmpty()) append(" · ${field.options.joinToString("/")}")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Close, contentDescription = "删除字段")
        }
    }
}

@Composable
private fun RecordsScreen(
    records: List<PomodoroRecord>,
    summaries: List<DailySummary>,
    templates: List<RecordTemplate>,
    todos: List<TodoItem>,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onUpdateRecord: (PomodoroRecord) -> Unit,
    onSaveSummary: (DailySummary) -> Unit
) {
    val scope = rememberCoroutineScope()
    var editingRecord by remember { mutableStateOf<PomodoroRecord?>(null) }
    var viewingSummary by remember { mutableStateOf<DailySummary?>(null) }
    var generatingSummary by remember { mutableStateOf(false) }
    var choosingRecordDate by rememberSaveable { mutableStateOf(false) }
    val todayKey = dayKey(System.currentTimeMillis())
    var selectedDayKey by rememberSaveable { mutableStateOf(todayKey) }
    val availableDayKeys = remember(records, summaries, todayKey, selectedDayKey) {
        (records.map { dayKey(it.endedAt) } + summaries.map { it.dayKey } + todayKey + selectedDayKey)
            .distinct()
            .sortedDescending()
    }
    val effectiveSelectedDayKey = selectedDayKey
        .takeIf { it in availableDayKeys }
        ?: availableDayKeys.firstOrNull()
        ?: todayKey
    LaunchedEffect(effectiveSelectedDayKey) {
        if (selectedDayKey != effectiveSelectedDayKey) {
            selectedDayKey = effectiveSelectedDayKey
        }
    }
    val selectedDayRecords = records
        .filter { dayKey(it.endedAt) == effectiveSelectedDayKey }
        .sortedBy { it.endedAt }
    val selectedDaySummary = summaries.firstOrNull { it.dayKey == effectiveSelectedDayKey }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Header(title = "番茄记录", subtitle = "每次专注结束后的完整内容会沉淀在这里")
            }
            item {
                DaySwitcher(
                    dayKeys = availableDayKeys,
                    selectedDayKey = effectiveSelectedDayKey,
                    records = records,
                    onSelected = { selectedDayKey = it },
                    onOpenDatePicker = { choosingRecordDate = true }
                )
            }
            item {
                DailySummaryCard(
                    dayKey = effectiveSelectedDayKey,
                    records = selectedDayRecords,
                    summary = selectedDaySummary,
                    loading = generatingSummary,
                    onGenerate = {
                        scope.launch {
                            generatingSummary = true
                            val summary = runCatching {
                                generateDailySummary(
                                    dayKey = effectiveSelectedDayKey,
                                    records = selectedDayRecords,
                                    templates = templates,
                                    providers = providers,
                                    agents = agents,
                                    nickname = nickname
                                )
                            }.getOrElse {
                                localDailySummary(effectiveSelectedDayKey, selectedDayRecords, nickname)
                            }
                            onSaveSummary(summary)
                            viewingSummary = summary
                            generatingSummary = false
                        }
                    },
                    onOpen = { selectedDaySummary?.let { viewingSummary = it } }
                )
            }
            if (selectedDayRecords.isEmpty()) {
                item {
                    EmptyState("这一天还没有记录。切换日期，或完成一轮专注后再回来看看。")
                }
            } else {
                item {
                    Text(
                        "时间轴",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(selectedDayRecords, key = { it.id }) { record ->
                    TimelineRecordItem(
                        record = record,
                        template = templates.firstOrNull { it.id == record.templateId },
                        isFirst = record.id == selectedDayRecords.first().id,
                        isLast = record.id == selectedDayRecords.last().id,
                        onEdit = { editingRecord = record }
                    )
                }
            }
        }

        editingRecord?.let { record ->
            val template = templates.firstOrNull { it.id == record.templateId }
                ?: RecordTemplate(record.templateId, record.templateName, emptyList())
            RecordEditDialog(
                record = record,
                template = template,
                todos = todos,
                onDismiss = { editingRecord = null },
                onSave = { updatedRecord ->
                    onUpdateRecord(updatedRecord)
                    editingRecord = null
                }
            )
        }

        viewingSummary?.let { summary ->
            DailySummaryDialog(summary = summary, onDismiss = { viewingSummary = null })
        }

        if (choosingRecordDate) {
            TodoDatePickerDialog(
                initialDate = effectiveSelectedDayKey,
                onDismiss = { choosingRecordDate = false },
                onClear = {
                    selectedDayKey = todayKey
                    choosingRecordDate = false
                },
                onSelected = { day ->
                    selectedDayKey = day
                    choosingRecordDate = false
                }
            )
        }
    }
}

@Composable
private fun DaySwitcher(
    dayKeys: List<String>,
    selectedDayKey: String,
    records: List<PomodoroRecord>,
    onSelected: (String) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenDatePicker) {
            Icon(Icons.Outlined.DateRange, contentDescription = "选择记录日期")
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dayKeys, key = { it }) { key ->
                val count = records.count { dayKey(it.endedAt) == key }
                FilterChip(
                    selected = selectedDayKey == key,
                    onClick = { onSelected(key) },
                    label = {
                        Text(
                            "${formatDayLabel(key)} · $count",
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
private fun DailySummaryCard(
    dayKey: String,
    records: List<PomodoroRecord>,
    summary: DailySummary?,
    loading: Boolean,
    onGenerate: () -> Unit,
    onOpen: () -> Unit
) {
    val focusMinutes = records.sumOf { it.focusMinutes }
    val focusedTodos = records.map { it.todoTitle }.distinct().size

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${formatDayLabel(dayKey)} 总结",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${records.size} 次专注 · $focusMinutes 分钟 · $focusedTodos 个对象",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    enabled = summary != null,
                    onClick = onOpen
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "查看总结")
                }
                FilledIconButton(
                    enabled = records.isNotEmpty() && !loading,
                    onClick = onGenerate
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_robot_svg),
                        contentDescription = "生成总结"
                    )
                }
            }
        }
    }
}

@Composable
private fun DailySummaryDialog(summary: DailySummary, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text(summary.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${summary.pomodoroCount} 个番茄 · ${summary.focusMinutes} 分钟 · ${formatTime(summary.generatedAt)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MarkdownText(summary.content)
            }
        }
    )
}

@Composable
private fun TimelineRecordItem(
    record: PomodoroRecord,
    template: RecordTemplate?,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit
) {
    var expanded by rememberSaveable(record.id) { mutableStateOf(false) }
    val answerCount = record.answers.size
    val lineColor = Color(0xFFF59E0B)
    val lineHeight = ((record.focusMinutes.coerceAtLeast(1) / 25f) * 74f).coerceIn(42f, 126f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (isFirst) 10.dp else 18.dp)
                    .background(if (isFirst) Color.Transparent else lineColor.copy(alpha = 0.55f))
            )
            Surface(
                modifier = Modifier.size(18.dp),
                shape = RoundedCornerShape(8.dp),
                color = lineColor
            ) {}
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(lineHeight.dp)
                    .background(if (isLast) Color.Transparent else lineColor)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                formatClockTime(record.endedAt),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "${record.focusMinutes} 分钟",
                color = lineColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        ElevatedCard(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .clickable { expanded = !expanded },
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        record.todoTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "编辑记录")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (expanded) "收起记录详情" else "展开记录详情",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                if (expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "${record.templateName} · $answerCount 条回答",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        record.answers.forEach { answer ->
                            val title = template?.fields?.firstOrNull { it.id == answer.fieldId }?.title ?: "字段"
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    answer.value.ifBlank { "未填写" },
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val lines = content.lines()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isBlank() -> Spacer(Modifier.height(2.dp))
                line.matches(Regex("-{3,}|\\*{3,}|_{3,}")) -> HorizontalDivider()
                line.startsWith("### ") -> MarkdownLine(
                    text = line.removePrefix("### "),
                    weight = FontWeight.SemiBold,
                    color = accentColor
                )
                line.startsWith("## ") -> MarkdownLine(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.titleMedium,
                    weight = FontWeight.Bold,
                    color = accentColor
                )
                line.startsWith("# ") -> MarkdownLine(
                    text = line.removePrefix("# "),
                    style = MaterialTheme.typography.titleLarge,
                    weight = FontWeight.Bold,
                    color = accentColor
                )
                line.startsWith(">") -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    MarkdownLine(
                        text = line.removePrefix(">").trim(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = contentColor
                    )
                }
                line.isMarkdownBullet() -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = accentColor, fontWeight = FontWeight.Bold)
                    MarkdownLine(
                        text = line.removeMarkdownBullet(),
                        modifier = Modifier.weight(1f),
                        color = contentColor
                    )
                }
                line.isMarkdownNumberedItem() -> {
                    val number = line.substringBefore(".")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("$number.", color = accentColor, fontWeight = FontWeight.Bold)
                        MarkdownLine(
                            text = line.substringAfter(".").trim(),
                            modifier = Modifier.weight(1f),
                            color = contentColor
                        )
                    }
                }
                else -> MarkdownLine(text = line, color = contentColor)
            }
        }
    }
}

@Composable
private fun MarkdownLine(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    weight: FontWeight? = null,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = markdownInlineText(text),
        modifier = modifier,
        style = style,
        fontWeight = weight,
        color = color
    )
}

@Composable
private fun markdownInlineText(text: String): AnnotatedString {
    val strong = SpanStyle(fontWeight = FontWeight.Bold)
    val emphasis = SpanStyle(fontStyle = FontStyle.Italic)
    val code = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        background = MaterialTheme.colorScheme.surfaceVariant
    )
    val deleted = SpanStyle(textDecoration = TextDecoration.LineThrough)
    return buildAnnotatedString {
        appendMarkdownSegments(
            text = text,
            markers = listOf(
                MarkdownMarker("**", strong),
                MarkdownMarker("__", strong),
                MarkdownMarker("~~", deleted),
                MarkdownMarker("`", code),
                MarkdownMarker("*", emphasis),
                MarkdownMarker("_", emphasis)
            )
        )
    }
}

private data class MarkdownMarker(
    val token: String,
    val style: SpanStyle
)

private fun AnnotatedString.Builder.appendMarkdownSegments(
    text: String,
    markers: List<MarkdownMarker>
) {
    var index = 0
    while (index < text.length) {
        val marker = markers.firstOrNull { text.startsWith(it.token, index) }
        if (marker == null) {
            append(text[index].toString())
            index += 1
            continue
        }
        val start = index + marker.token.length
        val end = text.indexOf(marker.token, start)
        if (end <= start) {
            append(marker.token)
            index += marker.token.length
            continue
        }
        withStyle(marker.style) {
            append(text.substring(start, end))
        }
        index = end + marker.token.length
    }
}

private fun String.isMarkdownBullet(): Boolean {
    return startsWith("- ") || startsWith("* ") || startsWith("• ")
}

private fun String.removeMarkdownBullet(): String {
    return drop(2).trim()
}

private fun String.isMarkdownNumberedItem(): Boolean {
    return matches(Regex("\\d+\\.\\s+.+"))
}

@Composable
private fun RecordTodoChip(title: String, bound: Boolean) {
    val containerColor = if (bound) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (bound) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = if (bound) "绑定任务 · $title" else "未绑定任务",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SettingsScreen(
    appState: AppState,
    appVersionName: String,
    onStateChange: (AppState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(title = "设置", subtitle = "本地身份、模型导入和版本")
        }
        item {
            ProjectVersionCard(
                appVersionName = appVersionName
            )
        }
        item {
            NicknameSettingsCard(
                nickname = appState.nickname,
                onNicknameChange = { nickname ->
                    onStateChange(appState.copy(nickname = nickname.ifBlank { DEFAULT_NICKNAME }))
                }
            )
        }
        item {
            FontScaleSettingsCard(
                fontScale = appState.fontScale,
                onChange = { fontScale ->
                    onStateChange(appState.copy(fontScale = fontScale.coerceIn(0.85f, 1.3f)))
                }
            )
        }
        item {
            WebDevSyncSettingsCard(
                settings = appState.webDevSync,
                onChange = { settings ->
                    onStateChange(appState.copy(webDevSync = settings))
                }
            )
        }
        item {
            LlmProviderListCard(
                providers = appState.llmProviders,
                onUpsert = { provider ->
                    val exists = appState.llmProviders.any { it.id == provider.id }
                    val providers = if (exists) {
                        appState.llmProviders.map { if (it.id == provider.id) provider else it }
                    } else {
                        appState.llmProviders + provider
                    }
                    onStateChange(appState.copy(llmProviders = providers))
                },
                onDelete = { provider ->
                    onStateChange(
                        appState.copy(
                            llmProviders = appState.llmProviders.filterNot { it.id == provider.id },
                            agents = appState.agents.map { agent ->
                                if (agent.providerId == provider.id) agent.copy(providerId = "") else agent
                            }
                        )
                    )
                }
            )
        }
        item {
            RecordPolishProviderCard(
                providers = appState.llmProviders,
                selectedProviderId = appState.recordPolishProviderId,
                onProviderSelected = { providerId ->
                    onStateChange(appState.copy(recordPolishProviderId = providerId))
                }
            )
        }
    }
}

@Composable
private fun WebDevSyncSettingsCard(
    settings: WebDevSyncSettings,
    onChange: (WebDevSyncSettings) -> Unit
) {
    val tokenMask = "***"
    val tokenDisplay = if (settings.authToken.isBlank()) "" else tokenMask
    val providerPresets = listOf("webdev", "jianguoyun")
    val presetEndpoints = providerPresets.map { it.syncEndpointPreset() }
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("WebDev 同步", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        settings.cloudProvider.cloudProviderLabel(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { onChange(settings.copy(enabled = it)) }
                )
            }
            OutlinedTextField(
                value = settings.endpoint,
                onValueChange = { onChange(settings.copy(endpoint = it.take(240))) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("同步接口地址") },
                singleLine = true
            )
            OutlinedTextField(
                value = tokenDisplay,
                onValueChange = { value ->
                    val nextToken = when {
                        value.isBlank() -> ""
                        value == tokenMask -> settings.authToken
                        value.startsWith(tokenMask) -> value.removePrefix(tokenMask).take(240)
                        else -> value.take(240)
                    }
                    onChange(settings.copy(authToken = nextToken))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("同步 Token") },
                singleLine = true
            )
            SingleChoiceSegmentedButtonRow {
                providerPresets.forEachIndexed { index, provider ->
                    SegmentedButton(
                        selected = settings.cloudProvider == provider,
                        onClick = {
                            val presetEndpoint = provider.syncEndpointPreset()
                            val shouldReplaceEndpoint = settings.endpoint.isBlank() ||
                                settings.endpoint in presetEndpoints
                            onChange(
                                settings.copy(
                                    cloudProvider = provider,
                                    endpoint = if (shouldReplaceEndpoint) presetEndpoint else settings.endpoint
                                )
                            )
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, providerPresets.size),
                        label = { Text(provider.cloudProviderLabel()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectVersionCard(
    appVersionName: String
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("版本", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "v$appVersionName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "v$appVersionName",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NicknameSettingsCard(
    nickname: String,
    onNicknameChange: (String) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("个人昵称", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = nickname,
                onValueChange = { onNicknameChange(it.take(24)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("本地昵称") },
                singleLine = true
            )
        }
    }
}

@Composable
private fun FontScaleSettingsCard(
    fontScale: Float,
    onChange: (Float) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("字体大小", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("x${"%.2f".format(fontScale)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Slider(
                value = fontScale,
                onValueChange = onChange,
                valueRange = 0.85f..1.3f
            )
        }
    }
}

@Composable
private fun LlmProviderListCard(
    providers: List<LlmProviderSettings>,
    onUpsert: (LlmProviderSettings) -> Unit,
    onDelete: (LlmProviderSettings) -> Unit
) {
    var editingProvider by remember { mutableStateOf<LlmProviderSettings?>(null) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var selectedProviderId by rememberSaveable(providers.map { it.id }.joinToString("|")) {
        mutableStateOf(providers.firstOrNull()?.id.orEmpty())
    }
    val selectedProvider = providers.firstOrNull { it.id == selectedProviderId }
        ?: providers.firstOrNull()

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "大模型 Provider",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = {
                        editingProvider = LlmProviderSettings(
                            id = newId(),
                            providerKey = "custom",
                            name = "自定义 Provider"
                        )
                    }
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("自定义")
                }
            }
            Box {
                OutlinedButton(
                    onClick = { providerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedProvider?.agentProviderLabel() ?: "选择 Provider",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = providerMenuExpanded,
                    onDismissRequest = { providerMenuExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.agentProviderLabel()) },
                            onClick = {
                                selectedProviderId = provider.id
                                providerMenuExpanded = false
                            }
                        )
                    }
                }
            }
            selectedProvider?.let { provider ->
                LlmProviderRow(provider = provider, onClick = { editingProvider = provider })
            }
        }
    }

    editingProvider?.let { provider ->
        LlmProviderEditDialog(
            initialProvider = provider,
            allowDelete = provider.id !in defaultLlmProviders().map { it.id },
            onDismiss = { editingProvider = null },
            onSave = {
                onUpsert(it)
                editingProvider = null
            },
            onDelete = {
                onDelete(provider)
                editingProvider = null
            }
        )
    }
}

@Composable
private fun LlmProviderRow(provider: LlmProviderSettings, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(provider.name, fontWeight = FontWeight.SemiBold)
                Text(
                    listOf(
                        provider.modelName.ifBlank { "未选择模型" },
                        provider.endpoint.ifBlank { "未设置接口" }
                    ).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (provider.apiKey.isBlank()) "未配置" else "已配置",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun LlmProviderEditDialog(
    initialProvider: LlmProviderSettings,
    allowDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (LlmProviderSettings) -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var provider by remember(initialProvider.id) { mutableStateOf(initialProvider) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadingModels by remember { mutableStateOf(false) }
    var modelFetchError by remember { mutableStateOf<String?>(null) }
    val apiKeyMask = "***"
    val apiKeyDisplay = if (provider.apiKey.isBlank()) "" else apiKeyMask
    val providerPresets = defaultLlmProviders()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(provider) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                if (allowDelete) {
                    TextButton(onClick = onDelete) {
                        Text("删除")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
        title = { Text("编辑 Provider") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { providerMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(provider.name, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = providerMenuExpanded,
                        onDismissRequest = { providerMenuExpanded = false }
                    ) {
                        providerPresets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name) },
                                onClick = {
                                    provider = provider.copy(
                                        providerKey = preset.providerKey,
                                        name = preset.name,
                                        endpoint = provider.endpoint.ifBlank { preset.endpoint }
                                    )
                                    providerMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = provider.name,
                    onValueChange = { provider = provider.copy(name = it.take(40)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("显示名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = provider.endpoint,
                    onValueChange = {
                        provider = provider.copy(endpoint = it.take(240))
                        fetchedModels = emptyList()
                        modelFetchError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("接口地址") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        enabled = provider.endpoint.isNotBlank() && !loadingModels,
                        onClick = {
                            scope.launch {
                                loadingModels = true
                                modelFetchError = null
                                fetchedModels = runCatching {
                                    fetchLlmModels(provider.endpoint, provider.apiKey)
                                }.getOrElse { error ->
                                    modelFetchError = error.message ?: "获取模型失败"
                                    emptyList()
                                }
                                loadingModels = false
                            }
                        }
                    ) {
                        if (loadingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("获取模型")
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            enabled = fetchedModels.isNotEmpty(),
                            onClick = { modelMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                provider.modelName.ifBlank { "选择模型" },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = modelMenuExpanded,
                            onDismissRequest = { modelMenuExpanded = false }
                        ) {
                            fetchedModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            model,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        provider = provider.copy(modelName = model)
                                        modelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (fetchedModels.isEmpty()) {
                    OutlinedTextField(
                        value = provider.modelName,
                        onValueChange = { provider = provider.copy(modelName = it.take(80)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("模型名称") },
                        singleLine = true
                    )
                }
                modelFetchError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(
                    value = apiKeyDisplay,
                    onValueChange = { value ->
                        provider = when {
                            value.isBlank() -> provider.copy(apiKey = "")
                            value == apiKeyMask -> provider
                            value.startsWith(apiKeyMask) -> {
                                provider.copy(apiKey = value.removePrefix(apiKeyMask).take(240))
                            }
                            else -> provider.copy(apiKey = value.take(240))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Key") },
                    singleLine = true
                )
            }
        }
    )
}

@Composable
private fun RecordPolishProviderCard(
    providers: List<LlmProviderSettings>,
    selectedProviderId: String,
    onProviderSelected: (String) -> Unit
) {
    var providerMenuExpanded by remember { mutableStateOf(false) }
    val selectedProvider = providers.firstOrNull { it.id == selectedProviderId }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("总结润色模型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(
                    enabled = providers.isNotEmpty(),
                    onClick = { providerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedProvider?.agentProviderLabel() ?: "选择 Provider",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = providerMenuExpanded,
                    onDismissRequest = { providerMenuExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.agentProviderLabel()) },
                            onClick = {
                                onProviderSelected(provider.id)
                                providerMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Text(
                selectedProvider?.let {
                    if (it.isConfiguredForChat()) "已配置" else "请先补全模型和 API Key"
                } ?: "未选择 Provider",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UnifiedAgentProviderCard(
    agents: List<AgentSettings>,
    providers: List<LlmProviderSettings>,
    onProviderSelected: (String) -> Unit
) {
    val agent = selectUnifiedAgent(agents)
    var providerMenuExpanded by remember { mutableStateOf(false) }
    val selectedProvider = providers.firstOrNull { it.id == agent?.providerId }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("对话模型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(
                    enabled = providers.isNotEmpty(),
                    onClick = { providerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedProvider?.agentProviderLabel() ?: "选择 Provider",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = providerMenuExpanded,
                    onDismissRequest = { providerMenuExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.agentProviderLabel()) },
                            onClick = {
                                onProviderSelected(provider.id)
                                providerMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Text(
                selectedProvider?.let {
                    if (it.isConfiguredForChat()) "已配置" else "请先在 Provider 中补全模型和 API Key"
                } ?: "未选择 Provider",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgentListCard(
    agents: List<AgentSettings>,
    providers: List<LlmProviderSettings>,
    nickname: String,
    onUpsert: (AgentSettings) -> Unit,
    onDelete: (AgentSettings) -> Unit
) {
    var editingAgent by remember { mutableStateOf<AgentSettings?>(null) }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Agent",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = {
                        editingAgent = AgentSettings(
                            id = newId(),
                            name = "新 Agent",
                            providerId = providers.firstOrNull()?.id.orEmpty(),
                            permissions = AgentDataPermissions()
                        )
                    }
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("添加")
                }
            }
            if (agents.isEmpty()) {
                Text("还没有 Agent。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                agents.forEach { agent ->
                    AgentRow(
                        agent = agent,
                        providerLabel = providers.firstOrNull {
                            it.id == agent.providerId
                        }?.agentProviderLabel() ?: "未选择模型",
                        onClick = { editingAgent = agent }
                    )
                }
            }
        }
    }

    editingAgent?.let { agent ->
        AgentEditDialog(
            initialAgent = agent,
            providers = providers,
            nickname = nickname,
            onDismiss = { editingAgent = null },
            onSave = {
                onUpsert(it)
                editingAgent = null
            },
            onDelete = {
                onDelete(agent)
                editingAgent = null
            }
        )
    }
}

@Composable
private fun AgentRow(
    agent: AgentSettings,
    providerLabel: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(agent.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "$providerLabel · ${agent.permissions.label()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (agent.enabled) "启用" else "停用",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun AgentEditDialog(
    initialAgent: AgentSettings,
    providers: List<LlmProviderSettings>,
    nickname: String,
    onDismiss: () -> Unit,
    onSave: (AgentSettings) -> Unit,
    onDelete: () -> Unit
) {
    var agent by remember(initialAgent.id) { mutableStateOf(initialAgent) }
    var providerMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(agent) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("删除")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
        title = { Text("编辑 Agent") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("启用", modifier = Modifier.weight(1f))
                    Switch(
                        checked = agent.enabled,
                        onCheckedChange = { agent = agent.copy(enabled = it) }
                    )
                }
                OutlinedTextField(
                    value = agent.name,
                    onValueChange = { agent = agent.copy(name = it.take(24)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Agent 名称") },
                    singleLine = true
                )
                Box {
                    OutlinedButton(
                        onClick = { providerMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            providers.firstOrNull {
                                it.id == agent.providerId
                            }?.agentProviderLabel() ?: "选择模型",
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = providerMenuExpanded,
                        onDismissRequest = { providerMenuExpanded = false }
                    ) {
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.agentProviderLabel()) },
                                onClick = {
                                    agent = agent.copy(providerId = provider.id)
                                    providerMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Text("数据权限", fontWeight = FontWeight.SemiBold)
                PermissionCheckbox(
                    checked = agent.permissions.dailyRecords,
                    label = "每日记录",
                    onCheckedChange = {
                        agent = agent.copy(
                            permissions = agent.permissions.copy(dailyRecords = it)
                        )
                    }
                )
                PermissionCheckbox(
                    checked = agent.permissions.todos,
                    label = "待办",
                    onCheckedChange = {
                        agent = agent.copy(
                            permissions = agent.permissions.copy(todos = it)
                        )
                    }
                )
                PermissionCheckbox(
                    checked = agent.permissions.templates,
                    label = "模板",
                    onCheckedChange = {
                        agent = agent.copy(
                            permissions = agent.permissions.copy(templates = it)
                        )
                    }
                )
                TextButton(
                    onClick = {
                        val prompt = agent.prompt
                        val nextPrompt = if ("{nickname}" in prompt || nickname in prompt) {
                            prompt
                        } else {
                            "请称呼我为「$nickname」。$prompt"
                        }
                        agent = agent.copy(prompt = nextPrompt.take(500))
                    }
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("插入昵称")
                }
                OutlinedTextField(
                    value = agent.prompt,
                    onValueChange = { agent = agent.copy(prompt = it.take(500)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("提示词") },
                    minLines = 3
                )
            }
        }
    )
}

@Composable
private fun PermissionCheckbox(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

private data class LlmChatMessage(
    val role: String,
    val content: String
)

@Composable
private fun RecordChatDialog(
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
private fun ChatBubble(message: LlmChatMessage) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val bubbleContentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val accentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 520.dp),
            color = bubbleColor,
            contentColor = bubbleContentColor,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp
            )
        ) {
            MarkdownText(
                content = message.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                contentColor = bubbleContentColor,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun CelebrationCanvas() {
    val transition = rememberInfiniteTransition(label = "celebration")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti"
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFFC857)
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        val cupY = size.height * 0.58f
        drawRect(
            color = colors[0],
            topLeft = Offset(size.width * 0.33f, cupY),
            size = Size(size.width * 0.12f, size.height * 0.22f)
        )
        drawRect(
            color = colors[2],
            topLeft = Offset(size.width * 0.55f, cupY),
            size = Size(size.width * 0.12f, size.height * 0.22f)
        )
        drawLine(
            color = colors[1],
            start = Offset(size.width * 0.45f, cupY + size.height * 0.05f),
            end = Offset(size.width * 0.56f, cupY),
            strokeWidth = 5.dp.toPx()
        )
        repeat(18) { index ->
            val xSeed = ((index * 37) % 100) / 100f
            val ySeed = ((index * 19) % 100) / 100f
            val x = size.width * xSeed
            val y = size.height * ((ySeed + phase) % 1f) * 0.72f
            drawCircle(
                color = colors[index % colors.size],
                radius = (3 + index % 4).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun RecordDialog(
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
private fun RecordEditDialog(
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

@Composable
private fun RecordTodoSelector(
    todos: List<TodoItem>,
    selectedTodoId: String?,
    fallbackTodoTitle: String?,
    dayLabel: String,
    onSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("绑定任务 · $dayLabel", fontWeight = FontWeight.SemiBold)
        if (todos.isEmpty() && fallbackTodoTitle == null) {
            Text("这一天还没有可绑定的待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            RecordTodoListRow(
                selected = selectedTodoId == null,
                onClick = { onSelected(null) },
                title = "不绑定",
                tags = emptyList()
            )
            if (fallbackTodoTitle != null) {
                RecordTodoListRow(
                    selected = true,
                    onClick = { },
                    title = fallbackTodoTitle,
                    tags = emptyList()
                )
            }
            todos.forEach { todo ->
                RecordTodoListRow(
                    selected = selectedTodoId == todo.id,
                    onClick = { onSelected(todo.id) },
                    title = todo.title,
                    tags = todo.tags,
                    completed = todo.completed
                )
            }
        }
    }
}

@Composable
private fun RecordTodoListRow(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    tags: List<String>,
    completed: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None
                )
                if (tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(tags, key = { it }) { tag ->
                            TodoTagChip(tag)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordAnswerFields(
    template: RecordTemplate,
    answers: androidx.compose.runtime.snapshots.SnapshotStateMap<String, String>,
    onPolish: (String, String) -> Unit,
    polishing: Boolean,
    showPolishButton: Boolean
) {
    template.fields.forEach { field ->
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (field.required) "${field.title} *" else field.title,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (showPolishButton && field.id == "field-summary-draft") {
                    TextButton(
                        enabled = answers[field.id].orEmpty().isNotBlank() && !polishing,
                        onClick = { onPolish(field.id, answers[field.id].orEmpty()) }
                    ) {
                        Text(if (polishing) "润色中" else "AI 润色")
                    }
                }
            }
            when (field.type) {
                FieldType.SingleChoice -> field.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = answers[field.id] == option,
                            onClick = { answers[field.id] = option }
                        )
                        Text(option)
                    }
                }

                FieldType.ShortAnswer -> OutlinedTextField(
                    value = answers[field.id].orEmpty(),
                    onValueChange = { answers[field.id] = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                FieldType.Attachment -> OutlinedTextField(
                    value = answers[field.id].orEmpty(),
                    onValueChange = { answers[field.id] = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("粘贴语音/图片链接或文件路径") },
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class PendingRecord(
    val todoId: String?,
    val todoTitle: String,
    val startedAt: Long,
    val endedAt: Long,
    val focusMinutes: Int,
    val templateId: String
)

private data class TodoDraft(
    val title: String,
    val dueDate: String? = null,
    val tags: List<String> = emptyList(),
    val plannedPomodoros: Int = 1
) {
    fun toTodoItem(): TodoItem {
        return TodoItem(
            id = newId(),
            title = title.trim(),
            dueDate = dueDate,
            tags = tags,
            plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
        )
    }
}

private fun todoDraftKey(draft: TodoDraft): String {
    return todoIdentityKey(draft.title, draft.dueDate)
}

private fun todoItemKey(todo: TodoItem): String {
    return todoIdentityKey(todo.title, todo.dueDate)
}

private fun todoIdentityKey(title: String, dueDate: String?): String {
    return "${title.trim().lowercase(Locale.getDefault())}|${dueDate.orEmpty()}"
}

private fun List<TodoDraft>.filterNewTodoDrafts(
    existingTodos: List<TodoItem>,
    handledKeys: Set<String>
): List<TodoDraft> {
    val existingKeys = existingTodos.map(::todoItemKey).toSet()
    return filter { draft ->
        val key = todoDraftKey(draft)
        key !in handledKeys && key !in existingKeys
    }
}

private data class TodoEditDraft(
    val id: String,
    val title: String? = null,
    val dueDate: String? = null,
    val tags: List<String>? = null,
    val plannedPomodoros: Int? = null,
    val completed: Boolean? = null
) {
    fun applyTo(todo: TodoItem): TodoItem {
        return todo.copy(
            title = title ?: todo.title,
            dueDate = dueDate ?: todo.dueDate,
            tags = tags ?: todo.tags,
            plannedPomodoros = plannedPomodoros ?: todo.plannedPomodoros,
            completed = completed ?: todo.completed
        )
    }

    fun previewTitle(original: TodoItem?): String {
        return title ?: original?.title ?: "待办更新"
    }

    fun toPreviewDraft(original: TodoItem?): TodoDraft {
        return TodoDraft(
            title = previewTitle(original),
            dueDate = dueDate ?: original?.dueDate,
            tags = tags ?: original?.tags.orEmpty(),
            plannedPomodoros = plannedPomodoros ?: original?.plannedPomodoros ?: 1
        )
    }
}

private fun todoEditDraftKey(edit: TodoEditDraft): String {
    return listOf(
        edit.id,
        edit.title.orEmpty(),
        edit.dueDate.orEmpty(),
        edit.tags?.joinToString(",").orEmpty(),
        edit.plannedPomodoros?.toString().orEmpty(),
        edit.completed?.toString().orEmpty()
    ).joinToString("|").lowercase(Locale.getDefault())
}

private fun List<TodoEditDraft>.filterNewTodoEdits(
    existingTodos: List<TodoItem>,
    handledKeys: Set<String>
): List<TodoEditDraft> {
    val todosById = existingTodos.associateBy { it.id }
    return filter { edit ->
        val original = todosById[edit.id] ?: return@filter false
        val updated = edit.applyTo(original)
        todoEditDraftKey(edit) !in handledKeys && updated != original
    }
}

private data class TodoPlanDraft(
    val action: String,
    val objectText: String,
    val dueDate: String? = null,
    val tags: List<String> = emptyList(),
    val plannedPomodoros: Int = 1
) {
    fun toTodoDraft(): TodoDraft {
        val title = if (action.isNotBlank() && objectText.isNotBlank()) {
            "$action$objectText".normalizeTodoPhrase().take(40)
        } else {
            (objectText.ifBlank { action }).normalizeTodoTitle()
        }
        return TodoDraft(
            title = title,
            dueDate = dueDate,
            tags = tags.ifEmpty { inferTodoTags(title) },
            plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
        )
    }
}

private fun AppState.withNextRevisionFrom(previous: AppState): AppState {
    val normalizedNext = copy(projectRevision = previous.projectRevision)
    return if (normalizedNext == previous) {
        normalizedNext
    } else {
        normalizedNext.copy(projectRevision = previous.projectRevision + 1)
    }
}

private fun TimerMode.label(): String = when (this) {
    TimerMode.Focus -> "专注"
    TimerMode.Break -> "休息"
}

private fun TimerMode.durationRange(): IntRange = when (this) {
    TimerMode.Focus -> 5..90
    TimerMode.Break -> 1..60
}

private fun FieldType.label(): String = when (this) {
    FieldType.SingleChoice -> "单选"
    FieldType.ShortAnswer -> "简答"
    FieldType.Attachment -> "附件"
}

private fun AgentDataPermissions.label(): String {
    val names = buildList {
        if (dailyRecords) add("每日记录")
        if (todos) add("待办")
        if (templates) add("模板")
    }
    return names.ifEmpty { listOf("无数据权限") }.joinToString("、")
}

private fun LlmProviderSettings.agentProviderLabel(): String {
    return "$name · ${modelName.ifBlank { "未选择模型" }}"
}

private fun LlmProviderSettings.isConfiguredForChat(): Boolean {
    return endpoint.isNotBlank() && modelName.isNotBlank() && apiKey.isNotBlank()
}

private fun selectUnifiedAgent(agents: List<AgentSettings>): AgentSettings? {
    val enabledAgents = agents.filter { it.enabled }
    return enabledAgents.firstOrNull { it.id == "agent-unified" }
        ?: enabledAgents.firstOrNull()
        ?: agents.firstOrNull()
}

private fun selectEncouragerAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

private fun selectDailySummaryAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

private fun selectTodoAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

private fun selectTemplateAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

private fun selectRecordAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

private fun String.withNickname(nickname: String): String {
    return replace("{nickname}", nickname.ifBlank { DEFAULT_NICKNAME })
}

private fun StringBuilder.appendAgentStylePrompt() {
    append("\n")
    append(AGENT_STYLE_PROMPT)
}

private fun List<AgentConversationMessage>.appendAgentMessage(
    role: String,
    content: String
): List<AgentConversationMessage> {
    return (this + AgentConversationMessage(
        id = newId(),
        role = role,
        content = content.trim()
    )).takeLast(80)
}

private fun String.looksLikeTodoPlanningRequest(): Boolean {
    val lowered = lowercase(Locale.getDefault())
    return listOf(
        "待办",
        "todo",
        "任务",
        "计划",
        "行程",
        "安排",
        "今天",
        "明天",
        "要做",
        "帮我拆"
    ).any { it in lowered }
}

private fun String.cloudProviderLabel(): String = when (this) {
    "jianguoyun" -> "坚果云"
    else -> "WebDev"
}

private fun String.syncEndpointPreset(): String = when (this) {
    "jianguoyun" -> "https://dav.jianguoyun.com/dav/"
    else -> ""
}

private fun localEncouragement(record: PendingRecord, nickname: String): String {
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    return "$name，你刚刚把 ${record.focusMinutes} 分钟真正交给了「${record.todoTitle}」。这不是一句轻飘飘的“不错”，而是一次清清楚楚的推进：你坐下来、守住了这一轮、让事情往前走了一步。先把这份完成感收下，然后用复盘写下一个最小下一步。你已经启动了惯性，接下来只需要顺着它再走一点。"
}

private suspend fun requestRecordPolish(
    rawText: String,
    pendingRecord: PendingRecord,
    template: RecordTemplate,
    answers: Map<String, String>,
    provider: LlmProviderSettings,
    nickname: String
): String {
    val systemPrompt = buildString {
        append("你是 Protato 的总结润色 Agent。你的任务是把用户刚填的草稿润色成更通顺、更清晰、更适合放进记录的中文总结。")
        append(" 你要提炼要点，合并重复表达，保持真实，不要添加没有出现过的事实。")
        append(" 输出必须是可直接替换的最终文本，不要解释过程，不要分点太碎。")
        append(" 语气温和、自然、简洁。")
    }
    val context = buildString {
        append("用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n")
        append("任务：${pendingRecord.todoTitle}\n")
        append("时长：${pendingRecord.focusMinutes} 分钟\n")
        append("模板：${template.name}\n")
        append("当前字段：\n")
        template.fields.forEach { field ->
            append("- ${field.title}：${answers[field.id].orEmpty()}\n")
        }
        append("待润色草稿：\n")
        append(rawText)
    }
    return requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", context)
        )
    )
}

private suspend fun requestUnifiedAgentChat(
    state: AppState,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): String {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你现在是 Protato 唯一的统一 Agent。你需要通过连续对话帮助用户确定一天行程、拆解待办、选择下一轮番茄对象，并在结束时总结。")
        append(" 你可以读取待办、今日番茄记录、复盘模板和长期记忆。")
        append(" 你可以按日期、标签、完成状态和关键词查询/筛选上下文中的待办，然后基于 todoId 提出编辑草案。")
        append(" 如果要新增待办，必须调用工具 propose_todos；用户确认后才会写入 TodoList，不要声称已经添加。")
        append(" 如果要修改已有待办，先根据上下文中的 todoId 精确定位，再调用工具 propose_todo_updates；用户确认后才会更新。")
        append("\n\n可用工具：")
        append("\npropose_todos：在对话中提出待办草案，由 App 展示确认/拒绝卡片。")
        append("\n调用格式必须放在独立 Markdown 代码块中：")
        append("\n```protato_tool")
        append("\n{\"tool\":\"propose_todos\",\"todos\":[{\"title\":\"明确可执行的待办\",\"dueDate\":\"yyyy-MM-dd 或 null\",\"tags\":[\"标签\"],\"plannedPomodoros\":1}]}")
        append("\n```")
        append("\npropose_todo_updates：提出已有待办的编辑草案。格式：")
        append("\n```protato_tool")
        append("\n{\"tool\":\"propose_todo_updates\",\"edits\":[{\"id\":\"todoId\",\"title\":\"新标题\",\"dueDate\":\"yyyy-MM-dd\",\"tags\":[\"标签\"],\"plannedPomodoros\":1,\"completed\":false}]}")
        append("\n```")
        append("\n同一次回复最多调用一次工具，todos/edits 最多 6 个；工具块之外可以用一句自然语言说明等待用户确认。")
        append(" 不要把本轮对话中已经确认添加、已经拒绝、TodoList 已存在或无需变化的待办再次放入工具调用。")
        append(" 回答要短、具体、可执行。")
        append("\n\n当前上下文：\n")
        append(state.toUnifiedAgentContext(nickname))
    }
    return requestLlmChat(
        provider = provider,
        messages = listOf(LlmChatMessage("system", systemPrompt)) +
            state.agentMessages.takeLast(16).map { LlmChatMessage(it.role, it.content) }
    )
}

private data class AgentToolResponse(
    val visibleReply: String,
    val todoDrafts: List<TodoDraft>,
    val todoEdits: List<TodoEditDraft>
)

private fun parseAgentToolResponse(rawReply: String): AgentToolResponse {
    val toolBlockRegex = Regex(
        pattern = "```\\s*protato_tool\\s*\\n(.*?)\\n```",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    val todoDrafts = toolBlockRegex.findAll(rawReply)
        .flatMap { match -> parseAgentTodoToolCall(match.groupValues[1]).asSequence() }
        .toList()
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
        .take(6)
    val todoEdits = toolBlockRegex.findAll(rawReply)
        .flatMap { match -> parseAgentTodoEditToolCall(match.groupValues[1]).asSequence() }
        .toList()
        .distinctBy(::todoEditDraftKey)
        .take(6)
    val visibleReply = rawReply.replace(toolBlockRegex, "")
        .trim()
        .ifBlank {
            if (todoDrafts.isNotEmpty() || todoEdits.isNotEmpty()) {
                "我整理了待办草案，请确认是否应用。"
            } else {
                rawReply.trim()
            }
        }
    return AgentToolResponse(
        visibleReply = visibleReply,
        todoDrafts = todoDrafts,
        todoEdits = todoEdits
    )
}

private fun parseAgentTodoToolCall(rawJson: String): List<TodoDraft> {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        if (!root.optString("tool").equals("propose_todos", ignoreCase = true)) {
            return@runCatching emptyList()
        }
        val todos = root.optJSONArray("todos") ?: return@runCatching emptyList()
        buildList {
            for (index in 0 until todos.length()) {
                val item = todos.optJSONObject(index) ?: continue
                val title = item.optString("title")
                    .trim()
                    .take(40)
                if (title.isBlank()) continue
                val dueDate = normalizedTodoDate(item.optString("dueDate"))
                    ?: normalizedTodoDate(item.optString("date"))
                val plannedPomodoros = item.optInt(
                    "plannedPomodoros",
                    item.optInt("pomodoros", 1)
                )
                val tags = item.optJSONArray("tags")?.toStringList()
                    ?.flatMap(::parseTodoTags)
                    ?.distinctBy { it.lowercase(Locale.getDefault()) }
                    ?.take(5)
                    .orEmpty()
                add(
                    TodoDraft(
                        title = title,
                        dueDate = dueDate,
                        tags = tags.ifEmpty { inferTodoTags(title) },
                        plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

private fun parseAgentTodoEditToolCall(rawJson: String): List<TodoEditDraft> {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        if (!root.optString("tool").equals("propose_todo_updates", ignoreCase = true)) {
            return@runCatching emptyList()
        }
        val edits = root.optJSONArray("edits") ?: return@runCatching emptyList()
        buildList {
            for (index in 0 until edits.length()) {
                val item = edits.optJSONObject(index) ?: continue
                val id = item.optString("id").trim()
                if (id.isBlank()) continue
                val title = item.optionalNonBlankString("title")?.take(40)
                val dueDate = item.optionalNonBlankString("dueDate")?.let(::normalizedTodoDate)
                val tags = item.optJSONArray("tags")?.toStringList()
                    ?.flatMap(::parseTodoTags)
                    ?.distinctBy { it.lowercase(Locale.getDefault()) }
                    ?.take(5)
                val plannedPomodoros = if (item.has("plannedPomodoros")) {
                    item.optInt("plannedPomodoros", 1).coerceIn(1, 24)
                } else {
                    null
                }
                val completed = if (item.has("completed")) item.optBoolean("completed") else null
                if (title == null && dueDate == null && tags == null && plannedPomodoros == null && completed == null) {
                    continue
                }
                add(
                    TodoEditDraft(
                        id = id,
                        title = title,
                        dueDate = dueDate,
                        tags = tags,
                        plannedPomodoros = plannedPomodoros,
                        completed = completed
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

private fun JSONArray.toStringList(): List<String> {
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index).trim()
            if (value.isNotBlank()) add(value)
        }
    }
}

private fun JSONObject.optionalNonBlankString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).trim().takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

private fun localUnifiedAgentReply(
    input: String,
    state: AppState,
    nickname: String
): String {
    val today = dayKey(System.currentTimeMillis())
    val todayTodos = state.todos.filter { it.dueDate == today && !it.completed }
    val todayRecords = state.records.filter { dayKey(it.endedAt) == today }
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    return when {
        "总结" in input -> {
            val minutes = todayRecords.sumOf { it.focusMinutes }
            "$name，今天目前有 ${todayRecords.size} 个番茄、$minutes 分钟专注记录。可以点上面的“生成今日总结”，我会把记录沉淀成一段正式复盘。"
        }
        input.looksLikeTodoPlanningRequest() -> {
            val count = parseTodoSuggestions(input).size
            if (count > 0) {
                "我先把你的想法整理成 $count 个待办草案了。看一下下方卡片，确认后我再写入 TodoList。"
            } else {
                "可以。先把今天最想推进的事情用一两句话丢给我，我会拆成可执行待办，再由你确认添加。"
            }
        }
        todayTodos.isNotEmpty() -> {
            "我在。今天还有 ${todayTodos.size} 个未完成待办。建议先选一个最小、最清楚的任务开一轮番茄；结束后我会继续帮你复盘和收束。"
        }
        else -> {
            "我在。你可以先告诉我今天想完成什么，我会帮你排成待办草案，再陪你用番茄钟一步步推进。"
        }
    }
}

private fun AppState.toUnifiedAgentContext(nickname: String): String {
    val today = dayKey(System.currentTimeMillis())
    val todayTodos = todos.filter { it.dueDate == today }
    val todayRecords = records.filter { dayKey(it.endedAt) == today }
    val historyCutoff = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
    val historicalTodos = todos
        .filter { it.createdAt >= historyCutoff || it.dueDate != null }
        .sortedWith(compareByDescending<TodoItem> { it.dueDate ?: "" }.thenByDescending { it.createdAt })
        .take(180)
    val historicalRecords = records
        .filter { it.endedAt >= historyCutoff }
        .sortedByDescending { it.endedAt }
        .take(120)
    val todoText = todayTodos.take(12).joinToString("\n") { todo ->
        "- id=${todo.id} [${if (todo.completed) "x" else " "}] ${todo.title}（日期：${todo.dueDate ?: "无"}，${todo.plannedPomodoros} 个番茄，标签：${todo.tags.joinToString("、").ifBlank { "无" }}）"
    }.ifBlank { "无" }
    val recordText = todayRecords.takeLast(8).joinToString("\n") { record ->
        "- ${formatClockTime(record.endedAt)} ${record.todoTitle}，${record.focusMinutes} 分钟"
    }.ifBlank { "无" }
    val templateText = templates.take(6).joinToString("、") { it.name }.ifBlank { "无" }
    val historyTodoText = historicalTodos.joinToString("\n") { todo ->
        val status = if (todo.completed) "已完成" else "未完成"
        val date = todo.dueDate ?: dayKey(todo.createdAt)
        "- id=${todo.id} $date [$status] ${todo.title}（${todo.tags.joinToString("、").ifBlank { "无标签" }}，${todo.plannedPomodoros} 个番茄）"
    }.ifBlank { "无" }
    val historyRecordText = historicalRecords.joinToString("\n") { record ->
        "- ${dayKey(record.endedAt)} ${record.todoTitle}，${record.focusMinutes} 分钟"
    }.ifBlank { "无" }
    return buildString {
        append("用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n")
        append("今天日期：$today\n")
        append("今日待办：\n$todoText\n")
        append("今日番茄记录：\n$recordText\n")
        append("近 365 天待办历史（用于识别同类事项和行程关联）：\n$historyTodoText\n")
        append("近 365 天番茄记录摘要：\n$historyRecordText\n")
        append("可用复盘模板：$templateText\n")
        append("长期记忆：${agentMemory.ifBlank { "暂无" }}")
    }
}

private suspend fun generateDailySummary(
    dayKey: String,
    records: List<PomodoroRecord>,
    templates: List<RecordTemplate>,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String
): DailySummary {
    val fallback = localDailySummary(dayKey, records, nickname)
    val agent = selectDailySummaryAgent(agents) ?: return fallback
    val provider = agent.providerId.let { providerId ->
        providers.firstOrNull { it.id == providerId }
    } ?: providers.firstOrNull { it.isConfiguredForChat() } ?: return fallback
    if (!provider.isConfiguredForChat()) return fallback
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的每日番茄总结 Agent。你要把当天番茄记录汇总成一篇自然、清晰、有洞察的中文总结文章。")
        append(" 必须包含：番茄数量、总专注时长、专注对象/次数、今天状态评价、一个具体改进建议，以及最后真诚有力的鼓励。")
        append(" 请使用 Markdown 输出，可以使用二级标题、列表和加粗。")
        append(" 不要编造记录之外的事实。")
    }
    val userPrompt = buildString {
        append("日期：$dayKey\n")
        append("用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n")
        append(records.toDailySummaryContext(templates))
        append("\n请写成 500 字以内的 Markdown 总结。")
    }
    val content = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", userPrompt)
        )
    )
    return fallback.copy(
        generatedAt = System.currentTimeMillis(),
        content = content.ifBlank { fallback.content }
    )
}

private fun localDailySummary(
    dayKey: String,
    records: List<PomodoroRecord>,
    nickname: String
): DailySummary {
    val totalMinutes = records.sumOf { it.focusMinutes }
    val focusTargets = records.groupingBy { it.todoTitle }.eachCount()
    val topTargets = focusTargets.entries.sortedByDescending { it.value }.take(3)
        .joinToString("、") { "${it.key} ${it.value} 次" }
        .ifBlank { "暂无明确对象" }
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    val content = if (records.isEmpty()) {
        """
        ## 🌱 今天还没有番茄记录

        $name，没关系。总结不是为了责备你，而是为了帮你重新看见下一步。

        - 先选一个足够小的任务
        - 完成一轮就好
        - 把第一条记录留在时间轴上
        """.trimIndent()
    } else {
        """
        ## 🍅 今天的专注轨迹

        $name，今天你完成了 **${records.size} 个番茄**，总专注 **$totalMinutes 分钟**。

        - 主要投入：$topTargets
        - 节奏观察：你已经把时间放进了具体事情里，而不只是停在“想做”
        - 下一步：复盘哪一轮最顺、哪一轮最容易分心，再把下一轮番茄安排给最重要的小目标

        > 请认真收下这份完成感。每一个番茄都不大，但它们合在一起，就是你把生活重新握回手里的证据。✨
        """.trimIndent()
    }
    return DailySummary(
        dayKey = dayKey,
        generatedAt = System.currentTimeMillis(),
        title = "$dayKey 今日番茄总结",
        content = content,
        pomodoroCount = records.size,
        focusMinutes = totalMinutes
    )
}

private suspend fun generateTodoSuggestions(
    rawInput: String,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): List<TodoDraft> {
    val today = dayKey(System.currentTimeMillis())
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的待办整理助手。把用户随口说出的计划整理成短小、明确、可执行的 Todo。")
        append(" 今天日期是 $today。")
        append(" 只输出待办列表，每行一个，格式严格为：任务标题 | yyyy-MM-dd 或 空 | 标签1,标签2 | 番茄数。")
        append(" 日期要根据今天、明天、周末、具体日期等信息推断；没有日期就留空。")
        append(" 标签要自动选择 1-3 个短标签，例如 工作、学习、生活、健康、沟通、复盘。不要输出解释。")
        append(" 番茄数是预计专注轮数，通常 1-6。")
    }
    val response = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", rawInput)
        )
    )
    return parseTodoSuggestions(response).ifEmpty { parseTodoSuggestions(rawInput) }
}

private suspend fun generateAgentTodoDrafts(
    rawInput: String,
    state: AppState,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String,
    planningMode: Boolean = false
): List<TodoDraft> {
    val today = dayKey(System.currentTimeMillis())
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的任务规划器。你要用 ReAct 风格在内部完成：Observation=理解用户真实意图；Decision=判断任务边界，合并同一件事的碎片；Action=输出可执行待办草案。")
        append(" 不要暴露 Observation 或 Decision，不要解释。")
        append(" 今天日期是 $today。")
        append(" 只返回结构化行，每行一个任务：TASK | 动词 | 对象/结果 | yyyy-MM-dd 或 空 | 标签1,标签2 | 番茄数。")
        append(" 原则：一件事只输出一行；不要照抄口语原文；标题必须读起来通顺，像“整理项目周报材料”“给客户回复报价邮件”。")
        append(" 如果用户只是描述一个目标，先收束成一个最小可执行动作。")
        if (planningMode) {
            append(" 当前处于计划模式：如果用户给出大型事件或目标，要拆成 2-6 个能分别用番茄钟推进的小待办。")
        }
        append(" 如果输入里有因果、补充说明、背景，不要拆成独立任务。")
        append(" 标签 1-3 个，番茄数 1-6。无法形成待办就返回 NONE。")
        append("\n\n当前上下文：\n")
        append(state.toUnifiedAgentContext(nickname))
    }
    val response = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", rawInput)
        )
    )
    return parseTodoPlanDrafts(response)
        .map { it.toTodoDraft() }
        .ifEmpty { localPlanTodoDrafts(rawInput) }
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
        .take(8)
}

private suspend fun generateTemplateSuggestion(
    rawInput: String,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): RecordTemplate {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的番茄复盘模板生成助手。根据用户描述生成一个简洁、可执行的记录模板。")
        append(" 第一行格式严格为：模板名称 | 名称。")
        append(" 后续每行格式严格为：字段 | 标题 | short 或 single | required 或 optional | 选项1,选项2。")
        append(" short 表示简答，single 表示单选；single 必须给 2-5 个选项。不要输出解释。")
    }
    val response = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", rawInput)
        )
    )
    return parseTemplateSuggestion(response) ?: localTemplateSuggestion(rawInput)
}

private fun parseTemplateSuggestion(text: String): RecordTemplate? {
    val lines = text.lines()
        .map { it.trim().trim('-', '*', '•', ' ') }
        .filter { it.isNotBlank() }
    val name = lines.firstNotNullOfOrNull { line ->
        val parts = line.split("|").map { it.trim() }
        when {
            parts.size >= 2 && parts[0].contains("模板") -> parts[1]
            line.startsWith("模板名称") -> line.substringAfter("：", line.substringAfter(":")).trim()
            else -> null
        }
    }?.take(24)?.ifBlank { null } ?: return null
    val fields = lines.mapNotNull { line ->
        val parts = line.split("|").map { it.trim() }
        if (parts.size < 4 || !parts[0].contains("字段")) return@mapNotNull null
        val type = when (parts.getOrNull(2)?.lowercase(Locale.getDefault())) {
            "single", "单选" -> FieldType.SingleChoice
            else -> FieldType.ShortAnswer
        }
        val options = if (type == FieldType.SingleChoice) {
            parseTodoTags(parts.getOrNull(4).orEmpty()).take(5)
        } else {
            emptyList()
        }
        if (type == FieldType.SingleChoice && options.size < 2) return@mapNotNull null
        TemplateField(
            id = newId(),
            title = parts[1].take(40),
            type = type,
            options = options,
            required = parts.getOrNull(3)?.contains("required", ignoreCase = true) == true ||
                parts.getOrNull(3)?.contains("必填") == true
        )
    }.filter { it.title.isNotBlank() }.take(8)
    return if (fields.isEmpty()) null else RecordTemplate(
        id = newId(),
        name = name,
        fields = fields
    )
}

private fun localTemplateSuggestion(rawInput: String): RecordTemplate {
    val topic = rawInput.lineSequence().firstOrNull { it.isNotBlank() }
        ?.cleanTodoTitle()
        ?.take(12)
        ?.ifBlank { null }
        ?: "自定义复盘"
    return RecordTemplate(
        id = newId(),
        name = "${topic}模板",
        fields = listOf(
            TemplateField(
                id = newId(),
                title = "这轮完成了什么",
                type = FieldType.ShortAnswer,
                required = true
            ),
            TemplateField(
                id = newId(),
                title = "专注状态",
                type = FieldType.SingleChoice,
                options = listOf("顺畅", "一般", "分心", "被打断"),
                required = true
            ),
            TemplateField(
                id = newId(),
                title = "下一步行动",
                type = FieldType.ShortAnswer
            )
        )
    )
}

private fun parseTodoPlanDrafts(text: String): List<TodoPlanDraft> {
    if (text.trim().equals("NONE", ignoreCase = true)) return emptyList()
    return text.lines()
        .map { it.trim().trim('-', '*', '•', ' ') }
        .filter { it.startsWith("TASK", ignoreCase = true) }
        .mapNotNull { line ->
            val parts = line.split("|").map { it.trim() }
            if (parts.size < 6) return@mapNotNull null
            val action = parts[1].normalizeTodoPhrase()
            val objectText = parts[2].normalizeTodoPhrase()
            val title = listOf(action, objectText).joinToString(" ").normalizeTodoTitle()
            if (title.isBlank()) return@mapNotNull null
            TodoPlanDraft(
                action = action.ifBlank { inferTodoAction(objectText) },
                objectText = objectText,
                dueDate = normalizedTodoDate(parts[3]) ?: inferTodoDate(line),
                tags = parseTodoTags(parts[4]).ifEmpty { inferTodoTags(title) },
                plannedPomodoros = parsePomodoroCount(parts[5]) ?: inferPlannedPomodoros(title)
            )
        }
        .filter { it.toTodoDraft().title.isNotBlank() }
        .distinctBy { it.toTodoDraft().title.lowercase(Locale.getDefault()) }
}

private fun localPlanTodoDrafts(rawInput: String): List<TodoDraft> {
    val candidates = rawInput
        .split(Regex("[\\n；;]+"))
        .flatMap { splitLooseTodoSentence(it) }
        .map { it.normalizeTodoTitle() }
        .filter { it.isNotBlank() }
        .map { title ->
            TodoDraft(
                title = title,
                dueDate = inferTodoDate(rawInput),
                tags = inferTodoTags(title),
                plannedPomodoros = parsePomodoroCount(rawInput) ?: inferPlannedPomodoros(title)
            )
        }
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
    return candidates.ifEmpty {
        val title = rawInput.normalizeTodoTitle()
        if (title.isBlank()) emptyList() else listOf(
            TodoDraft(
                title = title,
                dueDate = inferTodoDate(rawInput),
                tags = inferTodoTags(title),
                plannedPomodoros = parsePomodoroCount(rawInput) ?: inferPlannedPomodoros(title)
            )
        )
    }.take(8)
}

private fun splitLooseTodoSentence(value: String): List<String> {
    val normalized = value
        .replace(Regex("[，,。]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    if (normalized.isBlank()) return emptyList()
    val strongSplit = Regex("(?:^|\\s)(?:另外|还有|以及|再加上)(?:\\s|$)")
    val parts = normalized.split(strongSplit)
        .map { it.trim() }
        .filter { it.isNotBlank() }
    return parts.ifEmpty { listOf(normalized) }
}

private fun parseTodoSuggestions(text: String): List<TodoDraft> {
    return text
        .split("\n", "；", ";", "。")
        .map {
            it.trim()
                .replace(Regex("^[-*•\\d.、\\s]+"), "")
                .trim()
        }
        .filter { it.isNotBlank() }
        .map { parseTodoDraftLine(it) }
        .filter { it.title.isNotBlank() }
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
        .take(12)
}

private fun parseTodoDraftLine(line: String): TodoDraft {
    val structuredParts = line.split("|").map { it.trim() }
    if (structuredParts.size >= 3) {
        val title = structuredParts[0].cleanTodoTitle()
        val dueDate = normalizedTodoDate(structuredParts[1])
        val tags = parseTodoTags(structuredParts[2])
            .ifEmpty { inferTodoTags(title) }
        val plannedPomodoros = structuredParts.getOrNull(3)?.let(::parsePomodoroCount)
            ?: inferPlannedPomodoros(title)
        return TodoDraft(
            title = title,
            dueDate = dueDate ?: inferTodoDate(line),
            tags = tags,
            plannedPomodoros = plannedPomodoros
        )
    }

    val tags = extractTodoHashTags(line)
    val title = line
        .replace(Regex("#[\\p{L}\\p{N}_-]+"), "")
        .cleanTodoTitle()
    return TodoDraft(
        title = title,
        dueDate = inferTodoDate(line),
        tags = tags.ifEmpty { inferTodoTags(title) },
        plannedPomodoros = parsePomodoroCount(line) ?: inferPlannedPomodoros(title)
    )
}

private fun String.cleanTodoTitle(): String {
    return normalizeTodoTitle()
}

private fun String.normalizeTodoTitle(): String {
    val cleaned = replace(Regex("\\|.*$"), "")
        .replace(Regex("\\d{4}-\\d{2}-\\d{2}"), "")
        .replace(Regex("(?:今天|明天|后天|周末|今晚|上午|下午|晚上)(?:要|想|需要|准备)?"), "")
        .replace(Regex("[#＃][\\p{L}\\p{N}_-]+"), "")
        .replace(Regex("^(帮我|我想|我要|需要|准备|计划|安排|待办|任务|todo)[:：、\\s]*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*(因为|所以|但是|然后|顺便)\\s*"), " ")
        .replace(Regex("\\s+"), " ")
        .replace(Regex("^[,，。；;、\\s]+|[,，。；;、\\s]+$"), "")
        .trim()
    return cleaned.toReadableTodoTitle().take(40)
}

private fun String.normalizeTodoPhrase(): String {
    return replace(Regex("^(帮我|我想|我要|需要|准备|计划|安排)[:：、\\s]*"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(24)
}

private fun String.toReadableTodoTitle(): String {
    if (isBlank()) return ""
    val trimmed = trim()
    val hasKnownVerb = listOf(
        "整理",
        "完成",
        "回复",
        "检查",
        "阅读",
        "学习",
        "复盘",
        "写",
        "改",
        "做",
        "准备",
        "沟通",
        "确认",
        "提交",
        "设计",
        "开发"
    ).any { trimmed.startsWith(it) }
    return if (hasKnownVerb) trimmed else "${inferTodoAction(trimmed)}$trimmed"
}

private fun inferTodoAction(text: String): String {
    val lowered = text.lowercase(Locale.getDefault())
    return when {
        listOf("邮件", "消息", "客户", "沟通").any { it in lowered } -> "回复"
        listOf("文档", "资料", "材料", "报告", "周报").any { it in lowered } -> "整理"
        listOf("代码", "功能", "bug", "接口", "页面").any { it in lowered } -> "开发"
        listOf("课程", "书", "文章", "论文").any { it in lowered } -> "阅读"
        listOf("复盘", "总结").any { it in lowered } -> "完成"
        else -> "完成"
    }
}

private fun extractTodoHashTags(value: String): List<String> {
    return Regex("[#＃]([\\p{L}\\p{N}_-]+)")
        .findAll(value)
        .map { it.groupValues[1].take(24) }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.getDefault()) }
        .toList()
}

private fun inferTodoDate(text: String): String? {
    val todayMillis = System.currentTimeMillis()
    return when {
        "后天" in text -> dayKey(todayMillis + 2 * 24 * 60 * 60 * 1000L)
        "明天" in text -> dayKey(todayMillis + 24 * 60 * 60 * 1000L)
        "今天" in text || "今晚" in text -> dayKey(todayMillis)
        "周末" in text -> nextWeekendDayKey(todayMillis)
        else -> Regex("\\d{4}-\\d{2}-\\d{2}").find(text)?.value?.let(::normalizedTodoDate)
            ?: inferMonthDay(text)
            ?: inferWeekday(text)
    }
}

private fun inferMonthDay(text: String): String? {
    val match = Regex("(\\d{1,2})\\s*(?:月|/)(\\d{1,2})\\s*(?:日|号)?").find(text)
        ?: return null
    val month = match.groupValues[1].toIntOrNull() ?: return null
    val day = match.groupValues[2].toIntOrNull() ?: return null
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.MONTH, month - 1)
        set(java.util.Calendar.DAY_OF_MONTH, day)
        set(java.util.Calendar.HOUR_OF_DAY, 12)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        if (timeInMillis < System.currentTimeMillis() - 24 * 60 * 60 * 1000L) {
            add(java.util.Calendar.YEAR, 1)
        }
    }
    return dayKey(calendar.timeInMillis)
}

private fun inferWeekday(text: String): String? {
    val match = Regex("(下周|本周|这周)?(?:周|星期)([一二三四五六日天])").find(text)
        ?: return null
    val prefix = match.groupValues[1]
    val targetDay = when (match.groupValues[2]) {
        "一" -> java.util.Calendar.MONDAY
        "二" -> java.util.Calendar.TUESDAY
        "三" -> java.util.Calendar.WEDNESDAY
        "四" -> java.util.Calendar.THURSDAY
        "五" -> java.util.Calendar.FRIDAY
        "六" -> java.util.Calendar.SATURDAY
        else -> java.util.Calendar.SUNDAY
    }
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 12)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    var delta = (targetDay - currentDay + 7) % 7
    if (prefix == "下周") delta += 7
    if (prefix.isBlank() && delta == 0) delta = 7
    calendar.add(java.util.Calendar.DAY_OF_YEAR, delta)
    return dayKey(calendar.timeInMillis)
}

private fun nextWeekendDayKey(fromMillis: Long): String {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = fromMillis
    }
    while (calendar.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SATURDAY) {
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    return dayKey(calendar.timeInMillis)
}

private fun inferTodoTags(title: String): List<String> {
    val lowered = title.lowercase(Locale.getDefault())
    val tag = when {
        listOf("会议", "方案", "项目", "文档", "汇报", "邮件").any { it in lowered } -> "工作"
        listOf("学习", "阅读", "课程", "复习", "练习").any { it in lowered } -> "学习"
        listOf("运动", "跑步", "健身", "体检", "睡觉").any { it in lowered } -> "健康"
        listOf("买", "收拾", "打扫", "做饭", "缴费").any { it in lowered } -> "生活"
        listOf("复盘", "总结", "计划").any { it in lowered } -> "复盘"
        else -> "待办"
    }
    return listOf(tag)
}

private fun parsePomodoroCount(text: String): Int? {
    val trimmed = text.trim()
    trimmed.toIntOrNull()?.let { return it.coerceIn(1, 24) }
    return Regex("(\\d{1,2})\\s*(?:个|轮|次)?\\s*(?:番茄|pomodoro)", RegexOption.IGNORE_CASE)
        .find(text)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(1, 24)
}

private fun inferPlannedPomodoros(title: String): Int {
    val lowered = title.lowercase(Locale.getDefault())
    return when {
        listOf("方案", "报告", "论文", "开发", "重构", "复习", "整理资料").any { it in lowered } -> 4
        listOf("文档", "课程", "练习", "设计", "总结").any { it in lowered } -> 3
        listOf("会议", "阅读", "沟通", "邮件").any { it in lowered } -> 2
        else -> 1
    }
}

private suspend fun generateAiEncouragement(
    pendingRecord: PendingRecord,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): String {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你现在要在用户完成一轮番茄后给出鼓励。必须具体、真诚、有力量，不要空泛鸡汤，不要夸张，不要说教。")
        append(" 需要点名用户刚完成的任务和时长，并给一个很小、很容易继续执行的下一步。")
    }
    val userPrompt = "用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n任务：${pendingRecord.todoTitle}\n专注时长：${pendingRecord.focusMinutes} 分钟\n请给一段 80-140 字中文鼓励。"
    return requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", userPrompt)
        )
    )
}

private suspend fun requestRecordChat(
    record: PomodoroRecord,
    template: RecordTemplate,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String,
    messages: List<LlmChatMessage>
): String {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 里的记录对话 Agent。回答要具体、可执行、温和，不要编造不存在的数据。")
        if (agent.permissions.dailyRecords) {
            append("\n你可以读取当前这条番茄记录：\n")
            append(record.toPromptContext(template))
        } else {
            append("\n当前 Agent 没有每日记录权限，不能使用这条记录的具体内容，只能根据用户输入做一般建议。")
        }
        if (agent.permissions.todos) {
            append("\n你被允许讨论待办相关安排。")
        }
        if (agent.permissions.templates) {
            append("\n你被允许讨论记录模板和复盘字段。")
        }
    }
    return requestLlmChat(
        provider = provider,
        messages = listOf(LlmChatMessage("system", systemPrompt)) + messages.takeLast(12)
    )
}

private fun PomodoroRecord.toPromptContext(template: RecordTemplate): String {
    val answersText = answers.joinToString("\n") { answer ->
        val title = template.fields.firstOrNull { it.id == answer.fieldId }?.title ?: "字段"
        "- $title：${answer.value.ifBlank { "未填写" }}"
    }
    return buildString {
        append("任务：$todoTitle\n")
        append("专注时长：$focusMinutes 分钟\n")
        append("结束时间：${formatTime(endedAt)}\n")
        append("模板：$templateName\n")
        append("回答：\n")
        append(answersText.ifBlank { "无回答" })
    }
}

private fun List<PomodoroRecord>.toDailySummaryContext(templates: List<RecordTemplate>): String {
    val totalMinutes = sumOf { it.focusMinutes }
    val focusTargets = groupingBy { it.todoTitle }.eachCount()
        .entries
        .sortedByDescending { it.value }
        .joinToString("、") { "${it.key} ${it.value} 次" }
    val details = joinToString("\n\n") { record ->
        val template = templates.firstOrNull { it.id == record.templateId }
            ?: RecordTemplate(record.templateId, record.templateName, emptyList())
        record.toPromptContext(template)
    }
    return buildString {
        append("番茄数量：${size}\n")
        append("总专注时长：$totalMinutes 分钟\n")
        append("专注对象统计：${focusTargets.ifBlank { "无" }}\n\n")
        append("记录详情：\n")
        append(details.ifBlank { "无记录" })
    }
}

private fun hasMissingRequiredAnswers(
    template: RecordTemplate,
    answers: Map<String, String>
): Boolean {
    return template.fields.any { field ->
        field.required && answers[field.id].orEmpty().isBlank()
    }
}

fun Int.asClock(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatTime(value: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(value))
}

private fun formatClockTime(value: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value))
}

private fun dayKey(value: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(value))
}

private fun utcDayKey(value: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(value))
}

private fun dateKeyToUtcMillis(key: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(key)?.time
    } catch (_: ParseException) {
        null
    }
}

private fun normalizedTodoDate(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank() || !trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return null
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        isLenient = false
    }
    return try {
        dayKey(parser.parse(trimmed)?.time ?: return null)
    } catch (_: ParseException) {
        null
    }
}

private fun parseTodoTags(value: String): List<String> {
    return value
        .split(Regex("[,，、\\s]+"))
        .map { it.trim().removePrefix("#").take(24) }
        .filter { tag ->
            tag.isNotBlank() && tag.lowercase(Locale.getDefault()) !in setOf("空", "无", "none", "null")
        }
        .distinctBy { it.lowercase(Locale.getDefault()) }
}

private fun todoTagColor(tag: String): Color {
    val colors = listOf(
        Color(0xFFE94F37),
        Color(0xFF2F9E44),
        Color(0xFF456990),
        Color(0xFF8E6C8A),
        Color(0xFFB7791F),
        Color(0xFF008891),
        Color(0xFF6A7FDB),
        Color(0xFFD1495B)
    )
    val index = kotlin.math.abs(tag.lowercase(Locale.getDefault()).hashCode()) % colors.size
    return colors[index]
}

private fun formatDayLabel(key: String): String {
    val today = dayKey(System.currentTimeMillis())
    if (key == today) return "今天"
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatter = SimpleDateFormat("MM月dd日", Locale.getDefault())
    return try {
        formatter.format(parser.parse(key) ?: return key)
    } catch (_: ParseException) {
        key
    }
}

private fun emptyTemplate(): RecordTemplate {
    return RecordTemplate(
        id = newId(),
        name = "新模板",
        fields = emptyList()
    )
}

private fun PendingPomodoroRecord.toPendingRecordUi(): PendingRecord {
    return PendingRecord(
        todoId = todoId,
        todoTitle = todoTitle,
        startedAt = startedAt,
        endedAt = endedAt,
        focusMinutes = focusMinutes,
        templateId = templateId
    )
}

private fun completeSession(state: AppState): AppState {
    val session = state.activeSession ?: return state
    val pendingRecord = if (session.mode == TimerMode.Focus) {
        PendingPomodoroRecord(
            todoId = session.todoId,
            todoTitle = session.todoTitle,
            startedAt = session.startedAt,
            endedAt = session.endsAt,
            focusMinutes = session.totalSeconds / 60,
            templateId = session.templateId
        )
    } else {
        null
    }
    return state.copy(activeSession = null, pendingRecord = pendingRecord)
}

private fun Context.startPomodoroService(action: String) {
    val intent = Intent(this, PomodoroTimerService::class.java).setAction(action)
    if (action == ACTION_START_TIMER_SERVICE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(this, intent)
    } else {
        startService(intent)
    }
}

private suspend fun fetchLlmModels(endpoint: String, apiKey: String): List<String> {
    return withContext(Dispatchers.IO) {
        val connection = (URL(normalizeModelsEndpoint(endpoint)).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            if (apiKey.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $apiKey")
            }
        }
        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (responseCode !in 200..299) {
                throw IllegalStateException("获取模型失败：HTTP $responseCode")
            }
            parseModelNames(body).ifEmpty {
                throw IllegalStateException("接口未返回可选择模型")
            }
        } finally {
            connection.disconnect()
        }
    }
}

private suspend fun syncWebDevState(state: AppState) {
    val settings = state.webDevSync
    if (!settings.enabled || settings.endpoint.isBlank()) {
        throw IllegalStateException("请先在设置里启用 WebDev 同步并填写接口地址")
    }
    return withContext(Dispatchers.IO) {
        val connection = (URL(settings.endpoint.trim()).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            if (settings.authToken.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer ${settings.authToken}")
            }
        }
        try {
            connection.outputStream.use { stream ->
                stream.write(state.toWebDevSyncJson().toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("同步失败：HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
}

private fun AppState.toWebDevSyncJson(): JSONObject {
    return JSONObject()
        .put("schema", "protato.webdev.sync.v1")
        .put("projectRevision", projectRevision)
        .put("nickname", nickname)
        .put("cloudProvider", webDevSync.cloudProvider)
        .put("syncedAt", System.currentTimeMillis())
        .put("agentMemory", agentMemory)
        .put("agentMessages", agentMessages.toJsonArrayForSync { message ->
            JSONObject()
                .put("id", message.id)
                .put("role", message.role)
                .put("content", message.content)
                .put("createdAt", message.createdAt)
        })
        .put("todos", todos.toJsonArrayForSync { todo ->
            JSONObject()
                .put("id", todo.id)
                .put("title", todo.title)
                .put("completed", todo.completed)
                .put("createdAt", todo.createdAt)
                .put("tags", todo.tags.toJsonArrayForSync { it })
                .put("dueDate", todo.dueDate ?: "")
                .put("plannedPomodoros", todo.plannedPomodoros)
        })
        .put("records", records.toJsonArrayForSync { record ->
            JSONObject()
                .put("id", record.id)
                .put("todoId", record.todoId ?: "")
                .put("todoTitle", record.todoTitle)
                .put("templateName", record.templateName)
                .put("startedAt", record.startedAt)
                .put("endedAt", record.endedAt)
                .put("focusMinutes", record.focusMinutes)
                .put("answers", record.answers.toJsonArrayForSync { answer ->
                    JSONObject()
                        .put("fieldId", answer.fieldId)
                        .put("value", answer.value)
                })
        })
        .put("dailySummaries", dailySummaries.toJsonArrayForSync { summary ->
            JSONObject()
                .put("dayKey", summary.dayKey)
                .put("generatedAt", summary.generatedAt)
                .put("title", summary.title)
                .put("content", summary.content)
                .put("pomodoroCount", summary.pomodoroCount)
                .put("focusMinutes", summary.focusMinutes)
        })
}

private inline fun <T> List<T>.toJsonArrayForSync(transform: (T) -> Any): JSONArray {
    val array = JSONArray()
    forEach { array.put(transform(it)) }
    return array
}

private suspend fun requestLlmChat(
    provider: LlmProviderSettings,
    messages: List<LlmChatMessage>
): String {
    return withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("model", provider.modelName)
            .put("temperature", 0.8)
            .put(
                "messages",
                JSONArray().apply {
                    messages.forEach { message ->
                        put(
                            JSONObject()
                                .put("role", message.role)
                                .put("content", message.content)
                        )
                    }
                }
            )
        val connection = (URL(normalizeChatEndpoint(provider.endpoint)).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        }
        try {
            connection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (responseCode !in 200..299) {
                throw IllegalStateException("AI 请求失败：HTTP $responseCode")
            }
            parseChatContent(response).ifBlank {
                throw IllegalStateException("AI 没有返回内容")
            }
        } finally {
            connection.disconnect()
        }
    }
}

private fun normalizeModelsEndpoint(endpoint: String): String {
    val trimmed = endpoint.trim().trimEnd('/')
    return when {
        trimmed.endsWith("/models") -> trimmed
        trimmed.endsWith("/v1") -> "$trimmed/models"
        else -> "$trimmed/v1/models"
    }
}

private fun normalizeChatEndpoint(endpoint: String): String {
    val trimmed = endpoint.trim().trimEnd('/')
    return when {
        trimmed.endsWith("/chat/completions") -> trimmed
        trimmed.endsWith("/v1") -> "$trimmed/chat/completions"
        else -> "$trimmed/v1/chat/completions"
    }
}

private fun parseModelNames(body: String): List<String> {
    val root = body.trim()
    val models = if (root.startsWith("[")) {
        JSONArray(root)
    } else {
        JSONObject(root).optJSONArray("data") ?: JSONArray()
    }
    return List(models.length()) { index ->
        val item = models.opt(index)
        when (item) {
            is JSONObject -> item.optString("id").ifBlank { item.optString("name") }
            is String -> item
            else -> ""
        }
    }.filter { it.isNotBlank() }.distinct()
}

private fun parseChatContent(body: String): String {
    val root = JSONObject(body)
    val choice = root.optJSONArray("choices")?.optJSONObject(0) ?: return ""
    val messageContent = choice.optJSONObject("message")?.optString("content").orEmpty()
    return messageContent.ifBlank { choice.optString("text") }.trim()
}

private fun newId(): String = UUID.randomUUID().toString()
