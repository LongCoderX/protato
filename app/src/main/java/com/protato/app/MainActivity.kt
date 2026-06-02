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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

    val selectedTodo = appState.todos.firstOrNull { it.id == selectedTodoId }
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

    ProtatoTheme {
        var currentTab by rememberSaveable { mutableStateOf(MainTab.Focus) }
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
                        onAddMany = { titles ->
                            val newTodos = titles
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .distinct()
                                .map { TodoItem(id = newId(), title = it) }
                            if (newTodos.isNotEmpty()) {
                                setAppState(appState.copy(todos = newTodos + appState.todos))
                            }
                        }
                    )

                    MainTab.Templates -> TemplateScreen(
                        templates = appState.templates,
                        selectedTemplateId = appState.selectedTemplateId,
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
private fun ProtatoTheme(content: @Composable () -> Unit) {
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
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
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
                                Text(
                                    text = remainingSeconds.asClock(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(enabled = !isRunning) {
                                        editingDuration = true
                                    }
                                )
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

    if (choosingTodo) {
        TodoSelectDialog(
            todos = todos.filterNot { it.completed },
            selectedTodoId = selectedTodoId,
            onDismiss = { choosingTodo = false },
            onSelected = { todoId ->
                onSelectedTodo(todoId)
                choosingTodo = false
            }
        )
    }

    if (choosingTemplate) {
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
                        Text("还没有未完成待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onAddMany: (List<String>) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var tagsText by rememberSaveable { mutableStateOf("") }
    var dueDateText by rememberSaveable { mutableStateOf("") }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var showingAiTodo by remember { mutableStateOf(false) }
    var selectedTodoIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    val selecting = selectedTodoIds.isNotEmpty()
    val availableTags = remember(todos) {
        todos.flatMap { it.tags }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sorted()
    }
    val visibleTodos = remember(todos, selectedTag) {
        selectedTag?.let { tag ->
            todos.filter { todo -> todo.tags.any { it.equals(tag, ignoreCase = true) } }
        } ?: todos
    }
    val normalizedDueDate = normalizedTodoDate(dueDateText)
    val dateIsValid = dueDateText.isBlank() || normalizedDueDate != null

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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("新待办") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        FilledIconButton(
                            onClick = {
                                if (title.isNotBlank() && dateIsValid) {
                                    onAdd(
                                        TodoItem(
                                            id = newId(),
                                            title = title.trim(),
                                            tags = parseTodoTags(tagsText),
                                            dueDate = normalizedDueDate
                                        )
                                    )
                                    title = ""
                                    tagsText = ""
                                    dueDateText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "添加")
                        }
                        OutlinedButton(onClick = { showingAiTodo = true }) {
                            Text("AI")
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dueDateText,
                            onValueChange = { dueDateText = it.filter { char -> char.isDigit() || char == '-' }.take(10) },
                            modifier = Modifier.weight(0.9f),
                            label = { Text("日期 yyyy-MM-dd") },
                            singleLine = true,
                            isError = !dateIsValid,
                            leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null) }
                        )
                        OutlinedTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it.take(120) },
                            modifier = Modifier.weight(1.1f),
                            label = { Text("标签") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) }
                        )
                    }
                }
            }
            if (availableTags.isNotEmpty()) {
                TodoTagFilterBar(
                    tags = availableTags,
                    selectedTag = selectedTag,
                    onSelected = { selectedTag = it }
                )
            }
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
                            "还没有待办，先添加一个明确的小目标。"
                        } else {
                            "这个标签下还没有待办。"
                        }
                    )
                }
            }
            items(visibleTodos, key = { it.id }) { todo ->
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

    editingTodo?.let { todo ->
        TodoEditorDialog(
            todo = todo,
            onDismiss = { editingTodo = null },
            onSave = { updatedTodo ->
                onUpdate(updatedTodo)
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
            onAddTodos = { titles ->
                onAddMany(titles)
                showingAiTodo = false
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
    onAddTodos: (List<String>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var rawInput by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
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
                        Text(
                            suggestion,
                            modifier = Modifier.padding(10.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun TodoTagFilterBar(
    tags: List<String>,
    selectedTag: String?,
    onSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedTag == null,
                onClick = { onSelected(null) },
                label = { Text("全部") }
            )
        }
        items(tags, key = { it }) { tag ->
            FilterChip(
                selected = selectedTag == tag,
                onClick = { onSelected(if (selectedTag == tag) null else tag) },
                label = { Text("#$tag") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Label,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
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
                        if (selecting) onToggleSelected()
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
                    TodoMetadataRow(todo = todo)
                }
            }
        }
    }
}

@Composable
private fun TodoMetadataRow(todo: TodoItem) {
    if (todo.dueDate == null && todo.tags.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        todo.dueDate?.let { dueDate ->
            item {
                TodoDateChip(dueDate)
            }
        }
        items(todo.tags, key = { it }) { tag ->
            TodoTagChip(tag)
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
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Icon(
            imageVector = if (deleting) Icons.Outlined.Delete else Icons.Outlined.Edit,
            contentDescription = if (deleting) "删除待办" else "编辑待办",
            tint = if (deleting || editing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
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
private fun TodoEditorDialog(
    todo: TodoItem,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit
) {
    var title by rememberSaveable(todo.id) { mutableStateOf(todo.title) }
    var tagsText by rememberSaveable(todo.id) { mutableStateOf(todo.tags.joinToString("，")) }
    var dueDateText by rememberSaveable(todo.id) { mutableStateOf(todo.dueDate.orEmpty()) }
    val normalizedDueDate = normalizedTodoDate(dueDateText)
    val dateIsValid = dueDateText.isBlank() || normalizedDueDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && dateIsValid,
                onClick = {
                    onSave(
                        todo.copy(
                            title = title.trim(),
                            tags = parseTodoTags(tagsText),
                            dueDate = normalizedDueDate
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
        title = { Text("编辑待办") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("待办内容") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it.filter { char -> char.isDigit() || char == '-' }.take(10) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("日期 yyyy-MM-dd") },
                    singleLine = true,
                    isError = !dateIsValid,
                    leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null) }
                )
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("标签，逗号分隔") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) }
                )
            }
        }
    )
}

private fun Set<String>.toggle(itemId: String): Set<String> {
    return if (itemId in this) this - itemId else this + itemId
}

@Composable
private fun TemplateScreen(
    templates: List<RecordTemplate>,
    selectedTemplateId: String,
    onSelectedTemplate: (String) -> Unit,
    onUpsert: (RecordTemplate) -> Unit,
    onDelete: (RecordTemplate) -> Unit
) {
    var editingTemplate by remember { mutableStateOf<RecordTemplate?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(title = "记录模板", subtitle = "自定义番茄结束后要回答的问题")
        }
        item {
            Button(onClick = { editingTemplate = emptyTemplate() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("新建模板")
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
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${template.fields.size} 个字段", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            template.fields.forEach { field ->
                Text(
                    text = "${field.title} · ${field.type.label()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                            (fieldType == FieldType.ShortAnswer || options.isNotEmpty())
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
    val todayKey = dayKey(System.currentTimeMillis())
    val availableDayKeys = remember(records, summaries, todayKey) {
        (records.map { dayKey(it.endedAt) } + summaries.map { it.dayKey } + todayKey)
            .distinct()
            .sortedDescending()
    }
    var selectedDayKey by rememberSaveable { mutableStateOf(todayKey) }
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
                    onSelected = { selectedDayKey = it }
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
    }
}

@Composable
private fun DaySwitcher(
    dayKeys: List<String>,
    selectedDayKey: String,
    records: List<PomodoroRecord>,
    onSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
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
                },
                leadingIcon = {
                    Text(if (key == dayKey(System.currentTimeMillis())) "☀️" else "📅")
                }
            )
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
                        "${formatDayLabel(dayKey)} 总结 Agent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${records.size} 次专注 · $focusMinutes 分钟 · $focusedTodos 个对象",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    enabled = summary != null,
                    onClick = onOpen
                ) {
                    Text("查看")
                }
            }
            OutlinedButton(
                enabled = records.isNotEmpty() && !loading,
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        loading -> "生成中"
                        summary == null -> "生成这天总结"
                        else -> "重新生成"
                    }
                )
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
    val isBoundToTodo = record.todoId != null
    val answerCount = record.answers.size

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
                    .background(
                        if (isFirst) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )
            )
            Surface(
                modifier = Modifier.size(18.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {}
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (isLast) 10.dp else 88.dp)
                    .background(
                        if (isLast) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )
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
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            formatClockTime(record.endedAt),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            record.todoTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RecordTodoChip(title = record.todoTitle, bound = isBoundToTodo)
                        }
                        Text(
                            "${record.focusMinutes} 分钟 · ${record.templateName} · $answerCount 条回答",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
private fun MarkdownText(content: String) {
    val lines = content.lines()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isBlank() -> Spacer(Modifier.height(2.dp))
                line.matches(Regex("-{3,}|\\*{3,}|_{3,}")) -> HorizontalDivider()
                line.startsWith("### ") -> MarkdownLine(
                    text = line.removePrefix("### "),
                    weight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                line.startsWith("## ") -> MarkdownLine(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.titleMedium,
                    weight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                line.startsWith("# ") -> MarkdownLine(
                    text = line.removePrefix("# "),
                    style = MaterialTheme.typography.titleLarge,
                    weight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                line.startsWith(">") -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    MarkdownLine(
                        text = line.removePrefix(">").trim(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                line.isMarkdownBullet() -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    MarkdownLine(text = line.removeMarkdownBullet(), modifier = Modifier.weight(1f))
                }
                line.isMarkdownNumberedItem() -> {
                    val number = line.substringBefore(".")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("$number.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        MarkdownLine(
                            text = line.substringAfter(".").trim(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> MarkdownLine(text = line)
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
                projectRevision = appState.projectRevision,
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
            AgentListCard(
                agents = appState.agents,
                providers = appState.llmProviders,
                nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                onUpsert = { agent ->
                    val exists = appState.agents.any { it.id == agent.id }
                    val agents = if (exists) {
                        appState.agents.map { if (it.id == agent.id) agent else it }
                    } else {
                        appState.agents + agent
                    }
                    onStateChange(appState.copy(agents = agents))
                },
                onDelete = { agent ->
                    onStateChange(appState.copy(agents = appState.agents.filterNot { it.id == agent.id }))
                }
            )
        }
    }
}

@Composable
private fun ProjectVersionCard(
    projectRevision: Int,
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
                        text = "项目 v$projectRevision · 应用 $appVersionName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "v$projectRevision",
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
private fun LlmProviderListCard(
    providers: List<LlmProviderSettings>,
    onUpsert: (LlmProviderSettings) -> Unit,
    onDelete: (LlmProviderSettings) -> Unit
) {
    var editingProvider by remember { mutableStateOf<LlmProviderSettings?>(null) }

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
            providers.forEach { provider ->
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
                        providerName = providers.firstOrNull { it.id == agent.providerId }?.name ?: "未选择 Provider",
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
    providerName: String,
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
                    "$providerName · ${agent.permissions.label()}",
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
                            providers.firstOrNull { it.id == agent.providerId }?.name ?: "选择 Provider",
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
                                text = { Text(provider.name) },
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
    var selectedAgentId by remember(record.id) { mutableStateOf(selectableAgents.firstOrNull()?.id) }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message.content,
            modifier = Modifier.padding(10.dp)
        )
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
    onDismiss: () -> Unit,
    onSave: (List<FieldAnswer>) -> Unit
) {
    val answers = remember(template.id) { mutableStateMapOf<String, String>() }
    val missingRequired = hasMissingRequiredAnswers(template, answers)
    var encouragement by remember(pendingRecord.startedAt) {
        mutableStateOf(localEncouragement(pendingRecord, nickname))
    }
    var loadingEncouragement by remember(pendingRecord.startedAt) { mutableStateOf(false) }

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
                RecordAnswerFields(template = template, answers = answers)
            }
        }
    )
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
                    todos = todos,
                    selectedTodoId = selectedTodoId,
                    fallbackTodoTitle = record.todoTitle.takeIf { originalTodoStillSelected && selectedTodo == null },
                    onSelected = { selectedTodoId = it }
                )
                HorizontalDivider()
                RecordAnswerFields(template = editableTemplate, answers = answers)
            }
        }
    )
}

@Composable
private fun RecordTodoSelector(
    todos: List<TodoItem>,
    selectedTodoId: String?,
    fallbackTodoTitle: String?,
    onSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("绑定任务", fontWeight = FontWeight.SemiBold)
        if (todos.isEmpty() && fallbackTodoTitle == null) {
            Text("还没有可绑定的待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            FilterChip(
                selected = selectedTodoId == null,
                onClick = { onSelected(null) },
                label = { Text("不绑定") }
            )
            if (fallbackTodoTitle != null) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text(fallbackTodoTitle) }
                )
            }
            todos.forEach { todo ->
                FilterChip(
                    selected = selectedTodoId == todo.id,
                    onClick = { onSelected(todo.id) },
                    label = { TodoChoiceLabel(todo) }
                )
            }
        }
    }
}

