package com.protato.app


internal fun FieldType.label(): String = when (this) {
    FieldType.SingleChoice -> "单选"
    FieldType.ShortAnswer -> "简答"
    FieldType.Attachment -> "附件"
}

internal fun AgentDataPermissions.label(): String {
    val names = buildList {
        if (dailyRecords) add("每日记录")
        if (todos) add("待办")
        if (templates) add("模板")
    }
    return names.ifEmpty { listOf("无数据权限") }.joinToString("、")
}

internal fun LlmProviderSettings.agentProviderLabel(): String {
    return "$name · ${modelName.ifBlank { "未选择模型" }}"
}
