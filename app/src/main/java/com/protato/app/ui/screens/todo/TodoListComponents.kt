package com.protato.app.ui.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.width(44.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
        Text("$title · $count", fontWeight = FontWeight.SemiBold)
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
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "删除所选待办",
                tint = MaterialTheme.colorScheme.error
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
    if (todo.tags.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodoRow(
    todo: TodoItem,
    selected: Boolean,
    selecting: Boolean,
    onToggle: () -> Unit,
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
        ElevatedCard(
            shape = RoundedCornerShape(8.dp),
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
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = if (selecting) selected else todo.completed,
                    onCheckedChange = {
                        if (selecting) onToggleSelected() else onToggle()
                    }
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = todo.title,
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
                    TodoMetadataStrip(
                        todo = todo,
                        dayLabel = ::formatDayLabel,
                        timeLabel = ::formatTime,
                        tagColor = ::todoTagColor,
                        includeTags = !expanded
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
