package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.AgentSettings
import com.protato.app.DailySummary
import com.protato.app.DEFAULT_NICKNAME
import com.protato.app.FieldAnswer
import com.protato.app.LlmProviderSettings
import com.protato.app.PendingPomodoroRecord
import com.protato.app.RecordTemplate
import com.protato.app.PomodoroRecord
import com.protato.app.TimerMode
import com.protato.app.TimerSession
import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.TodoItem
import com.protato.app.defaultTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtatoStateHolderTest {
    @Test
    fun dispatchAddTodoUpdatesStateAndRevision() {
        val holder = testHolder()
        val todo = TodoItem(id = "todo-1", title = "写测试")

        val result = holder.dispatch(ProtatoEvent.AddTodo(todo))

        assertEquals(listOf(todo), holder.appState.todos)
        assertEquals(2, holder.appState.projectRevision)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun dispatchToggleTodoReturnsCompletionEffect() {
        val todo = TodoItem(id = "todo-1", title = "写测试", completed = false)
        val holder = testHolder(AppState(todos = listOf(todo)))

        val result = holder.dispatch(ProtatoEvent.ToggleTodo(todo))

        assertEquals(true, holder.appState.todos.single().completed)
        assertEquals(
            listOf(
                ProtatoEffect.CancelTodoReminder("todo-1"),
                ProtatoEffect.PlayTodoCompletedSound
            ),
            result.effects
        )
    }

    @Test
    fun dispatchDeleteTodosReturnsRemovedIds() {
        val first = TodoItem(id = "todo-1", title = "写测试")
        val second = TodoItem(id = "todo-2", title = "写文档")
        val holder = testHolder(AppState(todos = listOf(first, second)))

        val result = holder.dispatch(ProtatoEvent.DeleteTodos(setOf("todo-1")))

        assertEquals(listOf(second), holder.appState.todos)
        assertEquals(
            listOf(
                ProtatoEffect.CancelTodoReminder("todo-1"),
                ProtatoEffect.RemoveTodoSelection("todo-1")
            ),
            result.effects
        )
    }

    @Test
    fun dispatchUpdateRecordReplacesMatchingRecord() {
        val original = testRecord(id = "record-1", title = "旧标题")
        val other = testRecord(id = "record-2", title = "其他标题")
        val updated = original.copy(todoTitle = "新标题")
        val holder = testHolder(AppState(records = listOf(original, other)))

        holder.dispatch(ProtatoEvent.UpdateRecord(updated))

        assertEquals(listOf(updated, other), holder.appState.records)
    }

    @Test
    fun dispatchSaveDailySummaryReplacesSameDaySummary() {
        val oldSummary = testSummary(dayKey = "2026-06-12", title = "旧总结")
        val anotherSummary = testSummary(dayKey = "2026-06-11", title = "前一天")
        val newSummary = testSummary(dayKey = "2026-06-12", title = "新总结")
        val holder = testHolder(AppState(dailySummaries = listOf(oldSummary, anotherSummary)))

        holder.dispatch(ProtatoEvent.SaveDailySummary(newSummary))

        assertEquals(listOf(newSummary, anotherSummary), holder.appState.dailySummaries)
    }

    @Test
    fun dispatchSelectTemplateUpdatesSelectedTemplateId() {
        val template = testTemplate(id = "template-1", name = "模板一")
        val holder = testHolder(AppState(templates = listOf(template)))

        holder.dispatch(ProtatoEvent.SelectTemplate("template-1"))

        assertEquals("template-1", holder.appState.selectedTemplateId)
    }

    @Test
    fun dispatchUpsertTemplateAddsNewTemplateAndSelectsIt() {
        val existing = testTemplate(id = "template-1", name = "模板一")
        val newTemplate = testTemplate(id = "template-2", name = "模板二")
        val holder = testHolder(AppState(templates = listOf(existing), selectedTemplateId = existing.id))

        holder.dispatch(ProtatoEvent.UpsertTemplate(newTemplate))

        assertEquals(listOf(newTemplate, existing), holder.appState.templates)
        assertEquals("template-2", holder.appState.selectedTemplateId)
    }

    @Test
    fun dispatchUpsertTemplateReplacesExistingTemplateAndSelectsIt() {
        val existing = testTemplate(id = "template-1", name = "模板一")
        val updated = testTemplate(id = "template-1", name = "模板一新版")
        val holder = testHolder(AppState(templates = listOf(existing)))

        holder.dispatch(ProtatoEvent.UpsertTemplate(updated))

        assertEquals(listOf(updated), holder.appState.templates)
        assertEquals("template-1", holder.appState.selectedTemplateId)
    }

    @Test
    fun dispatchDeleteLastTemplateFallsBackToDefaultTemplate() {
        val template = testTemplate(id = "template-1", name = "模板一")
        val holder = testHolder(AppState(templates = listOf(template), selectedTemplateId = template.id))

        holder.dispatch(ProtatoEvent.DeleteTemplate(template))

        assertEquals(listOf(defaultTemplate()), holder.appState.templates)
        assertEquals(defaultTemplate().id, holder.appState.selectedTemplateId)
    }

    @Test
    fun dispatchUpdateNicknameFallsBackToDefaultNickname() {
        val holder = testHolder()

        holder.dispatch(ProtatoEvent.UpdateNickname(""))

        assertEquals(DEFAULT_NICKNAME, holder.appState.nickname)
    }

    @Test
    fun dispatchUpdateFontScaleCoercesRange() {
        val holder = testHolder()

        holder.dispatch(ProtatoEvent.UpdateFontScale(2.0f))

        assertEquals(1.3f, holder.appState.fontScale)
    }

    @Test
    fun dispatchUpsertLlmProviderAddsProvider() {
        val provider = testProvider(id = "provider-1")
        val holder = testHolder(AppState(llmProviders = emptyList()))

        holder.dispatch(ProtatoEvent.UpsertLlmProvider(provider))

        assertEquals(listOf(provider), holder.appState.llmProviders)
    }

    @Test
    fun dispatchDeleteLlmProviderClearsAgentProviderReferences() {
        val provider = testProvider(id = "provider-1")
        val agent = AgentSettings(id = "agent-1", providerId = provider.id)
        val holder = testHolder(
            AppState(
                llmProviders = listOf(provider),
                agents = listOf(agent)
            )
        )

        holder.dispatch(ProtatoEvent.DeleteLlmProvider(provider))

        assertEquals(emptyList<LlmProviderSettings>(), holder.appState.llmProviders)
        assertEquals("", holder.appState.agents.single().providerId)
    }

    @Test
    fun dispatchSelectRecordPolishProviderUpdatesProviderId() {
        val holder = testHolder()

        holder.dispatch(ProtatoEvent.SelectRecordPolishProvider("provider-1"))

        assertEquals("provider-1", holder.appState.recordPolishProviderId)
    }

    @Test
    fun dispatchUpdateTimerDurationCoercesFocusAndBreakRanges() {
        val holder = testHolder()

        holder.dispatch(ProtatoEvent.UpdateTimerDuration(TimerMode.Focus, 240))
        holder.dispatch(ProtatoEvent.UpdateTimerDuration(TimerMode.Break, 0))

        assertEquals(180, holder.appState.focusMinutes)
        assertEquals(1, holder.appState.restMinutes)
    }

    @Test
    fun dispatchSetActiveSessionStoresSessionAndStartsTimerService() {
        val holder = testHolder()
        val session = testSession()

        val result = holder.dispatch(ProtatoEvent.SetActiveSession(session))

        assertEquals(session, holder.appState.activeSession)
        assertEquals(
            listOf(
                ProtatoEffect.StartTimerService,
                ProtatoEffect.ScheduleTimerAlarm(session)
            ),
            result.effects
        )
    }

    @Test
    fun dispatchPauseActiveSessionStoresRemainingSecondsAndStopsTimerEffects() {
        val holder = testHolder(AppState(activeSession = testSession()))

        val result = holder.dispatch(ProtatoEvent.PauseActiveSession(42))

        assertEquals(42, holder.appState.activeSession?.pausedRemainingSeconds)
        assertEquals(
            listOf(
                ProtatoEffect.StopTimerService,
                ProtatoEffect.CancelTimerAlarm
            ),
            result.effects
        )
    }

    @Test
    fun dispatchClearActiveSessionClearsSessionAndStopsTimerEffects() {
        val holder = testHolder(AppState(activeSession = testSession()))

        val result = holder.dispatch(ProtatoEvent.ClearActiveSession)

        assertNull(holder.appState.activeSession)
        assertEquals(
            listOf(
                ProtatoEffect.StopTimerService,
                ProtatoEffect.CancelTimerAlarm
            ),
            result.effects
        )
    }

    @Test
    fun dispatchClearPendingRecordClearsPendingRecordAndCancelsNotification() {
        val holder = testHolder(AppState(pendingRecord = testPendingRecord()))

        val result = holder.dispatch(ProtatoEvent.ClearPendingRecord)

        assertNull(holder.appState.pendingRecord)
        assertEquals(listOf(ProtatoEffect.CancelCompletedNotification), result.effects)
    }

    @Test
    fun dispatchSavePendingRecordCreatesRecordAndCancelsNotification() {
        val template = testTemplate(id = "template-1", name = "专注复盘")
        val pendingRecord = testPendingRecord(templateId = template.id)
        val answers = listOf(FieldAnswer(fieldId = "field-1", value = "完成了主要任务"))
        val holder = testHolder(
            AppState(
                templates = listOf(template),
                selectedTemplateId = template.id,
                pendingRecord = pendingRecord
            )
        )

        val result = holder.dispatch(ProtatoEvent.SavePendingRecord(pendingRecord, answers))

        val savedRecord = holder.appState.records.single()
        assertEquals(pendingRecord.todoId, savedRecord.todoId)
        assertEquals(pendingRecord.todoTitle, savedRecord.todoTitle)
        assertEquals(template.id, savedRecord.templateId)
        assertEquals(template.name, savedRecord.templateName)
        assertEquals(pendingRecord.startedAt, savedRecord.startedAt)
        assertEquals(pendingRecord.endedAt, savedRecord.endedAt)
        assertEquals(pendingRecord.focusMinutes, savedRecord.focusMinutes)
        assertEquals(answers, savedRecord.answers)
        assertNull(holder.appState.pendingRecord)
        assertEquals(listOf(ProtatoEffect.CancelCompletedNotification), result.effects)
    }

    @Test
    fun dispatchAppendAgentMessageAddsMessage() {
        val holder = testHolder(AppState(agentMessages = emptyList()))

        holder.dispatch(ProtatoEvent.AppendAgentMessage("user", "  今天安排一下  "))

        val message = holder.appState.agentMessages.single()
        assertEquals("user", message.role)
        assertEquals("今天安排一下", message.content)
    }

    @Test
    fun dispatchClearAgentMessagesRemovesConversation() {
        val holder = testHolder(AppState(agentMessages = emptyList()))
        holder.dispatch(ProtatoEvent.AppendAgentMessage("user", "今天安排一下"))

        holder.dispatch(ProtatoEvent.ClearAgentMessages)

        assertEquals(emptyList<com.protato.app.AgentConversationMessage>(), holder.appState.agentMessages)
    }

    @Test
    fun dispatchAcceptAgentTodoDraftsAddsTodosAndConfirmationMessage() {
        val holder = testHolder(AppState(agentMessages = emptyList()))
        val drafts = listOf(
            TodoDraft(title = "写重构计划", dueDate = "2026-06-12"),
            TodoDraft(title = "跑测试", plannedPomodoros = 2)
        )

        holder.dispatch(ProtatoEvent.AcceptAgentTodoDrafts(drafts))

        assertEquals(listOf("写重构计划", "跑测试"), holder.appState.todos.map { it.title })
        assertEquals("已添加 2 个待办。", holder.appState.agentMessages.single().content)
    }

    @Test
    fun dispatchAcceptAgentTodoEditsUpdatesTodosAndConfirmationMessage() {
        val todo = TodoItem(id = "todo-1", title = "旧标题", plannedPomodoros = 1)
        val holder = testHolder(AppState(todos = listOf(todo), agentMessages = emptyList()))
        val edits = listOf(
            TodoEditDraft(id = "todo-1", title = "新标题", plannedPomodoros = 3)
        )

        holder.dispatch(ProtatoEvent.AcceptAgentTodoEdits(edits))

        assertEquals("新标题", holder.appState.todos.single().title)
        assertEquals(3, holder.appState.todos.single().plannedPomodoros)
        assertEquals("已更新 1 个待办。", holder.appState.agentMessages.single().content)
    }

    @Test
    fun dispatchRejectAgentDraftsAddsRejectionMessage() {
        val holder = testHolder(AppState(agentMessages = emptyList()))

        holder.dispatch(ProtatoEvent.RejectAgentTodoDrafts(count = 2))

        assertEquals("已拒绝本次待办草案。", holder.appState.agentMessages.single().content)
    }

    private fun testHolder(initialState: AppState = AppState()): ProtatoStateHolder {
        return ProtatoStateHolder(
            initialState = initialState,
            saveState = {},
            syncReminders = {}
        )
    }

    private fun testRecord(id: String, title: String): PomodoroRecord {
        return PomodoroRecord(
            id = id,
            todoId = null,
            todoTitle = title,
            templateId = "template",
            templateName = "模板",
            startedAt = 1L,
            endedAt = 2L,
            focusMinutes = 25,
            answers = emptyList()
        )
    }

    private fun testSummary(dayKey: String, title: String): DailySummary {
        return DailySummary(
            dayKey = dayKey,
            generatedAt = 1L,
            title = title,
            content = "$title 内容",
            pomodoroCount = 1,
            focusMinutes = 25
        )
    }

    private fun testTemplate(id: String, name: String): RecordTemplate {
        return RecordTemplate(
            id = id,
            name = name,
            fields = emptyList()
        )
    }

    private fun testProvider(id: String): LlmProviderSettings {
        return LlmProviderSettings(
            id = id,
            providerKey = "custom",
            name = "测试 Provider"
        )
    }

    private fun testSession(): TimerSession {
        return TimerSession(
            mode = TimerMode.Focus,
            todoId = "todo-1",
            todoTitle = "写测试",
            startedAt = 1_000L,
            endsAt = 2_500L,
            totalSeconds = 1_500,
            templateId = "template-1"
        )
    }

    private fun testPendingRecord(templateId: String = "template-1"): PendingPomodoroRecord {
        return PendingPomodoroRecord(
            todoId = "todo-1",
            todoTitle = "写测试",
            startedAt = 1_000L,
            endedAt = 2_500L,
            focusMinutes = 25,
            templateId = templateId
        )
    }
}
