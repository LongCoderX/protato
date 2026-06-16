package com.protato.app.ai

import com.protato.app.AgentConversationMessage
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

    @Test
    fun resolvesTodayTodoQuestionAsQuery() {
        val result = resolver.resolve(
            AgentIntentRequest(userText = "今天有什么任务？")
        )

        assertTrue(result.intent is AgentIntent.QueryTodos)
    }

    @Test
    fun resolvesOrganizeUnfinishedTodosAsQuery() {
        val result = resolver.resolve(
            AgentIntentRequest(userText = "帮我整理那些没有完成任务的东西")
        )

        assertTrue(result.intent is AgentIntent.QueryTodos)
    }

    @Test
    fun resolvesMoveOpenTodosToToday() {
        val result = resolver.resolve(
            AgentIntentRequest(userText = "把未完成任务都挪到今天")
        )

        assertTrue(result.intent is AgentIntent.MoveOpenTodosToToday)
    }

    @Test
    fun doesNotTreatWeakTaskWordsAsPlanning() {
        val result = resolver.resolve(
            AgentIntentRequest(userText = "我今天需要做什么？")
        )

        assertTrue(result.intent is AgentIntent.Chat || result.intent is AgentIntent.QueryTodos)
    }

    @Test
    fun resolvesWeakPlanningOnlyWithRecentPlanningContext() {
        val result = resolver.resolve(
            AgentIntentRequest(
                userText = "那就安排论文和健身",
                recentMessages = listOf(
                    AgentConversationMessage(
                        id = "m1",
                        role = "assistant",
                        content = "你可以告诉我想生成哪些待办，我会整理成计划。"
                    )
                )
            )
        )

        assertTrue(result.intent is AgentIntent.PlanTodos)
    }

    @Test
    fun resolvesCompletedHistoryQuestionAsQuery() {
        val result = resolver.resolve(
            AgentIntentRequest(userText = "我今天做了什么？")
        )

        assertTrue(result.intent is AgentIntent.QueryTodos)
    }

    @Test
    fun resolvesFollowUpPlanningQuestionAsPlanning() {
        val result = resolver.resolve(
            AgentIntentRequest(
                userText = "那就安排论文和健身",
                recentMessages = listOf(
                    AgentConversationMessage(
                        id = "m1",
                        role = "assistant",
                        content = "我可以把你的想法整理成待办。"
                    )
                )
            )
        )

        assertTrue(result.intent is AgentIntent.PlanTodos)
    }
}
