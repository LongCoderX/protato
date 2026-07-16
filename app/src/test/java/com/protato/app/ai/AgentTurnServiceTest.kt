package com.protato.app.ai

import com.protato.app.AgentDataPermissions
import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.AgentConversationMessage
import com.protato.app.domain.todo.TodoDraft
import com.protato.app.domain.todo.todoDraftKey
import com.protato.app.TodoItem
import com.protato.app.util.dayKey
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentTurnServiceTest {
    private val agentWithoutTodoPermission = AgentSettings(
        id = "agent-test",
        permissions = AgentDataPermissions(todos = false)
    )

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

        assertEquals(emptyList<TodoDraft>(), result.todoDrafts)
        assertTrue(result.reply.contains("不会重复提交"))
    }

    @Test
    fun standaloneOptimizationGoalCreatesConfirmationCardDrafts() = runBlocking {
        val result = runAgentTurn(
            text = "完成Protato的优化",
            userState = AppState(todos = emptyList(), agentMessages = emptyList()),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("卡片") || result.reply.contains("确认"))
        assertTrue(result.todoDrafts.any { it.title.contains("Protato") || it.title.contains("优化") })
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun planningAffirmationUsesRecentGoalForCardDrafts() = runBlocking {
        val result = runAgentTurn(
            text = "对，就是这个意思",
            userState = AppState(
                todos = emptyList(),
                agentMessages = listOf(
                    AgentConversationMessage(
                        id = "m1",
                        role = "user",
                        content = "优化交互相关的"
                    ),
                    AgentConversationMessage(
                        id = "m2",
                        role = "assistant",
                        content = "你想把交互优化整理成待办吗？"
                    ),
                    AgentConversationMessage(
                        id = "m3",
                        role = "user",
                        content = "对，就是这个意思"
                    )
                )
            ),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("卡片") || result.reply.contains("确认"))
        assertTrue(result.todoDrafts.any { it.title.contains("交互") || it.title.contains("优化") })
        assertTrue(result.todoEdits.isEmpty())
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
    fun todoQueryRequiresTodoPermission() = runBlocking {
        val today = dayKey(System.currentTimeMillis())
        val result = runAgentTurn(
            text = "今天有什么任务？",
            userState = AppState(
                todos = listOf(TodoItem(id = "todo-1", title = "写测试", dueDate = today)),
                agentMessages = emptyList()
            ),
            agent = agentWithoutTodoPermission,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("没有读取待办的权限"))
        assertTrue(result.reply.contains("Agent 设置"))
        assertTrue(!result.reply.contains("写测试"))
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
    fun movingOpenTodosToTodayRequiresTodoPermission() = runBlocking {
        val result = runAgentTurn(
            text = "把未完成任务都挪到今天",
            userState = AppState(
                todos = listOf(TodoItem(id = "todo-1", title = "无日期任务", dueDate = null)),
                agentMessages = emptyList()
            ),
            agent = agentWithoutTodoPermission,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet()
        )

        assertTrue(result.reply.contains("没有读取待办的权限"))
        assertTrue(!result.reply.contains("无日期任务"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun selectedContextTodoCanBePlannedIntoSubtasks() = runBlocking {
        val result = runAgentTurn(
            text = "把当前任务拆分成子任务",
            userState = AppState(
                todos = listOf(TodoItem(id = "todo-1", title = "完成Protato优化")),
                agentMessages = emptyList()
            ),
            agent = null,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            contextTodoId = "todo-1"
        )

        assertEquals(emptyList<TodoDraft>(), result.todoDrafts)
        assertEquals(listOf("todo-1"), result.todoEdits.map { it.id })
        assertTrue(result.todoEdits.single().subtasks.orEmpty().isNotEmpty())
        assertTrue(result.reply.contains("子任务草案"))
    }

    @Test
    fun planningSubtasksRequiresTodoPermission() = runBlocking {
        val result = runAgentTurn(
            text = "把当前任务拆分成子任务",
            userState = AppState(
                todos = listOf(TodoItem(id = "todo-1", title = "完成Protato优化")),
                agentMessages = emptyList()
            ),
            agent = agentWithoutTodoPermission,
            provider = null,
            nickname = "测试用户",
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            contextTodoId = "todo-1"
        )

        assertTrue(result.reply.contains("没有读取待办的权限"))
        assertTrue(!result.reply.contains("完成Protato优化"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
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

    @Test
    fun nativeToolCallCreatesPendingTodoDraftsEvenWithoutLocalPlanningIntent() {
        val result = buildAgentTurnResultFromRawReply(
            rawReply = """
                我整理了草案，请确认。
                {"tool_calls":[{"function":{"name":"propose_todos","arguments":"{\"todos\":[{\"title\":\"整理AI工具调用链路\",\"plannedPomodoros\":2,\"tags\":[\"开发\"]}]}"}}]}
            """.trimIndent(),
            fallbackTodoDrafts = emptyList(),
            existingTodos = emptyList(),
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            allowTodoActions = true,
            allowedTodoIds = emptySet()
        )

        assertEquals("我整理了草案，请确认。", result.reply)
        assertEquals("整理AI工具调用链路", result.todoDrafts.single().title)
        assertEquals(2, result.todoDrafts.single().plannedPomodoros)
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun handledNativeToolCallDoesNotCreateDuplicatePendingCards() {
        val draft = TodoDraft(
            title = "整理AI工具调用链路",
            tags = listOf("开发"),
            plannedPomodoros = 2
        )
        val result = buildAgentTurnResultFromRawReply(
            rawReply = """
                我整理了草案，请确认。
                {"tool_calls":[{"function":{"name":"propose_todos","arguments":"{\"todos\":[{\"title\":\"整理AI工具调用链路\",\"plannedPomodoros\":2,\"tags\":[\"开发\"]}]}"}}]}
            """.trimIndent(),
            fallbackTodoDrafts = emptyList(),
            existingTodos = emptyList(),
            handledTodoDraftKeys = setOf(todoDraftKey(draft)),
            handledTodoEditKeys = emptySet(),
            allowTodoActions = true,
            allowedTodoIds = emptySet()
        )

        assertTrue(result.reply.contains("不会重复提交"))
        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun parsedTodoActionsAreRejectedWithoutTodoPermission() {
        val existing = TodoItem(id = "todo-1", title = "受保护待办")
        val result = buildAgentTurnResultFromRawReply(
            rawReply = """
                不应接受越权草案。
                {"tool_calls":[{"function":{"name":"propose_todo_updates","arguments":"{\"edits\":[{\"id\":\"todo-1\",\"title\":\"越权修改\"}]}"}}]}
            """.trimIndent(),
            fallbackTodoDrafts = emptyList(),
            existingTodos = listOf(existing),
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            allowTodoActions = false,
            allowedTodoIds = emptySet()
        )

        assertTrue(result.todoDrafts.isEmpty())
        assertTrue(result.todoEdits.isEmpty())
    }

    @Test
    fun parsedTodoEditMustTargetAllowedExistingOpenTodo() {
        val allowed = TodoItem(id = "todo-allowed", title = "允许待办")
        val denied = TodoItem(id = "todo-denied", title = "范围外待办")
        val result = buildAgentTurnResultFromRawReply(
            rawReply = """
                只应接受允许范围内的修改。
                {"tool_calls":[{"function":{"name":"propose_todo_updates","arguments":"{\"edits\":[{\"id\":\"todo-allowed\",\"title\":\"允许修改\"},{\"id\":\"todo-denied\",\"title\":\"越权修改\"},{\"id\":\"missing\",\"title\":\"不存在\"}]}"}}]}
            """.trimIndent(),
            fallbackTodoDrafts = emptyList(),
            existingTodos = listOf(allowed, denied),
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            allowTodoActions = true,
            allowedTodoIds = setOf(allowed.id)
        )

        assertEquals(listOf(allowed.id), result.todoEdits.map { it.id })
    }

    @Test
    fun updateIntentCannotClaimSuccessWithoutExecutableEditDraft() {
        val existing = TodoItem(id = "todo-1", title = "凌晨前睡觉")
        val result = buildAgentTurnResultFromRawReply(
            rawReply = "已经帮你把提醒时间修改为 23:30。",
            fallbackTodoDrafts = emptyList(),
            existingTodos = listOf(existing),
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            allowTodoActions = true,
            allowedTodoIds = setOf(existing.id),
            requireTodoEdits = true
        )

        assertTrue(result.todoEdits.isEmpty())
        assertTrue(result.reply.contains("没有修改任何任务"))
        assertTrue("已经帮你" !in result.reply)
    }

    @Test
    fun updateIntentKeepsValidExecutableEditDraft() {
        val existing = TodoItem(id = "todo-1", title = "凌晨前睡觉")
        val result = buildAgentTurnResultFromRawReply(
            rawReply = """
                请确认修改。
                {"tool":"propose_todo_updates","edits":[{"id":"todo-1","title":"十一点半前睡觉"}]}
            """.trimIndent(),
            fallbackTodoDrafts = emptyList(),
            existingTodos = listOf(existing),
            handledTodoDraftKeys = emptySet(),
            handledTodoEditKeys = emptySet(),
            allowTodoActions = true,
            allowedTodoIds = setOf(existing.id),
            requireTodoEdits = true
        )

        assertEquals("十一点半前睡觉", result.todoEdits.single().title)
        assertEquals("请确认修改。", result.reply)
    }
}
