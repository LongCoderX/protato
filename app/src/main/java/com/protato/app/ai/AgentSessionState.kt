package com.protato.app.ai

import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.todoDraftKey
import com.protato.app.todoEditDraftKey

private const val MAX_HANDLED_AGENT_ACTION_KEYS = 120

internal data class AgentSessionState(
    val pendingTodos: List<TodoDraft> = emptyList(),
    val pendingTodoEdits: List<TodoEditDraft> = emptyList(),
    val handledTodoDraftKeys: List<String> = emptyList(),
    val handledTodoEditKeys: List<String> = emptyList()
) {
    val hasPendingActions: Boolean
        get() = pendingTodos.isNotEmpty() || pendingTodoEdits.isNotEmpty()

    val handledTodoDraftKeySet: Set<String>
        get() = handledTodoDraftKeys.toSet()

    val handledTodoEditKeySet: Set<String>
        get() = handledTodoEditKeys.toSet()

    fun startTurn(): AgentSessionState {
        return copy(
            pendingTodos = emptyList(),
            pendingTodoEdits = emptyList()
        )
    }

    fun completeTurn(result: AgentTurnResult): AgentSessionState {
        return copy(
            pendingTodos = result.todoDrafts,
            pendingTodoEdits = result.todoEdits
        )
    }

    fun acceptTodoDrafts(): AgentSessionState {
        return copy(
            pendingTodos = emptyList(),
            handledTodoDraftKeys = (handledTodoDraftKeys + pendingTodos.map(::todoDraftKey))
                .distinct()
                .takeLast(MAX_HANDLED_AGENT_ACTION_KEYS)
        )
    }

    fun rejectTodoDrafts(): AgentSessionState = acceptTodoDrafts()

    fun acceptTodoEdits(): AgentSessionState {
        return copy(
            pendingTodoEdits = emptyList(),
            handledTodoEditKeys = (handledTodoEditKeys + pendingTodoEdits.map(::todoEditDraftKey))
                .distinct()
                .takeLast(MAX_HANDLED_AGENT_ACTION_KEYS)
        )
    }

    fun rejectTodoEdits(): AgentSessionState = acceptTodoEdits()

    fun reset(): AgentSessionState = AgentSessionState()
}
