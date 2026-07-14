package com.protato.app

import com.protato.app.domain.todo.summarizeTodoWithSubtasks
import com.protato.app.domain.todo.toggleSubtask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoSubtaskTest {
    @Test
    fun toggleSubtaskUpdatesProgressSummary() {
        val todo = TodoItem(
            id = "todo-1",
            title = "大任务",
            subtasks = listOf(
                TodoSubtask(id = "sub-1", title = "第一步"),
                TodoSubtask(id = "sub-2", title = "第二步")
            )
        )

        val updated = todo.toggleSubtask("sub-1")

        assertEquals(true, updated.subtasks.first().completed)
        assertTrue(updated.summary.contains("1/2 个子任务完成"))
        assertTrue(updated.summary.contains("第二步"))
    }

    @Test
    fun summarizeTodoWithSubtasksHandlesCompletedProject() {
        val todo = TodoItem(
            id = "todo-1",
            title = "大任务",
            subtasks = listOf(
                TodoSubtask(id = "sub-1", title = "第一步", completed = true)
            )
        )

        val summary = summarizeTodoWithSubtasks(todo)

        assertEquals("1/1 个子任务完成。所有子任务已完成，可以进入最终复盘。", summary)
    }
}
