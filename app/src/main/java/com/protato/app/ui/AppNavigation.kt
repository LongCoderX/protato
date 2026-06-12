package com.protato.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkHistory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class MainTab(val label: String, val icon: ImageVector) {
    Todo("任务", Icons.AutoMirrored.Outlined.List),
    Focus("专注", Icons.Outlined.Timer),
    Agent("对话", Icons.AutoMirrored.Outlined.Chat),
    Records("记录", Icons.Outlined.WorkHistory),
    More("更多", Icons.Outlined.MoreHoriz)
}

enum class SecondaryDestination(val title: String) {
    Templates("记录模板"),
    Settings("设置")
}

@Composable
fun ProtatoBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        tab = MainTab.Todo,
                        selected = currentTab == MainTab.Todo,
                        onClick = { onTabSelected(MainTab.Todo) },
                        modifier = Modifier.weight(1f)
                    )
                    BottomNavItem(
                        tab = MainTab.Focus,
                        selected = currentTab == MainTab.Focus,
                        onClick = { onTabSelected(MainTab.Focus) },
                        modifier = Modifier.weight(1f)
                    )
                    Box(Modifier.weight(1.2f))
                    BottomNavItem(
                        tab = MainTab.Records,
                        selected = currentTab == MainTab.Records,
                        onClick = { onTabSelected(MainTab.Records) },
                        modifier = Modifier.weight(1f)
                    )
                    BottomNavItem(
                        tab = MainTab.More,
                        selected = currentTab == MainTab.More,
                        onClick = { onTabSelected(MainTab.More) },
                        modifier = Modifier.weight(1f)
                    )
                }
                AgentCenterButton(
                    selected = currentTab == MainTab.Agent,
                    onClick = { onTabSelected(MainTab.Agent) }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tab.icon,
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AgentCenterButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.surface
    }
    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                MainTab.Agent.icon,
                contentDescription = MainTab.Agent.label,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = MainTab.Agent.label,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
