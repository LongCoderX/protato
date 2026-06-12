package com.protato.app.ui.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.util.formatDayLabel
import com.protato.app.util.todoTagColor

@Composable
fun TodoFilterControls(
    dateKeys: List<String>,
    selectedDate: String?,
    selectedTag: String?,
    tags: List<String>,
    undatedCount: Int,
    onDateSelected: (String?) -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenTagFilter: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onOpenTagFilter, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedTag?.let { "#$it" } ?: "全部标签",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOpenDatePicker) {
                Icon(Icons.Outlined.DateRange, contentDescription = "选择日期")
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dateKeys, key = { it }) { key ->
                FilterChip(
                    selected = selectedDate == key,
                    onClick = { onDateSelected(key) },
                    label = {
                        Text(
                            formatDayLabel(key),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
            item {
                FilterChip(
                    selected = selectedDate == null,
                    onClick = { onDateSelected(null) },
                    label = { Text("无日期 · $undatedCount") }
                )
            }
        }
        if (tags.isEmpty() && selectedTag != null) {
            Text(
                "当前没有可用标签",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TodoTagFilterDialog(
    tags: List<String>,
    selectedTag: String?,
    onDismiss: () -> Unit,
    onSelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("筛选标签") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTag == null,
                        onClick = { onSelected(null) },
                        label = { Text("全部标签") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (tags.isEmpty()) {
                    item {
                        Text("还没有标签。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(tags, key = { it }) { tag ->
                        val tagColor = todoTagColor(tag)
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { onSelected(if (selectedTag == tag) null else tag) },
                            label = { Text("#$tag") },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    color = tagColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {}
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}
