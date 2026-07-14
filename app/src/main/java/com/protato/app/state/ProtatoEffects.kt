package com.protato.app.state

import com.protato.app.TimerSession

internal sealed interface ProtatoEffect {
    data class CancelTodoReminder(val todoId: String) : ProtatoEffect
    data class RemoveTodoSelection(val todoId: String) : ProtatoEffect
    data object PlayTodoCompletedSound : ProtatoEffect
    data class StartTimerService(val session: TimerSession) : ProtatoEffect
    data object StopTimerService : ProtatoEffect
    data class ScheduleTimerAlarm(val session: TimerSession) : ProtatoEffect
    data object CancelTimerAlarm : ProtatoEffect
    data object CancelTimerNotification : ProtatoEffect
    data object CancelCompletedNotification : ProtatoEffect
}

internal data class ProtatoEventResult(
    val effects: List<ProtatoEffect> = emptyList()
)
