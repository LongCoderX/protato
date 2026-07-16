package com.protato.app

import org.json.JSONObject

internal const val CURRENT_APP_STATE_SCHEMA_VERSION = 5

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
            val schemaVersion = root.optInt("schemaVersion", 0)
            require(schemaVersion <= CURRENT_APP_STATE_SCHEMA_VERSION) {
                "Unsupported app state schema version: $schemaVersion"
            }
            if (schemaVersion < CURRENT_APP_STATE_SCHEMA_VERSION) {
                add("Migrated app state schema from v$schemaVersion to v$CURRENT_APP_STATE_SCHEMA_VERSION.")
            }
            if (root.has("llmImport") && !root.has("llmProviders")) {
                add("Recovered legacy llmImport into llmProviders.")
            }
            if (root.has("encouragerAgent") && !root.has("agents")) {
                add("Recovered legacy encouragerAgent into unified agent settings.")
            }
            if (root.has("shortBreakMinutes") && !root.has("restMinutes")) {
                add("Recovered legacy shortBreakMinutes into restMinutes.")
            }
            if (root.has("webDevSync") && !root.has("webDavBackup")) {
                add("Migrated legacy webDevSync settings to webDavBackup.")
            }
        }
        return AppStateMigrationReport(messages)
    }
}
