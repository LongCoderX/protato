package com.protato.app.ai

import com.protato.app.AppState
import com.protato.app.DEFAULT_NICKNAME
import com.protato.app.util.dayKey
import java.util.Locale

internal fun String.looksLikeTodoPlanningRequest(): Boolean {
    val lowered = lowercase(Locale.getDefault())
    return listOf(
        "待办",
        "todo",
        "任务",
        "计划",
        "行程",
        "安排",
        "今天",
        "明天",
        "要做",
        "帮我拆"
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
