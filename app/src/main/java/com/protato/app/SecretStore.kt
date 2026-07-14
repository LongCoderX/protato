package com.protato.app

import org.json.JSONObject
import java.io.File

internal const val SECRET_FILE_NAME = "protato-secrets.json"
internal const val ENCRYPTED_SECRET_FILE_NAME = "protato-secrets.enc"

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
        return runCatching { readPlaintextSecrets(file) }.getOrDefault(ProtatoSecrets())
    }

    override fun save(secrets: ProtatoSecrets) {
        val root = JSONObject()
            .put("llmProviderApiKeys", secrets.llmProviderApiKeys.toJsonObject())
            .put("webDavAuthToken", secrets.webDavAuthToken)
        file.writeText(root.toString(2))
    }
}

internal fun readPlaintextSecrets(file: File): ProtatoSecrets {
    val root = JSONObject(file.readText())
    return ProtatoSecrets(
        llmProviderApiKeys = root.optJSONObject("llmProviderApiKeys").toStringMap(),
        webDavAuthToken = root.optString("webDavAuthToken")
    )
}

internal fun ProtatoSecrets.toJson(): String {
    return JSONObject()
        .put("llmProviderApiKeys", llmProviderApiKeys.toJsonObject())
        .put("webDavAuthToken", webDavAuthToken)
        .toString(2)
}

internal fun decodeSecrets(rawJson: String): ProtatoSecrets {
    val root = JSONObject(rawJson)
    return ProtatoSecrets(
        llmProviderApiKeys = root.optJSONObject("llmProviderApiKeys").toStringMap(),
        webDavAuthToken = root.optString("webDavAuthToken")
    )
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
