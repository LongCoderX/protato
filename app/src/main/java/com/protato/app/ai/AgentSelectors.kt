package com.protato.app.ai

import com.protato.app.AgentSettings

internal fun selectUnifiedAgent(agents: List<AgentSettings>): AgentSettings? {
    val enabledAgents = agents.filter { it.enabled }
    return enabledAgents.firstOrNull { it.id == "agent-unified" }
        ?: enabledAgents.firstOrNull()
        ?: agents.firstOrNull()
}

internal fun selectEncouragerAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

internal fun selectDailySummaryAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

internal fun selectTodoAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

internal fun selectTemplateAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}

internal fun selectRecordAgent(agents: List<AgentSettings>): AgentSettings? {
    return selectUnifiedAgent(agents)
}
