package com.protato.app.ai

import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.LlmProviderSettings
import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.filterNewTodoDrafts
import com.protato.app.filterNewTodoEdits
import com.protato.app.util.dayKey

internal data class AgentTurnResult(
    val reply: String,
    val todoDrafts: List<TodoDraft> = emptyList(),
    val todoEdits: List<TodoEditDraft> = emptyList(),
    val errorText: String? = null
)

internal suspend fun runAgentTurn(
    text: String,
    userState: AppState,
    agent: AgentSettings?,
    provider: LlmProviderSettings?,
    nickname: String,
    handledTodoDraftKeys: Set<String>,
    handledTodoEditKeys: Set<String>,
    hasPendingActions: Boolean = false
): AgentTurnResult {
    val intent = AgentIntentResolver().resolve(
        AgentIntentRequest(
            userText = text,
            recentMessages = userState.agentMessages.takeLast(8),
            hasPendingActions = hasPendingActions
        )
    ).intent
    when (intent) {
        is AgentIntent.QueryTodos -> {
            return AgentTurnResult(reply = localTodoQueryReply(text, userState, nickname))
        }

        is AgentIntent.MoveOpenTodosToToday -> {
            val today = dayKey(System.currentTimeMillis())
            val edits = userState.todos
                .filter { !it.completed && it.dueDate != today }
                .take(6)
                .map { todo -> TodoEditDraft(id = todo.id, dueDate = today) }
                .filterNewTodoEdits(
                    existingTodos = userState.todos,
                    handledKeys = handledTodoEditKeys
                )
            return AgentTurnResult(
                reply = if (edits.isEmpty()) {
                    "未完成任务已经在今天，或没有需要调整日期的任务。"
                } else {
                    "我整理了 ${edits.size} 个未完成任务的日期调整草案，请确认是否改到今天。"
                },
                todoEdits = edits
            )
        }

        is AgentIntent.AskClarification -> {
            return AgentTurnResult(reply = intent.question)
        }

        is AgentIntent.ConfirmPendingAction -> {
            return AgentTurnResult(reply = "请直接点击下方确认按钮应用这次草案。")
        }

        is AgentIntent.RejectPendingAction -> {
            return AgentTurnResult(reply = "请直接点击下方取消按钮放弃这次草案。")
        }

        else -> Unit
    }
    val todoDrafts = createTodoDraftsForTurn(
        text = text,
        shouldCreate = intent is AgentIntent.PlanTodos,
        userState = userState,
        agent = agent,
        provider = provider,
        nickname = nickname
    )
    var errorText: String? = null
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
    val parsedTodoDrafts = if (intent is AgentIntent.PlanTodos) {
        toolResponse.todoDrafts
    } else {
        emptyList()
    }
    val parsedTodoEdits = if (intent is AgentIntent.UpdateTodo) {
        toolResponse.todoEdits
    } else {
        emptyList()
    }
    val finalTodoDrafts = parsedTodoDrafts
        .ifEmpty { todoDrafts }
        .filterNewTodoDrafts(
            existingTodos = userState.todos,
            handledKeys = handledTodoDraftKeys
        )
    val finalTodoEdits = parsedTodoEdits.filterNewTodoEdits(
        existingTodos = userState.todos,
        handledKeys = handledTodoEditKeys
    )
    val reply = if (
        (
            parsedTodoDrafts.isNotEmpty() ||
                todoDrafts.isNotEmpty() ||
                parsedTodoEdits.isNotEmpty()
            ) &&
        finalTodoDrafts.isEmpty() &&
        finalTodoEdits.isEmpty()
    ) {
        "这些待办草案或编辑草案已经确认、拒绝或不再需要处理，我不会重复提交。"
    } else {
        toolResponse.visibleReply
    }
    return AgentTurnResult(
        reply = reply,
        todoDrafts = finalTodoDrafts,
        todoEdits = finalTodoEdits,
        errorText = errorText
    )
}

private suspend fun createTodoDraftsForTurn(
    text: String,
    shouldCreate: Boolean,
    userState: AppState,
    agent: AgentSettings?,
    provider: LlmProviderSettings?,
    nickname: String
): List<TodoDraft> {
    if (!shouldCreate) return emptyList()
    return runCatching {
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
}
