package com.protato.app.ai

import com.protato.app.AppState
import com.protato.app.TodoItem
import com.protato.app.util.dayKey
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentTurnServiceTest {
    @Test
    fun returnsLocalTodoDraftsWhenProviderIsUnavailable() = runBlocking {
        val result = runAgentTurn(
            text = "明天安排写周报和健身",
            userState = AppState(todos = emptyList(), agentMessages = emptyList()),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("待办草案"))
        assertTrue(result.todoDrafts.isNotEmpty())
        assertTrue(result.todoEdits.isEmpty())
        assertEquals(null, result.errorText)
    }

    @Test
    fun filtersExistingLocalTodoDrafts() = runBlocking {
        val existing = TodoItem(id = "todo-1", title = "写周报和健身")
        val result = runAgentTurn(
            text = "安排写周报和健身",
            userState = AppState(todos = listOf(existing), agentMessages = emptyList()),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertEquals(emptyList<com.protato.app.TodoDraft>(), result.todoDrafts)
        assertTrue(result.reply.contains("不会重复提交"))
    }

    @Test
    fun queryingTodayTodosDoesNotCreateConfirmationCards() = runBlocking {
        val today = dayKey(System.currentTimeMillis())
        val result = runAgentTurn(
            text = "今天有什么任务？",
            userState = AppState(
                todos = listOf(
                    TodoItem(id = "todo-1", title = "写测试", dueDate = today),
                    TodoItem(id = "todo-2", title = "整理无日期任务", dueDate = null)
                ),
                agentMessages = emptyList()
            ),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("写测试"))
        assertTrue(result.reply.contains("整理无日期任务"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun weakTaskQuestionDoesNotCreateConfirmationCards() = runBlocking {
        val today = dayKey(System.currentTimeMillis())
        val result = runAgentTurn(
            text = "我今天需要做什么？",
            userState = AppState(
                todos = listOf(TodoItem(id = "todo-1", title = "写测试", dueDate = today)),
                agentMessages = emptyList()
            ),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("写测试"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun organizingUnfinishedTodosDoesNotCreateConfirmationCards() = runBlocking {
        val result = runAgentTurn(
            text = "帮我整理那些没有完成任务的东西",
            userState = AppState(
                todos = listOf(
                    TodoItem(id = "todo-1", title = "整理周报"),
                    TodoItem(id = "todo-2", title = "已完成事项", completed = true)
                ),
                agentMessages = emptyList()
            ),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("整理周报"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun movingOpenTodosToTodayCreatesTodoEditDrafts() = runBlocking {
        val today = dayKey(System.currentTimeMillis())
        val result = runAgentTurn(
            text = "把未完成任务都挪到今天",
            userState = AppState(
                todos = listOf(
                    TodoItem(id = "todo-1", title = "无日期任务", dueDate = null),
                    TodoItem(id = "todo-2", title = "未来任务", dueDate = "2026-06-20"),
                    TodoItem(id = "todo-3", title = "今日任务", dueDate = today),
                    TodoItem(id = "todo-4", title = "已完成任务", completed = true, dueDate = null)
                ),
                agentMessages = emptyList()
            ),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertEquals(listOf("todo-1", "todo-2"), result.todoEdits.map { it.id })
        assertTrue(result.todoEdits.all { it.dueDate == today })
        assertTrue(result.todoDrafts.isEmpty())
    }

    @Test
    fun pendingConfirmationDoesNotCreateNewDrafts() = runBlocking {
        val result = runAgentTurn(
            text = "可以，就这样",
            userState = AppState(todos = emptyList(), agentMessages = emptyList()),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            hasPendingActions = true
        )

        assertTrue(result.reply.contains("确认按钮"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }
}
