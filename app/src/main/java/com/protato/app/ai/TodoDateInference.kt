package com.protato.app.ai

import com.protato.app.util.dayKey
import com.protato.app.util.normalizedTodoDate

internal fun inferTodoDate(text: String): String? {
    val todayMillis = System.currentTimeMillis()
    return when {
        "后天" in text -> dayKey(todayMillis + 2 * 24 * 60 * 60 * 1000L)
        "明天" in text -> dayKey(todayMillis + 24 * 60 * 60 * 1000L)
        "今天" in text || "今晚" in text -> dayKey(todayMillis)
        "周末" in text -> nextWeekendDayKey(todayMillis)
        else -> Regex("\\d{4}-\\d{2}-\\d{2}").find(text)?.value?.let(::normalizedTodoDate)
            ?: inferMonthDay(text)
            ?: inferWeekday(text)
    }
}

internal fun inferMonthDay(text: String): String? {
    val match = Regex("(\\d{1,2})\\s*(?:月|/)(\\d{1,2})\\s*(?:日|号)?").find(text)
        ?: return null
    val month = match.groupValues[1].toIntOrNull() ?: return null
    val day = match.groupValues[2].toIntOrNull() ?: return null
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.MONTH, month - 1)
        set(java.util.Calendar.DAY_OF_MONTH, day)
        set(java.util.Calendar.HOUR_OF_DAY, 12)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        if (timeInMillis < System.currentTimeMillis() - 24 * 60 * 60 * 1000L) {
            add(java.util.Calendar.YEAR, 1)
        }
    }
    return dayKey(calendar.timeInMillis)
}

internal fun inferWeekday(text: String): String? {
    val match = Regex("(下周|本周|这周)?(?:周|星期)([一二三四五六日天])").find(text)
        ?: return null
    val prefix = match.groupValues[1]
    val targetDay = when (match.groupValues[2]) {
        "一" -> java.util.Calendar.MONDAY
        "二" -> java.util.Calendar.TUESDAY
        "三" -> java.util.Calendar.WEDNESDAY
        "四" -> java.util.Calendar.THURSDAY
        "五" -> java.util.Calendar.FRIDAY
        "六" -> java.util.Calendar.SATURDAY
        else -> java.util.Calendar.SUNDAY
    }
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 12)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    var delta = (targetDay - currentDay + 7) % 7
    if (prefix == "下周") delta += 7
    if (prefix.isBlank() && delta == 0) delta = 7
    calendar.add(java.util.Calendar.DAY_OF_YEAR, delta)
    return dayKey(calendar.timeInMillis)
}

internal fun nextWeekendDayKey(fromMillis: Long): String {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = fromMillis
    }
    while (calendar.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SATURDAY) {
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    return dayKey(calendar.timeInMillis)
}
