package com.protato.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentActionParserTest {
    @Test
    fun parsesFencedTodoProposalIntoActions() {
        val response = parseAgentToolResponse(
            """
            我整理了两个待办，请确认。
            ```protato_tool
            {"tool":"propose_todos","todos":[{"title":"整理项目周报","dueDate":"2026-06-13","tags":["工作"],"plannedPomodoros":2},{"title":"晚上健身","dueDate":null,"tags":["健康"],"plannedPomodoros":1}]}
            ```
            """.trimIndent()
        )

        assertEquals("我整理了两个待办，请确认。", response.visibleReply)
        assertEquals(2, response.todoDrafts.size)
        assertEquals("整理项目周报", response.todoDrafts[0].title)
        assertEquals("2026-06-13", response.todoDrafts[0].dueDate)
        assertTrue(response.actions.any { it is AgentAction.Reply })
        assertTrue(response.actions.any { it is AgentAction.ProposeTodos })
    }

    @Test
    fun parsesNativeToolCallsIntoTodoUpdates() {
        val response = parseAgentToolResponse(
            """
            {"tool_calls":[{"function":{"name":"propose_todo_updates","arguments":"{\"edits\":[{\"id\":\"todo-1\",\"title\":\"整理新版周报\",\"plannedPomodoros\":3}]}"}}]}
            """.trimIndent()
        )

        assertEquals(1, response.todoEdits.size)
        assertEquals("todo-1", response.todoEdits.single().id)
        assertEquals("整理新版周报", response.todoEdits.single().title)
        assertEquals(3, response.todoEdits.single().plannedPomodoros)
        assertTrue(response.actions.any { it is AgentAction.ProposeTodoUpdates })
    }

    @Test
    fun ignoresUnknownTools() {
        val response = parseAgentToolResponse(
            """
            ```protato_tool
            {"tool":"delete_everything","todos":[{"title":"不应出现"}]}
            ```
            """.trimIndent()
        )

        assertTrue(response.todoDrafts.isEmpty())
        assertTrue(response.todoEdits.isEmpty())
        assertTrue(response.actions.isEmpty())
    }
}
