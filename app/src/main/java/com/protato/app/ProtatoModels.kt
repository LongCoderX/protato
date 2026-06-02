package com.protato.app

enum class TimerMode {
    Focus,
    Break
}

enum class FieldType {
    SingleChoice,
    ShortAnswer
}

data class TodoItem(
    val id: String,
    val title: String,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val dueDate: String? = null,
    val plannedPomodoros: Int = 1
)

data class TemplateField(
    val id: String,
    val title: String,
    val type: FieldType,
    val options: List<String> = emptyList(),
    val required: Boolean = false
)

data class RecordTemplate(
    val id: String,
    val name: String,
    val fields: List<TemplateField>
)

data class FieldAnswer(
    val fieldId: String,
    val value: String
)

data class TimerSession(
    val mode: TimerMode,
    val todoId: String?,
    val todoTitle: String,
    val startedAt: Long,
    val endsAt: Long,
    val totalSeconds: Int,
    val templateId: String,
    val pausedRemainingSeconds: Int? = null
)

data class PendingPomodoroRecord(
    val todoId: String?,
    val todoTitle: String,
    val startedAt: Long,
    val endedAt: Long,
    val focusMinutes: Int,
    val templateId: String
)

data class PomodoroRecord(
    val id: String,
    val todoId: String?,
    val todoTitle: String,
    val templateId: String,
    val templateName: String,
    val startedAt: Long,
    val endedAt: Long,
    val focusMinutes: Int,
    val answers: List<FieldAnswer>
)

data class DailySummary(
    val dayKey: String,
    val generatedAt: Long,
    val title: String,
    val content: String,
    val pomodoroCount: Int,
    val focusMinutes: Int
)

data class LlmImportSettings(
    val provider: String = "",
    val modelName: String = "",
    val endpoint: String = "",
    val apiKey: String = ""
)

data class LlmProviderSettings(
    val id: String,
    val providerKey: String,
    val name: String,
    val modelName: String = "",
    val endpoint: String = "",
    val apiKey: String = ""
)

data class EncouragerAgentSettings(
    val enabled: Boolean = false,
    val name: String = "鼓励师",
    val prompt: String = DEFAULT_ENCOURAGER_PROMPT
)

data class AgentDataPermissions(
    val dailyRecords: Boolean = false,
    val todos: Boolean = false,
    val templates: Boolean = false
)

data class AgentSettings(
    val id: String,
    val enabled: Boolean = true,
    val name: String = "鼓励师",
    val providerId: String = "",
    val prompt: String = DEFAULT_ENCOURAGER_PROMPT,
    val permissions: AgentDataPermissions = AgentDataPermissions(dailyRecords = true)
)

data class AppState(
    val todos: List<TodoItem> = emptyList(),
    val templates: List<RecordTemplate> = listOf(defaultTemplate()),
    val records: List<PomodoroRecord> = emptyList(),
    val dailySummaries: List<DailySummary> = emptyList(),
    val focusMinutes: Int = 25,
    val restMinutes: Int = 5,
    val selectedTemplateId: String = defaultTemplate().id,
    val activeSession: TimerSession? = null,
    val pendingRecord: PendingPomodoroRecord? = null,
    val projectRevision: Int = 1,
    val nickname: String = DEFAULT_NICKNAME,
    val llmProviders: List<LlmProviderSettings> = defaultLlmProviders(),
    val agents: List<AgentSettings> = defaultAgents()
)

const val DEFAULT_NICKNAME = "专注者"
const val DEFAULT_ENCOURAGER_PROMPT = "请称呼我为「{nickname}」，用温和、具体、不油腻的方式鼓励我继续完成下一轮番茄。"
const val DEFAULT_TODO_AGENT_PROMPT = "请称呼我为「{nickname}」，把我随口说出的计划整理成短小、明确、可执行的待办。"
const val DEFAULT_TEMPLATE_AGENT_PROMPT = "请称呼我为「{nickname}」，根据我的复盘目标生成简洁、好填写的番茄记录模板。"
const val DEFAULT_RECORD_AGENT_PROMPT = "请称呼我为「{nickname}」，基于我的番茄记录做具体、温和、可执行的复盘建议。"
const val AGENT_STYLE_PROMPT = "允许在自然语言回复中使用少量贴切 emoji 表情，让语气更有人味；不要堆砌，结构化输出场景必须优先遵守指定格式。"

fun defaultLlmProviders(): List<LlmProviderSettings> {
    return listOf(
        LlmProviderSettings("provider-deepseek", "deepseek", "DeepSeek", endpoint = "https://api.deepseek.com"),
        LlmProviderSettings("provider-minimax", "minimax", "MiniMax", endpoint = "https://api.minimax.io"),
        LlmProviderSettings("provider-qwen", "qwen", "Qwen", endpoint = "https://dashscope.aliyuncs.com/compatible-mode"),
        LlmProviderSettings("provider-openai", "openai", "OpenAI", endpoint = "https://api.openai.com"),
        LlmProviderSettings("provider-claude-code", "claude_code", "Claude Code"),
        LlmProviderSettings("provider-longcat", "longcat", "LongCat")
    )
}

fun defaultAgents(): List<AgentSettings> {
    return listOf(
        AgentSettings(
            id = "agent-encourager",
            name = "鼓励师",
            prompt = DEFAULT_ENCOURAGER_PROMPT,
            permissions = AgentDataPermissions(dailyRecords = true)
        ),
        AgentSettings(
            id = "agent-todo",
            name = "待办整理 AI",
            prompt = DEFAULT_TODO_AGENT_PROMPT,
            permissions = AgentDataPermissions(todos = true)
        ),
        AgentSettings(
            id = "agent-template",
            name = "模板生成 AI",
            prompt = DEFAULT_TEMPLATE_AGENT_PROMPT,
            permissions = AgentDataPermissions(templates = true)
        ),
        AgentSettings(
            id = "agent-record",
            name = "记录复盘 AI",
            prompt = DEFAULT_RECORD_AGENT_PROMPT,
            permissions = AgentDataPermissions(dailyRecords = true)
        )
    )
}

fun defaultTemplate(): RecordTemplate {
    return RecordTemplate(
        id = "template-default-review",
        name = "默认复盘",
        fields = listOf(
            TemplateField(
                id = "field-energy",
                title = "这轮专注状态",
                type = FieldType.SingleChoice,
                options = listOf("很专注", "有分心", "被打断"),
                required = true
            ),
            TemplateField(
                id = "field-result",
                title = "完成了什么",
                type = FieldType.ShortAnswer,
                required = true
            ),
            TemplateField(
                id = "field-next",
                title = "下一步",
                type = FieldType.ShortAnswer
            )
        )
    )
}
