package com.protato.app

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal interface AppStateRepository {
    suspend fun load(): ProtatoStoreLoadResult

    suspend fun save(state: AppState): AppStateSaveResult

    suspend fun update(transform: (AppState) -> AppState): AppStateUpdateResult
}

internal sealed interface AppStateUpdateResult {
    val state: AppState?

    data class Updated(override val state: AppState) : AppStateUpdateResult

    data class Unchanged(override val state: AppState) : AppStateUpdateResult

    data class Failed(
        val error: Throwable,
        override val state: AppState? = null
    ) : AppStateUpdateResult
}

internal class DefaultAppStateRepository(
    private val store: ProtatoStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AppStateRepository {
    constructor(context: Context) : this(ProtatoStore(context))

    override suspend fun load(): ProtatoStoreLoadResult = withContext(ioDispatcher) {
        store.loadResult()
    }

    override suspend fun save(state: AppState): AppStateSaveResult = withContext(ioDispatcher) {
        repositoryMutex.withLock { store.save(state) }
    }

    override suspend fun update(
        transform: (AppState) -> AppState
    ): AppStateUpdateResult = withContext(ioDispatcher) {
        repositoryMutex.withLock {
            val loadResult = store.loadResult()
            if (loadResult is ProtatoStoreLoadResult.Failed) {
                return@withLock AppStateUpdateResult.Failed(loadResult.error)
            }
            val current = loadResult.state
            val transformed = transform(current)
            if (transformed == current) {
                return@withLock AppStateUpdateResult.Unchanged(current)
            }
            val next = transformed.copy(projectRevision = current.projectRevision + 1)
            when (val saveResult = store.save(next)) {
                AppStateSaveResult.Success -> AppStateUpdateResult.Updated(next)
                is AppStateSaveResult.Failed -> AppStateUpdateResult.Failed(saveResult.error, current)
            }
        }
    }

    private companion object {
        val repositoryMutex = Mutex()
    }
}

internal fun mergeAppStateChanges(
    base: AppState,
    local: AppState,
    latest: AppState
): AppState {
    fun <T> localOrLatest(baseValue: T, localValue: T, latestValue: T): T {
        return if (localValue != baseValue) localValue else latestValue
    }
    return AppState(
        todos = localOrLatest(base.todos, local.todos, latest.todos),
        templates = localOrLatest(base.templates, local.templates, latest.templates),
        records = localOrLatest(base.records, local.records, latest.records),
        dailySummaries = localOrLatest(base.dailySummaries, local.dailySummaries, latest.dailySummaries),
        focusMinutes = localOrLatest(base.focusMinutes, local.focusMinutes, latest.focusMinutes),
        restMinutes = localOrLatest(base.restMinutes, local.restMinutes, latest.restMinutes),
        selectedTemplateId = localOrLatest(
            base.selectedTemplateId,
            local.selectedTemplateId,
            latest.selectedTemplateId
        ),
        activeSession = localOrLatest(base.activeSession, local.activeSession, latest.activeSession),
        pendingRecord = localOrLatest(base.pendingRecord, local.pendingRecord, latest.pendingRecord),
        projectRevision = latest.projectRevision,
        nickname = localOrLatest(base.nickname, local.nickname, latest.nickname),
        llmProviders = localOrLatest(base.llmProviders, local.llmProviders, latest.llmProviders),
        agents = localOrLatest(base.agents, local.agents, latest.agents),
        agentMessages = localOrLatest(base.agentMessages, local.agentMessages, latest.agentMessages),
        agentMemory = localOrLatest(base.agentMemory, local.agentMemory, latest.agentMemory),
        webDavBackup = localOrLatest(base.webDavBackup, local.webDavBackup, latest.webDavBackup),
        recordPolishProviderId = localOrLatest(
            base.recordPolishProviderId,
            local.recordPolishProviderId,
            latest.recordPolishProviderId
        ),
        fontScale = localOrLatest(base.fontScale, local.fontScale, latest.fontScale),
        themeChoice = localOrLatest(base.themeChoice, local.themeChoice, latest.themeChoice)
    )
}
