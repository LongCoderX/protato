package com.protato.app.domain.timer

import com.protato.app.AppState
import com.protato.app.PendingPomodoroRecord
import com.protato.app.TimerMode

internal data class PendingRecord(
    val todoId: String?,
    val todoTitle: String,
    val startedAt: Long,
    val endedAt: Long,
    val focusMinutes: Int,
    val templateId: String
)

internal fun AppState.withNextRevisionFrom(previous: AppState): AppState {
    val normalizedNext = copy(projectRevision = previous.projectRevision)
    return if (normalizedNext == previous) {
        normalizedNext
    } else {
        normalizedNext.copy(projectRevision = previous.projectRevision + 1)
    }
}

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

internal fun completeSession(state: AppState): AppState {
    val session = state.activeSession ?: return state
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
        null
    }
    return state.copy(activeSession = null, pendingRecord = pendingRecord)
}
