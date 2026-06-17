package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.PomodoroRecord
import com.protato.app.TimerMode
import com.protato.app.defaultTemplate
import com.protato.app.util.newId

internal fun reduceTimerEvent(
    state: AppState,
    event: TimerProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.UpdateTimerDuration -> {
            val minutes = when (event.mode) {
                TimerMode.Focus -> event.minutes.coerceIn(1, 180)
                TimerMode.Break -> event.minutes.coerceIn(1, 60)
            }
            StateReduction(
                when (event.mode) {
                    TimerMode.Focus -> state.copy(focusMinutes = minutes)
                    TimerMode.Break -> state.copy(restMinutes = minutes)
                }
            )
        }

        is ProtatoEvent.SetActiveSession -> {
            StateReduction(
                state = state.copy(activeSession = event.session),
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StartTimerService,
                        ProtatoEffect.ScheduleTimerAlarm(event.session)
                    )
                )
            )
        }

        is ProtatoEvent.UpdateActiveSessionTodo -> {
            val session = state.activeSession ?: return StateReduction(state)
            StateReduction(
                state.copy(
                    activeSession = session.copy(
                        todoId = event.todoId,
                        todoTitle = event.todoTitle.ifBlank { "未绑定任务" }
                    )
                )
            )
        }

        is ProtatoEvent.PauseActiveSession -> {
            val session = state.activeSession
                ?: return StateReduction(state)
            StateReduction(
                state = state.copy(
                    activeSession = session.copy(
                        pausedRemainingSeconds = event.remainingSeconds
                    )
                ),
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StopTimerService,
                        ProtatoEffect.CancelTimerAlarm
                    )
                )
            )
        }

        ProtatoEvent.ClearActiveSession -> {
            StateReduction(
                state = state.copy(activeSession = null),
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StopTimerService,
                        ProtatoEffect.CancelTimerAlarm
                    )
                )
            )
        }

        ProtatoEvent.ClearPendingRecord -> {
            StateReduction(
                state = state.copy(pendingRecord = null),
                result = ProtatoEventResult(
                    effects = listOf(ProtatoEffect.CancelCompletedNotification)
                )
            )
        }

        is ProtatoEvent.SavePendingRecord -> {
            val template = state.templates.firstOrNull { it.id == event.record.templateId }
                ?: defaultTemplate()
            val newRecord = PomodoroRecord(
                id = newId(),
                todoId = event.record.todoId,
                todoTitle = event.record.todoTitle,
                templateId = template.id,
                templateName = template.name,
                startedAt = event.record.startedAt,
                endedAt = event.record.endedAt,
                focusMinutes = event.record.focusMinutes,
                answers = event.answers
            )
            StateReduction(
                state = state.copy(
                    records = listOf(newRecord) + state.records,
                    pendingRecord = null
                ),
                result = ProtatoEventResult(
                    effects = listOf(ProtatoEffect.CancelCompletedNotification)
                )
            )
        }
    }
}
