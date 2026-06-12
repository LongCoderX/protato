package com.protato.app

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

internal data class TodoDraft(
    val title: String,
    val dueDate: String? = null,
    val reminderAt: Long? = null,
    val tags: List<String> = emptyList(),
    val plannedPomodoros: Int = 1
) {
    fun toTodoItem(): TodoItem {
        return TodoItem(
            id = newId(),
            title = title.trim(),
            dueDate = dueDate,
            reminderAt = reminderAt,
            tags = tags,
            plannedPomodoros = plannedPomodoros.coerceIn(1, 24)
        )
    }
}

internal fun todoDraftKey(draft: TodoDraft): String {
    return todoIdentityKey(draft.title, draft.dueDate)
}

internal fun todoItemKey(todo: TodoItem): String {
    return todoIdentityKey(todo.title, todo.dueDate)
}

internal fun todoIdentityKey(title: String, dueDate: String?): String {
    return "${title.trim().lowercase(Locale.getDefault())}|${dueDate.orEmpty()}"
}

internal fun List<TodoDraft>.filterNewTodoDrafts(
    existingTodos: List<TodoItem>,
    handledKeys: Set<String>
): List<TodoDraft> {
    val existingKeys = existingTodos.map(::todoItemKey).toSet()
    return filter { draft ->
        val key = todoDraftKey(draft)
        key !in handledKeys && key !in existingKeys
    }
}

internal data class TodoEditDraft(
    val id: String,
    val title: String? = null,
    val dueDate: String? = null,
    val reminderAt: Long? = null,
    val tags: List<String>? = null,
    val plannedPomodoros: Int? = null,
    val completed: Boolean? = null
) {
    fun applyTo(todo: TodoItem): TodoItem {
        return todo.copy(
            title = title ?: todo.title,
            dueDate = dueDate ?: todo.dueDate,
            reminderAt = reminderAt ?: todo.reminderAt,
            tags = tags ?: todo.tags,
            plannedPomodoros = plannedPomodoros ?: todo.plannedPomodoros,
            completed = completed ?: todo.completed
        )
    }

    fun previewTitle(original: TodoItem?): String {
        return title ?: original?.title ?: "待办更新"
    }

    fun toPreviewDraft(original: TodoItem?): TodoDraft {
        return TodoDraft(
            title = previewTitle(original),
            dueDate = dueDate ?: original?.dueDate,
            reminderAt = reminderAt ?: original?.reminderAt,
            tags = tags ?: original?.tags.orEmpty(),
            plannedPomodoros = plannedPomodoros ?: original?.plannedPomodoros ?: 1
        )
    }
}

internal fun todoEditDraftKey(edit: TodoEditDraft): String {
    return listOf(
        edit.id,
        edit.title.orEmpty(),
        edit.dueDate.orEmpty(),
        edit.reminderAt?.toString().orEmpty(),
        edit.tags?.joinToString(",").orEmpty(),
        edit.plannedPomodoros?.toString().orEmpty(),
        edit.completed?.toString().orEmpty()
    ).joinToString("|").lowercase(Locale.getDefault())
}

internal fun List<TodoEditDraft>.filterNewTodoEdits(
    existingTodos: List<TodoItem>,
    handledKeys: Set<String>
): List<TodoEditDraft> {
    val todosById = existingTodos.associateBy { it.id }
    return filter { edit ->
        val original = todosById[edit.id] ?: return@filter false
        if (original.completed) return@filter false
        val updated = edit.applyTo(original)
        todoEditDraftKey(edit) !in handledKeys && updated != original
    }
}
