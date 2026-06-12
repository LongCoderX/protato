package com.protato.app.ai

import com.protato.app.TodoDraft
import com.protato.app.util.normalizedTodoDate
import com.protato.app.util.parseTodoTags
import java.util.Locale

internal fun parseTodoSuggestions(text: String): List<TodoDraft> {
    return text
        .split("\n", "；", ";", "。")
        .map {
            it.trim()
                .replace(Regex("^[-*•\\d.、\\s]+"), "")
                .trim()
        }
        .filter { it.isNotBlank() }
        .map { parseTodoDraftLine(it) }
        .filter { it.title.isNotBlank() }
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
        .take(12)
}

internal fun parseTodoDraftLine(line: String): TodoDraft {
    val structuredParts = line.split("|").map { it.trim() }
    if (structuredParts.size >= 3) {
        val title = structuredParts[0].cleanTodoTitle()
        val dueDate = normalizedTodoDate(structuredParts[1])
        val tags = parseTodoTags(structuredParts[2])
            .ifEmpty { inferTodoTags(title) }
        val plannedPomodoros = structuredParts.getOrNull(3)?.let(::parsePomodoroCount)
            ?: inferPlannedPomodoros(title)
        return TodoDraft(
            title = title,
            dueDate = dueDate ?: inferTodoDate(line),
            tags = tags,
            plannedPomodoros = plannedPomodoros
        )
    }

    val tags = extractTodoHashTags(line)
    val title = line
        .replace(Regex("#[\\p{L}\\p{N}_-]+"), "")
        .cleanTodoTitle()
    return TodoDraft(
        title = title,
        dueDate = inferTodoDate(line),
        tags = tags.ifEmpty { inferTodoTags(title) },
        plannedPomodoros = parsePomodoroCount(line) ?: inferPlannedPomodoros(title)
    )
}

internal fun String.cleanTodoTitle(): String {
    return normalizeTodoTitle()
}

internal fun String.normalizeTodoTitle(): String {
    val cleaned = replace(Regex("\\|.*$"), "")
        .replace(Regex("\\d{4}-\\d{2}-\\d{2}"), "")
        .replace(Regex("(?:今天|明天|后天|周末|今晚|上午|下午|晚上)(?:要|想|需要|准备)?"), "")
        .replace(Regex("[#＃][\\p{L}\\p{N}_-]+"), "")
        .replace(Regex("^(帮我|我想|我要|需要|准备|计划|安排|待办|任务|todo)[:：、\\s]*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*(因为|所以|但是|然后|顺便)\\s*"), " ")
        .replace(Regex("\\s+"), " ")
        .replace(Regex("^[,，。；;、\\s]+|[,，。；;、\\s]+$"), "")
        .trim()
    return cleaned.toReadableTodoTitle().take(40)
}

internal fun String.normalizeTodoPhrase(): String {
    return replace(Regex("^(帮我|我想|我要|需要|准备|计划|安排)[:：、\\s]*"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(24)
}

internal fun String.toReadableTodoTitle(): String {
    if (isBlank()) return ""
    val trimmed = trim()
    val hasKnownVerb = listOf(
        "整理",
        "完成",
        "回复",
        "检查",
        "阅读",
        "学习",
        "复盘",
        "写",
        "改",
        "做",
        "准备",
        "沟通",
        "确认",
        "提交",
        "设计",
        "开发"
    ).any { trimmed.startsWith(it) }
    return if (hasKnownVerb) trimmed else "${inferTodoAction(trimmed)}$trimmed"
}

internal fun inferTodoAction(text: String): String {
    val lowered = text.lowercase(Locale.getDefault())
    return when {
        listOf("邮件", "消息", "客户", "沟通").any { it in lowered } -> "回复"
        listOf("文档", "资料", "材料", "报告", "周报").any { it in lowered } -> "整理"
        listOf("代码", "功能", "bug", "接口", "页面").any { it in lowered } -> "开发"
        listOf("课程", "书", "文章", "论文").any { it in lowered } -> "阅读"
        listOf("复盘", "总结").any { it in lowered } -> "完成"
        else -> "完成"
    }
}

internal fun extractTodoHashTags(value: String): List<String> {
    return Regex("[#＃]([\\p{L}\\p{N}_-]+)")
        .findAll(value)
        .map { it.groupValues[1].take(24) }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.getDefault()) }
        .toList()
}
