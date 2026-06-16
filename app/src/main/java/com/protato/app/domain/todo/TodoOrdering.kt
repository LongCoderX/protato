package com.protato.app.domain.todo

import com.protato.app.TodoItem

fun List<TodoItem>.sortedForAction(todayKey: String): List<TodoItem> {
    return sortedWith(
        compareBy<TodoItem> { todo -> todo.dueDate.priorityAgainst(todayKey) }
            .thenBy { it.completed }
            .thenByDescending { it.createdAt }
    )
}

private fun String?.priorityAgainst(todayKey: String): Int {
    return when (this) {
        null -> 0
        todayKey -> 1
        else -> 2
    }
}
