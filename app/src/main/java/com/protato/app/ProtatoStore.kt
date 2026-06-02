package com.protato.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProtatoStore(context: Context) {
    private val file = File(context.filesDir, "protato-state.json")

    fun load(): AppState {
        if (!file.exists()) return AppState()
        return runCatching {
            val root = JSONObject(file.readText())
            val templates = root.optJSONArray("templates")?.toTemplates().orEmpty()
            val selectedTemplateId = root.optString(
                "selectedTemplateId",
                templates.firstOrNull()?.id ?: defaultTemplate().id
            )
            val legacyLlmImport = root.optJSONObject("llmImport")?.toLlmImportSettings()
            val legacyEncouragerAgent = root.optJSONObject("encouragerAgent")?.toEncouragerAgentSettings()
            val llmProviders = root.optJSONArray("llmProviders")?.toLlmProviders().orEmpty()
            val agents = root.optJSONArray("agents")?.toAgents().orEmpty()
            AppState(
                todos = root.optJSONArray("todos")?.toTodos().orEmpty(),
                templates = templates.ifEmpty { listOf(defaultTemplate()) },
                records = root.optJSONArray("records")?.toRecords().orEmpty(),
                dailySummaries = root.optJSONArray("dailySummaries")?.toDailySummaries().orEmpty(),
                focusMinutes = root.optInt("focusMinutes", 25).coerceIn(1, 180),
                restMinutes = root.optInt(
                    "restMinutes",
                    root.optInt("shortBreakMinutes", 5)
                ).coerceIn(1, 60),
                selectedTemplateId = selectedTemplateId,
                activeSession = root.optJSONObject("activeSession")?.toTimerSession(),
                pendingRecord = root.optJSONObject("pendingRecord")?.toPendingRecord(),
                projectRevision = root.optInt("projectRevision", 1).coerceAtLeast(1),
                nickname = root.optString("nickname", DEFAULT_NICKNAME).ifBlank { DEFAULT_NICKNAME },
                llmProviders = llmProviders.ifEmpty {
                    defaultLlmProviders().withLegacyLlmImport(legacyLlmImport)
                },
                agents = agents.ifEmpty {
                    defaultAgents().withLegacyEncouragerAgent(legacyEncouragerAgent)
                }
            )
        }.getOrElse { AppState() }
    }

    fun save(state: AppState) {
        val root = JSONObject()
            .put("todos", state.todos.toJsonArray { it.toJson() })
            .put("templates", state.templates.toJsonArray { it.toJson() })
            .put("records", state.records.toJsonArray { it.toJson() })
            .put("dailySummaries", state.dailySummaries.toJsonArray { it.toJson() })
            .put("focusMinutes", state.focusMinutes)
            .put("restMinutes", state.restMinutes)
            .put("selectedTemplateId", state.selectedTemplateId)
            .put("activeSession", state.activeSession?.toJson() ?: JSONObject.NULL)
            .put("pendingRecord", state.pendingRecord?.toJson() ?: JSONObject.NULL)
            .put("projectRevision", state.projectRevision)
            .put("nickname", state.nickname)
            .put("llmProviders", state.llmProviders.toJsonArray { it.toJson() })
            .put("agents", state.agents.toJsonArray { it.toJson() })
        file.writeText(root.toString(2))
    }
}

private fun JSONArray.toTodos(): List<TodoItem> = mapObjects { item ->
    TodoItem(
        id = item.optString("id"),
        title = item.optString("title"),
        completed = item.optBoolean("completed"),
        createdAt = item.optLong("createdAt", System.currentTimeMillis())
    )
}.filter { it.id.isNotBlank() && it.title.isNotBlank() }

private fun JSONArray.toTemplates(): List<RecordTemplate> = mapObjects { item ->
    RecordTemplate(
        id = item.optString("id"),
        name = item.optString("name"),
        fields = item.optJSONArray("fields")?.toTemplateFields().orEmpty()
    )
}.filter { it.id.isNotBlank() && it.name.isNotBlank() }

private fun JSONArray.toTemplateFields(): List<TemplateField> = mapObjects { item ->
    val type = runCatching { FieldType.valueOf(item.optString("type")) }.getOrDefault(FieldType.ShortAnswer)
    TemplateField(
        id = item.optString("id"),
        title = item.optString("title"),
        type = type,
        options = item.optJSONArray("options")?.toStringList().orEmpty(),
        required = item.optBoolean("required")
    )
}.filter { it.id.isNotBlank() && it.title.isNotBlank() }

