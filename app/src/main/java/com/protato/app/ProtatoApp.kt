package com.protato.app
import com.protato.app.ui.screens.todo.TodoScreen
import com.protato.app.ui.screens.todo.TodoDetailScreen
import com.protato.app.ui.screens.templates.TemplateScreen
import com.protato.app.ui.screens.settings.SettingsScreen
import com.protato.app.ui.screens.records.RecordsScreen
import com.protato.app.ui.screens.focus.FocusScreen
import com.protato.app.ui.screens.agent.AgentScreen
import com.protato.app.ui.screens.agent.AgentSettingsScreen


import com.protato.app.domain.timer.*
import com.protato.app.domain.todo.toggleSubtask
import com.protato.app.platform.*
import com.protato.app.state.createProtatoStateHolder
import com.protato.app.state.ProtatoEffect
import com.protato.app.state.ProtatoEvent
import com.protato.app.state.ProtatoEventResult
import com.protato.app.util.*
import com.protato.app.state.ProtatoAppLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.protato.app.ai.AgentSessionState
import com.protato.app.ai.AgentTurnController
import com.protato.app.ai.AiDiagnostics
import com.protato.app.ui.MainTab
import com.protato.app.ui.ProtatoBottomBar
import com.protato.app.ui.ProtatoTheme
import com.protato.app.ui.SecondaryDestination
import com.protato.app.ui.screens.more.MoreScreen

