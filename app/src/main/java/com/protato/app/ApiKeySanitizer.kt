package com.protato.app

internal fun sanitizeLegacyApiKey(rawValue: String): String {
    var sanitized = rawValue.trim()
    while (sanitized.endsWith("***")) {
        sanitized = sanitized.dropLast(3)
    }
    return sanitized
}

internal fun String.containsApiKeyMask(): Boolean = '*' in this
