package com.protato.app.ai

import com.protato.app.TodoDraft
import com.protato.app.TodoEditDraft
import com.protato.app.todoEditDraftKey
import com.protato.app.util.normalizedReminderAt
import com.protato.app.util.normalizedTodoDate
import com.protato.app.util.parseTodoTags
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

internal data class AgentToolResponse(
    val visibleReply: String,
    val todoDrafts: List<TodoDraft>,
    val todoEdits: List<TodoEditDraft>,
    val actions: List<AgentAction> = buildList {
        if (visibleReply.isNotBlank()) add(AgentAction.Reply(visibleReply))
        if (todoDrafts.isNotEmpty()) add(AgentAction.ProposeTodos(todoDrafts))
        if (todoEdits.isNotEmpty()) add(AgentAction.ProposeTodoUpdates(todoEdits))
    }
)

internal fun parseAgentToolResponse(rawReply: String): AgentToolResponse {
    val toolJsonCandidates = extractAgentToolJsonCandidates(rawReply)
    val todoDrafts = toolJsonCandidates
        .flatMap { rawJson -> parseAgentTodoToolCall(rawJson).asSequence() }
        .toList()
        .distinctBy { it.title.lowercase(Locale.getDefault()) }
        .take(6)
    val todoEdits = toolJsonCandidates
        .flatMap { rawJson -> parseAgentTodoEditToolCall(rawJson).asSequence() }
        .toList()
        .distinctBy(::todoEditDraftKey)
        .take(6)
    val hasToolBlocks = toolJsonCandidates.isNotEmpty()
    val visibleReply = stripAgentToolBlocks(rawReply)
        .trim()
        .ifBlank {
            when {
                todoDrafts.isNotEmpty() || todoEdits.isNotEmpty() -> "我整理了待办草案，请确认是否应用。"
                hasToolBlocks -> ""
                else -> rawReply.trim()
            }
        }
    return AgentToolResponse(
        visibleReply = visibleReply,
        todoDrafts = todoDrafts,
        todoEdits = todoEdits
    )
}

internal fun extractAgentToolJsonCandidates(rawReply: String): List<String> {
    val fencedBlockRegex = Regex(
        pattern = "```\\s*(?:protato_tool|json)?\\s*\\n(.*?)\\n```",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    val fenced = fencedBlockRegex.findAll(rawReply).map { it.groupValues[1].trim() }
    val bareJson = rawReply.lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("{") && it.endsWith("}") && it.containsAgentToolJsonKey() }
    return (fenced + bareJson)
        .flatMap(::expandToolJsonCandidate)
        .distinct()
        .toList()
}

internal fun expandToolJsonCandidate(rawJson: String): List<String> {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        when {
            root.has("tool") -> listOf(root.toString())
            root.has("tool_calls") -> {
                val calls = root.optJSONArray("tool_calls") ?: return@runCatching emptyList()
                buildList {
                    for (index in 0 until calls.length()) {
                        val call = calls.optJSONObject(index) ?: continue
                        if (call.has("tool")) {
                            add(call.toString())
                        } else {
                            call.optJSONObject("function")?.let { function ->
                                val name = function.optString("name")
                                val arguments = function.optString("arguments")
                                if (name.isNotBlank() && arguments.isNotBlank()) {
                                    add(wrapFunctionToolArguments(name, arguments))
                                }
                            }
                        }
                    }
                }
            }
            else -> emptyList()
        }
    }.getOrElse { emptyList() }
}

internal fun wrapFunctionToolArguments(name: String, arguments: String): String {
    return runCatching {
        val args = JSONObject(arguments)
        args.put("tool", name).toString()
    }.getOrElse { arguments }
}

