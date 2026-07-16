package com.protato.app.domain.todo

import com.protato.app.TodoItem

fun List<TodoItem>.sortedForAction(todayKey: String): List<TodoItem> {
    return sortedWith(
        compareBy<TodoItem> { todo -> todo.dueDate.priorityAgainst(todayKey) }
            .thenBy { it.completed }
            .thenByDescending { it.createdAt }
    )
}

fun List<TodoItem>.overdueBefore(dayKey: String): List<TodoItem> {
    return filter { todo ->
        !todo.completed && todo.dueDate != null && todo.dueDate < dayKey
    }
}

private fun String?.priorityAgainst(todayKey: String): Int {
    return when (this) {
        null -> 0
        todayKey -> 1
        else -> 2
    }
}
