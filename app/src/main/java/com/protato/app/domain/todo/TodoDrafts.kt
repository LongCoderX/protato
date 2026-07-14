package com.protato.app.domain.todo

import java.util.Locale

import com.protato.app.TodoItem
import com.protato.app.TodoComplexity
import com.protato.app.TodoSubtask
import com.protato.app.util.newId

internal data class TodoDraft(
    val title: String,
    val dueDate: String? = null,
    val reminderAt: Long? = null,
    val tags: List<String> = emptyList(),
    val plannedPomodoros: Int = 1
) {
    fun toTodoItem(): TodoItem {
        return TodoItem(
            id = newId(),
            title = title.trim(),
            dueDate = dueDate,
            reminderAt = reminderAt,
            tags = tags,
            plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
        )
    }
}

internal fun todoDraftKey(draft: TodoDraft): String {
    return todoIdentityKey(draft.title, draft.dueDate)
}

internal fun todoItemKey(todo: TodoItem): String {
    return todoIdentityKey(todo.title, todo.dueDate)
}

internal fun todoIdentityKey(title: String, dueDate: String?): String {
    return "${title.trim().lowercase(Locale.getDefault())}|${dueDate.orEmpty()}"
}

internal fun List<TodoDraft>.filterNewTodoDrafts(
    existingTodos: List<TodoItem>,
    handledKeys: Set<String>
): List<TodoDraft> {
    val existingKeys = existingTodos.map(::todoItemKey).toSet()
    return filter { draft ->
        val key = todoDraftKey(draft)
        key !in handledKeys && key !in existingKeys
    }
}

internal data class TodoEditDraft(
    val id: String,
    val title: String? = null,
    val dueDate: String? = null,
    val reminderAt: Long? = null,
    val tags: List<String>? = null,
    val plannedPomodoros: Int? = null,
    val completed: Boolean? = null,
    val complexity: TodoComplexity? = null,
    val subtasks: List<TodoSubtask>? = null
) {
    fun applyTo(todo: TodoItem): TodoItem {
        val updated = todo.copy(
            title = title ?: todo.title,
            dueDate = dueDate ?: todo.dueDate,
            reminderAt = reminderAt ?: todo.reminderAt,
            tags = tags ?: todo.tags,
            plannedPomodoros = plannedPomodoros ?: todo.plannedPomodoros,
            completed = completed ?: todo.completed,
            complexity = complexity ?: todo.complexity,
            subtasks = subtasks ?: todo.subtasks
        )
        return if (subtasks != null) {
            updated.copy(summary = summarizeTodoWithSubtasks(updated))
        } else {
            updated
        }
    }

    fun previewTitle(original: TodoItem?): String {
        return title ?: original?.title ?: "待办更新"
    }

    fun toPreviewDraft(original: TodoItem?): TodoDraft {
        return TodoDraft(
            title = previewTitle(original),
            dueDate = dueDate ?: original?.dueDate,
            reminderAt = reminderAt ?: original?.reminderAt,
            tags = tags ?: original?.tags.orEmpty(),
            plannedPomodoros = plannedPomodoros ?: original?.plannedPomodoros ?: 1
        )
    }
}

internal fun todoEditDraftKey(edit: TodoEditDraft): String {
    return listOf(
        edit.id,
        edit.title.orEmpty(),
        edit.dueDate.orEmpty(),
        edit.reminderAt?.toString().orEmpty(),
        edit.tags?.joinToString(",").orEmpty(),
        edit.plannedPomodoros?.toString().orEmpty(),
        edit.completed?.toString().orEmpty(),
        edit.complexity?.name.orEmpty(),
        edit.subtasks?.joinToString(",") { "${it.title}:${it.completed}" }.orEmpty()
    ).joinToString("|").lowercase(Locale.getDefault())
}

internal fun List<TodoEditDraft>.filterNewTodoEdits(
    existingTodos: List<TodoItem>,
    handledKeys: Set<String>
): List<TodoEditDraft> {
    val todosById = existingTodos.associateBy { it.id }
    return filter { edit ->
        val original = todosById[edit.id] ?: return@filter false
        if (original.completed) return@filter false
        val updated = edit.applyTo(original)
        todoEditDraftKey(edit) !in handledKeys && updated != original
    }
}

internal fun Set<String>.toggle(itemId: String): Set<String> {
    return if (itemId in this) this - itemId else this + itemId
}

internal fun TodoItem.toggleSubtask(subtaskId: String): TodoItem {
    val updatedSubtasks = subtasks.map { subtask ->
        if (subtask.id == subtaskId) subtask.copy(completed = !subtask.completed) else subtask
    }
    return copy(
        subtasks = updatedSubtasks,
        summary = summarizeTodoWithSubtasks(copy(subtasks = updatedSubtasks))
    )
}

internal fun summarizeTodoWithSubtasks(todo: TodoItem): String {
    if (todo.subtasks.isEmpty()) return todo.summary
    val completedCount = todo.subtasks.count { it.completed }
    val openSubtasks = todo.subtasks.filterNot { it.completed }.take(3)
    val progressText = "$completedCount/${todo.subtasks.size} 个子任务完成"
    val nextText = if (openSubtasks.isEmpty()) {
        "所有子任务已完成，可以进入最终复盘。"
    } else {
        "下一步：${openSubtasks.joinToString("、") { it.title }}。"
    }
    return "$progressText。$nextText"
}
