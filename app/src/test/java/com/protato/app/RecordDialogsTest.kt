package com.protato.app

import com.protato.app.domain.timer.PendingRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordDialogsTest {
    @Test
    fun suggestRecordTodoIdMatchesReviewTextToDatedTodo() {
        val pendingRecord = PendingRecord(
            todoId = null,
            todoTitle = "未绑定任务",
            startedAt = 1_000L,
            endedAt = 2_500L,
            focusMinutes = 25,
            templateId = "template-1"
        )
        val todos = listOf(
            TodoItem(id = "todo-1", title = "整理论文资料", dueDate = "2026-06-14"),
            TodoItem(id = "todo-2", title = "健身", dueDate = "2026-06-14")
        )
        val answers = listOf(FieldAnswer(fieldId = "field-1", value = "刚才整理了论文资料的大纲"))

        val suggestion = suggestRecordTodoId(pendingRecord, answers, todos)

        assertEquals("todo-1", suggestion)
    }

    @Test
    fun suggestRecordTodoIdReturnsNullWithoutMatch() {
        val pendingRecord = PendingRecord(
            todoId = null,
            todoTitle = "未绑定任务",
            startedAt = 1_000L,
            endedAt = 2_500L,
            focusMinutes = 25,
            templateId = "template-1"
        )

        val suggestion = suggestRecordTodoId(
            pendingRecord = pendingRecord,
            answers = listOf(FieldAnswer(fieldId = "field-1", value = "写了一点随手记录")),
            todos = listOf(TodoItem(id = "todo-1", title = "健身", dueDate = "2026-06-14"))
        )

        assertNull(suggestion)
    }
}
