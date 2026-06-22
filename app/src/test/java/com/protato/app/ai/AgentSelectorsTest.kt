package com.protato.app.ai

import com.protato.app.AgentSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgentSelectorsTest {
    @Test
    fun selectUnifiedAgentReturnsNullWhenAllAgentsAreDisabled() {
        val result = selectUnifiedAgent(
            listOf(
                AgentSettings(id = "agent-unified", enabled = false),
                AgentSettings(id = "agent-other", enabled = false)
            )
        )

        assertNull(result)
    }

    @Test
    fun selectUnifiedAgentPrefersEnabledUnifiedAgent() {
        val result = selectUnifiedAgent(
            listOf(
                AgentSettings(id = "agent-other", enabled = true),
                AgentSettings(id = "agent-unified", enabled = true)
            )
        )

        assertEquals("agent-unified", result?.id)
    }
}
