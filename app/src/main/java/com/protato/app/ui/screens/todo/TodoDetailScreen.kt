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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.TodoItem
import com.protato.app.ui.components.ProtatoPanel
import com.protato.app.util.formatDayLabel

@Composable
internal fun TodoDetailScreen(
    todo: TodoItem,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onStartFocus: () -> Unit
) {
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
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "更多操作")
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
                        "专注任务 · ${todo.plannedPomodoros} 个番茄",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        todo.title.ifBlank { "未命名任务" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (todo.summary.isNotBlank()) {
                        Text(
                            todo.summary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            todo.dueDate?.let(::formatDayLabel) ?: "未安排日期",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "+$xp XP",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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
}
