package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.TodoEditDraft
import com.protato.app.ai.appendAgentMessage

internal fun reduceAgentEvent(
    state: AppState,
    event: AgentProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.AppendAgentMessage -> {
            StateReduction(
                state.copy(
                    agentMessages = state.agentMessages.appendAgentMessage(
                        role = event.role,
                        content = event.content
                    )
                )
            )
        }

        ProtatoEvent.ClearAgentMessages -> {
            StateReduction(state.copy(agentMessages = emptyList()))
        }

        is ProtatoEvent.AcceptAgentTodoDrafts -> {
            val newTodos = event.drafts.map { it.toTodoItem() }
            StateReduction(
                state.copy(
                    todos = newTodos + state.todos,
                    agentMessages = state.agentMessages.appendAgentMessage(
                        role = "assistant",
                        content = "已添加 ${newTodos.size} 个待办。"
                    )
                )
            )
        }

        is ProtatoEvent.RejectAgentTodoDrafts -> {
            StateReduction(
                state.copy(
                    agentMessages = state.agentMessages.appendAgentMessage(
                        role = "assistant",
                        content = "已拒绝本次待办草案。"
                    )
                )
            )
        }

        is ProtatoEvent.AcceptAgentTodoEdits -> {
            val editsById = event.edits.associateBy(TodoEditDraft::id)
            val updatedTodos = state.todos.map { todo ->
                editsById[todo.id]?.applyTo(todo) ?: todo
            }
            StateReduction(
                state.copy(
                    todos = updatedTodos,
                    agentMessages = state.agentMessages.appendAgentMessage(
                        role = "assistant",
                        content = "已更新 ${event.edits.size} 个待办。"
                    )
                )
            )
        }

        is ProtatoEvent.RejectAgentTodoEdits -> {
            StateReduction(
                state.copy(
                    agentMessages = state.agentMessages.appendAgentMessage(
                        role = "assistant",
                        content = "已拒绝本次待办编辑草案。"
                    )
                )
            )
        }
    }
}
