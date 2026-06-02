package com.protato.app

enum class TimerMode {
    Focus,
    ShortBreak,
    LongBreak
}

enum class FieldType {
    SingleChoice,
    ShortAnswer
}

data class TodoItem(
    val id: String,
    val title: String,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
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
    val templateId: String
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

data class AppState(
    val todos: List<TodoItem> = emptyList(),
    val templates: List<RecordTemplate> = listOf(defaultTemplate()),
    val records: List<PomodoroRecord> = emptyList(),
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val selectedTemplateId: String = defaultTemplate().id,
    val activeSession: TimerSession? = null,
    val pendingRecord: PendingPomodoroRecord? = null
)

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
