package com.protato.app.sync

import com.protato.app.AppState
import com.protato.app.AppStateJsonCodec
import com.protato.app.JIANGUOYUN_WEBDAV_BACKUP_FILE_URL
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_URL
import com.protato.app.LlmProviderSettings
import com.protato.app.TodoItem
import com.protato.app.WebDavBackupSettings
import com.protato.app.withoutSecrets
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebDavBackupTest {
    @Test
    fun backupJsonContainsCompleteStateWithoutSecrets() {
        val state = AppState(
            todos = listOf(TodoItem(id = "todo-1", title = "验证备份")),
            nickname = "备份用户",
            llmProviders = listOf(
                LlmProviderSettings(
                    id = "provider-1",
                    providerKey = "custom",
                    name = "Provider",
                    apiKey = "provider-secret"
                )
            ),
            webDavBackup = WebDavBackupSettings(
                enabled = true,
                username = "backup@example.com",
                authToken = "webdav-secret"
            )
        )

        val backup = state.toWebDavBackupJson(createdAtMillis = 123_456L)
        val rawBackup = backup.toString()
        val restored = AppStateJsonCodec.decode(backup.getJSONObject("appState").toString())

        assertEquals("protato.webdav.backup.v1", backup.getString("schema"))
        assertEquals(123_456L, backup.getLong("createdAt"))
        assertEquals(state.withoutSecrets(), restored)
        assertFalse(rawBackup.contains("provider-secret"))
        assertFalse(rawBackup.contains("webdav-secret"))
    }

    @Test
    fun uploadUsesMkcolThenAuthenticatedJsonPut() = runBlocking {
        val folderConnection = RecordingHttpConnection(JIANGUOYUN_WEBDAV_FOLDER_URL, 405)
        val uploadConnection = RecordingHttpConnection(JIANGUOYUN_WEBDAV_BACKUP_FILE_URL, 201)
        val openedUrls = mutableListOf<String>()
        val factory = WebDavConnectionFactory { url ->
            openedUrls += url
            if (url == JIANGUOYUN_WEBDAV_FOLDER_URL) folderConnection else uploadConnection
        }
        val state = AppState(
            webDavBackup = WebDavBackupSettings(
                enabled = true,
                username = "backup@example.com",
                authToken = "app-password"
            )
        )

        uploadWebDavBackup(state, factory, nowMillis = 999L)

        assertEquals(
            listOf(JIANGUOYUN_WEBDAV_FOLDER_URL, JIANGUOYUN_WEBDAV_BACKUP_FILE_URL),
            openedUrls
        )
        assertEquals("MKCOL", folderConnection.requestMethod)
        assertEquals("PUT", uploadConnection.requestMethod)
        assertEquals("application/json", uploadConnection.getRequestProperty("Content-Type"))
        assertTrue(uploadConnection.getRequestProperty("Authorization").startsWith("Basic "))
        val body = JSONObject(uploadConnection.body.toString(Charsets.UTF_8.name()))
        assertEquals("protato.webdav.backup.v1", body.getString("schema"))
        assertEquals(999L, body.getLong("createdAt"))
    }
}

private class RecordingHttpConnection(url: String, private val status: Int) :
    HttpURLConnection(URL(url)) {
    val body = ByteArrayOutputStream()

    override fun setRequestMethod(method: String) {
        this.method = method
    }

    override fun getOutputStream(): OutputStream = body

    override fun getResponseCode(): Int = status

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit
}
