package com.protato.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class PomodoroAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(session: TimerSession) {
        val pendingIntent = completionIntent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    session.endsAt,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    session.endsAt,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                session.endsAt,
                pendingIntent
            )
        }
    }

    fun cancel() {
        alarmManager.cancel(completionIntent())
    }

    private fun completionIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            10,
            Intent(context, PomodoroAlarmReceiver::class.java).setAction(ACTION_TIMER_COMPLETED),
            pendingIntentFlags()
        )
    }
}
