package com.protato.app.ai

import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.LlmProviderSettings
import com.protato.app.TodoComplexity
import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.TodoItem
import com.protato.app.TodoSubtask
import com.protato.app.filterNewTodoDrafts
import com.protato.app.filterNewTodoEdits
import com.protato.app.util.newId
import com.protato.app.util.dayKey
import java.util.Locale

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
    hasPendingActions: Boolean = false,
    contextTodoId: String? = null
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

        is AgentIntent.PlanSubtasks -> {
            val targetTodo = findAgentContextTodo(userState.todos, contextTodoId, intent.targetHint)
            if (targetTodo == null) {
                return AgentTurnResult(reply = "我还不能确定要拆分哪一个待办。请先在对话页选择一个待办，或在消息里写清楚待办标题。")
            }
            val edit = TodoEditDraft(
                id = targetTodo.id,
                complexity = TodoComplexity.Complex,
                subtasks = localPlanSubtasks(intent.rawGoal, targetTodo).ifEmpty {
                    defaultSubtasksFor(targetTodo)
                }
            )
            val edits = listOf(edit).filterNewTodoEdits(
                existingTodos = userState.todos,
                handledKeys = handledTodoEditKeys
            )
            return AgentTurnResult(
                reply = if (edits.isEmpty()) {
                    "这个待办的子任务草案已经处理过，或没有新的变化。"
                } else {
                    "我把「${targetTodo.title}」拆成了 ${edit.subtasks.orEmpty().size} 个子任务草案，请确认是否应用。"
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
    val planningText = when (intent) {
        is AgentIntent.PlanTodos -> intent.rawGoal
        else -> text
    }
    val todoDrafts = createTodoDraftsForTurn(
        text = planningText,
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
    } else if (finalTodoDrafts.isNotEmpty() && parsedTodoDrafts.isEmpty()) {
        "我整理了 ${finalTodoDrafts.size} 个待办草案，请在下方卡片确认是否添加。"
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

private fun findAgentContextTodo(
    todos: List<TodoItem>,
    contextTodoId: String?,
    targetHint: String
): TodoItem? {
    contextTodoId?.let { id ->
        todos.firstOrNull { it.id == id && !it.completed }?.let { return it }
    }
    val lowered = targetHint.lowercase(Locale.getDefault())
    return todos
        .filterNot { it.completed }
        .firstOrNull { todo -> todo.title.lowercase(Locale.getDefault()) in lowered }
        ?: todos.filterNot { it.completed }.firstOrNull { todo ->
            todo.title.lowercase(Locale.getDefault()).split(Regex("\\s+")).any { it.length >= 2 && it in lowered }
        }
}

private fun localPlanSubtasks(rawGoal: String, todo: TodoItem): List<TodoSubtask> {
    val explicitParts = rawGoal
        .split(Regex("[\\n；;。]+"))
        .map { it.normalizeTodoTitle() }
        .filter { it.isNotBlank() && it != todo.title }
    val parts = explicitParts.ifEmpty {
        defaultSubtasksFor(todo).map { it.title }
    }
    return parts
        .distinctBy { it.lowercase(Locale.getDefault()) }
        .take(8)
        .map { title -> TodoSubtask(id = newId(), title = title.take(60)) }
}

private fun defaultSubtasksFor(todo: TodoItem): List<TodoSubtask> {
    val title = todo.title
    return listOf(
        "明确「$title」的完成标准",
        "整理「$title」需要的资料和约束",
        "完成「$title」的第一版执行",
        "检查结果并记录下一步"
    ).map { TodoSubtask(id = newId(), title = it.take(60)) }
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
