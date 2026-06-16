package com.protato.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppStateJsonCodecTest {
    @Test
    fun encodeThenDecodePreservesAppStateFields() {
        val template = RecordTemplate(
            id = "template-review",
            name = "深度复盘",
            fields = listOf(
                TemplateField(
                    id = "field-result",
                    title = "完成了什么",
                    type = FieldType.ShortAnswer,
                    required = true
                ),
                TemplateField(
                    id = "field-energy",
                    title = "状态",
                    type = FieldType.SingleChoice,
                    options = listOf("专注", "分心")
                )
            )
        )
        val state = AppState(
            todos = listOf(
                TodoItem(
                    id = "todo-1",
                    title = "拆分数据层",
                    completed = true,
                    createdAt = 1_000L,
                    tags = listOf("refactor", "data"),
                    dueDate = "2026-06-12",
                    plannedPomodoros = 3,
                    reminderAt = 2_000L,
                    complexity = TodoComplexity.Complex,
                    completionReview = TodoCompletionReview(
                        problems = "状态入口太多",
                        solution = "收敛到 reducer",
                        blockers = "还需要继续拆 UI",
                        completedAt = 2_500L
                    ),
                    subtasks = listOf(
                        TodoSubtask(id = "sub-1", title = "拆 codec", completed = true),
                        TodoSubtask(id = "sub-2", title = "补测试")
                    ),
                    summary = "1/2 个子任务完成。下一步：补测试。"
                )
            ),
            templates = listOf(template),
            records = listOf(
                PomodoroRecord(
                    id = "record-1",
                    todoId = "todo-1",
                    todoTitle = "拆分数据层",
                    templateId = template.id,
                    templateName = template.name,
                    startedAt = 3_000L,
                    endedAt = 4_500L,
                    focusMinutes = 25,
                    answers = listOf(FieldAnswer("field-result", "完成 codec 拆分"))
                )
            ),
            dailySummaries = listOf(
                DailySummary(
                    dayKey = "2026-06-12",
                    generatedAt = 5_000L,
                    title = "重构日",
                    content = "状态层和数据层继续收敛。",
                    pomodoroCount = 4,
                    focusMinutes = 100
                )
            ),
            focusMinutes = 40,
            restMinutes = 8,
            selectedTemplateId = template.id,
            activeSession = TimerSession(
                mode = TimerMode.Focus,
                todoId = "todo-1",
                todoTitle = "拆分数据层",
                startedAt = 6_000L,
                endsAt = 7_500L,
                totalSeconds = 1_500,
                templateId = template.id,
                pausedRemainingSeconds = 600
            ),
            pendingRecord = PendingPomodoroRecord(
                todoId = "todo-1",
                todoTitle = "拆分数据层",
                startedAt = 8_000L,
                endedAt = 9_500L,
                focusMinutes = 25,
                templateId = template.id
            ),
            projectRevision = 12,
            nickname = "测试用户",
            llmProviders = listOf(
                LlmProviderSettings(
                    id = "provider-1",
                    providerKey = "custom",
                    name = "测试 Provider",
                    modelName = "test-model",
                    endpoint = "https://example.com/v1",
                    apiKey = "secret"
                )
            ),
            agents = listOf(
                AgentSettings(
                    id = "agent-unified",
                    name = "对话",
                    providerId = "provider-1",
                    prompt = "测试提示词",
                    permissions = AgentDataPermissions(
                        dailyRecords = true,
                        todos = true,
                        templates = true
                    )
                )
            ),
            agentMessages = listOf(
                AgentConversationMessage(
                    id = "message-1",
                    role = "user",
                    content = "今天继续重构",
                    createdAt = 10_000L
                ),
                AgentConversationMessage(
                    id = "message-2",
                    role = "assistant",
                    content = "先稳住存储边界。",
                    createdAt = 11_000L
                )
            ),
            agentMemory = "偏好小步重构",
            webDevSync = WebDevSyncSettings(
                enabled = true,
                username = "sync-user",
                authToken = "sync-token"
            ),
            recordPolishProviderId = "provider-1",
            fontScale = 1.15f
        )

        val decoded = AppStateJsonCodec.decode(AppStateJsonCodec.encode(state))

        assertEquals(state, decoded)
    }

    @Test
    fun decodeMinimalJsonFallsBackToDefaultAppStateShape() {
        val decoded = AppStateJsonCodec.decode("{}")

        assertEquals(emptyList<TodoItem>(), decoded.todos)
        assertEquals(listOf(defaultTemplate()), decoded.templates)
        assertEquals(defaultTemplate().id, decoded.selectedTemplateId)
        assertEquals(defaultLlmProviders(), decoded.llmProviders)
        assertEquals(defaultAgents(), decoded.agents)
        assertEquals(1, decoded.agentMessages.size)
        assertEquals("agent-welcome", decoded.agentMessages.single().id)
        assertEquals("assistant", decoded.agentMessages.single().role)
        assertEquals(defaultAgentConversation().single().content, decoded.agentMessages.single().content)
        assertEquals(WebDevSyncSettings(), decoded.webDevSync)
    }
}
