package com.protato.app.ai

import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.LlmProviderSettings
import com.protato.app.domain.todo.TodoDraft
import com.protato.app.util.dayKey
import java.util.Locale

internal suspend fun generateTodoSuggestions(
    rawInput: String,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): List<TodoDraft> {
    val today = dayKey(System.currentTimeMillis())
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的待办整理助手。把用户随口说出的计划整理成短小、明确、可执行的 Todo。")
        append(" 今天日期是 $today。")
        append(" 只输出待办列表，每行一个，格式严格为：任务标题 | yyyy-MM-dd 或 空 | 标签1,标签2 | 番茄数。")
        append(" 日期要根据今天、明天、周末、具体日期等信息推断；没有日期就留空。")
        append(" 标签要自动选择 1-3 个短标签，例如 工作、学习、生活、健康、沟通、复盘。不要输出解释。")
        append(" 番茄数是预计专注轮数，通常 1-6。")
    }
    val response = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", rawInput)
        )
    )
    return parseTodoSuggestions(response).ifEmpty { parseTodoSuggestions(rawInput) }
}

internal suspend fun generateAgentTodoDrafts(
    rawInput: String,
    state: AppState,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String,
    planningMode: Boolean = false
): List<TodoDraft> {
    val today = dayKey(System.currentTimeMillis())
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的任务规划器。你要用 ReAct 风格在内部完成：Observation=理解用户真实意图；Decision=判断任务边界，合并同一件事的碎片；Action=输出可执行待办草案。")
        append(" 不要暴露 Observation 或 Decision，不要解释。")
        append(" 今天日期是 $today。")
        append(" 只返回结构化行，每行一个任务：TASK | 动词 | 对象/结果 | yyyy-MM-dd 或 空 | 标签1,标签2 | 番茄数。")
        append(" 原则：一件事只输出一行；不要照抄口语原文；标题必须读起来通顺，像“整理项目周报材料”“给客户回复报价邮件”。")
        append(" 如果用户只是描述一个目标，先收束成一个最小可执行动作。")
        if (planningMode) {
            append(" 当前处于计划模式：如果用户给出大型事件或目标，要拆成 2-6 个能分别用番茄钟推进的小待办。")
        }
        append(" 如果输入里有因果、补充说明、背景，不要拆成独立任务。")
        append(" 标签 1-3 个，番茄数 1-6。无法形成待办就返回 NONE。")
        append("\n\n当前上下文：\n")
        append(state.toUnifiedAgentContext(nickname, agent.permissions))
    }
    val response = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", rawInput)
        )
    )
    return parseTodoPlanDrafts(response)
        .map { it.toTodoDraft() }
        .ifEmpty { localPlanTodoDrafts(rawInput) }
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
        .take(8)
}
