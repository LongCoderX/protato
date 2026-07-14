package com.protato.app.ai

import com.protato.app.*
import com.protato.app.domain.timer.*
import com.protato.app.util.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.outlined.List
import com.protato.app.ai.LlmChatMessage
import com.protato.app.ai.requestLlmChat

internal fun localEncouragement(record: PendingRecord, nickname: String): String {
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    return "$name，你刚刚把 ${record.focusMinutes} 分钟真正交给了「${record.todoTitle}」。这不是一句轻飘飘的“不错”，而是一次清清楚楚的推进：你坐下来、守住了这一轮、让事情往前走了一步。先把这份完成感收下，然后用复盘写下一个最小下一步。你已经启动了惯性，接下来只需要顺着它再走一点。"
}

internal suspend fun requestRecordPolish(
    rawText: String,
    pendingRecord: PendingRecord,
    template: RecordTemplate,
    answers: Map<String, String>,
    provider: LlmProviderSettings,
    nickname: String
): String {
    val systemPrompt = buildString {
        append("你是 Protato 的总结润色 Agent。你的任务是把用户刚填的草稿润色成更通顺、更清晰、更适合放进记录的中文总结。")
        append(" 你要提炼要点，合并重复表达，保持真实，不要添加没有出现过的事实。")
        append(" 输出必须是可直接替换的最终文本，不要解释过程，不要分点太碎。")
        append(" 语气温和、自然、简洁。")
    }
    val context = buildString {
        append("用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n")
        append("任务：${pendingRecord.todoTitle}\n")
        append("时长：${pendingRecord.focusMinutes} 分钟\n")
        append("模板：${template.name}\n")
        append("当前字段：\n")
        template.fields.forEach { field ->
            append("- ${field.title}：${answers[field.id].orEmpty()}\n")
        }
        append("待润色草稿：\n")
        append(rawText)
    }
    return requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", context)
        )
    )
}

internal suspend fun generateDailySummary(
    dayKey: String,
    records: List<PomodoroRecord>,
    templates: List<RecordTemplate>,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String
): DailySummary {
    val fallback = localDailySummary(dayKey, records, nickname)
    val agent = selectDailySummaryAgent(agents) ?: return fallback
    val provider = agent.providerId.let { providerId ->
        providers.firstOrNull { it.id == providerId }
    } ?: providers.firstOrNull { it.isConfiguredForChat() } ?: return fallback
    if (!provider.isConfiguredForChat()) return fallback
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的每日番茄总结 Agent。你要把当天番茄记录汇总成一篇自然、清晰、有洞察的中文总结文章。")
        append(" 必须包含：番茄数量、总专注时长、专注对象/次数、今天状态评价、一个具体改进建议，以及最后真诚有力的鼓励。")
        append(" 请使用 Markdown 输出，可以使用二级标题、列表和加粗。")
        append(" 不要编造记录之外的事实。")
    }
    val userPrompt = buildString {
        append("日期：$dayKey\n")
        append("用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n")
        append(records.toDailySummaryContext(templates))
        append("\n请写成 500 字以内的 Markdown 总结。")
    }
    val content = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", userPrompt)
        )
    )
    return fallback.copy(
        generatedAt = System.currentTimeMillis(),
        content = content.ifBlank { fallback.content }
    )
}

