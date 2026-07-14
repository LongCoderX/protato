package com.protato.app.ui.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.TodoCompletionReview
import com.protato.app.TodoItem

@Composable
internal fun TodoCompletionReviewDialog(
    todo: TodoItem,
    onDismiss: () -> Unit,
    onSave: (TodoCompletionReview) -> Unit
) {
    var problems by rememberSaveable(todo.id) { mutableStateOf(todo.completionReview?.problems.orEmpty()) }
    var solution by rememberSaveable(todo.id) { mutableStateOf(todo.completionReview?.solution.orEmpty()) }
    var blockers by rememberSaveable(todo.id) { mutableStateOf(todo.completionReview?.blockers.orEmpty()) }
    val hasAnyAnswer = problems.isNotBlank() || solution.isNotBlank() || blockers.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = hasAnyAnswer,
                onClick = {
                    onSave(
                        TodoCompletionReview(
                            problems = problems.trim(),
                            solution = solution.trim(),
                            blockers = blockers.trim()
                        )
                    )
                }
            ) { Text("完成任务") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("复杂任务复盘") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    todo.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = problems,
                    onValueChange = { problems = it.take(600) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("遇到什么问题") },
                    minLines = 2
                )
                OutlinedTextField(
                    value = solution,
                    onValueChange = { solution = it.take(600) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("如何解决") },
                    minLines = 2
                )
                OutlinedTextField(
                    value = blockers,
                    onValueChange = { blockers = it.take(600) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("没解决的卡点") },
                    minLines = 2
                )
            }
        }
    )
}
