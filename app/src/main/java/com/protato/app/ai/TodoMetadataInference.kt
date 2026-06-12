package com.protato.app.ai

import java.util.Locale

internal fun inferTodoTags(title: String): List<String> {
    val lowered = title.lowercase(Locale.getDefault())
    val tag = when {
        listOf("会议", "方案", "项目", "文档", "汇报", "邮件").any { it in lowered } -> "工作"
        listOf("学习", "阅读", "课程", "复习", "练习").any { it in lowered } -> "学习"
        listOf("运动", "跑步", "健身", "体检", "睡觉").any { it in lowered } -> "健康"
        listOf("买", "收拾", "打扫", "做饭", "缴费").any { it in lowered } -> "生活"
        listOf("复盘", "总结", "计划").any { it in lowered } -> "复盘"
        else -> "待办"
    }
    return listOf(tag)
}

internal fun parsePomodoroCount(text: String): Int? {
    val trimmed = text.trim()
    trimmed.toIntOrNull()?.let { return it.coerceIn(1, 24) }
    return Regex("(\\d{1,2})\\s*(?:个|轮|次)?\\s*(?:番茄|pomodoro)", RegexOption.IGNORE_CASE)
        .find(text)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(1, 24)
}

internal fun inferPlannedPomodoros(title: String): Int {
    val lowered = title.lowercase(Locale.getDefault())
    return when {
        listOf("方案", "报告", "论文", "开发", "重构", "复习", "整理资料").any { it in lowered } -> 4
        listOf("文档", "课程", "练习", "设计", "总结").any { it in lowered } -> 3
        listOf("会议", "阅读", "沟通", "邮件").any { it in lowered } -> 2
        else -> 1
    }
}
