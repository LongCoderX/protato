package com.protato.app.platform

import android.content.Context
import android.content.Intent
import com.protato.app.ACTION_START_TIMER_SERVICE
import com.protato.app.ACTION_STOP_TIMER_SERVICE
import com.protato.app.PomodoroAlarmScheduler
import com.protato.app.PomodoroNotifications
import com.protato.app.PomodoroTimerService
import com.protato.app.TodoReminderScheduler
import com.protato.app.state.ProtatoEffect

internal class ProtatoEffectRunner(private val context: Context) {
    fun run(effect: ProtatoEffect) {
        when (effect) {
            is ProtatoEffect.CancelTodoReminder -> {
                TodoReminderScheduler(context).cancel(effect.todoId)
            }

            ProtatoEffect.PlayTodoCompletedSound -> {
                playTodoCompletedSound()
            }

            ProtatoEffect.StartTimerService -> {
                context.startPomodoroService(ACTION_START_TIMER_SERVICE)
            }

            ProtatoEffect.StopTimerService -> {
                context.startPomodoroService(ACTION_STOP_TIMER_SERVICE)
                context.stopService(Intent(context, PomodoroTimerService::class.java))
            }

            is ProtatoEffect.ScheduleTimerAlarm -> {
                PomodoroAlarmScheduler(context).schedule(effect.session)
            }

            ProtatoEffect.CancelTimerAlarm -> {
                PomodoroAlarmScheduler(context).cancel()
            }

            ProtatoEffect.CancelTimerNotification -> {
                PomodoroNotifications(context).cancelTimer()
            }

            ProtatoEffect.CancelCompletedNotification -> {
                PomodoroNotifications(context).cancelCompleted()
            }

            is ProtatoEffect.RemoveTodoSelection -> Unit
        }
    }

    fun runAll(effects: List<ProtatoEffect>) {
        effects.forEach(::run)
    }
}
