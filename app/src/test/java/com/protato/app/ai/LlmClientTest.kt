package com.protato.app.ai

import com.protato.app.LlmProviderSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class LlmClientTest {
    @Test
    fun chatConfigurationRequiresHttpsEndpoint() {
        val httpProvider = LlmProviderSettings(
            id = "provider-test",
            providerKey = "openai",
            name = "Test",
            endpoint = "http://example.com",
            modelName = "model",
            apiKey = "test-key-123"
        )
        val httpsProvider = httpProvider.copy(endpoint = "https://example.com")

        assertFalse(httpProvider.isConfiguredForChat())
        assertTrue(httpsProvider.isConfiguredForChat())
        assertFalse(httpsProvider.copy(apiKey = "***").isConfiguredForChat())
        assertFalse(httpsProvider.copy(apiKey = "test***key").isConfiguredForChat())
    }

    @Test
    fun requestLlmChatRejectsHttpEndpointBeforeNetworkRequest() = runBlocking {
        val provider = LlmProviderSettings(
            id = "provider-test",
            providerKey = "openai",
            name = "Test",
            endpoint = "http://example.com",
            modelName = "model",
            apiKey = "key"
        )

        val error = runCatching {
            requestLlmChat(provider, listOf(LlmChatMessage("user", "hello")))
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error?.message.orEmpty().contains("HTTPS"))
    }

    @Test
    fun fetchLlmModelsRejectsHttpEndpointBeforeNetworkRequest() = runBlocking {
        val error = runCatching {
            fetchLlmModels("http://example.com", "key")
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error?.message.orEmpty().contains("HTTPS"))
    }

    @Test
    fun providerEndpointsNormalizeWithoutDuplicatingV1() {
        assertEquals("https://api.deepseek.com/v1/models", normalizeModelsEndpoint("https://api.deepseek.com"))
        assertEquals("https://api.minimaxi.com/v1/models", normalizeModelsEndpoint("https://api.minimaxi.com/v1"))
        assertEquals(
            "https://api.minimax.io/v1/chat/completions",
            normalizeChatEndpoint("https://api.minimax.io/v1")
        )
    }

    @Test
    fun providerErrorIncludesJsonMessage() {
        val error = providerHttpError(
            prefix = "AI 请求失败",
            responseCode = 401,
            body = """{"error":{"message":"Invalid API key"}}"""
        )

        assertTrue(error.message.orEmpty().contains("HTTP 401"))
        assertTrue(error.message.orEmpty().contains("Invalid API key"))
    }

    @Test
    fun parsesMiniMaxLegacyErrorMessage() {
        val detail = parseProviderError(
            """{"base_resp":{"status_code":1004,"status_msg":"login fail"}}"""
        )

        assertEquals("login fail", detail)
    }

    @Test
    fun parseChatContentPreservesNativeToolCalls() {
        val content = parseChatContent(
            """
            {
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "我整理了草案，请确认。",
                    "tool_calls": [
                      {
                        "id": "call_1",
                        "type": "function",
                        "function": {
                          "name": "propose_todos",
                          "arguments": "{\"todos\":[{\"title\":\"整理周报\",\"plannedPomodoros\":2}]}"
                        }
                      }
                    ]
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val parsed = parseAgentToolResponse(content)

        assertTrue(content.contains("\"tool_calls\""))
        assertEquals("我整理了草案，请确认。", parsed.visibleReply)
        assertEquals("整理周报", parsed.todoDrafts.single().title)
        assertEquals(2, parsed.todoDrafts.single().plannedPomodoros)
    }

    @Test
    fun parseChatContentPreservesLegacyFunctionCall() {
        val content = parseChatContent(
            """
            {
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": null,
                    "function_call": {
                      "name": "propose_todo_updates",
                      "arguments": "{\"edits\":[{\"id\":\"todo-1\",\"completed\":true}]}"
                    }
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val parsed = parseAgentToolResponse(content)

        assertTrue(parsed.visibleReply.contains("确认"))
        assertEquals("todo-1", parsed.todoEdits.single().id)
        assertEquals(true, parsed.todoEdits.single().completed)
    }
}
