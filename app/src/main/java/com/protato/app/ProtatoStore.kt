package com.protato.app

import android.content.Context
import java.io.File

internal const val STATE_FILE_NAME = "protato-state.json"

class ProtatoStore internal constructor(
    private val file: File,
    private val secretStore: SecretStore = FileSecretStore.inDirectory(file.parentFile ?: File("."))
) {
    constructor(context: Context) : this(
        file = File(context.filesDir, STATE_FILE_NAME),
        secretStore = FileSecretStore.inDirectory(context.filesDir)
    )

    fun load(): AppState {
        return loadResult().state
    }

    fun loadResult(): ProtatoStoreLoadResult {
        if (!file.exists()) return ProtatoStoreLoadResult.Missing()
        return runCatching {
            val rawJson = file.readText()
            val migrationReport = AppStateMigration.inspect(rawJson)
            val decodedState = AppStateJsonCodec.decode(rawJson)
            val state = decodedState.withSecrets(secretStore.load())
            if (migrationReport.hasMessages) {
                ProtatoStoreLoadResult.Recovered(
                    state = state,
                    messages = migrationReport.messages
                )
            } else {
                ProtatoStoreLoadResult.Success(state)
            }
        }.getOrElse { error ->
            ProtatoStoreLoadResult.Failed(error)
        }
    }

    fun save(state: AppState) {
        secretStore.save(state.toSecrets())
        file.writeText(AppStateJsonCodec.encode(state.withoutSecrets()))
    }
}

private fun AppState.toSecrets(): ProtatoSecrets {
    return ProtatoSecrets(
        llmProviderApiKeys = llmProviders
            .filter { it.id.isNotBlank() && it.apiKey.isNotBlank() }
            .associate { it.id to it.apiKey },
        webDavAuthToken = webDevSync.authToken
    )
}

private fun AppState.withoutSecrets(): AppState {
    return copy(
        llmProviders = llmProviders.map { provider -> provider.copy(apiKey = "") },
        webDevSync = webDevSync.copy(authToken = "")
    )
}

private fun AppState.withSecrets(secrets: ProtatoSecrets): AppState {
    return copy(
        llmProviders = llmProviders.map { provider ->
            provider.copy(apiKey = secrets.llmProviderApiKeys[provider.id] ?: provider.apiKey)
        },
        webDevSync = webDevSync.copy(
            authToken = secrets.webDavAuthToken.ifBlank { webDevSync.authToken }
        )
    )
}
