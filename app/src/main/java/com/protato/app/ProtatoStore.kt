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
                shortBreakMinutes = root.optInt("shortBreakMinutes", 5).coerceIn(1, 60),
                longBreakMinutes = root.optInt("longBreakMinutes", 15).coerceIn(1, 120),
                selectedTemplateId = selectedTemplateId
            )
        }.getOrElse { AppState() }
    }

    fun save(state: AppState) {
        val root = JSONObject()
            .put("todos", state.todos.toJsonArray { it.toJson() })
            .put("templates", state.templates.toJsonArray { it.toJson() })
            .put("records", state.records.toJsonArray { it.toJson() })
            .put("focusMinutes", state.focusMinutes)
            .put("shortBreakMinutes", state.shortBreakMinutes)
            .put("longBreakMinutes", state.longBreakMinutes)
            .put("selectedTemplateId", state.selectedTemplateId)
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
