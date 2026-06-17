package com.protato.app.ai

import com.protato.app.AppState
import com.protato.app.DEFAULT_NICKNAME
import com.protato.app.TodoItem
import com.protato.app.domain.todo.sortedForAction
import com.protato.app.util.dayKey
import com.protato.app.util.formatDayLabel
import java.util.Locale

internal fun String.looksLikeTodoPlanningRequest(hasPlanningContext: Boolean = false): Boolean {
    val lowered = lowercase(Locale.getDefault())
    if (lowered.looksLikeTodoQuery()) return false
    val questionLike = listOf("什么", "哪些", "几", "吗", "？", "?", "如何", "怎么").any { it in lowered }
    val strongPlanningVerb = listOf(
        "帮我安排",
        "帮我规划",
        "帮我拆",
        "帮我优化",
        "帮我改进",
        "帮我修复",
        "拆成",
        "整理成待办",
        "生成待办",
        "创建待办",
        "新增待办",
        "添加待办",
        "列个计划",
        "做个计划",
        "安排一下",
        "规划一下",
        "优化一下",
        "改进一下",
        "修复一下",
        "帮我做计划",
        "帮我列计划"
    ).any { it in lowered }
    if (strongPlanningVerb) return true
    val goalVerb = listOf("完成", "优化", "改进", "修复", "实现", "开发", "设计").any { it in lowered }
    if (goalVerb && !questionLike && lowered.length >= 4) return true
    if (listOf("安排", "规划", "拆解", "整理").any { it in lowered } && !questionLike) return true
    if (!hasPlanningContext) return false
    if (questionLike) return false
    val weakPlanningVerb = listOf("安排", "规划", "拆解", "整理", "列出", "罗列").any { it in lowered }
    val taskContext = listOf("任务", "待办", "计划", "行程", "事情", "项目", "优化", "交互", "卡片").any { it in lowered }
    return weakPlanningVerb && taskContext
}

internal fun String.looksLikeTodoQuery(): Boolean {
    val lowered = lowercase(Locale.getDefault())
    return listOf(
        "有什么任务",
        "什么任务",
        "有哪些待办",
        "查一下",
        "看看待办",
        "今天还有",
        "今天有什么",
        "未完成",
        "没完成",
        "没有完成",
        "还没做",
        "没做完",
        "已完成",
        "完成了什么",
        "今天做了什么",
        "最近完成了什么",
        "需要做什么",
        "要做什么",
        "该做什么"
    ).any { it in lowered }
}

internal fun localUnifiedAgentReply(
    input: String,
    state: AppState,
    nickname: String
): String {
    val today = dayKey(System.currentTimeMillis())
    val todayTodos = state.todos.filter { it.dueDate == today && !it.completed }
    val todayRecords = state.records.filter { dayKey(it.endedAt) == today }
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    return when {
        "总结" in input -> {
            val minutes = todayRecords.sumOf { it.focusMinutes }
            "$name，今天目前有 ${todayRecords.size} 个番茄、$minutes 分钟专注记录。可以点上面的“生成今日总结”，我会把记录沉淀成一段正式复盘。"
        }
        input.looksLikeTodoQuery() -> {
            localTodoQueryReply(input, state, nickname)
        }
        input.looksLikeTodoPlanningRequest() -> {
            val count = parseTodoSuggestions(input).size
            if (count > 0) {
                "我先把你的想法整理成 $count 个待办草案了。看一下下方卡片，确认后我再写入 TodoList。"
            } else {
                "可以。先把今天最想推进的事情用一两句话丢给我，我会拆成可执行待办，再由你确认添加。"
            }
        }
        todayTodos.isNotEmpty() -> {
            "我在。今天还有 ${todayTodos.size} 个未完成待办。建议先选一个最小、最清楚的任务开一轮番茄；结束后我会继续帮你复盘和收束。"
        }
        else -> {
            "我在。你可以先告诉我今天想完成什么，我会帮你排成待办草案，再陪你用番茄钟一步步推进。"
        }
    }
}

internal fun localTodoQueryReply(
    input: String,
    state: AppState,
    nickname: String
): String {
    val today = dayKey(System.currentTimeMillis())
    val lowered = input.lowercase(Locale.getDefault())
    val includeCompleted = listOf("已完成", "完成了什么", "完成过").any { it in lowered }
    val todos = state.todos
        .filter { todo ->
            val dateMatches = if ("今天" in lowered || "今日" in lowered) {
                todo.dueDate == null || todo.dueDate == today
            } else {
                true
            }
            val statusMatches = if (includeCompleted) todo.completed else !todo.completed
            dateMatches && statusMatches
        }
        .sortedForAction(today)
        .take(8)
    val name = nickname.ifBlank { DEFAULT_NICKNAME }
    if (todos.isEmpty()) {
        return if (includeCompleted) {
            "$name，目前没有查到符合条件的已完成任务。"
        } else {
            "$name，目前没有查到符合条件的未完成任务。"
        }
    }
    val title = if (includeCompleted) "已完成任务" else "未完成任务"
    return buildString {
        append("$name，$title 有 ${todos.size} 个：")
        todos.forEachIndexed { index, todo ->
            append("\n${index + 1}. ${todo.readableLine(today)}")
        }
    }
}

private fun TodoItem.readableLine(today: String): String {
    val dateText = when (dueDate) {
        null -> "收集箱"
        today -> "今天"
        else -> formatDayLabel(dueDate)
    }
    val status = if (completed) "已完成" else "未完成"
    return "$title · $dateText · $status"
}
