package com.protato.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkHistory
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val label: String, val icon: ImageVector) {
    Agent("对话", Icons.AutoMirrored.Outlined.Chat),
    Focus("专注", Icons.Outlined.Timer),
    Todo("待办", Icons.AutoMirrored.Outlined.List),
    Templates("模板", Icons.AutoMirrored.Outlined.ViewList),
    Records("记录", Icons.Outlined.WorkHistory),
    Settings("设置", Icons.Outlined.Settings)
}
