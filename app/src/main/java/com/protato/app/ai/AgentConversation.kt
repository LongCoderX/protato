package com.protato.app.ai

import com.protato.app.AgentConversationMessage
import com.protato.app.util.newId

internal fun List<AgentConversationMessage>.appendAgentMessage(
    role: String,
    content: String
): List<AgentConversationMessage> {
    return (this + AgentConversationMessage(
        id = newId(),
        role = role,
        content = content.trim()
    )).takeLast(80)
}
