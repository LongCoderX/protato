package com.protato.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

const val ACTION_START_TIMER_SERVICE = "com.protato.app.action.START_TIMER_SERVICE"
const val ACTION_STOP_TIMER_SERVICE = "com.protato.app.action.STOP_TIMER_SERVICE"
const val ACTION_TIMER_COMPLETED = "com.protato.app.action.TIMER_COMPLETED"

const val TIMER_NOTIFICATION_ID = 1001
const val COMPLETED_NOTIFICATION_ID = 1002

private const val TIMER_CHANNEL_ID = "pomodoro_timer"
private const val COMPLETED_CHANNEL_ID = "pomodoro_completed"

class PomodoroNotifications(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "番茄计时",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示正在进行的番茄倒计时"
            setShowBadge(false)
        }

        val completedChannel = NotificationChannel(
            COMPLETED_CHANNEL_ID,
            "番茄结束",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "番茄计时结束提醒"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(timerChannel)
        manager.createNotificationChannel(completedChannel)
    }

    fun buildTimerNotification(session: TimerSession): Notification {
        ensureChannels()
        val remainingSeconds = session.remainingSeconds()
        val elapsedSeconds = (session.totalSeconds - remainingSeconds).coerceIn(0, session.totalSeconds)
        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            pendingIntentFlags()
        )
        val stopIntent = PendingIntent.getService(
            context,
            1,
            Intent(context, PomodoroTimerService::class.java).setAction(ACTION_STOP_TIMER_SERVICE),
            pendingIntentFlags()
        )

        return NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(session.mode.notificationTitle())
            .setContentText("${session.todoTitle} · 还剩 ${remainingSeconds.asClock()}")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(session.endsAt)
            .setProgress(session.totalSeconds, elapsedSeconds, false)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "结束",
                stopIntent
            )
            .build()
    }

    fun showCompletedNotification(record: PendingPomodoroRecord) {
        if (!canPostNotifications()) return
        ensureChannels()
        val openIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java),
            pendingIntentFlags()
        )
        val notification = NotificationCompat.Builder(context, COMPLETED_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("番茄完成")
            .setContentText("${record.todoTitle} · 点击填写复盘")
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        notificationManager.notify(COMPLETED_NOTIFICATION_ID, notification)
    }

    fun cancelTimer() {
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
    }

    fun cancelCompleted() {
        notificationManager.cancel(COMPLETED_NOTIFICATION_ID)
    }

    fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}

fun TimerSession.remainingSeconds(now: Long = System.currentTimeMillis()): Int {
    return (((endsAt - now).coerceAtLeast(0L) + 999L) / 1000L).toInt()
}

fun TimerMode.notificationTitle(): String = when (this) {
    TimerMode.Focus -> "正在专注"
    TimerMode.ShortBreak -> "短休息中"
    TimerMode.LongBreak -> "长休息中"
}

fun pendingIntentFlags(): Int {
    val mutability = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
    return mutability or PendingIntent.FLAG_UPDATE_CURRENT
}
