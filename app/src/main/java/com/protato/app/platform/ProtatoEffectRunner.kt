package com.protato.app.platform

import android.content.Context
import android.content.Intent
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

            is ProtatoEffect.StartTimerService -> {
                context.startPomodoroService(effect.session)
            }

            ProtatoEffect.StopTimerService -> {
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
