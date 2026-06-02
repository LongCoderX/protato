package com.protato.app

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
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
        when (intent?.action) {
            ACTION_STOP_TIMER_SERVICE -> stopTimer()
            else -> startTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tickerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun startTimer() {
        val store = ProtatoStore(this)
        val session = store.load().activeSession ?: run {
            stopSelf()
            return
        }
        val notifications = PomodoroNotifications(this)
        ServiceCompat.startForeground(
            this,
            TIMER_NOTIFICATION_ID,
            notifications.buildTimerNotification(session),
            foregroundServiceType()
        )
        val notificationManager = NotificationManagerCompat.from(this)
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                val currentSession = store.load().activeSession
                if (currentSession == null || currentSession.remainingSeconds() <= 0) {
                    notifications.cancelTimer()
                    stopSelf()
                    return@launch
                }
                if (notifications.canPostNotifications()) {
                    notifications.cancelCompleted()
                    notificationManager.notify(
                        TIMER_NOTIFICATION_ID,
                        notifications.buildTimerNotification(currentSession)
                    )
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        val store = ProtatoStore(this)
        val state = store.load()
        store.save(state.copy(activeSession = null))
        PomodoroAlarmScheduler(this).cancel()
        PomodoroNotifications(this).cancelTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun foregroundServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
    }
}
