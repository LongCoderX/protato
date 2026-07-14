package com.protato.app.ai

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRequestControllersTest {
    @Test
    fun requestFailureLeavesLoadingState() = runBlocking {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.Default)
        val controller = AsyncRequestController<String>(scope)

        assertTrue(controller.submit(request = { error("request failed") }))
        awaitState { controller.state.value is AsyncRequestState.Failed }

        assertTrue(controller.state.value is AsyncRequestState.Failed)
        job.cancel()
    }

    @Test
    fun duplicateSubmitIsRejectedWhileRequestIsRunning() = runBlocking {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.Default)
        val controller = AsyncRequestController<String>(scope)
        val gate = CompletableDeferred<String>()

        assertTrue(controller.submit(request = { gate.await() }))
        assertFalse(controller.submit(request = { "duplicate" }))
        gate.complete("first")
        awaitState { controller.state.value is AsyncRequestState.Success }
        assertEquals("first", (controller.state.value as AsyncRequestState.Success).value)
        job.cancel()
    }

    @Test
    fun cancelStopsRequestAndResetsState() = runBlocking {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.Default)
        val controller = AsyncRequestController<String>(scope)
        val started = AtomicBoolean(false)
        val gate = CompletableDeferred<String>()

        controller.submit(request = {
            started.set(true)
            gate.await()
        })
        awaitState { started.get() }
        controller.cancel()

        assertEquals(AsyncRequestState.Idle, controller.state.value)
        job.cancel()
    }

    @Test
    fun latestRequestWinsWhenOlderRequestFinishesLater() = runBlocking {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.Default)
        val controller = AsyncRequestController<String>(scope)
        val oldGate = CompletableDeferred<String>()
        val newGate = CompletableDeferred<String>()

        controller.submitLatest(request = { oldGate.await() })
        controller.submitLatest(request = { newGate.await() })
        oldGate.complete("old")
        newGate.complete("new")
        awaitState {
            (controller.state.value as? AsyncRequestState.Success)?.value == "new"
        }

        assertEquals("new", (controller.state.value as AsyncRequestState.Success).value)
        job.cancel()
    }

    private suspend fun awaitState(predicate: () -> Boolean) {
        withTimeout(2_000) {
            while (!predicate()) delay(5)
        }
    }
}