private fun JSONArray.toRecords(): List<PomodoroRecord> = mapObjects { item ->
    val todoId = item.optString("todoId")
    PomodoroRecord(
        id = item.optString("id"),
        todoId = todoId.takeIf { it.isNotBlank() },
        todoTitle = item.optString("todoTitle"),
        templateId = item.optString("templateId"),
        templateName = item.optString("templateName"),
        startedAt = item.optLong("startedAt"),
        endedAt = item.optLong("endedAt"),
        focusMinutes = item.optInt("focusMinutes"),
        answers = item.optJSONArray("answers")?.toAnswers().orEmpty()
    )
}.filter { it.id.isNotBlank() }

private fun JSONArray.toDailySummaries(): List<DailySummary> = mapObjects { item ->
    DailySummary(
        dayKey = item.optString("dayKey"),
        generatedAt = item.optLong("generatedAt"),
        title = item.optString("title"),
        content = item.optString("content"),
        pomodoroCount = item.optInt("pomodoroCount"),
        focusMinutes = item.optInt("focusMinutes")
    )
}.filter { it.dayKey.isNotBlank() && it.content.isNotBlank() }

private fun JSONArray.toAnswers(): List<FieldAnswer> = mapObjects { item ->
    FieldAnswer(
        fieldId = item.optString("fieldId"),
        value = item.optString("value")
    )
}.filter { it.fieldId.isNotBlank() }

private fun JSONObject.toTimerSession(): TimerSession? {
    val mode = when (optString("mode")) {
        "Focus" -> TimerMode.Focus
        "Break", "ShortBreak", "LongBreak" -> TimerMode.Break
        else -> return null
    }
    return TimerSession(
        mode = mode,
        todoId = optString("todoId").takeIf { it.isNotBlank() },
        todoTitle = optString("todoTitle"),
        startedAt = optLong("startedAt"),
        endsAt = optLong("endsAt"),
        totalSeconds = optInt("totalSeconds"),
        templateId = optString("templateId"),
        pausedRemainingSeconds = optInt("pausedRemainingSeconds")
            .takeIf { opt("pausedRemainingSeconds") != null && it > 0 }
    ).takeIf { it.startedAt > 0L && it.endsAt > 0L && it.totalSeconds > 0 }
}

private fun JSONObject.toPendingRecord(): PendingPomodoroRecord? {
    return PendingPomodoroRecord(
        todoId = optString("todoId").takeIf { it.isNotBlank() },
        todoTitle = optString("todoTitle"),
        startedAt = optLong("startedAt"),
        endedAt = optLong("endedAt"),
        focusMinutes = optInt("focusMinutes"),
        templateId = optString("templateId")
    ).takeIf { it.startedAt > 0L && it.endedAt > 0L && it.focusMinutes > 0 }
}

private fun JSONObject.toLlmImportSettings(): LlmImportSettings {
    return LlmImportSettings(
        provider = optString("provider"),
        modelName = optString("modelName"),
        endpoint = optString("endpoint"),
        apiKey = optString("apiKey")
    )
}

private fun JSONArray.toLlmProviders(): List<LlmProviderSettings> = mapObjects { item ->
    item.toLlmProviderSettings()
}.filter { it.id.isNotBlank() && it.name.isNotBlank() }

private fun JSONObject.toLlmProviderSettings(): LlmProviderSettings {
    return LlmProviderSettings(
        id = optString("id"),
        providerKey = optString("providerKey"),
        name = optString("name"),
        modelName = optString("modelName"),
        endpoint = optString("endpoint"),
        apiKey = optString("apiKey")
    )
}

private fun JSONObject.toEncouragerAgentSettings(): EncouragerAgentSettings {
    return EncouragerAgentSettings(
        enabled = optBoolean("enabled"),
        name = optString("name", "鼓励师").ifBlank { "鼓励师" },
        prompt = optString(
            "prompt",
            DEFAULT_ENCOURAGER_PROMPT
        ).ifBlank {
            DEFAULT_ENCOURAGER_PROMPT
        }
    )
}

private fun JSONArray.toAgents(): List<AgentSettings> = mapObjects { item ->
    AgentSettings(
        id = item.optString("id"),
        enabled = item.optBoolean("enabled", true),
        name = item.optString("name", "Agent").ifBlank { "Agent" },
        providerId = item.optString("providerId"),
        prompt = item.optString("prompt", DEFAULT_ENCOURAGER_PROMPT).ifBlank {
            DEFAULT_ENCOURAGER_PROMPT
        },
        permissions = item.optJSONObject("permissions")?.toAgentDataPermissions()
            ?: AgentDataPermissions()
    )
}.filter { it.id.isNotBlank() && it.name.isNotBlank() }

