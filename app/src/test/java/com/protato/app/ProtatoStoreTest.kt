package com.protato.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ProtatoStoreTest {
    @Test
    fun loadResultReturnsMissingWhenStateFileDoesNotExist() {
        val store = ProtatoStore(tempStateFile())

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Missing)
        assertEquals(AppState(), result.state)
    }

    @Test
    fun loadResultReturnsSuccessForCurrentStateJson() {
        val store = ProtatoStore(tempStateFile())
        val state = AppState(
            todos = listOf(TodoItem(id = "todo-1", title = "写测试")),
            nickname = "测试用户"
        )
        assertEquals(AppStateSaveResult.Success, store.save(state))

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Success)
        assertEquals(state, result.state)
    }

    @Test
    fun saveMovesSecretsOutOfStateJsonAndLoadRestoresRuntimeState() {
        val dir = tempStateDir()
        val stateFile = File(dir, STATE_FILE_NAME)
        val secretFile = File(dir, SECRET_FILE_NAME)
        val store = ProtatoStore(stateFile)
        val state = AppState(
            llmProviders = listOf(
                LlmProviderSettings(
                    id = "provider-1",
                    providerKey = "custom",
                    name = "测试 Provider",
                    modelName = "test-model",
                    endpoint = "https://example.com",
                    apiKey = "provider-secret"
                )
            ),
            webDavBackup = WebDavBackupSettings(
                enabled = true,
                username = "sync-user",
                authToken = "webdav-secret"
            )
        )

        store.save(state)

        val savedStateJson = stateFile.readText()
        val savedSecretsJson = secretFile.readText()
        assertTrue(!savedStateJson.contains("provider-secret"))
        assertTrue(!savedStateJson.contains("webdav-secret"))
        assertTrue(savedSecretsJson.contains("provider-secret"))
        assertTrue(savedSecretsJson.contains("webdav-secret"))
        assertEquals(state, store.load())
    }

    @Test
    fun legacyPlainTextSecretsMoveToSecretFileAfterSave() {
        val dir = tempStateDir()
        val stateFile = File(dir, STATE_FILE_NAME)
        val secretFile = File(dir, SECRET_FILE_NAME)
        stateFile.writeText(
            AppStateJsonCodec.encode(
                AppState(
                    llmProviders = listOf(
                        LlmProviderSettings(
                            id = "provider-legacy",
                            providerKey = "custom",
                            name = "Legacy Provider",
                            modelName = "legacy-model",
                            endpoint = "https://example.com",
                            apiKey = "legacy-api-key"
                        )
                    ),
                    webDavBackup = WebDavBackupSettings(
                        enabled = true,
                        username = "legacy-user",
                        authToken = "legacy-webdav-token"
                    )
                )
            )
        )
        val store = ProtatoStore(stateFile)

        val loaded = store.load()
        store.save(loaded)

        assertEquals("legacy-api-key", loaded.llmProviders.single().apiKey)
        assertEquals("legacy-webdav-token", loaded.webDavBackup.authToken)
        assertTrue(!stateFile.readText().contains("legacy-api-key"))
        assertTrue(!stateFile.readText().contains("legacy-webdav-token"))
        assertTrue(secretFile.readText().contains("legacy-api-key"))
        assertTrue(secretFile.readText().contains("legacy-webdav-token"))
    }

    @Test
    fun loadRepairsAndPersistsLegacyApiKeyMaskSuffix() {
        val stateFile = tempStateFile()
        stateFile.writeText(
            AppStateJsonCodec.encode(
                AppState(
                    llmProviders = listOf(
                        LlmProviderSettings(
                            id = "provider-1",
                            providerKey = "deepseek",
                            name = "DeepSeek",
                            modelName = "deepseek-chat",
                            endpoint = "https://api.deepseek.com"
                        )
                    )
                )
            )
        )
        val secretStore = RecordingSecretStore(
            ProtatoSecrets(llmProviderApiKeys = mapOf("provider-1" to "real-secret***"))
        )
        val store = ProtatoStore(stateFile, secretStore = secretStore)

        val result = store.loadResult()
        val loaded = result.state

        assertEquals("real-secret", loaded.llmProviders.single().apiKey)
        assertEquals("real-secret", secretStore.current.llmProviderApiKeys["provider-1"])
        assertTrue(result is ProtatoStoreLoadResult.Recovered)
        result as ProtatoStoreLoadResult.Recovered
        assertTrue(result.messages.any { it.contains("mask suffixes") })
    }

    @Test
    fun loadResultReturnsRecoveredForLegacyStateJson() {
        val file = tempStateFile()
        file.writeText(
            """
            {
              "shortBreakMinutes": 7,
              "llmImport": {
                "provider": "custom",
                "modelName": "legacy-model",
                "endpoint": "https://example.com",
                "apiKey": "legacy-key"
              },
              "encouragerAgent": {
                "enabled": true,
                "name": "旧鼓励师",
                "prompt": "继续推进"
              }
            }
            """.trimIndent()
        )
        val store = ProtatoStore(file)

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Recovered)
        result as ProtatoStoreLoadResult.Recovered
        assertEquals(7, result.state.restMinutes)
        assertEquals("legacy-model", result.state.llmProviders.first().modelName)
        assertEquals("对话", result.state.agents.single().name)
        assertTrue(result.messages.any { it.contains("llmImport") })
        assertTrue(result.messages.any { it.contains("encouragerAgent") })
        assertTrue(result.messages.any { it.contains("shortBreakMinutes") })
        assertTrue(result.messages.any { it.contains("schema") })
    }

    @Test
    fun failedWriteDoesNotDamagePreviousStateFile() {
        val file = tempStateFile()
        val originalState = AppState(nickname = "原状态")
        val workingStore = ProtatoStore(file)
        assertEquals(AppStateSaveResult.Success, workingStore.save(originalState))
        val originalJson = file.readText()
        val failingStore = ProtatoStore(
            file = file,
            atomicWriter = { _, _ -> error("disk full") }
        )

        val result = failingStore.save(AppState(nickname = "不应落盘"))

        assertTrue(result is AppStateSaveResult.Failed)
        assertEquals(originalJson, file.readText())
        assertEquals(originalState, workingStore.load())
    }

    @Test
    fun failedStateWriteRollsBackSecrets() {
        val oldSecrets = ProtatoSecrets(
            llmProviderApiKeys = mapOf("provider-1" to "old-secret"),
            webDavAuthToken = "old-token"
        )
        val secretStore = RecordingSecretStore(oldSecrets)
        val store = ProtatoStore(
            file = tempStateFile(),
            secretStore = secretStore,
            atomicWriter = { _, _ -> error("disk full") }
        )
        val state = AppState(
            llmProviders = listOf(
                LlmProviderSettings(
                    id = "provider-1",
                    providerKey = "custom",
                    name = "Provider",
                    apiKey = "new-secret"
                )
            ),
            webDavBackup = WebDavBackupSettings(authToken = "new-token")
        )

        val result = store.save(state)

        assertTrue(result is AppStateSaveResult.Failed)
        assertEquals(oldSecrets, secretStore.current)
        assertEquals("new-secret", secretStore.saved[0].llmProviderApiKeys["provider-1"])
        assertEquals(oldSecrets, secretStore.saved[1])
    }

    @Test
    fun corruptedMainFileRecoversLastValidBackup() {
        val file = tempStateFile()
        val store = ProtatoStore(file)
        val backedUpState = AppState(nickname = "可恢复状态")
        assertEquals(AppStateSaveResult.Success, store.save(backedUpState))
        assertEquals(AppStateSaveResult.Success, store.save(AppState(nickname = "最新状态")))
        file.writeText("{ broken")

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Recovered)
        result as ProtatoStoreLoadResult.Recovered
        assertEquals(backedUpState, result.state)
        assertTrue(result.messages.any { it.contains("backup") })
    }

    @Test
    fun loadResultReturnsFailedForInvalidJsonAndLoadFallsBackToDefaultState() {
        val file = tempStateFile()
        file.writeText("{ invalid json")
        val store = ProtatoStore(file)

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Failed)
        assertTrue(result.state.todos.isEmpty())
        assertEquals(DEFAULT_NICKNAME, result.state.nickname)
    }

    @Test
    fun secretDecryptionFailureReturnsExplicitLoadFailure() {
        val file = tempStateFile()
        file.writeText(AppStateJsonCodec.encode(AppState()))
        val store = ProtatoStore(file, secretStore = ThrowingSecretStore())

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Failed)
        result as ProtatoStoreLoadResult.Failed
        assertEquals("密钥不可解密", result.error.message)
    }

    private fun tempStateFile(): File = File(tempStateDir(), STATE_FILE_NAME)

    private fun tempStateDir(): File = Files.createTempDirectory("protato-store-test").toFile()
}

private class RecordingSecretStore(initial: ProtatoSecrets) : SecretStore {
    var current: ProtatoSecrets = initial
    val saved = mutableListOf<ProtatoSecrets>()

    override fun load(): ProtatoSecrets = current

    override fun save(secrets: ProtatoSecrets) {
        current = secrets
        saved += secrets
    }
}

private class ThrowingSecretStore : SecretStore {
    override fun load(): ProtatoSecrets = throw SecretDecryptionException(IllegalStateException("invalid key"))

    override fun save(secrets: ProtatoSecrets) = error("must not save")
}
