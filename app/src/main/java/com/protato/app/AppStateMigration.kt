package com.protato.app

import org.json.JSONObject

internal data class AppStateMigrationReport(
    val messages: List<String> = emptyList()
) {
    val hasMessages: Boolean
        get() = messages.isNotEmpty()
}

internal object AppStateMigration {
    fun inspect(rawJson: String): AppStateMigrationReport {
        val root = JSONObject(rawJson)
        val messages = buildList {
            if (root.has("llmImport") && !root.has("llmProviders")) {
                add("Recovered legacy llmImport into llmProviders.")
            }
            if (root.has("encouragerAgent") && !root.has("agents")) {
                add("Recovered legacy encouragerAgent into unified agent settings.")
            }
            if (root.has("shortBreakMinutes") && !root.has("restMinutes")) {
                add("Recovered legacy shortBreakMinutes into restMinutes.")
            }
        }
        return AppStateMigrationReport(messages)
    }
}