private fun JSONObject.toAgentDataPermissions(): AgentDataPermissions {
    return AgentDataPermissions(
        dailyRecords = optBoolean("dailyRecords"),
        todos = optBoolean("todos"),
        templates = optBoolean("templates")
    )
}

private fun TodoItem.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("completed", completed)
    .put("createdAt", createdAt)

private fun RecordTemplate.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("fields", fields.toJsonArray { it.toJson() })

private fun TemplateField.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("type", type.name)
    .put("options", options.toJsonArray { it })
    .put("required", required)

private fun PomodoroRecord.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("todoId", todoId ?: "")
    .put("todoTitle", todoTitle)
    .put("templateId", templateId)
    .put("templateName", templateName)
    .put("startedAt", startedAt)
    .put("endedAt", endedAt)
    .put("focusMinutes", focusMinutes)
    .put("answers", answers.toJsonArray { it.toJson() })

private fun DailySummary.toJson(): JSONObject = JSONObject()
    .put("dayKey", dayKey)
    .put("generatedAt", generatedAt)
    .put("title", title)
    .put("content", content)
    .put("pomodoroCount", pomodoroCount)
    .put("focusMinutes", focusMinutes)

private fun FieldAnswer.toJson(): JSONObject = JSONObject()
    .put("fieldId", fieldId)
    .put("value", value)

private fun TimerSession.toJson(): JSONObject = JSONObject()
    .put("mode", mode.name)
    .put("todoId", todoId ?: "")
    .put("todoTitle", todoTitle)
    .put("startedAt", startedAt)
    .put("endsAt", endsAt)
    .put("totalSeconds", totalSeconds)
    .put("templateId", templateId)
    .put("pausedRemainingSeconds", pausedRemainingSeconds ?: JSONObject.NULL)

private fun PendingPomodoroRecord.toJson(): JSONObject = JSONObject()
    .put("todoId", todoId ?: "")
    .put("todoTitle", todoTitle)
    .put("startedAt", startedAt)
    .put("endedAt", endedAt)
    .put("focusMinutes", focusMinutes)
    .put("templateId", templateId)

private fun LlmImportSettings.toJson(): JSONObject = JSONObject()
    .put("provider", provider)
    .put("modelName", modelName)
    .put("endpoint", endpoint)
    .put("apiKey", apiKey)

private fun LlmProviderSettings.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("providerKey", providerKey)
    .put("name", name)
    .put("modelName", modelName)
    .put("endpoint", endpoint)
    .put("apiKey", apiKey)

private fun EncouragerAgentSettings.toJson(): JSONObject = JSONObject()
    .put("enabled", enabled)
    .put("name", name)
    .put("prompt", prompt)

private fun AgentSettings.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("enabled", enabled)
    .put("name", name)
    .put("providerId", providerId)
    .put("prompt", prompt)
    .put("permissions", permissions.toJson())

private fun AgentDataPermissions.toJson(): JSONObject = JSONObject()
    .put("dailyRecords", dailyRecords)
    .put("todos", todos)
    .put("templates", templates)

private fun List<LlmProviderSettings>.withLegacyLlmImport(
    legacy: LlmImportSettings?
): List<LlmProviderSettings> {
    if (legacy == null || legacy == LlmImportSettings()) return this
    val providerKey = legacy.provider.ifBlank { "custom" }.lowercase()
    val legacyProvider = LlmProviderSettings(
        id = "provider-legacy",
        providerKey = providerKey,
        name = legacy.provider.ifBlank { "自定义 Provider" },
        modelName = legacy.modelName,
        endpoint = legacy.endpoint,
        apiKey = legacy.apiKey
    )
    return listOf(legacyProvider) + filterNot { it.providerKey == providerKey }
}

private fun List<AgentSettings>.withLegacyEncouragerAgent(
    legacy: EncouragerAgentSettings?
): List<AgentSettings> {
    if (legacy == null) return this
    val legacyAgent = AgentSettings(
        id = "agent-encourager",
        enabled = legacy.enabled,
        name = legacy.name,
        prompt = legacy.prompt,
        permissions = AgentDataPermissions(dailyRecords = true)
    )
    return listOf(legacyAgent) + filterNot { it.id == legacyAgent.id }
}

private fun JSONArray.toStringList(): List<String> {
    return List(length()) { index -> optString(index) }.filter { it.isNotBlank() }
}

private inline fun <T> List<T>.toJsonArray(transform: (T) -> Any): JSONArray {
    val array = JSONArray()
    forEach { array.put(transform(it)) }
    return array
}

private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
    return List(length()) { index -> transform(optJSONObject(index) ?: JSONObject()) }
}
