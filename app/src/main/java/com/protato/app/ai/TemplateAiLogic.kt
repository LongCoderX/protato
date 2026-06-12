package com.protato.app.ai

import com.protato.app.*
import com.protato.app.domain.timer.*
import com.protato.app.util.*
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.protato.app.ai.LlmChatMessage
import com.protato.app.ai.fetchLlmModels
import com.protato.app.ai.requestLlmChat
import com.protato.app.ui.MainTab
import com.protato.app.ui.ProtatoBottomBar
import com.protato.app.ui.ProtatoTheme
import com.protato.app.ui.SecondaryDestination
import com.protato.app.ui.components.CelebrationCanvas
import com.protato.app.ui.components.ChatBubble
import com.protato.app.ui.components.EmptyState
import com.protato.app.ui.components.Header
import com.protato.app.ui.components.SecondaryHeader
import com.protato.app.ui.components.SettingActionRow
import com.protato.app.ui.components.TodoDateChip
import com.protato.app.ui.components.TodoPomodoroChip
import com.protato.app.ui.components.TodoReminderChip
import com.protato.app.ui.components.TodoTagChip
import com.protato.app.ui.screens.more.MoreScreen
import com.protato.app.ui.screens.records.DailySummaryDialog
import com.protato.app.ui.screens.records.DailySummaryCard
import com.protato.app.ui.screens.records.DaySwitcher
import com.protato.app.ui.screens.records.RecordAnswerFields
import com.protato.app.ui.screens.records.RecordDayOption
import com.protato.app.ui.screens.records.RecordTodoSelector
import com.protato.app.ui.screens.focus.DurationEditDialog
import com.protato.app.ui.screens.focus.TimerRing
import com.protato.app.ui.screens.records.RecordTodoChip
import com.protato.app.ui.screens.records.TimelineRecordItem
import com.protato.app.ui.screens.settings.FontScaleSettingsCard
import com.protato.app.ui.screens.settings.LlmProviderListCard
import com.protato.app.ui.screens.settings.NicknameSettingsCard
import com.protato.app.ui.screens.settings.ProjectVersionCard
import com.protato.app.ui.screens.settings.RecordPolishProviderCard
import com.protato.app.ui.screens.settings.WebDevSyncSettingsCard
import com.protato.app.ui.screens.templates.TemplateCard
import com.protato.app.ui.screens.templates.TemplateAiDialog
import com.protato.app.ui.screens.templates.TemplateEditorDialog
import com.protato.app.ui.screens.todo.TodoChoiceLabel
import com.protato.app.ui.screens.todo.TodoDraftMetadataRow
import com.protato.app.ui.screens.todo.TodoExpandedDetails
import com.protato.app.ui.screens.todo.TodoMetadataStrip
import com.protato.app.ui.screens.todo.TodoSectionHeader
import com.protato.app.ui.screens.todo.TodoSelectionBar
import com.protato.app.ui.screens.todo.TodoSwipeBackground
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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