@Composable
private fun RecordAnswerFields(
    template: RecordTemplate,
    answers: androidx.compose.runtime.snapshots.SnapshotStateMap<String, String>
) {
    template.fields.forEach { field ->
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (field.required) "${field.title} *" else field.title,
                fontWeight = FontWeight.SemiBold
            )
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
}

private fun AgentDataPermissions.label(): String {
    val names = buildList {
        if (dailyRecords) add("每日记录")
        if (todos) add("待办")
        if (templates) add("模板")
    }
    return names.ifEmpty { listOf("无数据权限") }.joinToString("、")
}

private fun LlmProviderSettings.isConfiguredForChat(): Boolean {
    return endpoint.isNotBlank() && modelName.isNotBlank() && apiKey.isNotBlank()
}

private fun selectEncouragerAgent(agents: List<AgentSettings>): AgentSettings? {
    val enabledAgents = agents.filter { it.enabled }
    return enabledAgents.firstOrNull { it.name.contains("鼓励") } ?: enabledAgents.firstOrNull()
}

private fun selectDailySummaryAgent(agents: List<AgentSettings>): AgentSettings? {
    val enabledAgents = agents.filter { it.enabled }
    return enabledAgents.firstOrNull { it.name.contains("总结") && it.permissions.dailyRecords }
        ?: enabledAgents.firstOrNull { it.permissions.dailyRecords }
        ?: enabledAgents.firstOrNull()
}

