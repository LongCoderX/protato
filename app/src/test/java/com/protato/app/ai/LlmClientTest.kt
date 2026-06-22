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
            apiKey = "key"
        )
        val httpsProvider = httpProvider.copy(endpoint = "https://example.com")

        assertFalse(httpProvider.isConfiguredForChat())
        assertTrue(httpsProvider.isConfiguredForChat())
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
