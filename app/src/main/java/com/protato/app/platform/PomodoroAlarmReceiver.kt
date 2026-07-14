package com.protato.app.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.protato.app.AppStateUpdateResult
import com.protato.app.DefaultAppStateRepository
import com.protato.app.domain.timer.completeTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PomodoroAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TIMER_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = DefaultAppStateRepository(context.applicationContext)
                when (val result = repository.update { state ->
                    completeTimer(state, System.currentTimeMillis())
                }) {
                    is AppStateUpdateResult.Updated -> {
                        val nextState = result.state
                        val notifications = PomodoroNotifications(context)
                        notifications.cancelTimer()
                        nextState.pendingRecord?.let(notifications::showCompletedNotification)
                    }

                    is AppStateUpdateResult.Unchanged -> Unit
                    is AppStateUpdateResult.Failed -> Log.e(
                        "PomodoroAlarmReceiver",
                        "Unable to persist completed timer.",
                        result.error
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
