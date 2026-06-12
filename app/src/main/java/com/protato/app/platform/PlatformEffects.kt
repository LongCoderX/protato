package com.protato.app.platform

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService
import com.protato.app.ACTION_START_TIMER_SERVICE
import com.protato.app.PomodoroTimerService

internal fun Context.startPomodoroService(action: String) {
    val intent = Intent(this, PomodoroTimerService::class.java).setAction(action)
    if (action == ACTION_START_TIMER_SERVICE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(this, intent)
    } else {
        startService(intent)
    }
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
