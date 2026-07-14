package com.protato.app.sync

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.protato.app.AppState
import com.protato.app.AppStateJsonCodec
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_NAME
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_URL
import com.protato.app.JIANGUOYUN_WEBDAV_PROVIDER
import com.protato.app.JIANGUOYUN_WEBDAV_BACKUP_FILE_URL
import com.protato.app.withoutSecrets
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val BACKUP_LOG_TAG = "ProtatoBackup"

internal fun interface WebDavConnectionFactory {
    fun open(url: String): HttpURLConnection
}

private val defaultConnectionFactory = WebDavConnectionFactory { url ->
    URL(url).openConnection() as HttpURLConnection
}

internal class WebDavBackupController(private val scope: CoroutineScope) {
    var backingUp by mutableStateOf(false)
        private set

    fun upload(state: AppState, onFinished: (String) -> Unit) {
        if (backingUp) return
        scope.launch {
            backingUp = true
            val message = runCatching {
                uploadWebDavBackup(state)
                "备份完成"
            }.getOrElse { error -> error.message ?: "备份失败" }
            backingUp = false
            onFinished(message)
        }
    }
}

internal suspend fun uploadWebDavBackup(
    state: AppState,
    connectionFactory: WebDavConnectionFactory = defaultConnectionFactory,
    nowMillis: Long = System.currentTimeMillis()
) {
    val settings = state.webDavBackup
    if (!settings.enabled) {
        throw IllegalStateException("请先在设置里启用坚果云 WebDAV 备份")
    }
    if (settings.username.isBlank() || settings.authToken.isBlank()) {
        throw IllegalStateException("请填写坚果云注册邮箱和第三方应用密码")
    }
    return withContext(Dispatchers.IO) {
        val credentials = "${settings.username.trim()}:${settings.authToken}"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))
        ensureJianguoyunBackupFolder(encodedCredentials, connectionFactory)
        val connection = connectionFactory.open(JIANGUOYUN_WEBDAV_BACKUP_FILE_URL).apply {
            setHttpMethod("PUT")
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Basic $encodedCredentials")
        }
        try {
            connection.outputStream.use { stream ->
                stream.write(state.toWebDavBackupJson(nowMillis).toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val responseBody = connection.readResponseBody()
                Log.e(BACKUP_LOG_TAG, "PUT failed: HTTP $responseCode body=$responseBody")
                if (responseCode == 409) {
                    throw IllegalStateException(
                        "备份失败：坚果云目录 /${JIANGUOYUN_WEBDAV_FOLDER_NAME}/ 不存在或路径冲突 ${responseBody.toToastDetail()}"
                    )
                }
                throw IllegalStateException("备份失败：HTTP $responseCode ${responseBody.toToastDetail()}")
            }
        } finally {
            connection.disconnect()
        }
    }
}

internal fun ensureJianguoyunBackupFolder(
    encodedCredentials: String,
    connectionFactory: WebDavConnectionFactory = defaultConnectionFactory
) {
    val connection = connectionFactory.open(JIANGUOYUN_WEBDAV_FOLDER_URL)
    runCatching {
        connection.setHttpMethod("MKCOL")
    }.getOrElse { error ->
        connection.disconnect()
        throw IllegalStateException("创建坚果云备份目录失败：无法设置 WebDAV MKCOL 方法", error)
    }
    connection.apply {
        connectTimeout = 15_000
        readTimeout = 30_000
        setRequestProperty("Authorization", "Basic $encodedCredentials")
    }
    try {
        val responseCode = connection.responseCode
        if (responseCode !in setOf(200, 201, 204, 405)) {
            val responseBody = connection.readResponseBody()
            Log.e(BACKUP_LOG_TAG, "MKCOL failed: HTTP $responseCode body=$responseBody")
            throw IllegalStateException("创建坚果云备份目录失败：HTTP $responseCode ${responseBody.toToastDetail()}")
        }
    } finally {
        connection.disconnect()
    }
}

internal fun HttpURLConnection.readResponseBody(): String {
    val stream = errorStream ?: runCatching { inputStream }.getOrNull() ?: return ""
    return runCatching {
        BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }.getOrDefault("")
}

internal fun String.toToastDetail(): String {
    val normalized = replace(Regex("\\s+"), " ").trim()
    return if (normalized.isBlank()) "" else normalized.take(160)
}

internal fun HttpURLConnection.setHttpMethod(method: String) {
    runCatching {
        requestMethod = method
    }.getOrElse { error ->
        if (!setHttpMethodReflectively(method)) {
            throw IllegalStateException("设置 HTTP 方法 $method 失败", error)
        }
    }
}

internal fun HttpURLConnection.setHttpMethodReflectively(method: String): Boolean {
    val visited = mutableSetOf<Any>()
    var target: Any? = this
    while (target != null && visited.add(target)) {
        if (target.setFieldValue("method", method) == true) return true
        target = target.getFieldValue("delegate")
    }
    return false
}

internal fun Any.setFieldValue(fieldName: String, value: Any): Boolean? {
    val field = findField(javaClass, fieldName) ?: return null
    return runCatching {
        field.isAccessible = true
        field.set(this, value)
        true
    }.getOrDefault(false)
}

internal fun Any.getFieldValue(fieldName: String): Any? {
    val field = findField(javaClass, fieldName) ?: return null
    return runCatching {
        field.isAccessible = true
        field.get(this)
    }.getOrNull()
}

internal fun findField(type: Class<*>, fieldName: String): java.lang.reflect.Field? {
    var current: Class<*>? = type
    while (current != null) {
        runCatching {
            return current.getDeclaredField(fieldName)
        }
        current = current.superclass
    }
    return null
}

internal fun AppState.toWebDavBackupJson(createdAtMillis: Long): JSONObject {
    return JSONObject()
        .put("schema", "protato.webdav.backup.v1")
        .put("createdAt", createdAtMillis)
        .put("cloudProvider", JIANGUOYUN_WEBDAV_PROVIDER)
        .put("appState", JSONObject(AppStateJsonCodec.encode(withoutSecrets())))
}