private fun selectTodoAgent(agents: List<AgentSettings>): AgentSettings? {
    val enabledAgents = agents.filter { it.enabled }
    return enabledAgents.firstOrNull { it.permissions.todos }
        ?: enabledAgents.firstOrNull()
}

private fun String.withNickname(nickname: String): String {
    return replace("{nickname}", nickname.ifBlank { DEFAULT_NICKNAME })
}

private fun localEncouragement(record: PendingRecord, nickname: String): String {
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    return "$name，你刚刚把 ${record.focusMinutes} 分钟真正交给了「${record.todoTitle}」。这不是一句轻飘飘的“不错”，而是一次清清楚楚的推进：你坐下来、守住了这一轮、让事情往前走了一步。先把这份完成感收下，然后用复盘写下一个最小下一步。你已经启动了惯性，接下来只需要顺着它再走一点。"
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
        append("\n你是 Protato 的每日番茄总结 Agent。你要把当天番茄记录汇总成一篇自然、清晰、有洞察的中文总结文章。")
        append(" 必须包含：番茄数量、总专注时长、专注对象/次数、今天状态评价、一个具体改进建议，以及最后真诚有力的鼓励。")
        append(" 请使用 Markdown 输出，可以使用二级标题、列表、加粗和少量贴切 emoji，让总结更生动但不要花哨。")
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
): List<String> {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        append("\n你是 Protato 的待办整理助手。把用户随口说出的计划整理成短小、明确、可执行的 Todo。")
        append(" 只输出待办列表，每行一个，不要编号以外的解释。每条控制在 24 个中文字符以内。")
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

private fun parseTodoSuggestions(text: String): List<String> {
    return text
        .split("\n", "；", ";", "。")
        .map {
            it.trim()
                .replace(Regex("^[-*•\\d.、\\s]+"), "")
                .trim()
        }
        .filter { it.isNotBlank() }
        .distinct()
        .take(12)
}

private suspend fun generateAiEncouragement(
    pendingRecord: PendingRecord,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): String {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
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
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.getDefault()) }
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
        state.pendingRecord
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
