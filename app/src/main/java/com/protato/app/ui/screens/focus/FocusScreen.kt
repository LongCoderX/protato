package com.protato.app.ui.screens.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.FieldAnswer
import com.protato.app.LlmProviderSettings
import com.protato.app.RecordTemplate
import com.protato.app.TimerMode
import com.protato.app.domain.timer.PendingRecord
import com.protato.app.state.ProtatoEvent
import com.protato.app.ui.components.Header
import com.protato.app.ui.screens.records.RecordDialog

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
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(title = "Protato", subtitle = "番茄时钟、任务和复盘记录都在一条线上")
            TimerCard(
                modifier = Modifier.fillMaxWidth().weight(1f),
                mode = timerMode,
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
                isRunning = isRunning,
                hasPausedSession = hasPausedSession,
                todos = appState.todos,
                selectedTodoId = selectedTodoId,
                selectedTodoTitle = selectedTodo?.title,
                onModeChange = onModeChange,
                onDurationChange = { minutes ->
                    onEvent(ProtatoEvent.UpdateTimerDuration(timerMode, minutes))
                },
                onSelectedTodo = onSelectedTodo,
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
                todos = appState.todos,
                recordPolishProviderId = appState.recordPolishProviderId,
                onDismiss = onDismissRecord,
                onSave = onSaveRecord
            )
        }
    }
}
