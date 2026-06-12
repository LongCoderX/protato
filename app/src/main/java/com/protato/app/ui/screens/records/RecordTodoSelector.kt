package com.protato.app.ui.screens.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.protato.app.ui.components.TodoTagChip

@Composable
fun RecordTodoSelector(
    todos: List<TodoItem>,
    selectedTodoId: String?,
    fallbackTodoTitle: String?,
    dayLabel: String,
    tagColor: (String) -> Color,
    onSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("绑定任务 · $dayLabel", fontWeight = FontWeight.SemiBold)
        if (todos.isEmpty() && fallbackTodoTitle == null) {
            Text("这一天还没有可绑定的待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            RecordTodoListRow(
                selected = selectedTodoId == null,
                onClick = { onSelected(null) },
                title = "不绑定",
                tags = emptyList(),
                tagColor = tagColor
            )
            if (fallbackTodoTitle != null) {
                RecordTodoListRow(
                    selected = true,
                    onClick = { },
                    title = fallbackTodoTitle,
                    tags = emptyList(),
                    tagColor = tagColor
                )
            }
            todos.forEach { todo ->
                RecordTodoListRow(
                    selected = selectedTodoId == todo.id,
                    onClick = { onSelected(todo.id) },
                    title = todo.title,
                    tags = todo.tags,
                    completed = todo.completed,
                    tagColor = tagColor
                )
            }
        }
    }
}

@Composable
private fun RecordTodoListRow(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    tags: List<String>,
    tagColor: (String) -> Color,
    completed: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None
                )
                if (tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(tags, key = { it }) { tag ->
                            TodoTagChip(tag, tagColor(tag))
                        }
                    }
                }
            }
        }
    }
}
