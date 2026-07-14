package com.protato.app.ui.screens.focus

import com.protato.app.*

import com.protato.app.domain.timer.*
import com.protato.app.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.domain.todo.sortedForAction
import com.protato.app.ui.screens.focus.DurationEditDialog
import com.protato.app.ui.screens.focus.TimerRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimerCard(
    modifier: Modifier = Modifier,
    mode: TimerMode,
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    hasPausedSession: Boolean,
    todos: List<TodoItem>,
    selectedTodoId: String?,
    selectedTodoTitle: String?,
    onModeChange: (TimerMode) -> Unit,
    onDurationChange: (Int) -> Unit,
    onSelectedTodo: (String?) -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    var editingDuration by rememberSaveable { mutableStateOf(false) }
    var choosingTodo by rememberSaveable { mutableStateOf(false) }
    val durationMinutes = totalSeconds / 60
    val showFocusControls = mode == TimerMode.Focus
    val statusLabel = when {
        isRunning -> "正在计时"
        hasPausedSession -> "已暂停"
        else -> "准备开始"
    }

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            CityFocusBackdrop(Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.70f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            mode.label(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    AssistChip(
                        onClick = { editingDuration = true },
                        label = { Text("${durationMinutes} 分钟") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Timer, contentDescription = null)
                        },
                        enabled = !isRunning
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val timerModes = listOf(TimerMode.Focus, TimerMode.Break)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        timerModes.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = mode == item,
                                onClick = { onModeChange(item) },
                                shape = SegmentedButtonDefaults.itemShape(index, timerModes.size),
                                label = { Text(item.label()) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val ringSize = minOf(minOf(maxWidth, maxHeight), 360.dp)
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
                            TimerRing(progress = remainingSeconds / totalSeconds.toFloat().coerceAtLeast(1f))
                            Column(
                                modifier = Modifier.padding(horizontal = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (showFocusControls) {
                                    Text(
                                        text = "本轮任务",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "休息",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = remainingSeconds.asClock(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(enabled = !isRunning) {
                                        editingDuration = true
                                    }
                                )
                                if (showFocusControls) {
                                    Text(
                                        text = selectedTodoTitle ?: "选择一个待办开始",
                                        modifier = Modifier
                                            .widthIn(max = ringSize * 0.78f)
                                            .clickable {
                                                choosingTodo = true
                                            },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "起身、喝水、离开屏幕一会儿",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStartPause,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (isRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                isRunning -> "暂停"
                                hasPausedSession -> "继续"
                                else -> "开始"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("重置")
                    }
                }
            }
        }
    }

    if (editingDuration) {
        DurationEditDialog(
            modeKey = mode,
            modeLabel = mode.label(),
            durationRange = mode.durationRange(),
            initialMinutes = durationMinutes,
            onDismiss = { editingDuration = false },
            onSave = { minutes ->
                onDurationChange(minutes)
                editingDuration = false
            }
        )
    }

    if (choosingTodo && showFocusControls) {
        val todayKey = dayKey(System.currentTimeMillis())
        TodoSelectDialog(
            todos = todos
                .filter { !it.completed }
                .sortedForAction(todayKey),
            selectedTodoId = selectedTodoId,
            onDismiss = { choosingTodo = false },
            onSelected = { todoId ->
                onSelectedTodo(todoId)
                choosingTodo = false
            }
        )
    }

}
