package com.protato.app.ui.screens.agent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.AppState
import com.protato.app.state.ProtatoEvent
import com.protato.app.ui.components.SecondaryHeader

@Composable
internal fun AgentSettingsScreen(
    appState: AppState,
    onEvent: (ProtatoEvent) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SecondaryHeader(
            title = "AI 配置",
            subtitle = "Provider、模型和工具权限",
            onBack = onBack
        )
        AgentConfigurationContent(
            appState = appState,
            onEvent = onEvent,
            modifier = Modifier.weight(1f)
        )
    }
}
