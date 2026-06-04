package com.protato.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TodoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TODO_REMINDER) return
        val todoId = intent.getStringExtra(EXTRA_TODO_ID) ?: return
        val state = ProtatoStore(context).load()
        val todo = state.todos.firstOrNull { it.id == todoId && !it.completed } ?: return
        PomodoroNotifications(context).showTodoReminder(todo)
    }
}
