package com.protato.app.platform

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.ContextCompat.startForegroundService
import com.protato.app.TimerSession

internal fun Context.startPomodoroService(session: TimerSession) {
    val intent = Intent(this, PomodoroTimerService::class.java)
        .setAction(ACTION_START_TIMER_SERVICE)
        .putExtra(EXTRA_TIMER_MODE, session.mode.name)
        .putExtra(EXTRA_TIMER_TODO_ID, session.todoId)
        .putExtra(EXTRA_TIMER_TODO_TITLE, session.todoTitle)
        .putExtra(EXTRA_TIMER_STARTED_AT, session.startedAt)
        .putExtra(EXTRA_TIMER_ENDS_AT, session.endsAt)
        .putExtra(EXTRA_TIMER_TOTAL_SECONDS, session.totalSeconds)
        .putExtra(EXTRA_TIMER_TEMPLATE_ID, session.templateId)
    startForegroundService(this, intent)
}

internal fun playTodoCompletedSound() {
    playToneSequence(
        listOf(
            ToneGenerator.TONE_PROP_ACK to 120,
            ToneGenerator.TONE_PROP_BEEP to 120
        )
    )
}

internal fun playEncouragementSound() {
    playToneSequence(
        listOf(
            ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 140,
            ToneGenerator.TONE_PROP_ACK to 140,
            ToneGenerator.TONE_PROP_ACK to 180
        )
    )
}

internal fun playToneSequence(tones: List<Pair<Int, Int>>) {
    val generator = runCatching {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    }.getOrNull() ?: return
    Thread {
        tones.forEach { (tone, durationMs) ->
            generator.startTone(tone, durationMs)
            Thread.sleep(durationMs.toLong() + 40L)
        }
        generator.release()
    }.start()
}