internal fun localDailySummary(
    dayKey: String,
    records: List<PomodoroRecord>,
    nickname: String
): DailySummary {
    val totalMinutes = records.sumOf { it.focusMinutes }
    val focusTargets = records.groupingBy { it.todoTitle }.eachCount()
    val topTargets = focusTargets.entries.sortedByDescending { it.value }.take(3)
        .joinToString("、") { "${it.key} ${it.value} 次" }
        .ifBlank { "暂无明确对象" }
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    val content = if (records.isEmpty()) {
        """
        ## 🌱 今天还没有番茄记录

        $name，没关系。总结不是为了责备你，而是为了帮你重新看见下一步。

        - 先选一个足够小的任务
        - 完成一轮就好
        - 把第一条记录留在时间轴上
        """.trimIndent()
    } else {
        """
        ## 🍅 今天的专注轨迹

        $name，今天你完成了 **${records.size} 个番茄**，总专注 **$totalMinutes 分钟**。

        - 主要投入：$topTargets
        - 节奏观察：你已经把时间放进了具体事情里，而不只是停在“想做”
        - 下一步：复盘哪一轮最顺、哪一轮最容易分心，再把下一轮番茄安排给最重要的小目标

        > 请认真收下这份完成感。每一个番茄都不大，但它们合在一起，就是你把生活重新握回手里的证据。✨
        """.trimIndent()
    }
    return DailySummary(
        dayKey = dayKey,
        generatedAt = System.currentTimeMillis(),
        title = "$dayKey 今日番茄总结",
        content = content,
        pomodoroCount = records.size,
        focusMinutes = totalMinutes
    )
}

internal suspend fun generateAiEncouragement(
    pendingRecord: PendingRecord,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): String {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你现在要在用户完成一轮番茄后给出鼓励。必须具体、真诚、有力量，不要空泛鸡汤，不要夸张，不要说教。")
        append(" 需要点名用户刚完成的任务和时长，并给一个很小、很容易继续执行的下一步。")
    }
    val userPrompt = "用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n任务：${pendingRecord.todoTitle}\n专注时长：${pendingRecord.focusMinutes} 分钟\n请给一段 80-140 字中文鼓励。"
    return requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", userPrompt)
        )
    )
}

internal suspend fun requestRecordChat(
    record: PomodoroRecord,
    template: RecordTemplate,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String,
    messages: List<LlmChatMessage>
): String {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 里的记录对话 Agent。回答要具体、可执行、温和，不要编造不存在的数据。")
        if (agent.permissions.dailyRecords) {
            append("\n你可以读取当前这条番茄记录：\n")
            append(record.toPromptContext(template))
        } else {
            append("\n当前 Agent 没有每日记录权限，不能使用这条记录的具体内容，只能根据用户输入做一般建议。")
        }
        if (agent.permissions.todos) {
            append("\n你被允许讨论待办相关安排。")
        }
        if (agent.permissions.templates) {
            append("\n你被允许讨论记录模板和复盘字段。")
        }
    }
    return requestLlmChat(
        provider = provider,
        messages = listOf(LlmChatMessage("system", systemPrompt)) + messages.takeLast(12)
    )
}

internal fun PomodoroRecord.toPromptContext(template: RecordTemplate): String {
    val answersText = answers.joinToString("\n") { answer ->
        val title = template.fields.firstOrNull { it.id == answer.fieldId }?.title ?: "字段"
        "- $title：${answer.value.ifBlank { "未填写" }}"
    }
    return buildString {
        append("任务：$todoTitle\n")
        append("专注时长：$focusMinutes 分钟\n")
        append("结束时间：${formatTime(endedAt)}\n")
        append("模板：$templateName\n")
        append("回答：\n")
        append(answersText.ifBlank { "无回答" })
    }
}

internal fun List<PomodoroRecord>.toDailySummaryContext(templates: List<RecordTemplate>): String {
    val totalMinutes = sumOf { it.focusMinutes }
    val focusTargets = groupingBy { it.todoTitle }.eachCount()
        .entries
        .sortedByDescending { it.value }
        .joinToString("、") { "${it.key} ${it.value} 次" }
    val details = joinToString("\n\n") { record ->
        val template = templates.firstOrNull { it.id == record.templateId }
            ?: RecordTemplate(record.templateId, record.templateName, emptyList())
        record.toPromptContext(template)
    }
    return buildString {
        append("番茄数量：${size}\n")
        append("总专注时长：$totalMinutes 分钟\n")
        append("专注对象统计：${focusTargets.ifBlank { "无" }}\n\n")
        append("记录详情：\n")
        append(details.ifBlank { "无记录" })
    }
}

internal fun hasMissingRequiredAnswers(
    template: RecordTemplate,
    answers: Map<String, String>
): Boolean {
    return template.fields.any { field ->
        field.required && answers[field.id].orEmpty().isBlank()
    }
}
