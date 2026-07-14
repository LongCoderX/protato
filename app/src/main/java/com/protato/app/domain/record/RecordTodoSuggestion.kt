package com.protato.app.domain.record

import com.protato.app.FieldAnswer
import com.protato.app.TodoItem
import com.protato.app.domain.timer.PendingRecord
import java.util.Locale

internal fun suggestRecordTodoId(
    pendingRecord: PendingRecord,
    answers: List<FieldAnswer>,
    todos: List<TodoItem>
): String? {
    if (todos.isEmpty()) return null
    val source = buildString {
        append(pendingRecord.todoTitle)
        answers.forEach { answer ->
            append(' ')
            append(answer.value)
        }
    }
    val tokens = source.recordMatchTokens()
    if (tokens.isEmpty()) return null
    return todos
        .map { todo ->
            val title = todo.title.lowercase(Locale.getDefault())
            val tokenScore = tokens.count { token -> token in title || title in token }
            val charScore = title.count { char -> char.toString() in source }
            val score = tokenScore * 4 + charScore
            todo to score
        }
        .filter { (_, score) -> score > 0 }
        .sortedWith(
            compareByDescending<Pair<TodoItem, Int>> { it.second }
                .thenByDescending { it.first.createdAt }
        )
        .firstOrNull()
        ?.first
        ?.id
}

internal fun String.recordMatchTokens(): List<String> {
    return lowercase(Locale.getDefault())
        .split(Regex("[\\s，,。；;：:、/\\\\|\\-_*（）()【】\\[\\]{}]+"))
        .map { it.trim() }
        .filter { it.length >= 2 && it !in recordMatchStopWords }
        .distinct()
        .take(12)
}

private val recordMatchStopWords = setOf(
    "未绑定任务",
    "完成",
    "主要",
    "任务",
    "今天",
    "问题",
    "解决",
    "卡点",
    "没有",
    "一下"
)
