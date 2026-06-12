package com.protato.app.ai

import com.protato.app.AgentDataPermissions
import com.protato.app.AppState
import com.protato.app.FieldAnswer
import com.protato.app.PomodoroRecord
import com.protato.app.RecordTemplate
import com.protato.app.TemplateField
import com.protato.app.FieldType
import com.protato.app.TodoItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentContextBuilderTest {
    @Test
    fun hidesTodosRecordsAndTemplatesWhenPermissionsAreDisabled() {
        val context = sampleState().toUnifiedAgentContext(
            nickname = "测试用户",
            permissions = AgentDataPermissions(
                dailyRecords = false,
                todos = false,
                templates = false
            )
        )

        assertTrue(context.contains("今日待办：未授权"))
        assertTrue(context.contains("今日番茄记录：未授权"))
        assertTrue(context.contains("可用复盘模板：未授权"))
        assertFalse(context.contains("秘密待办标题"))
        assertFalse(context.contains("秘密记录标题"))
        assertFalse(context.contains("秘密模板"))
    }

    @Test
    fun includesAuthorizedTodosRecordsAndTemplates() {
        val context = sampleState().toUnifiedAgentContext(
            nickname = "测试用户",
            permissions = AgentDataPermissions(
                dailyRecords = true,
                todos = true,
                templates = true
            )
        )

        assertTrue(context.contains("秘密待办标题"))
        assertTrue(context.contains("秘密记录标题"))
        assertTrue(context.contains("秘密模板"))
    }

    private fun sampleState(): AppState {
        val now = System.currentTimeMillis()
        return AppState(
            todos = listOf(
                TodoItem(
                    id = "todo-secret",
                    title = "秘密待办标题",
                    dueDate = com.protato.app.util.dayKey(now)
                )
            ),
            records = listOf(
                PomodoroRecord(
                    id = "record-secret",
                    todoId = "todo-secret",
                    todoTitle = "秘密记录标题",
                    templateId = "template-secret",
                    templateName = "秘密模板",
                    startedAt = now - 25 * 60 * 1000L,
                    endedAt = now,
                    focusMinutes = 25,
                    answers = listOf(FieldAnswer("field-secret", "秘密回答"))
                )
            ),
            templates = listOf(
                RecordTemplate(
                    id = "template-secret",
                    name = "秘密模板",
                    fields = listOf(
                        TemplateField(
                            id = "field-secret",
                            title = "秘密字段",
                            type = FieldType.ShortAnswer
                        )
                    )
                )
            )
        )
    }
}
