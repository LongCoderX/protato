package com.protato.app.ai

import com.protato.app.TodoItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordAiLogicTest {
    @Test
    fun dailyTodoSummaryIncludesDueAndOverdueTaskStatus() {
        val context = listOf(
            TodoItem(id = "due-open", title = "今天未完成", dueDate = "2026-07-16"),
            TodoItem(id = "due-done", title = "今天已完成", dueDate = "2026-07-16", completed = true),
            TodoItem(id = "overdue", title = "昨天遗留", dueDate = "2026-07-15"),
            TodoItem(id = "future", title = "明天任务", dueDate = "2026-07-17"),
            TodoItem(id = "inbox", title = "收集箱任务")
        ).toDailyTodoSummaryContext("2026-07-16")

        assertTrue(context.contains("当天任务：2 个，完成 1 个，未完成 1 个"))
        assertTrue(context.contains("[未完成] 今天未完成"))
        assertTrue(context.contains("[已完成] 今天已完成"))
        assertTrue(context.contains("截至总结生成时逾期未完成：1 个"))
        assertTrue(context.contains("[原日期 2026-07-15] 昨天遗留"))
        assertFalse(context.contains("明天任务"))
        assertFalse(context.contains("收集箱任务"))
    }

    @Test
    fun localDailySummaryReportsOverdueOpenTasksWithoutPomodoroRecords() {
        val summary = localDailySummary(
            dayKey = "2026-07-16",
            records = emptyList(),
            todos = listOf(TodoItem(id = "overdue", title = "昨天遗留", dueDate = "2026-07-15")),
            nickname = "测试用户"
        )

        assertTrue(summary.content.contains("逾期未完成：**1 个**"))
        assertTrue(summary.content.contains("昨天遗留"))
    }
}
