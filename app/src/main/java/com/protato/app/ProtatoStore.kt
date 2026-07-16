package com.protato.app

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal const val STATE_FILE_NAME = "protato-state.json"
internal const val STATE_BACKUP_FILE_NAME = "protato-state.backup.json"

class ProtatoStore internal constructor(
    private val file: File,
    private val secretStore: SecretStore = FileSecretStore.inDirectory(file.parentFile ?: File(".")),
    private val atomicWriter: (File, String) -> Unit = ::writeStateAtomically
) {
    constructor(context: Context) : this(
        file = File(context.filesDir, STATE_FILE_NAME),
        secretStore = EncryptedSecretStore.inDirectory(context.filesDir)
    )

    fun load(): AppState {
        return loadResult().state
    }

    fun loadResult(): ProtatoStoreLoadResult {
        val backupFile = file.backupFile()
        if (!file.exists() && !backupFile.exists()) return ProtatoStoreLoadResult.Missing()
        val mainResult = file.takeIf(File::exists)?.let(::decodeStateFile)
        if (mainResult?.isSuccess == true) {
            return mainResult.getOrThrow().toLoadResult()
        }
        val backupResult = backupFile.takeIf(File::exists)?.let(::decodeStateFile)
        if (backupResult?.isSuccess == true) {
            val decoded = backupResult.getOrThrow()
            return ProtatoStoreLoadResult.Recovered(
                state = decoded.state,
                messages = decoded.messages + "Recovered app state from the last valid backup."
            )
        }
        return ProtatoStoreLoadResult.Failed(
            mainResult?.exceptionOrNull()
                ?: backupResult?.exceptionOrNull()
                ?: IllegalStateException("No readable app state file.")
        )
    }

    fun save(state: AppState): AppStateSaveResult {
        val nextSecrets = state.toSecrets()
        return runCatching {
            val previousSecrets = secretStore.load()
            val secretsChanged = nextSecrets != previousSecrets
            if (secretsChanged) secretStore.save(nextSecrets)
            try {
                atomicWriter(file, AppStateJsonCodec.encode(state.withoutSecrets()))
            } catch (stateWriteError: Throwable) {
                if (secretsChanged) {
                    runCatching { secretStore.save(previousSecrets) }
                        .onFailure(stateWriteError::addSuppressed)
                }
                throw stateWriteError
            }
        }.fold(
            onSuccess = { AppStateSaveResult.Success },
            onFailure = AppStateSaveResult::Failed
        )
    }

    private fun decodeStateFile(source: File): Result<DecodedState> = runCatching {
        val rawJson = source.readText()
        val migrationReport = AppStateMigration.inspect(rawJson)
        val loadedSecrets = secretStore.load()
        val sanitizedSecrets = loadedSecrets.sanitizeLegacyApiKeys()
        val secretMigrationMessages = if (sanitizedSecrets != loadedSecrets) {
            runCatching { secretStore.save(sanitizedSecrets) }.fold(
                onSuccess = { listOf("Removed legacy API key mask suffixes from encrypted secrets.") },
                onFailure = { emptyList() }
            )
        } else {
            emptyList()
        }
        DecodedState(
            state = AppStateJsonCodec.decode(rawJson).withSecrets(sanitizedSecrets),
            messages = migrationReport.messages + secretMigrationMessages
        )
    }
}

private fun ProtatoSecrets.sanitizeLegacyApiKeys(): ProtatoSecrets {
    return copy(
        llmProviderApiKeys = llmProviderApiKeys.mapValues { (_, apiKey) ->
            sanitizeLegacyApiKey(apiKey)
        }.filterValues { it.isNotBlank() }
    )
}

private data class DecodedState(
    val state: AppState,
    val messages: List<String>
) {
    fun toLoadResult(): ProtatoStoreLoadResult {
        return if (messages.isEmpty()) {
            ProtatoStoreLoadResult.Success(state)
        } else {
            ProtatoStoreLoadResult.Recovered(state, messages)
        }
    }
}

private fun writeStateAtomically(target: File, content: String) {
    val directory = target.parentFile ?: error("State file must have a parent directory.")
    check(directory.exists() || directory.mkdirs()) { "Unable to create state directory: $directory" }
    val temporary = File(directory, "${target.name}.tmp")
    val backupTemporary = File(directory, "${target.name}.backup.tmp")
    try {
        temporary.writeAndSync(content)
        if (target.exists() && runCatching {
                val currentJson = target.readText()
                AppStateMigration.inspect(currentJson)
                AppStateJsonCodec.decode(currentJson)
            }.isSuccess
        ) {
            backupTemporary.writeAndSync(target.readText())
            moveReplacing(backupTemporary, target.backupFile())
        }
        moveReplacing(temporary, target)
    } finally {
        temporary.delete()
        backupTemporary.delete()
    }
}

private fun File.writeAndSync(content: String) {
    FileOutputStream(this).use { output ->
        output.write(content.toByteArray(Charsets.UTF_8))
        output.fd.sync()
    }
}

private fun moveReplacing(source: File, target: File) {
    try {
        Files.move(
            source.toPath(),
            target.toPath(),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING
        )
    } catch (_: AtomicMoveNotSupportedException) {
        Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}

private fun File.backupFile(): File = File(parentFile, STATE_BACKUP_FILE_NAME)

private fun AppState.toSecrets(): ProtatoSecrets {
    return ProtatoSecrets(
        llmProviderApiKeys = llmProviders
            .mapNotNull { provider ->
                val apiKey = sanitizeLegacyApiKey(provider.apiKey)
                if (provider.id.isBlank() || apiKey.isBlank()) null else provider.id to apiKey
            }
            .toMap(),
        webDavAuthToken = webDavBackup.authToken
    )
}

internal fun AppState.withoutSecrets(): AppState {
    return copy(
        llmProviders = llmProviders.map { provider -> provider.copy(apiKey = "") },
        webDavBackup = webDavBackup.copy(authToken = "")
    )
}

private fun AppState.withSecrets(secrets: ProtatoSecrets): AppState {
    return copy(
        llmProviders = llmProviders.map { provider ->
            provider.copy(
                apiKey = sanitizeLegacyApiKey(
                    secrets.llmProviderApiKeys[provider.id] ?: provider.apiKey
                )
            )
        },
        webDavBackup = webDavBackup.copy(
            authToken = secrets.webDavAuthToken.ifBlank { webDavBackup.authToken }
        )
    )
}
