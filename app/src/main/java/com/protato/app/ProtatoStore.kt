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
            AppState(
                todos = root.optJSONArray("todos")?.toTodos().orEmpty(),
                templates = templates.ifEmpty { listOf(defaultTemplate()) },
                records = root.optJSONArray("records")?.toRecords().orEmpty(),
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
                llmImport = root.optJSONObject("llmImport")?.toLlmImportSettings() ?: LlmImportSettings(),
                encouragerAgent = root.optJSONObject("encouragerAgent")?.toEncouragerAgentSettings()
                    ?: EncouragerAgentSettings()
            )
        }.getOrElse { AppState() }
    }

    fun save(state: AppState) {
        val root = JSONObject()
            .put("todos", state.todos.toJsonArray { it.toJson() })
            .put("templates", state.templates.toJsonArray { it.toJson() })
            .put("records", state.records.toJsonArray { it.toJson() })
            .put("focusMinutes", state.focusMinutes)
            .put("restMinutes", state.restMinutes)
            .put("selectedTemplateId", state.selectedTemplateId)
            .put("activeSession", state.activeSession?.toJson() ?: JSONObject.NULL)
            .put("pendingRecord", state.pendingRecord?.toJson() ?: JSONObject.NULL)
            .put("projectRevision", state.projectRevision)
            .put("nickname", state.nickname)
            .put("llmImport", state.llmImport.toJson())
            .put("encouragerAgent", state.encouragerAgent.toJson())
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

private fun EncouragerAgentSettings.toJson(): JSONObject = JSONObject()
    .put("enabled", enabled)
    .put("name", name)
    .put("prompt", prompt)

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
