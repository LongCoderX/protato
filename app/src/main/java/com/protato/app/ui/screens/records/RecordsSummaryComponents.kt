package com.protato.app.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.R

data class RecordDayOption(
    val key: String,
    val label: String,
    val count: Int
)

@Composable
fun DaySwitcher(
    dayOptions: List<RecordDayOption>,
    selectedDayKey: String,
    onSelected: (String) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenDatePicker) {
            Icon(Icons.Outlined.DateRange, contentDescription = "选择记录日期")
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dayOptions, key = { it.key }) { option ->
                FilterChip(
                    selected = selectedDayKey == option.key,
                    onClick = { onSelected(option.key) },
                    label = {
                        Text(
                            "${option.label} · ${option.count}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    title: String,
    statsText: String,
    hasSummary: Boolean,
    hasRecords: Boolean,
    loading: Boolean,
    onGenerate: () -> Unit,
    onOpen: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        statsText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    enabled = hasSummary,
                    onClick = onOpen
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "查看总结")
                }
                FilledIconButton(
                    enabled = hasRecords && !loading,
                    onClick = onGenerate
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_robot_svg),
                        contentDescription = "生成总结"
                    )
                }
            }
        }
    }
}
