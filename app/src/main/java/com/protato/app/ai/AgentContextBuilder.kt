package com.protato.app.ai

import com.protato.app.AppState
import com.protato.app.AgentDataPermissions
import com.protato.app.DEFAULT_NICKNAME
import com.protato.app.TodoItem
import com.protato.app.domain.todo.sortedForAction
import com.protato.app.util.dayKey
import com.protato.app.util.formatClockTime
import com.protato.app.util.formatTime

internal fun AppState.toUnifiedAgentContext(nickname: String): String {
    return toUnifiedAgentContext(
        nickname = nickname,
        permissions = AgentDataPermissions(
            dailyRecords = true,
            todos = true,
            templates = true
        )
    )
}

internal fun AppState.toUnifiedAgentContext(
    nickname: String,
    permissions: AgentDataPermissions
): String {
    val today = dayKey(System.currentTimeMillis())
    val todayTodos = todos
        .filter { it.dueDate == null || it.dueDate == today }
        .sortedForAction(today)
    val todayRecords = records.filter { dayKey(it.endedAt) == today }
    val historyCutoff = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
    val historicalTodos = todos
        .filter { it.createdAt >= historyCutoff || it.dueDate != null }
        .sortedWith(compareBy<TodoItem> { it.completed }.thenByDescending { it.dueDate ?: "9999-99-99" }.thenByDescending { it.createdAt })
        .take(180)
    val historicalRecords = records
        .filter { it.endedAt >= historyCutoff }
        .sortedByDescending { it.endedAt }
        .take(120)
    val todoText = todayTodos.take(12).joinToString("\n") { todo ->
        "- id=${todo.id} [${if (todo.completed) "x" else " "}] ${todo.title}（日期：${todo.dueDate ?: "无"}，类型：${todo.complexity.label()}，提醒：${todo.reminderAt?.let(::formatTime) ?: "无"}，${todo.plannedPomodoros} 个番茄，子任务：${todo.subtaskContext()}，总结：${todo.summary.ifBlank { "无" }}，标签：${todo.tags.joinToString("、").ifBlank { "无" }}）"
    }.ifBlank { "无" }
    val recordText = todayRecords.takeLast(8).joinToString("\n") { record ->
        "- ${formatClockTime(record.endedAt)} ${record.todoTitle}，${record.focusMinutes} 分钟"
    }.ifBlank { "无" }
    val templateText = templates.take(6).joinToString("、") { it.name }.ifBlank { "无" }
    val historyTodoText = historicalTodos.joinToString("\n") { todo ->
        val status = if (todo.completed) "已完成" else "未完成"
        val date = todo.dueDate ?: dayKey(todo.createdAt)
        val review = todo.completionReview?.let {
            "，复盘：问题=${it.problems.ifBlank { "无" }}；解决=${it.solution.ifBlank { "无" }}；卡点=${it.blockers.ifBlank { "无" }}"
        }.orEmpty()
        "- id=${todo.id} $date [$status] ${todo.title}（类型：${todo.complexity.label()}，提醒：${todo.reminderAt?.let(::formatTime) ?: "无"}，子任务：${todo.subtaskContext()}，总结：${todo.summary.ifBlank { "无" }}，${todo.tags.joinToString("、").ifBlank { "无标签" }}，${todo.plannedPomodoros} 个番茄$review）"
    }.ifBlank { "无" }
    val historyRecordText = historicalRecords.joinToString("\n") { record ->
        "- ${dayKey(record.endedAt)} ${record.todoTitle}，${record.focusMinutes} 分钟"
    }.ifBlank { "无" }
    return buildString {
        append("用户昵称：${nickname.ifBlank { DEFAULT_NICKNAME }}\n")
        append("今天日期：$today\n")
        if (permissions.todos) {
            append("今日待办：\n$todoText\n")
            append("近 365 天待办历史（用于识别同类事项和行程关联）：\n$historyTodoText\n")
        } else {
            append("今日待办：未授权\n")
            append("近 365 天待办历史：未授权\n")
        }
        if (permissions.dailyRecords) {
            append("今日番茄记录：\n$recordText\n")
            append("近 365 天番茄记录摘要：\n$historyRecordText\n")
        } else {
            append("今日番茄记录：未授权\n")
            append("近 365 天番茄记录摘要：未授权\n")
        }
        if (permissions.templates) {
            append("可用复盘模板：$templateText\n")
        } else {
            append("可用复盘模板：未授权\n")
        }
        append("长期记忆：${agentMemory.ifBlank { "暂无" }}")
    }
}

private fun TodoItem.subtaskContext(): String {
    if (subtasks.isEmpty()) return "无"
    return subtasks.joinToString("；") { subtask ->
        "${if (subtask.completed) "已完成" else "未完成"} ${subtask.title}"
    }
}

private fun com.protato.app.TodoComplexity.label(): String {
    return when (this) {
        com.protato.app.TodoComplexity.Simple -> "简单"
        com.protato.app.TodoComplexity.Complex -> "复杂"
    }
}
