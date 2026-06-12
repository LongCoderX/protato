package com.protato.app.sync

import android.util.Log
import com.protato.app.AppState
import com.protato.app.FieldAnswer
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_NAME
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_URL
import com.protato.app.JIANGUOYUN_WEBDAV_PROVIDER
import com.protato.app.JIANGUOYUN_WEBDAV_SYNC_FILE_URL
import com.protato.app.TodoItem
import com.protato.app.PomodoroRecord
import com.protato.app.DailySummary
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val SYNC_LOG_TAG = "ProtatoSync"

internal suspend fun syncWebDevState(state: AppState) {
    val settings = state.webDevSync
    if (!settings.enabled) {
        throw IllegalStateException("请先在设置里启用坚果云 WebDAV 同步")
    }
    if (settings.username.isBlank() || settings.authToken.isBlank()) {
        throw IllegalStateException("请填写坚果云注册邮箱和第三方应用密码")
    }
    return withContext(Dispatchers.IO) {
        val credentials = "${settings.username.trim()}:${settings.authToken}"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))
        ensureJianguoyunSyncFolder(encodedCredentials)
        val connection = (URL(JIANGUOYUN_WEBDAV_SYNC_FILE_URL).openConnection() as HttpURLConnection).apply {
            setHttpMethod("PUT")
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Basic $encodedCredentials")
        }
        try {
            connection.outputStream.use { stream ->
                stream.write(state.toWebDevSyncJson().toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val responseBody = connection.readResponseBody()
                Log.e(SYNC_LOG_TAG, "PUT failed: HTTP $responseCode body=$responseBody")
                if (responseCode == 409) {
                    throw IllegalStateException(
                        "同步失败：坚果云目录 /${JIANGUOYUN_WEBDAV_FOLDER_NAME}/ 不存在或路径冲突 ${responseBody.toToastDetail()}"
                    )
                }
                throw IllegalStateException("同步失败：HTTP $responseCode ${responseBody.toToastDetail()}")
            }
        } finally {
            connection.disconnect()
        }
    }
}

internal fun ensureJianguoyunSyncFolder(encodedCredentials: String) {
    val connection = URL(JIANGUOYUN_WEBDAV_FOLDER_URL).openConnection() as HttpURLConnection
    runCatching {
        connection.setHttpMethod("MKCOL")
    }.getOrElse { error ->
        connection.disconnect()
        throw IllegalStateException("创建坚果云同步目录失败：无法设置 WebDAV MKCOL 方法", error)
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
            Log.e(SYNC_LOG_TAG, "MKCOL failed: HTTP $responseCode body=$responseBody")
            throw IllegalStateException("创建坚果云同步目录失败：HTTP $responseCode ${responseBody.toToastDetail()}")
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

internal fun AppState.toWebDevSyncJson(): JSONObject {
    return JSONObject()
        .put("schema", "protato.jianguoyun.webdav.sync.v1")
        .put("projectRevision", projectRevision)
        .put("nickname", nickname)
        .put("cloudProvider", JIANGUOYUN_WEBDAV_PROVIDER)
        .put("syncedAt", System.currentTimeMillis())
        .put("agentMemory", agentMemory)
        .put("agentMessages", agentMessages.toJsonArrayForSync { message ->
            JSONObject()
                .put("id", message.id)
                .put("role", message.role)
                .put("content", message.content)
                .put("createdAt", message.createdAt)
        })
        .put("todos", todos.toJsonArrayForSync { todo ->
            JSONObject()
                .put("id", todo.id)
                .put("title", todo.title)
                .put("completed", todo.completed)
                .put("createdAt", todo.createdAt)
                .put("tags", todo.tags.toJsonArrayForSync { it })
                .put("dueDate", todo.dueDate ?: "")
                .put("plannedPomodoros", todo.plannedPomodoros)
        })
        .put("records", records.toJsonArrayForSync { record ->
            JSONObject()
                .put("id", record.id)
                .put("todoId", record.todoId ?: "")
                .put("todoTitle", record.todoTitle)
                .put("templateName", record.templateName)
                .put("startedAt", record.startedAt)
                .put("endedAt", record.endedAt)
                .put("focusMinutes", record.focusMinutes)
                .put("answers", record.answers.toJsonArrayForSync { answer ->
                    JSONObject()
                        .put("fieldId", answer.fieldId)
                        .put("value", answer.value)
                })
        })
        .put("dailySummaries", dailySummaries.toJsonArrayForSync { summary ->
            JSONObject()
                .put("dayKey", summary.dayKey)
                .put("generatedAt", summary.generatedAt)
                .put("title", summary.title)
                .put("content", summary.content)
                .put("pomodoroCount", summary.pomodoroCount)
                .put("focusMinutes", summary.focusMinutes)
        })
}

internal inline fun <T> List<T>.toJsonArrayForSync(transform: (T) -> Any): JSONArray {
    val array = JSONArray()
    forEach { array.put(transform(it)) }
    return array
}
