package com.protato.app.ui.screens.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.TodoItem
import com.protato.app.ui.screens.todo.TodoChoiceLabel
import com.protato.app.util.formatDayLabel

@Composable
internal fun TodoSelectDialog(
    todos: List<TodoItem>,
    selectedTodoId: String?,
    onDismiss: () -> Unit,
    onSelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        title = { Text("选择本轮任务") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTodoId == null,
                        onClick = { onSelected(null) },
                        label = { Text("不绑定") }
                    )
                }
                if (todos.isEmpty()) {
                    item {
                        Text("今天还没有未完成待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(todos, key = TodoItem::id) { todo ->
                        FilterChip(
                            selected = selectedTodoId == todo.id,
                            onClick = { onSelected(todo.id) },
                            label = {
                                TodoChoiceLabel(
                                    todo = todo,
                                    dateLabel = todo.dueDate?.let(::formatDayLabel)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}
