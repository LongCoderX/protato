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
        store.save(state)

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
            webDevSync = WebDevSyncSettings(
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
                    webDevSync = WebDevSyncSettings(
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
        assertEquals("legacy-webdav-token", loaded.webDevSync.authToken)
        assertTrue(!stateFile.readText().contains("legacy-api-key"))
        assertTrue(!stateFile.readText().contains("legacy-webdav-token"))
        assertTrue(secretFile.readText().contains("legacy-api-key"))
        assertTrue(secretFile.readText().contains("legacy-webdav-token"))
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
    }

    @Test
    fun loadResultReturnsFailedForInvalidJsonAndLoadFallsBackToDefaultState() {
        val file = tempStateFile()
        file.writeText("{ invalid json")
        val store = ProtatoStore(file)

        val result = store.loadResult()

        assertTrue(result is ProtatoStoreLoadResult.Failed)
        assertEquals(AppState(), result.state)
        assertEquals(AppState(), store.load())
    }

    private fun tempStateFile(): File = File(tempStateDir(), STATE_FILE_NAME)

    private fun tempStateDir(): File = Files.createTempDirectory("protato-store-test").toFile()
}
