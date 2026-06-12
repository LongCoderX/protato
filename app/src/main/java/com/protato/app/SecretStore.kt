package com.protato.app

import org.json.JSONObject
import java.io.File

internal const val SECRET_FILE_NAME = "protato-secrets.json"

internal interface SecretStore {
    fun load(): ProtatoSecrets
    fun save(secrets: ProtatoSecrets)
}

internal data class ProtatoSecrets(
    val llmProviderApiKeys: Map<String, String> = emptyMap(),
    val webDavAuthToken: String = ""
)

internal class FileSecretStore(
    private val file: File
) : SecretStore {
    companion object {
        fun inDirectory(filesDir: File): FileSecretStore {
            return FileSecretStore(File(filesDir, SECRET_FILE_NAME))
        }
    }

    override fun load(): ProtatoSecrets {
        if (!file.exists()) return ProtatoSecrets()
        return runCatching {
            val root = JSONObject(file.readText())
            ProtatoSecrets(
                llmProviderApiKeys = root.optJSONObject("llmProviderApiKeys").toStringMap(),
                webDavAuthToken = root.optString("webDavAuthToken")
            )
        }.getOrDefault(ProtatoSecrets())
    }

    override fun save(secrets: ProtatoSecrets) {
        val root = JSONObject()
            .put("llmProviderApiKeys", secrets.llmProviderApiKeys.toJsonObject())
            .put("webDavAuthToken", secrets.webDavAuthToken)
        file.writeText(root.toString(2))
    }
}

private fun JSONObject?.toStringMap(): Map<String, String> {
    if (this == null) return emptyMap()
    return keys().asSequence()
        .associateWith { key -> optString(key) }
        .filterValues { it.isNotBlank() }
}

private fun Map<String, String>.toJsonObject(): JSONObject {
    val root = JSONObject()
    forEach { (key, value) ->
        if (key.isNotBlank() && value.isNotBlank()) {
            root.put(key, value)
        }
    }
    return root
}
