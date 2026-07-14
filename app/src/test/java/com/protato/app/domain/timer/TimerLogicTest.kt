package com.protato.app.domain.timer

import com.protato.app.AppState
import com.protato.app.TimerMode
import com.protato.app.TimerSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerLogicTest {
    @Test
    fun backgroundCompletionUsesSameIdempotentTransition() {
        val session = TimerSession(
            mode = TimerMode.Focus,
            todoId = "todo-1",
            todoTitle = "写测试",
            startedAt = 1_000L,
            endsAt = 61_000L,
            totalSeconds = 60,
            templateId = "template-1"
        )
        val initialState = AppState(activeSession = session)

        val completedState = completeTimer(initialState, nowMillis = session.endsAt)
        val duplicateState = completeTimer(completedState, nowMillis = session.endsAt + 1_000L)

        assertNull(completedState.activeSession)
        assertEquals(session.todoId, completedState.pendingRecord?.todoId)
        assertEquals(1, listOfNotNull(completedState.pendingRecord).size)
        assertEquals(completedState, duplicateState)
    }
}
