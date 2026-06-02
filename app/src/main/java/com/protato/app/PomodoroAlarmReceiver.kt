package com.protato.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PomodoroAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TIMER_COMPLETED) return

        val store = ProtatoStore(context)
        val state = store.load()
        val session = state.activeSession ?: return
        if (session.pausedRemainingSeconds != null) return
        val pendingRecord = PendingPomodoroRecord(
            todoId = session.todoId,
            todoTitle = session.todoTitle,
            startedAt = session.startedAt,
            endedAt = session.endsAt,
            focusMinutes = session.totalSeconds / 60,
            templateId = session.templateId
        )
        store.save(
            state.copy(
                activeSession = null,
                pendingRecord = if (session.mode == TimerMode.Focus) pendingRecord else state.pendingRecord
            )
        )
        PomodoroNotifications(context).cancelTimer()
        if (session.mode == TimerMode.Focus) {
            PomodoroNotifications(context).showCompletedNotification(pendingRecord)
        }
    }
}
