package com.protato.app.ai

import com.protato.app.LlmProviderSettings
import com.protato.app.containsApiKeyMask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL
import javax.net.ssl.SSLException
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
        hasUsableApiKey()
}

internal fun LlmProviderSettings.hasUsableApiKey(): Boolean {
    val normalized = apiKey.trim()
    return normalized.length >= 8 && !normalized.containsApiKeyMask()
}

suspend fun fetchLlmModels(endpoint: String, apiKey: String): List<String> {
    require(endpoint.trim().startsWith("https://", ignoreCase = true)) {
        "AI endpoint 必须使用 HTTPS"
    }
    return withContext(Dispatchers.IO) {
        val requestUrl = normalizeModelsEndpoint(endpoint)
        val startedAt = System.currentTimeMillis()
        AiDiagnostics.record("MODEL_LIST start endpoint=$requestUrl")
        var connection: HttpURLConnection? = null
        try {
            val activeConnection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                if (apiKey.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $apiKey")
                }
            }
            connection = activeConnection
            val responseCode = activeConnection.responseCode
            val body = activeConnection.readResponseBody(responseCode)
            if (responseCode !in 200..299) {
                throw providerHttpError("获取模型失败", responseCode, body)
            }
            val models = parseModelNames(body).ifEmpty {
                throw IllegalStateException("接口未返回可选择模型")
            }
            AiDiagnostics.record(
                "MODEL_LIST success endpoint=$requestUrl status=$responseCode models=${models.size} durationMs=${System.currentTimeMillis() - startedAt}"
            )
            models
        } catch (error: Throwable) {
            val mapped = error.toAiNetworkError("获取模型失败", requestUrl)
            AiDiagnostics.record(
                "MODEL_LIST failure endpoint=$requestUrl durationMs=${System.currentTimeMillis() - startedAt} error=${mapped.message.orEmpty()}"
            )
            throw mapped
        } finally {
            connection?.disconnect()
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
        val requestUrl = normalizeChatEndpoint(provider.endpoint)
        val startedAt = System.currentTimeMillis()
        AiDiagnostics.record(
            "CHAT start provider=${provider.name} model=${provider.modelName} endpoint=$requestUrl messages=${messages.size} tools=${tools.size}"
        )
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
        var connection: HttpURLConnection? = null
        try {
            val activeConnection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
            }
            connection = activeConnection
            activeConnection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = activeConnection.responseCode
            val response = activeConnection.readResponseBody(responseCode)
            if (responseCode !in 200..299) {
                throw providerHttpError("AI 请求失败", responseCode, response)
            }
            val content = parseChatContent(response).ifBlank {
                throw IllegalStateException("AI 没有返回内容")
            }
            AiDiagnostics.record(
                "CHAT success provider=${provider.name} model=${provider.modelName} status=$responseCode durationMs=${System.currentTimeMillis() - startedAt}"
            )
            content
        } catch (error: Throwable) {
            val mapped = error.toAiNetworkError("AI 请求失败", requestUrl)
            AiDiagnostics.record(
                "CHAT failure provider=${provider.name} model=${provider.modelName} endpoint=$requestUrl durationMs=${System.currentTimeMillis() - startedAt} error=${mapped.message.orEmpty()}"
            )
            throw mapped
        } finally {
            connection?.disconnect()
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

internal fun normalizeModelsEndpoint(endpoint: String): String {
    val trimmed = endpoint.trim().trimEnd('/')
    return when {
        trimmed.endsWith("/models") -> trimmed
        trimmed.endsWith("/v1") -> "$trimmed/models"
        else -> "$trimmed/v1/models"
    }
}

internal fun normalizeChatEndpoint(endpoint: String): String {
    val trimmed = endpoint.trim().trimEnd('/')
    return when {
        trimmed.endsWith("/chat/completions") -> trimmed
        trimmed.endsWith("/v1") -> "$trimmed/chat/completions"
        else -> "$trimmed/v1/chat/completions"
    }
}

private fun HttpURLConnection.readResponseBody(responseCode: Int): String {
    val stream = if (responseCode in 200..299) {
        inputStream
    } else {
        errorStream ?: runCatching { inputStream }.getOrNull()
    } ?: return ""
    return BufferedReader(InputStreamReader(stream)).use { it.readText() }
}

internal fun providerHttpError(prefix: String, responseCode: Int, body: String): IllegalStateException {
    val detail = parseProviderError(body)
    val message = buildString {
        append(prefix)
        append("：HTTP ")
        append(responseCode)
        if (detail.isNotBlank()) {
            append(" · ")
            append(detail)
        }
    }
    return IllegalStateException(message)
}

internal fun parseProviderError(body: String): String {
    val normalized = body.replace(Regex("\\s+"), " ").trim()
    if (normalized.isBlank()) return ""
    return runCatching {
        val root = JSONObject(normalized)
        val error = root.optJSONObject("error")
        val baseResponse = root.optJSONObject("base_resp")
        when {
            error != null -> error.optString("message").ifBlank { error.optString("type") }
            baseResponse != null -> baseResponse.optString("status_msg")
            root.optString("message").isNotBlank() -> root.optString("message")
            else -> normalized
        }
    }.getOrDefault(normalized).take(300)
}

private fun Throwable.toAiNetworkError(prefix: String, endpoint: String): Throwable {
    if (this is IllegalStateException || this is IllegalArgumentException) return this
    val detail = when (this) {
        is UnknownHostException -> "无法解析域名，请检查网络、VPN 或 DNS"
        is SocketTimeoutException -> "连接超时，请检查网络质量或 Provider 服务状态"
        is SSLException -> "TLS 连接失败，请检查系统时间、证书或代理"
        is IOException -> message?.takeIf { it.isNotBlank() } ?: "网络读写失败"
        else -> message?.takeIf { it.isNotBlank() } ?: javaClass.simpleName
    }
    return IllegalStateException("$prefix：$detail · $endpoint", this)
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
