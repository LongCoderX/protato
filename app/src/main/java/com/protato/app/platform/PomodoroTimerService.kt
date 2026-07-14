package com.protato.app.platform

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.protato.app.TimerMode
import com.protato.app.TimerSession
import com.protato.app.domain.timer.remainingSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PomodoroTimerService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var tickerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        PomodoroNotifications(this).ensureChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_START_TIMER_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        val session = intent.toTimerSession() ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        startTimer(session)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tickerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun startTimer(session: TimerSession) {
        val notifications = PomodoroNotifications(this)
        ServiceCompat.startForeground(
            this,
            TIMER_NOTIFICATION_ID,
            notifications.buildTimerNotification(session),
            foregroundServiceType()
        )
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                if (session.remainingSeconds(System.currentTimeMillis()) <= 0) {
                    notifications.cancelTimer()
                    stopSelf()
                    return@launch
                }
                if (notifications.canPostNotifications()) {
                    notifications.cancelCompleted()
                    notifications.showTimerNotification(session)
                }
                delay(1000)
            }
        }
    }

    private fun foregroundServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
    }

    private fun Intent.toTimerSession(): TimerSession? {
        val mode = getStringExtra(EXTRA_TIMER_MODE)
            ?.let { value -> TimerMode.entries.firstOrNull { it.name == value } }
            ?: return null
        val todoTitle = getStringExtra(EXTRA_TIMER_TODO_TITLE) ?: return null
        val templateId = getStringExtra(EXTRA_TIMER_TEMPLATE_ID) ?: return null
        val totalSeconds = getIntExtra(EXTRA_TIMER_TOTAL_SECONDS, 0)
        if (totalSeconds <= 0) return null
        return TimerSession(
            mode = mode,
            todoId = getStringExtra(EXTRA_TIMER_TODO_ID),
            todoTitle = todoTitle,
            startedAt = getLongExtra(EXTRA_TIMER_STARTED_AT, 0L),
            endsAt = getLongExtra(EXTRA_TIMER_ENDS_AT, 0L),
            totalSeconds = totalSeconds,
            templateId = templateId
        )
    }
}

internal const val EXTRA_TIMER_MODE = "timerMode"
internal const val EXTRA_TIMER_TODO_ID = "timerTodoId"
internal const val EXTRA_TIMER_TODO_TITLE = "timerTodoTitle"
internal const val EXTRA_TIMER_STARTED_AT = "timerStartedAt"
internal const val EXTRA_TIMER_ENDS_AT = "timerEndsAt"
internal const val EXTRA_TIMER_TOTAL_SECONDS = "timerTotalSeconds"
internal const val EXTRA_TIMER_TEMPLATE_ID = "timerTemplateId"
