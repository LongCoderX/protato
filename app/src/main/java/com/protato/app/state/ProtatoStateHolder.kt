package com.protato.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.protato.app.AppState
import com.protato.app.ProtatoStore
import com.protato.app.ProtatoStoreLoadResult
import com.protato.app.TodoReminderScheduler
import android.content.Context
import android.util.Log

internal class ProtatoStateHolder(
    initialState: AppState,
    private val saveState: (AppState) -> Unit,
    private val syncReminders: (List<com.protato.app.TodoItem>) -> Unit
) {
    var appState by mutableStateOf(initialState)
        private set

    fun setState(nextState: AppState): AppState {
        val revisedState = nextState.withNextRevisionFrom(appState)
        appState = revisedState
        return revisedState
    }

    fun dispatch(event: ProtatoEvent): ProtatoEventResult {
        val reduction = when (event) {
            is TodoProtatoEvent -> reduceTodoEvent(appState, event)
            is RecordProtatoEvent -> reduceRecordEvent(appState, event)
            is TemplateProtatoEvent -> reduceTemplateEvent(appState, event)
            is SettingsProtatoEvent -> reduceSettingsEvent(appState, event)
            is TimerProtatoEvent -> reduceTimerEvent(appState, event)
            is AgentProtatoEvent -> reduceAgentEvent(appState, event)
        }
        setState(reduction.state)
        return reduction.result
    }

    fun saveCurrentState() {
        saveState(appState)
        syncReminders(appState.todos)
    }
}

internal data class StateReduction(
    val state: AppState,
    val result: ProtatoEventResult = ProtatoEventResult()
)

private fun AppState.withNextRevisionFrom(previous: AppState): AppState {
    val normalizedNext = copy(projectRevision = previous.projectRevision)
    return if (normalizedNext == previous) {
        previous
    } else {
        normalizedNext.copy(projectRevision = previous.projectRevision + 1)
    }
}

internal fun createProtatoStateHolder(context: Context): ProtatoStateHolder {
    val store = ProtatoStore(context)
    val loadResult = store.loadResult()
    when (loadResult) {
        is ProtatoStoreLoadResult.Missing,
        is ProtatoStoreLoadResult.Success -> Unit
        is ProtatoStoreLoadResult.Recovered -> Log.w(
            "ProtatoStore",
            "Recovered state during load: ${loadResult.messages.joinToString()}"
        )
        is ProtatoStoreLoadResult.Failed -> Log.e(
            "ProtatoStore",
            "Failed to load state; using defaults.",
            loadResult.error
        )
    }
    val reminderScheduler = TodoReminderScheduler(context)
    return ProtatoStateHolder(
        initialState = loadResult.state,
        saveState = store::save,
        syncReminders = reminderScheduler::sync
    )
}
