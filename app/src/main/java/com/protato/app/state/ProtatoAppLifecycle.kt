package com.protato.app.state

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.protato.app.AppState
import com.protato.app.TimerMode
import com.protato.app.TimerSession
import com.protato.app.domain.timer.remainingSeconds
import com.protato.app.platform.ProtatoEffectRunner
import kotlinx.coroutines.delay

@Composable
internal fun ProtatoAppLifecycle(
    stateHolder: ProtatoStateHolder,
    appState: AppState,
    activeSession: TimerSession?,
    isRunning: Boolean,
    effectRunner: ProtatoEffectRunner,
    onTimerModeChange: (TimerMode) -> Unit,
    onNowMillisChange: (Long) -> Unit,
    onTimerComplete: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        stateHolder.loadCurrentState()
    }
    LaunchedEffect(appState, stateHolder.isLoaded) {
        if (stateHolder.isLoaded) stateHolder.saveCurrentState()
    }
    LaunchedEffect(stateHolder.saveErrorMessage) {
        stateHolder.saveErrorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            stateHolder.clearSaveError()
        }
    }
    LaunchedEffect(activeSession?.mode) {
        activeSession?.let { onTimerModeChange(it.mode) }
    }
    LaunchedEffect(stateHolder.isLoaded) {
        activeSession
            ?.takeIf { stateHolder.isLoaded && it.pausedRemainingSeconds == null }
            ?.let { session ->
                effectRunner.runAll(
                    listOf(
                        ProtatoEffect.StartTimerService(session),
                        ProtatoEffect.ScheduleTimerAlarm(session)
                    )
                )
            }
    }
    LaunchedEffect(activeSession?.endsAt, activeSession?.pausedRemainingSeconds) {
        while (activeSession != null && isRunning) {
            val nowMillis = System.currentTimeMillis()
            onNowMillisChange(nowMillis)
            if (activeSession.remainingSeconds(nowMillis) <= 0) {
                onTimerComplete()
                break
            }
            delay(1_000)
        }
    }
}
