package com.protato.app.ai

import com.protato.app.domain.todo.TodoDraft
import com.protato.app.util.normalizedTodoDate
import com.protato.app.util.parseTodoTags
import java.util.Locale

internal fun parseTodoPlanDrafts(text: String): List<TodoPlanDraft> {
    if (text.trim().equals("NONE", ignoreCase = true)) return emptyList()
    return text.lines()
        .map { it.trim().trim('-', '*', '•', ' ') }
        .filter { it.startsWith("TASK", ignoreCase = true) }
        .mapNotNull { line ->
            val parts = line.split("|").map { it.trim() }
            if (parts.size < 6) return@mapNotNull null
            val action = parts[1].normalizeTodoPhrase()
            val objectText = parts[2].normalizeTodoPhrase()
            val title = listOf(action, objectText).joinToString(" ").normalizeTodoTitle()
            if (title.isBlank()) return@mapNotNull null
            TodoPlanDraft(
                action = action.ifBlank { inferTodoAction(objectText) },
                objectText = objectText,
                dueDate = normalizedTodoDate(parts[3]) ?: inferTodoDate(line),
                tags = parseTodoTags(parts[4]).ifEmpty { inferTodoTags(title) },
                plannedPomodoros = parsePomodoroCount(parts[5]) ?: inferPlannedPomodoros(title)
            )
        }
        .filter { it.toTodoDraft().title.isNotBlank() }
        .distinctBy { it.toTodoDraft().title.lowercase(Locale.getDefault()) }
}

internal data class TodoPlanDraft(
    val action: String,
    val objectText: String,
    val dueDate: String? = null,
    val tags: List<String> = emptyList(),
    val plannedPomodoros: Int = 1
) {
    fun toTodoDraft(): TodoDraft {
        val title = if (action.isNotBlank() && objectText.isNotBlank()) {
            "$action$objectText".normalizeTodoPhrase().take(40)
        } else {
            (objectText.ifBlank { action }).normalizeTodoTitle()
        }
        return TodoDraft(
            title = title,
            dueDate = dueDate,
            tags = tags.ifEmpty { inferTodoTags(title) },
            plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
        )
    }
}

internal fun localPlanTodoDrafts(rawInput: String): List<TodoDraft> {
    val candidates = rawInput
        .split(Regex("[\\n；;]+"))
        .flatMap { splitLooseTodoSentence(it) }
        .map { it.normalizeTodoTitle() }
        .filter { it.isNotBlank() }
        .map { title ->
            TodoDraft(
                title = title,
                dueDate = inferTodoDate(rawInput),
                tags = inferTodoTags(title),
                plannedPomodoros = parsePomodoroCount(rawInput) ?: inferPlannedPomodoros(title)
            )
        }
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
    return candidates.ifEmpty {
        val title = rawInput.normalizeTodoTitle()
        if (title.isBlank()) emptyList() else listOf(
            TodoDraft(
                title = title,
                dueDate = inferTodoDate(rawInput),
                tags = inferTodoTags(title),
                plannedPomodoros = parsePomodoroCount(rawInput) ?: inferPlannedPomodoros(title)
            )
        )
    }.take(8)
}

internal fun splitLooseTodoSentence(value: String): List<String> {
    val normalized = value
        .replace(Regex("[，,。]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    if (normalized.isBlank()) return emptyList()
    val strongSplit = Regex("(?:^|\\s)(?:另外|还有|以及|再加上)(?:\\s|$)")
    val parts = normalized.split(strongSplit)
        .map { it.trim() }
        .filter { it.isNotBlank() }
    return parts.ifEmpty { listOf(normalized) }
}
