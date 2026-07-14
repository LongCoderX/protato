package com.protato.app.ai

import com.protato.app.*
import com.protato.app.domain.timer.*
import com.protato.app.util.*
import androidx.compose.foundation.layout.size
import com.protato.app.ai.LlmChatMessage
import com.protato.app.ai.requestLlmChat
import java.util.Locale

internal suspend fun generateTemplateSuggestion(
    rawInput: String,
    agent: AgentSettings,
    provider: LlmProviderSettings,
    nickname: String
): RecordTemplate {
    val systemPrompt = buildString {
        append(agent.prompt.withNickname(nickname))
        appendAgentStylePrompt()
        append("\n你是 Protato 的番茄复盘模板生成助手。根据用户描述生成一个简洁、可执行的记录模板。")
        append(" 第一行格式严格为：模板名称 | 名称。")
        append(" 后续每行格式严格为：字段 | 标题 | short 或 single | required 或 optional | 选项1,选项2。")
        append(" short 表示简答，single 表示单选；single 必须给 2-5 个选项。不要输出解释。")
    }
    val response = requestLlmChat(
        provider = provider,
        messages = listOf(
            LlmChatMessage("system", systemPrompt),
            LlmChatMessage("user", rawInput)
        )
    )
    return parseTemplateSuggestion(response) ?: localTemplateSuggestion(rawInput)
}

internal fun parseTemplateSuggestion(text: String): RecordTemplate? {
    val lines = text.lines()
        .map { it.trim().trim('-', '*', '•', ' ') }
        .filter { it.isNotBlank() }
    val name = lines.firstNotNullOfOrNull { line ->
        val parts = line.split("|").map { it.trim() }
        when {
            parts.size >= 2 && parts[0].contains("模板") -> parts[1]
            line.startsWith("模板名称") -> line.substringAfter("：", line.substringAfter(":")).trim()
            else -> null
        }
    }?.take(24)?.ifBlank { null } ?: return null
    val fields = lines.mapNotNull { line ->
        val parts = line.split("|").map { it.trim() }
        if (parts.size < 4 || !parts[0].contains("字段")) return@mapNotNull null
        val type = when (parts.getOrNull(2)?.lowercase(Locale.getDefault())) {
            "single", "单选" -> FieldType.SingleChoice
            else -> FieldType.ShortAnswer
        }
        val options = if (type == FieldType.SingleChoice) {
            parseTodoTags(parts.getOrNull(4).orEmpty()).take(5)
        } else {
            emptyList()
        }
        if (type == FieldType.SingleChoice && options.size < 2) return@mapNotNull null
        TemplateField(
            id = newId(),
            title = parts[1].take(40),
            type = type,
            options = options,
            required = parts.getOrNull(3)?.contains("required", ignoreCase = true) == true ||
                parts.getOrNull(3)?.contains("必填") == true
        )
    }.filter { it.title.isNotBlank() }.take(8)
    return if (fields.isEmpty()) null else RecordTemplate(
        id = newId(),
        name = name,
        fields = fields
    )
}

internal fun localTemplateSuggestion(rawInput: String): RecordTemplate {
    val topic = rawInput.lineSequence().firstOrNull { it.isNotBlank() }
        ?.cleanTodoTitle()
        ?.take(12)
        ?.ifBlank { null }
        ?: "自定义复盘"
    return RecordTemplate(
        id = newId(),
        name = "${topic}模板",
        fields = listOf(
            TemplateField(
                id = newId(),
                title = "这轮完成了什么",
                type = FieldType.ShortAnswer,
                required = true
            ),
            TemplateField(
                id = newId(),
                title = "专注状态",
                type = FieldType.SingleChoice,
                options = listOf("顺畅", "一般", "分心", "被打断"),
                required = true
            ),
            TemplateField(
                id = newId(),
                title = "下一步行动",
                type = FieldType.ShortAnswer
            )
        )
    )
}
