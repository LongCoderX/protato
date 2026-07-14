package com.protato.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.protato.app.AppStateUpdateResult
import com.protato.app.DefaultAppStateRepository
import com.protato.app.AppState
import com.protato.app.ProtatoStoreLoadResult
import com.protato.app.mergeAppStateChanges
import com.protato.app.platform.TodoReminderScheduler
import android.content.Context
import android.util.Log

internal class ProtatoStateHolder(
    initialState: AppState,
    private val loadState: suspend () -> ProtatoStoreLoadResult = {
        ProtatoStoreLoadResult.Success(initialState)
    },
    private val saveState: suspend (AppState, AppState) -> AppStateUpdateResult,
    private val syncReminders: (List<com.protato.app.TodoItem>) -> Unit,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    initiallyLoaded: Boolean = true
) {
    var appState by mutableStateOf(initialState)
        private set
    var isLoaded by mutableStateOf(initiallyLoaded)
        private set
    var saveErrorMessage by mutableStateOf<String?>(null)
        private set

    private var lastSavedState: AppState? = initialState.takeIf { initiallyLoaded }
    private var lastSyncedTodos = initialState.todos.takeIf { initiallyLoaded }

    fun setState(nextState: AppState): AppState {
        val revisedState = nextState.withNextRevisionFrom(appState)
        appState = revisedState
        return revisedState
    }

    fun dispatch(event: ProtatoEvent): ProtatoEventResult {
        val reduction = when (event) {
            is TodoProtatoEvent -> reduceTodoEvent(appState, event, nowMillis())
            is RecordProtatoEvent -> reduceRecordEvent(appState, event)
            is TemplateProtatoEvent -> reduceTemplateEvent(appState, event)
            is SettingsProtatoEvent -> reduceSettingsEvent(appState, event)
            is TimerProtatoEvent -> reduceTimerEvent(appState, event, nowMillis())
            is AgentProtatoEvent -> reduceAgentEvent(appState, event)
        }
        setState(reduction.state)
        return reduction.result
    }

    suspend fun loadCurrentState(): ProtatoStoreLoadResult {
        val result = loadState()
        appState = result.state
        lastSavedState = result.state
        isLoaded = true
        syncReminders(result.state.todos)
        lastSyncedTodos = result.state.todos
        if (result is ProtatoStoreLoadResult.Failed) {
            saveErrorMessage = result.error.message ?: "状态加载失败"
        }
        return result
    }

    suspend fun saveCurrentState(): AppStateUpdateResult {
        if (!isLoaded) return AppStateUpdateResult.Unchanged(appState)
        val snapshot = appState
        val base = lastSavedState ?: snapshot
        if (snapshot == base) return AppStateUpdateResult.Unchanged(snapshot)
        return saveState(base, snapshot).also { result ->
            when (result) {
                is AppStateUpdateResult.Updated,
                is AppStateUpdateResult.Unchanged -> {
                    val persisted = requireNotNull(result.state)
                    val current = appState
                    appState = if (current == snapshot) {
                        persisted
                    } else {
                        mergeAppStateChanges(snapshot, current, persisted)
                    }
                    lastSavedState = persisted
                    saveErrorMessage = null
                    if (persisted.todos != lastSyncedTodos) {
                        syncReminders(persisted.todos)
                        lastSyncedTodos = persisted.todos
                    }
                }

                is AppStateUpdateResult.Failed -> {
                    saveErrorMessage = result.error.message ?: "状态保存失败"
                }
            }
        }
    }

    fun clearSaveError() {
        saveErrorMessage = null
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
    val repository = DefaultAppStateRepository(context)
    val reminderScheduler = TodoReminderScheduler(context)
    return ProtatoStateHolder(
        initialState = AppState(),
        loadState = {
            repository.load().also { result ->
                when (result) {
                    is ProtatoStoreLoadResult.Missing,
                    is ProtatoStoreLoadResult.Success -> Unit
                    is ProtatoStoreLoadResult.Recovered -> Log.w(
                        "ProtatoStore",
                        "Recovered state during load: ${result.messages.joinToString()}"
                    )
                    is ProtatoStoreLoadResult.Failed -> Log.e(
                        "ProtatoStore",
                        "Failed to load state; using defaults.",
                        result.error
                    )
                }
            }
        },
        saveState = { base, local ->
            repository.update { latest -> mergeAppStateChanges(base, local, latest) }
        },
        syncReminders = reminderScheduler::sync,
        initiallyLoaded = false
    )
}
