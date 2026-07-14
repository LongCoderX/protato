package com.protato.app.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.protato.app.TodoItem

class TodoReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(todo: TodoItem) {
        val reminderAt = todo.reminderAt ?: return
        if (todo.completed || reminderAt <= System.currentTimeMillis()) {
            cancel(todo.id)
            return
        }
        val pendingIntent = reminderIntent(todo.id)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
        }
    }

    fun cancel(todoId: String) {
        alarmManager.cancel(reminderIntent(todoId))
    }

    fun sync(todos: List<TodoItem>) {
        todos.forEach { todo ->
            if (todo.reminderAt != null && !todo.completed) {
                schedule(todo)
            } else {
                cancel(todo.id)
            }
        }
    }

    private fun reminderIntent(todoId: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            todoId.hashCode(),
            Intent(context, TodoReminderReceiver::class.java)
                .setAction(ACTION_TODO_REMINDER)
                .putExtra(EXTRA_TODO_ID, todoId),
            pendingIntentFlags()
        )
    }
}

const val EXTRA_TODO_ID = "todoId"
