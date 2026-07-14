package com.protato.app.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.protato.app.DefaultAppStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TODO_REMINDER) return
        val todoId = intent.getStringExtra(EXTRA_TODO_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = DefaultAppStateRepository(context.applicationContext).load().state
                val todo = state.todos.firstOrNull { it.id == todoId && !it.completed }
                    ?: return@launch
                PomodoroNotifications(context).showTodoReminder(todo)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
