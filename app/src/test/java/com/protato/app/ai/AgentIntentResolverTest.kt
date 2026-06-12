package com.protato.app.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AgentIntentResolverTest {
    private val resolver = AgentIntentResolver()

    @Test
    fun confirmsPendingActionOnlyWhenPendingActionExists() {
        val result = resolver.resolve(
            AgentIntentRequest(
                userText = "可以，就这样",
                hasPendingActions = true
            )
        )

        assertTrue(result.intent is AgentIntent.ConfirmPendingAction)
        assertTrue(result.confidence > 0.9f)
    }

    @Test
    fun asksClarificationForPendingReferenceWithoutPendingAction() {
        val result = resolver.resolve(
            AgentIntentRequest(
                userText = "就按第二个来",
                hasPendingActions = false
            )
        )

        assertTrue(result.intent is AgentIntent.AskClarification)
        assertTrue(result.confidence < 0.5f)
    }

    @Test
    fun resolvesTodoPlanningRequest() {
        val result = resolver.resolve(
            AgentIntentRequest(userText = "明天帮我安排论文和健身")
        )

        assertTrue(result.intent is AgentIntent.PlanTodos)
    }
}
