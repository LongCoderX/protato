package com.protato.app.ui.screens.agent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.LlmProviderSettings
import com.protato.app.ai.fetchLlmModels
import com.protato.app.ai.isConfiguredForChat
import com.protato.app.agentProviderLabel
import com.protato.app.defaultAgents
import com.protato.app.defaultLlmProviders
import com.protato.app.state.ProtatoEvent
import com.protato.app.ui.components.ProtatoPanel
import com.protato.app.ui.screens.settings.LlmProviderListCard
import com.protato.app.ui.screens.settings.RecordPolishProviderCard
import com.protato.app.util.newId

@Composable
internal fun AgentConfigurationContent(
    appState: AppState,
    onEvent: (ProtatoEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val agent = appState.agents.firstOrNull { it.id == "agent-unified" }
        ?: appState.agents.firstOrNull()
        ?: defaultAgents().single()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LlmProviderListCard(
                providers = appState.llmProviders,
                newProvider = {
                    LlmProviderSettings(
                        id = newId(),
                        providerKey = "custom",
                        name = "自定义 Provider"
                    )
                },
                defaultProviderIds = defaultLlmProviders().map { it.id }.toSet(),
                providerPresets = defaultLlmProviders(),
                providerLabel = LlmProviderSettings::agentProviderLabel,
                fetchModels = ::fetchLlmModels,
                onUpsert = { onEvent(ProtatoEvent.UpsertLlmProvider(it)) },
                onDelete = { onEvent(ProtatoEvent.DeleteLlmProvider(it)) }
            )
        }
        item {
            AssistantToolsCard(
                agent = agent,
                providers = appState.llmProviders,
                onAgentChange = { onEvent(ProtatoEvent.UpdateAgent(it)) }
            )
        }
        item {
            RecordPolishProviderCard(
                providers = appState.llmProviders,
                selectedProviderId = appState.recordPolishProviderId,
                providerLabel = LlmProviderSettings::agentProviderLabel,
                providerStatus = { provider ->
                    provider?.let {
                        if (it.isConfiguredForChat()) "已配置" else "请先补全模型和 API Key"
                    } ?: "未选择 Provider"
                },
                onProviderSelected = {
                    onEvent(ProtatoEvent.SelectRecordPolishProvider(it))
                }
            )
        }
        item {
            AiDiagnosticsCard()
        }
    }
}

@Composable
private fun AssistantToolsCard(
    agent: AgentSettings,
    providers: List<LlmProviderSettings>,
    onAgentChange: (AgentSettings) -> Unit
) {
    var providerMenuExpanded by remember { mutableStateOf(false) }
    val selectedProvider = providers.firstOrNull { it.id == agent.providerId }

    ProtatoPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "助理与工具权限",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "模型只能读取已授权的数据；待办修改仍需你确认。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Box {
                OutlinedButton(
                    enabled = providers.isNotEmpty(),
                    onClick = { providerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedProvider?.agentProviderLabel() ?: "选择助理 Provider",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = providerMenuExpanded,
                    onDismissRequest = { providerMenuExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.agentProviderLabel()) },
                            onClick = {
                                onAgentChange(agent.copy(providerId = provider.id))
                                providerMenuExpanded = false
                            }
                        )
                    }
                }
            }
            ToolPermissionRow(
                title = "专注记录",
                description = "读取番茄记录并生成复盘",
                checked = agent.permissions.dailyRecords,
                onCheckedChange = {
                    onAgentChange(agent.copy(permissions = agent.permissions.copy(dailyRecords = it)))
                }
            )
            ToolPermissionRow(
                title = "待办工具",
                description = "读取待办并提出新增或修改草案",
                checked = agent.permissions.todos,
                onCheckedChange = {
                    onAgentChange(agent.copy(permissions = agent.permissions.copy(todos = it)))
                }
            )
            ToolPermissionRow(
                title = "复盘模板",
                description = "读取模板字段以理解复盘结构",
                checked = agent.permissions.templates,
                onCheckedChange = {
                    onAgentChange(agent.copy(permissions = agent.permissions.copy(templates = it)))
                }
            )
        }
    }
}

@Composable
private fun ToolPermissionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
