package com.protato.app.ai

import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.LlmProviderSettings
import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.filterNewTodoDrafts
import com.protato.app.filterNewTodoEdits

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
    handledTodoEditKeys: Set<String>
): AgentTurnResult {
    val todoDrafts = createTodoDraftsForTurn(
        text = text,
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
    val finalTodoDrafts = toolResponse.todoDrafts
        .ifEmpty { todoDrafts }
        .filterNewTodoDrafts(
            existingTodos = userState.todos,
            handledKeys = handledTodoDraftKeys
        )
    val finalTodoEdits = toolResponse.todoEdits.filterNewTodoEdits(
        existingTodos = userState.todos,
        handledKeys = handledTodoEditKeys
    )
    val reply = if (
        (
            toolResponse.todoDrafts.isNotEmpty() ||
                todoDrafts.isNotEmpty() ||
                toolResponse.todoEdits.isNotEmpty()
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
    userState: AppState,
    agent: AgentSettings?,
    provider: LlmProviderSettings?,
    nickname: String
): List<TodoDraft> {
    if (!text.looksLikeTodoPlanningRequest()) return emptyList()
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
