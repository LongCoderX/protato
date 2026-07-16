package com.protato.app

import java.io.File
import java.nio.file.Files
import com.protato.app.domain.timer.completeTimer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AppStateRepositoryTest {
    @Test
    fun saveRunsFileWriteOffCallingThread() = runBlocking {
        val callingThreadId = Thread.currentThread().id
        var writingThreadId = callingThreadId
        val directory = Files.createTempDirectory("app-state-repository-test").toFile()
        val store = ProtatoStore(
            file = File(directory, STATE_FILE_NAME),
            atomicWriter = { _, _ -> writingThreadId = Thread.currentThread().id }
        )
        val repository = DefaultAppStateRepository(store)

        val result = repository.save(AppState(nickname = "IO test"))

        assertEquals(AppStateSaveResult.Success, result)
        assertNotEquals(callingThreadId, writingThreadId)
    }

    @Test
    fun staleForegroundSavePreservesBackgroundTimerCompletion() = runBlocking {
        val directory = Files.createTempDirectory("app-state-repository-race-test").toFile()
        val session = TimerSession(
            mode = TimerMode.Focus,
            todoId = "todo-1",
            todoTitle = "写测试",
            startedAt = 1_000L,
            endsAt = 61_000L,
            totalSeconds = 60,
            templateId = "template-1"
        )
        val base = AppState(activeSession = session)
        val store = ProtatoStore(File(directory, STATE_FILE_NAME))
        val repository = DefaultAppStateRepository(store)
        assertEquals(AppStateSaveResult.Success, repository.save(base))

        val staleForegroundState = base.copy(
            todos = listOf(TodoItem(id = "todo-2", title = "保留前台修改"))
        )
        repository.update { latest -> completeTimer(latest, nowMillis = session.endsAt) }
        repository.update { latest -> mergeAppStateChanges(base, staleForegroundState, latest) }

        val finalState = repository.load().state
        assertEquals(listOf("todo-2"), finalState.todos.map(TodoItem::id))
        assertNull(finalState.activeSession)
        assertNotNull(finalState.pendingRecord)
        assertEquals("todo-1", finalState.pendingRecord?.todoId)
    }

    @Test
    fun mergePreservesLocallyChangedAvatarStyle() {
        val base = AppState(avatarStyle = ProfileAvatarStyle.Focus)
        val local = base.copy(avatarStyle = ProfileAvatarStyle.Rocket)
        val latest = base.copy(nickname = "后台更新")

        val merged = mergeAppStateChanges(base, local, latest)

        assertEquals(ProfileAvatarStyle.Rocket, merged.avatarStyle)
        assertEquals("后台更新", merged.nickname)
    }

    @Test
    fun mergeUsesLatestAvatarStyleWhenLocalDidNotChangeIt() {
        val base = AppState(avatarStyle = ProfileAvatarStyle.Focus)
        val local = base.copy(nickname = "前台更新")
        val latest = base.copy(avatarStyle = ProfileAvatarStyle.Trophy)

        val merged = mergeAppStateChanges(base, local, latest)

        assertEquals(ProfileAvatarStyle.Trophy, merged.avatarStyle)
        assertEquals("前台更新", merged.nickname)
    }

    @Test
    fun mergePreservesLocallyHandledOverduePromptDay() {
        val base = AppState(lastOverdueTodoDecisionDay = "2026-07-15")
        val local = base.copy(lastOverdueTodoDecisionDay = "2026-07-16")
        val latest = base.copy(nickname = "后台更新")

        val merged = mergeAppStateChanges(base, local, latest)

        assertEquals("2026-07-16", merged.lastOverdueTodoDecisionDay)
        assertEquals("后台更新", merged.nickname)
    }
}
