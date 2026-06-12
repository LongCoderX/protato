package com.protato.app.ai

import com.protato.app.AppState
import com.protato.app.TodoItem
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
}
