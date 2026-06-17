package com.protato.app.state

import com.protato.app.DailySummary
import com.protato.app.LlmProviderSettings
import com.protato.app.FieldAnswer
import com.protato.app.PendingPomodoroRecord
import com.protato.app.PomodoroRecord
import com.protato.app.ProtatoThemeChoice
import com.protato.app.RecordTemplate
import com.protato.app.TimerMode
import com.protato.app.TimerSession
import com.protato.app.TodoCompletionReview
import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.TodoItem
import com.protato.app.WebDevSyncSettings

internal sealed interface ProtatoEvent {
    data class AddTodo(val todo: TodoItem) : TodoProtatoEvent
    data class AddTodos(val todos: List<TodoItem>) : TodoProtatoEvent
    data class ToggleTodo(val todo: TodoItem) : TodoProtatoEvent
    data class CompleteTodoWithReview(val todo: TodoItem, val review: TodoCompletionReview) : TodoProtatoEvent
    data class DeleteTodo(val todo: TodoItem) : TodoProtatoEvent
    data class DeleteTodos(val todoIds: Set<String>) : TodoProtatoEvent
    data class MoveTodosToToday(val todoIds: Set<String>) : TodoProtatoEvent
    data class UpdateTodo(val todo: TodoItem) : TodoProtatoEvent
    data class UpdateRecord(val record: PomodoroRecord) : RecordProtatoEvent
    data class SaveDailySummary(val summary: DailySummary) : RecordProtatoEvent
    data class SelectTemplate(val templateId: String) : TemplateProtatoEvent
    data class UpsertTemplate(val template: RecordTemplate) : TemplateProtatoEvent
    data class DeleteTemplate(val template: RecordTemplate) : TemplateProtatoEvent
    data class UpdateNickname(val nickname: String) : SettingsProtatoEvent
    data class UpdateFontScale(val fontScale: Float) : SettingsProtatoEvent
    data class UpdateThemeChoice(val themeChoice: ProtatoThemeChoice) : SettingsProtatoEvent
    data class UpdateWebDavSync(val settings: WebDevSyncSettings) : SettingsProtatoEvent
    data class UpsertLlmProvider(val provider: LlmProviderSettings) : SettingsProtatoEvent
    data class DeleteLlmProvider(val provider: LlmProviderSettings) : SettingsProtatoEvent
    data class SelectRecordPolishProvider(val providerId: String) : SettingsProtatoEvent
    data class UpdateTimerDuration(val mode: TimerMode, val minutes: Int) : TimerProtatoEvent
    data class SetActiveSession(val session: TimerSession) : TimerProtatoEvent
    data class UpdateActiveSessionTodo(val todoId: String?, val todoTitle: String) : TimerProtatoEvent
    data class PauseActiveSession(val remainingSeconds: Int) : TimerProtatoEvent
    data object ClearActiveSession : TimerProtatoEvent
    data object ClearPendingRecord : TimerProtatoEvent
    data class SavePendingRecord(val record: PendingPomodoroRecord, val answers: List<FieldAnswer>) : TimerProtatoEvent
    data class AppendAgentMessage(val role: String, val content: String) : AgentProtatoEvent
    data object ClearAgentMessages : AgentProtatoEvent
    data class AcceptAgentTodoDrafts(val drafts: List<TodoDraft>) : AgentProtatoEvent
    data class RejectAgentTodoDrafts(val count: Int) : AgentProtatoEvent
    data class AcceptAgentTodoEdits(val edits: List<TodoEditDraft>) : AgentProtatoEvent
    data class RejectAgentTodoEdits(val count: Int) : AgentProtatoEvent
}

internal sealed interface TodoProtatoEvent : ProtatoEvent
internal sealed interface RecordProtatoEvent : ProtatoEvent
internal sealed interface TemplateProtatoEvent : ProtatoEvent
internal sealed interface SettingsProtatoEvent : ProtatoEvent
internal sealed interface TimerProtatoEvent : ProtatoEvent
internal sealed interface AgentProtatoEvent : ProtatoEvent
