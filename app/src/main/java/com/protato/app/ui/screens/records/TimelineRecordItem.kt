package com.protato.app.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.PomodoroRecord
import com.protato.app.RecordTemplate

@Composable
fun TimelineRecordItem(
    record: PomodoroRecord,
    template: RecordTemplate?,
    endedAtLabel: String,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit
) {
    var expanded by rememberSaveable(record.id) { mutableStateOf(false) }
    val answerCount = record.answers.size
    val lineColor = Color(0xFFF59E0B)
    val lineHeight = ((record.focusMinutes.coerceAtLeast(1) / 25f) * 74f).coerceIn(42f, 126f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (isFirst) 10.dp else 18.dp)
                    .background(if (isFirst) Color.Transparent else lineColor.copy(alpha = 0.55f))
            )
            Surface(
                modifier = Modifier.size(18.dp),
                shape = RoundedCornerShape(8.dp),
                color = lineColor
            ) {}
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(lineHeight.dp)
                    .background(if (isLast) Color.Transparent else lineColor)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                endedAtLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "${record.focusMinutes} 分钟",
                color = lineColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        ElevatedCard(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .clickable { expanded = !expanded },
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        record.todoTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "编辑记录")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (expanded) "收起记录详情" else "展开记录详情",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                if (expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "${record.templateName} · $answerCount 条回答",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        record.answers.forEach { answer ->
                            val title = template?.fields?.firstOrNull { it.id == answer.fieldId }?.title ?: "字段"
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    answer.value.ifBlank { "未填写" },
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
