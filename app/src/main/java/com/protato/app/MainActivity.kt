package com.protato.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.delay

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
    Records("记录", Icons.Outlined.WorkHistory)
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
        TimerMode.ShortBreak -> appState.shortBreakMinutes
        TimerMode.LongBreak -> appState.longBreakMinutes
    }
    val totalSeconds = durationMinutes * 60
    val activeSession = appState.activeSession
    val isRunning = activeSession != null
    val remainingSeconds = activeSession?.remainingSeconds(nowMillis) ?: totalSeconds
    val pendingRecord = appState.pendingRecord?.toPendingRecordUi()

    LaunchedEffect(appState) {
        store.save(appState)
    }

    LaunchedEffect(activeSession?.mode) {
        activeSession?.let { timerMode = it.mode }
    }

    LaunchedEffect(activeSession?.endsAt) {
        if (activeSession != null) {
            context.startPomodoroService(ACTION_START_TIMER_SERVICE)
            PomodoroAlarmScheduler(context).schedule(activeSession)
        }
    }

    LaunchedEffect(activeSession?.endsAt) {
        while (activeSession != null) {
            nowMillis = System.currentTimeMillis()
            if (activeSession.remainingSeconds(nowMillis) <= 0) {
                val nextState = completeSession(appState)
                appState = nextState
                store.save(nextState)
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
                        timerMode = timerMode,
                        remainingSeconds = remainingSeconds,
                        totalSeconds = totalSeconds,
                        isRunning = isRunning,
                        pendingRecord = pendingRecord,
                        onStateChange = { appState = it },
                        onSelectedTodo = { selectedTodoId = it },
                        onModeChange = { mode ->
                            timerMode = mode
                            appState = appState.copy(activeSession = null)
                            PomodoroAlarmScheduler(context).cancel()
                            context.startPomodoroService(ACTION_STOP_TIMER_SERVICE)
                        },
                        onStartPause = {
                            if (isRunning) {
                                appState = appState.copy(activeSession = null)
                                PomodoroAlarmScheduler(context).cancel()
                                context.startPomodoroService(ACTION_STOP_TIMER_SERVICE)
                            } else {
                                val startedAt = System.currentTimeMillis()
                                val session = TimerSession(
                                    mode = timerMode,
                                    todoId = selectedTodo?.id,
                                    todoTitle = selectedTodo?.title ?: "未绑定任务",
                                    startedAt = startedAt,
                                    endsAt = startedAt + totalSeconds * 1000L,
                                    totalSeconds = totalSeconds,
                                    templateId = selectedTemplate.id
                                )
                                val nextState = appState.copy(activeSession = session)
                                appState = nextState
                                store.save(nextState)
                                nowMillis = startedAt
                                PomodoroAlarmScheduler(context).schedule(session)
                                context.startPomodoroService(ACTION_START_TIMER_SERVICE)
                            }
                        },
                        onReset = {
                            appState = appState.copy(activeSession = null)
                            PomodoroAlarmScheduler(context).cancel()
                            context.startPomodoroService(ACTION_STOP_TIMER_SERVICE)
                        },
                        onDismissRecord = {
                            appState = appState.copy(pendingRecord = null)
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
                            appState = appState.copy(
                                records = listOf(newRecord) + appState.records,
                                pendingRecord = null
                            )
                            PomodoroNotifications(context).cancelCompleted()
                        }
                    )

                    MainTab.Todo -> TodoScreen(
                        todos = appState.todos,
                        onAdd = { title ->
                            appState = appState.copy(
                                todos = listOf(
                                    TodoItem(id = newId(), title = title.trim())
                                ) + appState.todos
                            )
                        },
                        onToggle = { item ->
                            appState = appState.copy(
                                todos = appState.todos.map {
                                    if (it.id == item.id) it.copy(completed = !it.completed) else it
                                }
                            )
                        },
                        onDelete = { item ->
                            appState = appState.copy(todos = appState.todos.filterNot { it.id == item.id })
                            if (selectedTodoId == item.id) selectedTodoId = null
                        },
                        onUpdate = { updatedItem ->
                            appState = appState.copy(
                                todos = appState.todos.map { item ->
                                    if (item.id == updatedItem.id) updatedItem else item
                                }
                            )
                        },
                        onDeleteMany = { itemIds ->
                            appState = appState.copy(
                                todos = appState.todos.filterNot { it.id in itemIds }
                            )
                            if (selectedTodoId in itemIds) selectedTodoId = null
                        }
                    )

                    MainTab.Templates -> TemplateScreen(
                        templates = appState.templates,
                        selectedTemplateId = appState.selectedTemplateId,
                        onSelectedTemplate = { templateId ->
                            appState = appState.copy(selectedTemplateId = templateId)
                        },
                        onUpsert = { template ->
                            val exists = appState.templates.any { it.id == template.id }
                            val templates = if (exists) {
                                appState.templates.map { if (it.id == template.id) template else it }
                            } else {
                                listOf(template) + appState.templates
                            }
                            appState = appState.copy(
                                templates = templates,
                                selectedTemplateId = template.id
                            )
                        },
                        onDelete = { template ->
                            val nextTemplates = appState.templates.filterNot { it.id == template.id }.ifEmpty {
                                listOf(defaultTemplate())
                            }
                            appState = appState.copy(
                                templates = nextTemplates,
                                selectedTemplateId = nextTemplates.first().id
                            )
                        }
                    )

                    MainTab.Records -> RecordsScreen(
                        records = appState.records,
                        templates = appState.templates,
                        todos = appState.todos,
                        onUpdateRecord = { updatedRecord ->
                            appState = appState.copy(
                                records = appState.records.map { record ->
                                    if (record.id == updatedRecord.id) updatedRecord else record
                                }
                            )
                        }
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
    timerMode: TimerMode,
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
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
                            TimerMode.ShortBreak -> appState.copy(shortBreakMinutes = minutes)
                            TimerMode.LongBreak -> appState.copy(longBreakMinutes = minutes)
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
            FilledIconButton(
                onClick = { choosingTemplate = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(42.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = "选择记录模板")
            }

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
                    SingleChoiceSegmentedButtonRow {
                        TimerMode.entries.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = mode == item,
                                onClick = { onModeChange(item) },
                                shape = SegmentedButtonDefaults.itemShape(index, TimerMode.entries.size),
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                    }
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
                        Text(if (isRunning) "暂停" else "开始")
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
                            label = { Text(todo.title) }
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
                        label = { Text("${template.name} · ${template.fields.size} 个字段") }
                    )
                }
            }
        }
    )
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
    onAdd: (String) -> Unit,
    onToggle: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onUpdate: (TodoItem) -> Unit,
    onDeleteMany: (Set<String>) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var selectedTodoIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    val selecting = selectedTodoIds.isNotEmpty()

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
                            if (title.isNotBlank()) {
                                onAdd(title)
                                title = ""
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (todos.isEmpty()) {
                item {
                    EmptyState("还没有待办，先添加一个明确的小目标。")
                }
            }
            items(todos, key = { it.id }) { todo ->
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
            onSave = { updatedTitle ->
                onUpdate(todo.copy(title = updatedTitle))
                editingTodo = null
            }
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
                Text(
                    text = todo.title,
                    modifier = Modifier.weight(1f),
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (todo.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
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
    onSave: (String) -> Unit
) {
    var title by rememberSaveable(todo.id) { mutableStateOf(todo.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onSave(title.trim()) }
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("待办内容") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
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
    templates: List<RecordTemplate>,
    todos: List<TodoItem>,
    onUpdateRecord: (PomodoroRecord) -> Unit
) {
    var editingRecord by remember { mutableStateOf<PomodoroRecord?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Header(title = "番茄记录", subtitle = "每次专注结束后的完整内容会沉淀在这里")
            }
            if (records.isEmpty()) {
                item {
                    EmptyState("完成一轮专注后，记录会出现在这里。")
                }
            } else {
                items(records, key = { it.id }) { record ->
                    RecordCard(
                        record = record,
                        template = templates.firstOrNull { it.id == record.templateId },
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
    }
}

@Composable
private fun RecordCard(record: PomodoroRecord, template: RecordTemplate?, onEdit: () -> Unit) {
    var expanded by rememberSaveable(record.id) { mutableStateOf(false) }
    val isBoundToTodo = record.todoId != null
    val answerCount = record.answers.size

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RecordTodoChip(title = record.todoTitle, bound = isBoundToTodo)
                    }
                    Text(
                        "${record.focusMinutes} 分钟 · ${formatTime(record.endedAt)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${record.templateName} · $answerCount 条回答",
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
                    if (expanded) "收起模板回答" else "展开模板回答",
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
private fun RecordDialog(
    pendingRecord: PendingRecord,
    template: RecordTemplate,
    onDismiss: () -> Unit,
    onSave: (List<FieldAnswer>) -> Unit
) {
    val answers = remember(template.id) { mutableStateMapOf<String, String>() }
    val missingRequired = hasMissingRequiredAnswers(template, answers)

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
                Text(
                    "${pendingRecord.todoTitle} · ${pendingRecord.focusMinutes} 分钟",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    label = { Text(todo.title) }
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

private fun TimerMode.label(): String = when (this) {
    TimerMode.Focus -> "专注"
    TimerMode.ShortBreak -> "短休"
    TimerMode.LongBreak -> "长休"
}

private fun TimerMode.durationRange(): IntRange = when (this) {
    TimerMode.Focus -> 5..90
    TimerMode.ShortBreak -> 1..30
    TimerMode.LongBreak -> 5..60
}

private fun FieldType.label(): String = when (this) {
    FieldType.SingleChoice -> "单选"
    FieldType.ShortAnswer -> "简答"
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

private fun newId(): String = UUID.randomUUID().toString()
