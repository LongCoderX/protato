package com.protato.app.ui.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.TodoItem

@Composable
fun TodoChoiceLabel(
    todo: TodoItem,
    dateLabel: String?,
    metadataColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            todo.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val metadata = buildList {
            add("${todo.plannedPomodoros.coerceAtLeast(1)} 个番茄")
            dateLabel?.let(::add)
            addAll(todo.tags.map { "#$it" })
        }.joinToString(" · ")
        if (metadata.isNotBlank()) {
            Text(
                metadata,
                color = metadataColor,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
