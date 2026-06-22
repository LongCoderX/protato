package com.protato.app.ai

import com.protato.app.LlmProviderSettings
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class LlmChatMessage(
    val role: String,
    val content: String
)

internal data class LlmChatTool(
    val name: String,
    val description: String,
    val parameters: JSONObject
)

internal fun LlmProviderSettings.isConfiguredForChat(): Boolean {
    return endpoint.isNotBlank() &&
        endpoint.trim().startsWith("https://", ignoreCase = true) &&
        modelName.isNotBlank() &&
        apiKey.isNotBlank()
}

suspend fun fetchLlmModels(endpoint: String, apiKey: String): List<String> {
    require(endpoint.trim().startsWith("https://", ignoreCase = true)) {
        "AI endpoint 必须使用 HTTPS"
    }
    return withContext(Dispatchers.IO) {
        val connection = (URL(normalizeModelsEndpoint(endpoint)).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            if (apiKey.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $apiKey")
            }
        }
        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (responseCode !in 200..299) {
                throw IllegalStateException("获取模型失败：HTTP $responseCode")
            }
            parseModelNames(body).ifEmpty {
                throw IllegalStateException("接口未返回可选择模型")
            }
        } finally {
            connection.disconnect()
        }
    }
}

internal suspend fun requestLlmChat(
    provider: LlmProviderSettings,
    messages: List<LlmChatMessage>,
    tools: List<LlmChatTool> = emptyList()
): String {
    require(provider.endpoint.trim().startsWith("https://", ignoreCase = true)) {
        "AI endpoint 必须使用 HTTPS"
    }
    return withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("model", provider.modelName)
            .put("temperature", 0.8)
            .put(
                "messages",
                JSONArray().apply {
                    messages.forEach { message ->
                        put(
                            JSONObject()
                                .put("role", message.role)
                                .put("content", message.content)
                        )
                    }
                }
            )
        if (tools.isNotEmpty()) {
            body
                .put("tools", tools.toChatCompletionToolsJson())
                .put("tool_choice", "auto")
        }
        val connection = (URL(normalizeChatEndpoint(provider.endpoint)).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        }
        try {
            connection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (responseCode !in 200..299) {
                throw IllegalStateException("AI 请求失败：HTTP $responseCode")
            }
            parseChatContent(response).ifBlank {
                throw IllegalStateException("AI 没有返回内容")
            }
        } finally {
            connection.disconnect()
        }
    }
}

private fun List<LlmChatTool>.toChatCompletionToolsJson(): JSONArray {
    return JSONArray().apply {
        forEach { tool ->
            put(
                JSONObject()
                    .put("type", "function")
                    .put(
                        "function",
                        JSONObject()
                            .put("name", tool.name)
                            .put("description", tool.description)
                            .put("parameters", tool.parameters)
                    )
            )
        }
    }
}

private fun normalizeModelsEndpoint(endpoint: String): String {
    val trimmed = endpoint.trim().trimEnd('/')
    return when {
        trimmed.endsWith("/models") -> trimmed
        trimmed.endsWith("/v1") -> "$trimmed/models"
        else -> "$trimmed/v1/models"
    }
}

private fun normalizeChatEndpoint(endpoint: String): String {
    val trimmed = endpoint.trim().trimEnd('/')
    return when {
        trimmed.endsWith("/chat/completions") -> trimmed
        trimmed.endsWith("/v1") -> "$trimmed/chat/completions"
        else -> "$trimmed/v1/chat/completions"
    }
}

private fun parseModelNames(body: String): List<String> {
    val root = body.trim()
    val models = if (root.startsWith("[")) {
        JSONArray(root)
    } else {
        JSONObject(root).optJSONArray("data") ?: JSONArray()
    }
    return List(models.length()) { index ->
        val item = models.opt(index)
        when (item) {
            is JSONObject -> item.optString("id").ifBlank { item.optString("name") }
            is String -> item
            else -> ""
        }
    }.filter { it.isNotBlank() }.distinct()
}

internal fun parseChatContent(body: String): String {
    val root = JSONObject(body)
    val choice = root.optJSONArray("choices")?.optJSONObject(0) ?: return ""
    val message = choice.optJSONObject("message")
    val messageContent = message?.optString("content").orEmpty()
    val toolCalls = message?.optJSONArray("tool_calls")
    val functionCall = message?.optJSONObject("function_call")
    val text = choice.optString("text")
    return buildString {
        if (messageContent.isNotBlank()) {
            append(messageContent.trim())
        } else if (text.isNotBlank()) {
            append(text.trim())
        }
        if (toolCalls != null && toolCalls.length() > 0) {
            if (isNotBlank()) append("\n")
            append(JSONObject().put("tool_calls", toolCalls).toString())
        } else if (functionCall != null) {
            if (isNotBlank()) append("\n")
            append(JSONObject().put("tool_calls", JSONArray().put(JSONObject().put("function", functionCall))).toString())
        }
    }.trim()
}
