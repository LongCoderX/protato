package com.protato.app.ui.screens.records
import com.protato.app.ui.screens.todo.TodoDatePickerDialog

import com.protato.app.*

import com.protato.app.ai.*
import com.protato.app.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.protato.app.ui.components.EmptyState
import com.protato.app.ui.components.Header
import com.protato.app.ui.screens.records.DailySummaryDialog
import com.protato.app.ui.screens.records.DailySummaryCard
import com.protato.app.ui.screens.records.DaySwitcher
import com.protato.app.ui.screens.records.RecordDayOption
import com.protato.app.ui.screens.records.TimelineRecordItem

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
    val summaryController = remember(scope) { DailySummaryController(scope) }
    val summaryRequestState by summaryController.state.collectAsState()
    var editingRecord by remember { mutableStateOf<PomodoroRecord?>(null) }
    var viewingSummary by remember { mutableStateOf<DailySummary?>(null) }
    val generatingSummary = summaryRequestState is AsyncRequestState.Loading
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
                Header(title = "番茄记录", subtitle = "每次专注结束后的完整内容")
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
                        summaryController.submit(
                            dayKey = effectiveSelectedDayKey,
                            records = selectedDayRecords,
                            templates = templates,
                            providers = providers,
                            agents = agents,
                            nickname = nickname
                        ) { summary ->
                            onSaveSummary(summary)
                            viewingSummary = summary
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
