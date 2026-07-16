package com.protato.app.ui.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.TodoItem
import com.protato.app.TodoComplexity
import com.protato.app.ui.components.ProtatoPanel
import com.protato.app.util.formatDayLabel
import com.protato.app.util.formatTime

@Composable
internal fun TodoDetailScreen(
    todo: TodoItem,
    availableTags: List<String>,
    onBack: () -> Unit,
    onUpdate: (TodoItem) -> Unit,
    onToggleSubtask: (String) -> Unit,
    onStartFocus: () -> Unit
) {
    var editing by remember(todo.id) { mutableStateOf(false) }
    val steps = todo.subtasks
    val xp = todo.plannedPomodoros.coerceAtLeast(1) * 50
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
                Text("任务详情", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { editing = true }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "编辑任务")
                }
            }
        }
        item {
            ProtatoPanel(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (todo.completed) "已完成任务 · +$xp XP" else "进行中 · 完成可得 $xp XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        todo.title.ifBlank { "未命名任务" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "任务说明",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        todo.description.ifBlank { "暂无任务说明，点击右上角编辑补充。" },
                        color = if (todo.description.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (todo.summary.isNotBlank()) {
                        Text(
                            "进度摘要",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            todo.summary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item {
            ProtatoPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("任务信息", style = MaterialTheme.typography.titleSmall)
                    TodoDetailInfoRow(
                        icon = Icons.Outlined.CheckCircle,
                        label = "状态",
                        value = if (todo.completed) "已完成" else "进行中"
                    )
                    TodoDetailInfoRow(
                        icon = Icons.Outlined.DateRange,
                        label = "计划日期",
                        value = todo.dueDate?.let(::formatDayLabel) ?: "未安排"
                    )
                    TodoDetailInfoRow(
                        icon = Icons.Outlined.Notifications,
                        label = "提醒",
                        value = todo.reminderAt?.let(::formatTime) ?: "未设置"
                    )
                    TodoDetailInfoRow(
                        icon = Icons.Outlined.Timer,
                        label = "预计专注",
                        value = "${todo.plannedPomodoros.coerceAtLeast(1)} 个番茄"
                    )
                    TodoDetailInfoRow(
                        icon = Icons.Outlined.AccountTree,
                        label = "任务类型",
                        value = if (todo.complexity == TodoComplexity.Complex) "复杂任务" else "简单任务"
                    )
                    TodoDetailInfoRow(
                        icon = Icons.AutoMirrored.Outlined.Label,
                        label = "标签",
                        value = todo.tags.joinToString(" · ") { "#$it" }.ifBlank { "未添加" }
                    )
                    TodoDetailInfoRow(
                        icon = Icons.Outlined.AccessTime,
                        label = "创建时间",
                        value = formatTime(todo.createdAt)
                    )
                }
            }
        }
        if (steps.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("任务步骤", style = MaterialTheme.typography.titleSmall)
                    Text("${steps.count { it.completed }} / ${steps.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            itemsIndexed(steps, key = { _, step -> step.id }) { _, step ->
                Surface(
                    onClick = { onToggleSubtask(step.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (step.completed) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = if (step.completed) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            if (step.completed) {
                                Icon(Icons.Outlined.Check, contentDescription = "已完成", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(5.dp).size(16.dp))
                            } else {
                                androidx.compose.foundation.layout.Spacer(Modifier.size(26.dp))
                            }
                        }
                        Text(
                            step.title,
                            modifier = Modifier.weight(1f),
                            textDecoration = if (step.completed) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (step.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    "完成一轮专注后，这个任务会生成一条复盘记录。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        todo.completionReview?.let { review ->
            item {
                ProtatoPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("完成复盘", style = MaterialTheme.typography.titleSmall)
                        ReviewText("遇到的问题", review.problems)
                        ReviewText("解决方式", review.solution)
                        ReviewText("仍有阻碍", review.blockers)
                        TodoDetailInfoRow(
                            icon = Icons.Outlined.History,
                            label = "完成时间",
                            value = formatTime(review.completedAt)
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = onStartFocus,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                Text("进入专注")
            }
        }
    }

    if (editing) {
        TodoEditorDialog(
            todo = todo,
            availableTags = availableTags,
            onDismiss = { editing = false },
            onSave = { updatedTodo ->
                onUpdate(updatedTodo)
                editing = false
            }
        )
    }
}

@Composable
private fun TodoDetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewText(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
