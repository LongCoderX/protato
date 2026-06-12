package com.protato.app.util

import androidx.compose.ui.graphics.Color
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun formatTime(value: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(value))
}

internal fun formatClockTime(value: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value))
}

internal fun dayKey(value: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(value))
}

internal fun utcDayKey(value: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(value))
}

internal fun dateKeyToUtcMillis(key: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(key)?.time
    } catch (_: ParseException) {
        null
    }
}

internal fun dateKeyToLocalMillis(key: String, hour: Int, minute: Int): Long {
    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        isLenient = false
    }
    val timeText = "%02d:%02d".format(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    return parser.parse("$key $timeText")?.time
        ?: System.currentTimeMillis()
}

internal fun normalizedTodoDate(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank() || !trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return null
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        isLenient = false
    }
    return try {
        dayKey(parser.parse(trimmed)?.time ?: return null)
    } catch (_: ParseException) {
        null
    }
}

internal fun normalizedReminderAt(value: String): Long? {
    val trimmed = value.trim()
    val patterns = listOf("yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss")
    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).apply {
                isLenient = false
            }.parse(trimmed)?.time
        }.getOrNull()
    }
}

internal fun parseTodoTags(value: String): List<String> {
    return value
        .split(Regex("[,，、\\s]+"))
        .map { it.trim().removePrefix("#").take(24) }
        .filter { tag ->
            tag.isNotBlank() && tag.lowercase(Locale.getDefault()) !in setOf("空", "无", "none", "null")
        }
        .distinctBy { it.lowercase(Locale.getDefault()) }
}

internal fun todoTagColor(tag: String): Color {
    val colors = listOf(
        Color(0xFFE94F37),
        Color(0xFF2F9E44),
        Color(0xFF456990),
        Color(0xFF8E6C8A),
        Color(0xFFB7791F),
        Color(0xFF008891),
        Color(0xFF6A7FDB),
        Color(0xFFD1495B)
    )
    val index = kotlin.math.abs(tag.lowercase(Locale.getDefault()).hashCode()) % colors.size
    return colors[index]
}

internal fun formatDayLabel(key: String): String {
    val today = dayKey(System.currentTimeMillis())
    if (key == today) return "今天"
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatter = SimpleDateFormat("MM月dd日", Locale.getDefault())
    return try {
        formatter.format(parser.parse(key) ?: return key)
    } catch (_: ParseException) {
        key
    }
}
