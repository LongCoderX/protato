package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.PomodoroRecord
import com.protato.app.TimerMode
import com.protato.app.defaultTemplate
import com.protato.app.domain.timer.completeTimer
import com.protato.app.domain.timer.pauseTimer
import com.protato.app.domain.timer.resetTimer
import com.protato.app.domain.timer.resumeTimer
import com.protato.app.domain.timer.startTimer
import com.protato.app.util.newId

internal fun reduceTimerEvent(
    state: AppState,
    event: TimerProtatoEvent,
    nowMillis: Long
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

        is ProtatoEvent.StartTimer -> {
            val nextState = startTimer(
                state = state,
                mode = event.mode,
                todoId = event.todoId,
                todoTitle = event.todoTitle,
                totalSeconds = event.totalSeconds,
                templateId = event.templateId,
                nowMillis = nowMillis
            )
            val session = nextState.activeSession
            if (nextState == state || session == null) return StateReduction(state)
            StateReduction(
                state = nextState,
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StartTimerService(session),
                        ProtatoEffect.ScheduleTimerAlarm(session)
                    )
                )
            )
        }

        is ProtatoEvent.UpdateActiveSessionTodo -> {
            val session = state.activeSession ?: return StateReduction(state)
            val updatedSession = session.copy(
                todoId = event.todoId,
                todoTitle = event.todoTitle.ifBlank { "未绑定任务" }
            )
            StateReduction(
                state = state.copy(activeSession = updatedSession),
                result = ProtatoEventResult(
                    effects = if (updatedSession.pausedRemainingSeconds == null) {
                        listOf(ProtatoEffect.StartTimerService(updatedSession))
                    } else {
                        emptyList()
                    }
                )
            )
        }

        ProtatoEvent.PauseTimer -> {
            val nextState = pauseTimer(state, nowMillis)
            if (nextState == state) return StateReduction(state)
            StateReduction(
                state = nextState,
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StopTimerService,
                        ProtatoEffect.CancelTimerAlarm
                    )
                )
            )
        }

        ProtatoEvent.ResumeTimer -> {
            val nextState = resumeTimer(state, nowMillis)
            val session = nextState.activeSession
            if (nextState == state || session == null) return StateReduction(state)
            StateReduction(
                state = nextState,
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StartTimerService(session),
                        ProtatoEffect.ScheduleTimerAlarm(session)
                    )
                )
            )
        }

        ProtatoEvent.ResetTimer -> {
            val nextState = resetTimer(state)
            if (nextState == state) return StateReduction(state)
            StateReduction(
                state = nextState,
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StopTimerService,
                        ProtatoEffect.CancelTimerAlarm
                    )
                )
            )
        }

        ProtatoEvent.CompleteTimer -> {
            val nextState = completeTimer(state, nowMillis)
            if (nextState == state) return StateReduction(state)
            StateReduction(
                state = nextState,
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.StopTimerService,
                        ProtatoEffect.CancelTimerAlarm,
                        ProtatoEffect.CancelTimerNotification
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
