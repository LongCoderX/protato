package com.protato.app.domain.timer

import com.protato.app.AppState
import com.protato.app.PendingPomodoroRecord
import com.protato.app.TimerMode
import com.protato.app.TimerSession

internal data class PendingRecord(
    val todoId: String?,
    val todoTitle: String,
    val startedAt: Long,
    val endedAt: Long,
    val focusMinutes: Int,
    val templateId: String
)

internal fun TimerMode.label(): String = when (this) {
    TimerMode.Focus -> "专注"
    TimerMode.Break -> "休息"
}

internal fun TimerMode.durationRange(): IntRange = when (this) {
    TimerMode.Focus -> 5..90
    TimerMode.Break -> 1..60
}

fun Int.asClock(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}

internal fun PendingPomodoroRecord.toPendingRecordUi(): PendingRecord {
    return PendingRecord(
        todoId = todoId,
        todoTitle = todoTitle,
        startedAt = startedAt,
        endedAt = endedAt,
        focusMinutes = focusMinutes,
        templateId = templateId
    )
}

internal fun startTimer(
    state: AppState,
    mode: TimerMode,
    todoId: String?,
    todoTitle: String,
    totalSeconds: Int,
    templateId: String,
    nowMillis: Long
): AppState {
    if (state.activeSession != null) return state
    val durationSeconds = totalSeconds.coerceAtLeast(1)
    return state.copy(
        activeSession = TimerSession(
            mode = mode,
            todoId = todoId,
            todoTitle = todoTitle.ifBlank { "未绑定任务" },
            startedAt = nowMillis,
            endsAt = nowMillis + durationSeconds * 1000L,
            totalSeconds = durationSeconds,
            templateId = templateId
        )
    )
}

internal fun pauseTimer(state: AppState, nowMillis: Long): AppState {
    val session = state.activeSession ?: return state
    if (session.pausedRemainingSeconds != null) return state
    return state.copy(
        activeSession = session.copy(pausedRemainingSeconds = session.remainingSeconds(nowMillis))
    )
}

internal fun resumeTimer(state: AppState, nowMillis: Long): AppState {
    val session = state.activeSession ?: return state
    val remainingSeconds = session.pausedRemainingSeconds ?: return state
    return state.copy(
        activeSession = session.copy(
            endsAt = nowMillis + remainingSeconds * 1000L,
            pausedRemainingSeconds = null
        )
    )
}

internal fun resetTimer(state: AppState): AppState {
    if (state.activeSession == null) return state
    return state.copy(activeSession = null)
}

internal fun completeTimer(state: AppState, nowMillis: Long): AppState {
    val session = state.activeSession ?: return state
    if (session.pausedRemainingSeconds != null || session.remainingSeconds(nowMillis) > 0) return state
    val pendingRecord = if (session.mode == TimerMode.Focus) {
        PendingPomodoroRecord(
            todoId = session.todoId,
            todoTitle = session.todoTitle,
            startedAt = session.startedAt,
            endedAt = session.endsAt,
            focusMinutes = session.totalSeconds / 60,
            templateId = session.templateId
        )
    } else {
        state.pendingRecord
    }
    return state.copy(activeSession = null, pendingRecord = pendingRecord)
}

internal fun TimerSession.remainingSeconds(nowMillis: Long): Int {
    pausedRemainingSeconds?.let { return it.coerceAtLeast(0) }
    return (((endsAt - nowMillis).coerceAtLeast(0L) + 999L) / 1000L).toInt()
}
