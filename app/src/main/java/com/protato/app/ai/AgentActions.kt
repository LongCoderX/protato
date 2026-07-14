package com.protato.app.ai

import com.protato.app.domain.todo.TodoDraft
import com.protato.app.domain.todo.TodoEditDraft

internal sealed interface AgentAction {
    data class Reply(val text: String) : AgentAction
    data class ProposeTodos(val todos: List<TodoDraft>) : AgentAction
    data class ProposeTodoUpdates(val edits: List<TodoEditDraft>) : AgentAction
    data class AskClarification(val question: String) : AgentAction
}
