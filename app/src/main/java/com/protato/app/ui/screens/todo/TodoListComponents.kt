package com.protato.app.ui.screens.todo

import com.protato.app.domain.todo.*

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.TodoItem
import com.protato.app.ui.components.TodoDateChip
import com.protato.app.ui.components.TodoPomodoroChip
import com.protato.app.ui.components.TodoReminderChip
import com.protato.app.ui.components.TodoTagChip
import com.protato.app.util.formatDayLabel
import com.protato.app.util.formatTime
import com.protato.app.util.todoTagColor

@Composable
fun TodoSectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.width(44.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
        Text(
            "$title · $count",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Icon(
            if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
    }
}

@Composable
fun TodoSwipeBackground(dismissValue: SwipeToDismissBoxValue) {
    val deleting = dismissValue == SwipeToDismissBoxValue.EndToStart
    val editing = dismissValue == SwipeToDismissBoxValue.StartToEnd
    val editContentColor = if (editing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = when {
        deleting -> MaterialTheme.colorScheme.error
        editing -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 18.dp),
        horizontalArrangement = if (deleting) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!deleting) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "编辑待办",
                tint = editContentColor
            )
            Spacer(Modifier.width(8.dp))
            Text("编辑", color = editContentColor, fontWeight = FontWeight.SemiBold)
        } else {
            Text("删除", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "删除待办",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TodoSelectionBar(
    selectedCount: Int,
    canMoveToToday: Boolean,
    onCancel: () -> Unit,
    onMoveToToday: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancel) {
            Icon(Icons.Outlined.Close, contentDescription = "退出多选")
        }
        Text(
            text = "已选择 $selectedCount 项",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold
        )
        IconButton(
            enabled = canMoveToToday,
            onClick = onMoveToToday
        ) {
            Icon(Icons.Outlined.DateRange, contentDescription = "移到今天")
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "删除所选待办",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoQuestCard(
    todo: TodoItem,
    selected: Boolean,
    selecting: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onToggleSelected: () -> Unit,
    onLongPress: () -> Unit
) {
    val baseColor = when (Math.abs(todo.id.hashCode()) % 4) {
        0 -> MaterialTheme.colorScheme.surfaceVariant
        1 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
        2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f)
    }
    val xp = (todo.plannedPomodoros.coerceAtLeast(1) * 50)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .combinedClickable(
                onClick = { if (selecting) onToggleSelected() else onOpen() },
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else baseColor,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { if (selecting) onToggleSelected() else onToggle() },
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                ) {
                    Icon(
                        if (todo.completed) Icons.Outlined.Check else Icons.Outlined.Timer,
                        contentDescription = if (todo.completed) "已完成" else "专注任务",
                        tint = if (todo.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(7.dp).size(16.dp)
                    )
                }
                Text(
                    "+$xp XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                todo.title.ifBlank { "未命名任务" },
                style = MaterialTheme.typography.titleSmall,
                color = if (todo.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                when {
                    todo.dueDate != null -> "${todo.plannedPomodoros} 个番茄 · ${formatDayLabel(todo.dueDate)}"
                    else -> "${todo.plannedPomodoros} 个番茄"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TodoDraftMetadataRow(
    plannedPomodoros: Int,
    dueDate: String?,
    reminderAt: Long?,
    tags: List<String>,
    dayLabel: (String) -> String,
    timeLabel: (Long) -> String,
    tagColor: (String) -> Color
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item { TodoPomodoroChip(plannedPomodoros) }
        dueDate?.let { dueDate ->
            item { TodoDateChip(dayLabel(dueDate)) }
        }
        reminderAt?.let { reminderAt ->
            item { TodoReminderChip(timeLabel(reminderAt)) }
        }
        items(tags, key = { it }) { tag ->
            TodoTagChip(tag, tagColor(tag))
        }
    }
}

@Composable
fun TodoMetadataStrip(
    todo: TodoItem,
    dayLabel: (String) -> String,
    timeLabel: (Long) -> String,
    tagColor: (String) -> Color,
    includeTags: Boolean = true
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item { TodoPomodoroChip(todo.plannedPomodoros) }
        todo.dueDate?.let { dueDate ->
            item { TodoDateChip(dayLabel(dueDate)) }
        }
        todo.reminderAt?.let { reminderAt ->
            item { TodoReminderChip(timeLabel(reminderAt)) }
        }
        if (includeTags) {
            items(todo.tags, key = { it }) { tag ->
                TodoTagChip(tag, tagColor(tag))
            }
        }
    }
}

@Composable
fun TodoExpandedDetails(
    todo: TodoItem,
    tagColor: (String) -> Color
) {
    if (todo.tags.isEmpty() && todo.summary.isBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (todo.summary.isNotBlank()) {
            Text(
                todo.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (todo.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(todo.tags, key = { it }) { tag ->
                    TodoTagChip(tag, tagColor(tag))
                }
            }
        }
    }
}

@Composable
private fun TodoSubtaskTree(
    todo: TodoItem,
    expanded: Boolean,
    onToggleSubtask: (String) -> Unit
) {
    if (todo.subtasks.isEmpty()) return

    val visibleSubtasks = if (expanded) {
        todo.subtasks
    } else {
        todo.subtasks.take(3)
    }
    val hiddenCount = todo.subtasks.size - visibleSubtasks.size

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        visibleSubtasks.forEachIndexed { index, subtask ->
            TodoSubtaskTreeRow(
                title = subtask.title,
                completed = subtask.completed,
                isLastVisible = index == visibleSubtasks.lastIndex && hiddenCount == 0,
                onToggle = { onToggleSubtask(subtask.id) }
            )
        }
        if (hiddenCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TodoTreeConnector(isLast = true)
                Text(
                    text = "还有 $hiddenCount 个子任务",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TodoSubtaskTreeRow(
    title: String,
    completed: Boolean,
    isLastVisible: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TodoTreeConnector(isLast = isLastVisible)
        Checkbox(
            checked = completed,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(34.dp)
        )
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = if (completed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TodoTreeConnector(isLast: Boolean) {
    val color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    Canvas(
        modifier = Modifier
            .width(28.dp)
            .height(34.dp)
    ) {
        val x = 9.dp.toPx()
        val branchY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, if (isLast) branchY else size.height),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(x, branchY),
            end = Offset(size.width, branchY),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodoRow(
    todo: TodoItem,
    selected: Boolean,
    selecting: Boolean,
    onToggle: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelected: () -> Unit,
    onLongPress: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    var expanded by rememberSaveable(todo.id) { mutableStateOf(false) }

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.StartToEnd -> {
                onEdit()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !selecting,
        enableDismissFromEndToStart = !selecting,
        backgroundContent = {
            TodoSwipeBackground(dismissValue = dismissState.targetValue)
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (selecting) {
                            onToggleSelected()
                        } else {
                            expanded = !expanded
                        }
                    },
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(8.dp),
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TodoCheckControl(
                    checked = if (selecting) selected else todo.completed,
                    selectedMode = selecting,
                    onClick = {
                        if (selecting) onToggleSelected() else onToggle()
                    }
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (todo.completed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (todo.completed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (todo.summary.isNotBlank() && !expanded) {
                        Text(
                            text = todo.summary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TodoMetadataStrip(
                        todo = todo,
                        dayLabel = ::formatDayLabel,
                        timeLabel = ::formatTime,
                        tagColor = ::todoTagColor,
                        includeTags = !expanded
                    )
                    TodoSubtaskTree(
                        todo = todo,
                        expanded = expanded,
                        onToggleSubtask = onToggleSubtask
                    )
                    if (expanded) {
                        TodoExpandedDetails(
                            todo = todo,
                            tagColor = ::todoTagColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoCheckControl(
    checked: Boolean,
    selectedMode: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        checked && selectedMode -> MaterialTheme.colorScheme.secondary
        checked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val iconColor = if (checked) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Outlined.Check,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(19.dp)
        )
    }
}