@Composable
fun ProtatoApp() {
    val context = LocalContext.current
    SideEffect { AiDiagnostics.initialize(context.filesDir) }
    val scope = rememberCoroutineScope()
    val stateHolder = remember { createProtatoStateHolder(context) }
    val effectRunner = remember { ProtatoEffectRunner(context) }
    val appState = stateHolder.appState
    var selectedTodoId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailTodoId by rememberSaveable { mutableStateOf<String?>(null) }
    var timerMode by rememberSaveable { mutableStateOf(TimerMode.Focus) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var agentInput by rememberSaveable { mutableStateOf("") }
    var agentErrorText by rememberSaveable { mutableStateOf<String?>(null) }
    var agentSessionState by remember { mutableStateOf(AgentSessionState()) }
    val agentTurnController = remember(scope) { AgentTurnController(scope) }
    val selectedTodo = appState.todos.firstOrNull {
        it.id == selectedTodoId && !it.completed
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

    ProtatoAppLifecycle(
        stateHolder = stateHolder,
        appState = appState,
        activeSession = activeSession,
        isRunning = isRunning,
        effectRunner = effectRunner,
        onTimerModeChange = { timerMode = it },
        onNowMillisChange = { nowMillis = it },
        onTimerComplete = { dispatchEvent(ProtatoEvent.CompleteTimer) }
    )

    if (!stateHolder.isLoaded) {
        ProtatoTheme(
            fontScale = appState.fontScale,
            themeChoice = appState.themeChoice
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        return
    }


    ProtatoTheme(
        fontScale = appState.fontScale,
        themeChoice = appState.themeChoice
    ) {
        var currentTab by rememberSaveable { mutableStateOf(MainTab.Todo) }
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
                                onEvent = ::dispatchEvent,
                                onBack = { secondaryDestination = null }
                            )

                            SecondaryDestination.AiSettings -> AgentSettingsScreen(
                                appState = appState,
                                onEvent = ::dispatchEvent,
                                onBack = { secondaryDestination = null }
                            )

                            SecondaryDestination.TodoDetail -> detailTodoId?.let { todoId ->
                                appState.todos.firstOrNull { it.id == todoId }?.let { todo ->
                                    TodoDetailScreen(
                                        todo = todo,
                                        availableTags = appState.todos.flatMap { it.tags }.distinct(),
                                        onBack = {
                                            detailTodoId = null
                                            secondaryDestination = null
                                        },
                                        onUpdate = { updatedTodo ->
                                            dispatchEvent(ProtatoEvent.UpdateTodo(updatedTodo))
                                        },
                                        onToggleSubtask = { subtaskId ->
                                            dispatchEvent(ProtatoEvent.UpdateTodo(todo.toggleSubtask(subtaskId)))
                                        },
                                        onStartFocus = {
                                            selectedTodoId = todo.id
                                            detailTodoId = null
                                            secondaryDestination = null
                                            currentTab = MainTab.Focus
                                        }
                                    )
                                }
                            }

                            null -> Unit
                        }
                    } else {
                        when (currentTab) {
                        MainTab.Agent -> AgentScreen(
                            appState = appState,
                            input = agentInput,
                            errorText = agentErrorText,
                            sessionState = agentSessionState,
                            turnController = agentTurnController,
                            contextTodoId = selectedTodoId,
                            onInputChange = { agentInput = it },
                            onErrorTextChange = { agentErrorText = it },
                            onSessionStateChange = { agentSessionState = it },
                            onContextTodoIdChange = { selectedTodoId = it },
                            onEvent = ::dispatchEvent
                        )

                        MainTab.Focus -> FocusScreen(
                            appState = appState,
                            selectedTodoId = activeSession?.todoId ?: selectedTodoId,
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
                            onSelectedTodo = { todoId ->
                                selectedTodoId = todoId
                                val updatedTodo = appState.todos.firstOrNull { it.id == todoId }
                                if (activeSession != null) {
                                    dispatchEvent(
                                        ProtatoEvent.UpdateActiveSessionTodo(
                                            todoId = updatedTodo?.id,
                                            todoTitle = updatedTodo?.title ?: "未绑定任务"
                                        )
                                    )
                                }
                            },
                            onModeChange = { mode ->
                                timerMode = mode
                                dispatchEvent(ProtatoEvent.ResetTimer)
                            },
                            onStartPause = {
                                if (isRunning) {
                                    dispatchEvent(ProtatoEvent.PauseTimer)
                                } else if (hasPausedSession) {
                                    dispatchEvent(ProtatoEvent.ResumeTimer)
                                } else {
                                    val startedAt = System.currentTimeMillis()
                                    dispatchEvent(
                                        ProtatoEvent.StartTimer(
                                            mode = timerMode,
                                            todoId = selectedTodo?.id,
                                            todoTitle = selectedTodo?.title ?: "未绑定任务",
                                            totalSeconds = totalSeconds,
                                            templateId = selectedTemplate.id
                                        )
                                    )
                                    nowMillis = startedAt
                                }
                            },
                            onReset = {
                                dispatchEvent(ProtatoEvent.ResetTimer)
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
                            records = appState.records,
                            providers = appState.llmProviders,
                            agents = appState.agents,
                            nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                            lastOverdueTodoDecisionDay = appState.lastOverdueTodoDecisionDay,
                            onOpenDetail = { todo ->
                                detailTodoId = todo.id
                                secondaryDestination = SecondaryDestination.TodoDetail
                            },
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
                            onMoveManyToToday = { itemIds ->
                                dispatchEvent(ProtatoEvent.MoveTodosToToday(itemIds))
                            },
                            onResolveOverdueTodos = { itemIds, moveToToday ->
                                dispatchEvent(
                                    ProtatoEvent.ResolveOverdueTodos(
                                        todoIds = itemIds,
                                        moveToToday = moveToToday
                                    )
                                )
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
                            onOpenSettings = { secondaryDestination = SecondaryDestination.Settings },
                            onOpenTemplates = { secondaryDestination = SecondaryDestination.Templates },
                            onOpenAgentSettings = { secondaryDestination = SecondaryDestination.AiSettings },
                            nickname = appState.nickname.ifBlank { DEFAULT_NICKNAME },
                            avatarStyle = appState.avatarStyle,
                            focusMinutes = appState.records.sumOf { it.focusMinutes },
                            level = appState.records.size / 5 + 1,
                            streak = appState.records.map { dayKey(it.endedAt) }.distinct().size,
                            xp = appState.records.size * 50 + appState.todos.count { it.completed } * 20
                        )
                        }
                }
            }
        }
    }
}
