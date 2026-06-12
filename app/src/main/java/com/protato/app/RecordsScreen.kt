package com.protato.app

import com.protato.app.ai.*
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

@Composable
internal fun RecordsScreen(
    records: List<PomodoroRecord>,
    summaries: List<DailySummary>,
    templates: List<RecordTemplate>,
    todos: List<TodoItem>,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onUpdateRecord: (PomodoroRecord) -> Unit,
    onSaveSummary: (DailySummary) -> Unit
) {
    val scope = rememberCoroutineScope()
    var editingRecord by remember { mutableStateOf<PomodoroRecord?>(null) }
    var viewingSummary by remember { mutableStateOf<DailySummary?>(null) }
    var generatingSummary by remember { mutableStateOf(false) }
    var choosingRecordDate by rememberSaveable { mutableStateOf(false) }
    val todayKey = dayKey(System.currentTimeMillis())
    var selectedDayKey by rememberSaveable { mutableStateOf(todayKey) }
    val availableDayKeys = remember(records, summaries, todayKey, selectedDayKey) {
        (records.map { dayKey(it.endedAt) } + summaries.map { it.dayKey } + todayKey + selectedDayKey)
            .distinct()
            .sortedDescending()
    }
    val effectiveSelectedDayKey = selectedDayKey
        .takeIf { it in availableDayKeys }
        ?: availableDayKeys.firstOrNull()
        ?: todayKey
    LaunchedEffect(effectiveSelectedDayKey) {
        if (selectedDayKey != effectiveSelectedDayKey) {
            selectedDayKey = effectiveSelectedDayKey
        }
    }
    val selectedDayRecords = records
        .filter { dayKey(it.endedAt) == effectiveSelectedDayKey }
        .sortedBy { it.endedAt }
    val selectedDaySummary = summaries.firstOrNull { it.dayKey == effectiveSelectedDayKey }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Header(title = "番茄记录", subtitle = "每次专注结束后的完整内容会沉淀在这里")
            }
            item {
                DaySwitcher(
                    dayOptions = availableDayKeys.map { key ->
                        RecordDayOption(
                            key = key,
                            label = formatDayLabel(key),
                            count = records.count { dayKey(it.endedAt) == key }
                        )
                    },
                    selectedDayKey = effectiveSelectedDayKey,
                    onSelected = { selectedDayKey = it },
                    onOpenDatePicker = { choosingRecordDate = true }
                )
            }
            item {
                DailySummaryCard(
                    title = "${formatDayLabel(effectiveSelectedDayKey)} 总结",
                    statsText = "${selectedDayRecords.size} 次专注 · ${selectedDayRecords.sumOf { it.focusMinutes }} 分钟 · ${selectedDayRecords.map { it.todoTitle }.distinct().size} 个对象",
                    hasSummary = selectedDaySummary != null,
                    hasRecords = selectedDayRecords.isNotEmpty(),
                    loading = generatingSummary,
                    onGenerate = {
                        scope.launch {
                            generatingSummary = true
                            val summary = runCatching {
                                generateDailySummary(
                                    dayKey = effectiveSelectedDayKey,
                                    records = selectedDayRecords,
                                    templates = templates,
                                    providers = providers,
                                    agents = agents,
                                    nickname = nickname
                                )
                            }.getOrElse {
                                localDailySummary(effectiveSelectedDayKey, selectedDayRecords, nickname)
                            }
                            onSaveSummary(summary)
                            viewingSummary = summary
                            generatingSummary = false
                        }
                    },
                    onOpen = { selectedDaySummary?.let { viewingSummary = it } }
                )
            }
            if (selectedDayRecords.isEmpty()) {
                item {
                    EmptyState("这一天还没有记录。切换日期，或完成一轮专注后再回来看看。")
                }
            } else {
                item {
                    Text(
                        "时间轴",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(selectedDayRecords, key = { it.id }) { record ->
                    TimelineRecordItem(
                        record = record,
                        template = templates.firstOrNull { it.id == record.templateId },
                        endedAtLabel = formatClockTime(record.endedAt),
                        isFirst = record.id == selectedDayRecords.first().id,
                        isLast = record.id == selectedDayRecords.last().id,
                        onEdit = { editingRecord = record }
                    )
                }
            }
        }

        editingRecord?.let { record ->
            val template = templates.firstOrNull { it.id == record.templateId }
                ?: RecordTemplate(record.templateId, record.templateName, emptyList())
            RecordEditDialog(
                record = record,
                template = template,
                todos = todos,
                onDismiss = { editingRecord = null },
                onSave = { updatedRecord ->
                    onUpdateRecord(updatedRecord)
                    editingRecord = null
                }
            )
        }

        viewingSummary?.let { summary ->
            DailySummaryDialog(
                summary = summary,
                generatedAtLabel = formatTime(summary.generatedAt),
                onDismiss = { viewingSummary = null }
            )
        }

        if (choosingRecordDate) {
            TodoDatePickerDialog(
                initialDate = effectiveSelectedDayKey,
                onDismiss = { choosingRecordDate = false },
                onClear = {
                    selectedDayKey = todayKey
                    choosingRecordDate = false
                },
                onSelected = { day ->
                    selectedDayKey = day
                    choosingRecordDate = false
                }
            )
        }
    }
}