internal fun stripAgentToolBlocks(rawReply: String): String {
    val fencedToolRegex = Regex(
        pattern = "```\\s*(?:protato_tool|json)?\\s*\\n(.*?)\\n```",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    return rawReply
        .replace(fencedToolRegex) { match ->
            if (match.value.contains("\"tool\"")) "" else match.value
        }
        .lineSequence()
        .filterNot { line ->
            val trimmed = line.trim()
            trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.containsAgentToolJsonKey()
        }
        .joinToString("\n")
}

private fun String.containsAgentToolJsonKey(): Boolean {
    return contains("\"tool\"") || contains("\"tool_calls\"")
}

internal fun parseAgentTodoToolCall(rawJson: String): List<TodoDraft> {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        if (!root.optString("tool").equals("propose_todos", ignoreCase = true)) {
            return@runCatching emptyList()
        }
        val todos = root.optJSONArray("todos") ?: return@runCatching emptyList()
        buildList {
            for (index in 0 until todos.length()) {
                val item = todos.optJSONObject(index) ?: continue
                val title = item.optString("title")
                    .trim()
                    .take(40)
                if (title.isBlank()) continue
                val dueDate = normalizedTodoDate(item.optString("dueDate"))
                    ?: normalizedTodoDate(item.optString("date"))
                val reminderAt = item.optionalReminderAt()
                val plannedPomodoros = item.optInt(
                    "plannedPomodoros",
                    item.optInt("pomodoros", 1)
                )
                val tags = item.optJSONArray("tags")?.toAgentStringList()
                    ?.flatMap(::parseTodoTags)
                    ?.distinctBy { it.lowercase(Locale.getDefault()) }
                    ?.take(5)
                    .orEmpty()
                add(
                    TodoDraft(
                        title = title,
                        dueDate = dueDate,
                        reminderAt = reminderAt,
                        tags = tags.ifEmpty { inferTodoTags(title) },
                        plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

internal fun parseAgentTodoEditToolCall(rawJson: String): List<TodoEditDraft> {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        if (!root.optString("tool").equals("propose_todo_updates", ignoreCase = true)) {
            return@runCatching emptyList()
        }
        val edits = root.optJSONArray("edits") ?: return@runCatching emptyList()
        buildList {
            for (index in 0 until edits.length()) {
                val item = edits.optJSONObject(index) ?: continue
                val id = item.optString("id").trim()
                if (id.isBlank()) continue
                val title = item.optionalNonBlankString("title")?.take(40)
                val dueDate = item.optionalNonBlankString("dueDate")?.let(::normalizedTodoDate)
                val reminderAt = item.optionalReminderAt()
                val tags = item.optJSONArray("tags")?.toAgentStringList()
                    ?.flatMap(::parseTodoTags)
                    ?.distinctBy { it.lowercase(Locale.getDefault()) }
                    ?.take(5)
                val plannedPomodoros = if (item.has("plannedPomodoros")) {
                    item.optInt("plannedPomodoros", 1).coerceIn(1, 24)
                } else {
                    null
                }
                val completed = if (item.has("completed")) item.optBoolean("completed") else null
                if (
                    title == null &&
                    dueDate == null &&
                    reminderAt == null &&
                    tags == null &&
                    plannedPomodoros == null &&
                    completed == null
                ) {
                    continue
                }
                add(
                    TodoEditDraft(
                        id = id,
                        title = title,
                        dueDate = dueDate,
                        reminderAt = reminderAt,
                        tags = tags,
                        plannedPomodoros = plannedPomodoros,
                        completed = completed
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

private fun JSONArray.toAgentStringList(): List<String> {
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index).trim()
            if (value.isNotBlank()) add(value)
        }
    }
}

internal fun JSONObject.optionalNonBlankString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).trim().takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

internal fun JSONObject.optionalReminderAt(): Long? {
    if (!has("reminderAt") || isNull("reminderAt")) return null
    val raw = optString("reminderAt").trim()
    if (raw.isBlank() || raw.equals("null", ignoreCase = true)) return null
    raw.toLongOrNull()?.let { return it.takeIf { value -> value > 0L } }
    return normalizedReminderAt(raw)
}
