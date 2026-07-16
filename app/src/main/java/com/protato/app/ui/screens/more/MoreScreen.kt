package com.protato.app.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.protato.app.ui.components.Header
import com.protato.app.ui.components.ProtatoPanel
import com.protato.app.ui.components.ProfileAvatar
import com.protato.app.ProfileAvatarStyle

@Composable
fun MoreScreen(
    nickname: String,
    avatarStyle: ProfileAvatarStyle,
    focusMinutes: Int,
    level: Int,
    streak: Int,
    xp: Int,
    onOpenSettings: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenAgentSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(title = "更多", subtitle = "设置和低频配置入口")
        }
        item {
            ProtatoPanel(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = onOpenSettings,
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileAvatar(avatarStyle = avatarStyle, contentDescription = "编辑个人资料")
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(nickname, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "LV $level · 连续 $streak 天 · $xp XP · $focusMinutes 分钟专注",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("0.2.9", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { MoreEntryRow(Icons.Outlined.Settings, "设置", "个人资料、主题和字体", onOpenSettings) }
        item { MoreEntryRow(Icons.Outlined.Description, "复盘模板", "定义每轮专注结束后的问题", onOpenTemplates) }
        item { MoreEntryRow(Icons.Outlined.AutoAwesome, "AI 助理", "Provider、模型和工具权限", onOpenAgentSettings) }
    }
}

@Composable
private fun MoreEntryRow(icon: ImageVector, title: String, subtitle: String, onClick: (() -> Unit)?) {
    ProtatoPanel(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick ?: {},
            enabled = onClick != null,
            color = androidx.compose.ui.graphics.Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Icon(icon, contentDescription = title, modifier = Modifier.padding(9.dp).size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
