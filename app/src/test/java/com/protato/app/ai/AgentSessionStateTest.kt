package com.protato.app.ai

import com.protato.app.domain.todo.TodoDraft
import com.protato.app.domain.todo.TodoEditDraft
import com.protato.app.domain.todo.todoDraftKey
import com.protato.app.domain.todo.todoEditDraftKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentSessionStateTest {
    @Test
    fun completeTurnStoresPendingActions() {
        val draft = TodoDraft(title = "写周报")
        val edit = TodoEditDraft(id = "todo-1", title = "改标题")

        val state = AgentSessionState().completeTurn(
            AgentTurnResult(
                reply = "请确认",
                todoDrafts = listOf(draft),
                todoEdits = listOf(edit)
            )
        )

        assertEquals(listOf(draft), state.pendingTodos)
        assertEquals(listOf(edit), state.pendingTodoEdits)
        assertTrue(state.hasPendingActions)
    }

    @Test
    fun acceptTodoDraftsClearsPendingAndStoresHandledKeys() {
        val draft = TodoDraft(title = "写周报")
        val state = AgentSessionState(pendingTodos = listOf(draft)).acceptTodoDrafts()

        assertEquals(emptyList<TodoDraft>(), state.pendingTodos)
        assertEquals(setOf(todoDraftKey(draft)), state.handledTodoDraftKeySet)
        assertFalse(state.hasPendingActions)
    }

    @Test
    fun rejectTodoEditsClearsPendingAndStoresHandledKeys() {
        val edit = TodoEditDraft(id = "todo-1", title = "改标题")
        val state = AgentSessionState(pendingTodoEdits = listOf(edit)).rejectTodoEdits()

        assertEquals(emptyList<TodoEditDraft>(), state.pendingTodoEdits)
        assertEquals(setOf(todoEditDraftKey(edit)), state.handledTodoEditKeySet)
        assertFalse(state.hasPendingActions)
    }

    @Test
    fun resetClearsPendingAndHandledKeys() {
        val state = AgentSessionState(
            pendingTodos = listOf(TodoDraft(title = "写周报")),
            pendingTodoEdits = listOf(TodoEditDraft(id = "todo-1", title = "改标题")),
            handledTodoDraftKeys = listOf("draft"),
            handledTodoEditKeys = listOf("edit")
        ).reset()

        assertEquals(AgentSessionState(), state)
    }
}
