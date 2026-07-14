package com.protato.app.ui.screens.settings

import com.protato.app.*

import com.protato.app.state.ProtatoEvent
import com.protato.app.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.ai.fetchLlmModels
import com.protato.app.ai.isConfiguredForChat
import com.protato.app.ui.components.SecondaryHeader
import com.protato.app.ui.screens.settings.FontScaleSettingsCard
import com.protato.app.ui.screens.settings.LlmProviderListCard
import com.protato.app.ui.screens.settings.NicknameSettingsCard
import com.protato.app.ui.screens.settings.ProjectVersionCard
import com.protato.app.ui.screens.settings.RecordPolishProviderCard
import com.protato.app.ui.screens.settings.ThemeSettingsCard
import com.protato.app.ui.screens.settings.WebDavBackupSettingsCard

@Composable
internal fun SettingsScreen(
    appState: AppState,
    appVersionName: String,
    backingUp: Boolean,
    onBackup: () -> Unit,
    onEvent: (ProtatoEvent) -> Unit,
    onBack: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SecondaryHeader(
                title = "设置",
                subtitle = "本地身份、模型导入和版本",
                onBack = onBack
            )
        }
        item {
            ProjectVersionCard(
                appVersionName = appVersionName
            )
        }
        item {
            NicknameSettingsCard(
                nickname = appState.nickname,
                onNicknameChange = { nickname ->
                    onEvent(ProtatoEvent.UpdateNickname(nickname))
                }
            )
        }
        item {
            FontScaleSettingsCard(
                fontScale = appState.fontScale,
                onChange = { fontScale ->
                    onEvent(ProtatoEvent.UpdateFontScale(fontScale))
                }
            )
        }
        item {
            ThemeSettingsCard(
                selectedTheme = appState.themeChoice,
                onChange = { themeChoice ->
                    onEvent(ProtatoEvent.UpdateThemeChoice(themeChoice))
                }
            )
        }
        item {
            WebDavBackupSettingsCard(
                settings = appState.webDavBackup,
                backingUp = backingUp,
                onBackup = onBackup,
                onChange = { settings ->
                    onEvent(ProtatoEvent.UpdateWebDavBackup(settings))
                }
            )
        }
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
                providerLabel = { it.agentProviderLabel() },
                fetchModels = ::fetchLlmModels,
                onUpsert = { provider ->
                    onEvent(ProtatoEvent.UpsertLlmProvider(provider))
                },
                onDelete = { provider ->
                    onEvent(ProtatoEvent.DeleteLlmProvider(provider))
                }
            )
        }
        item {
            RecordPolishProviderCard(
                providers = appState.llmProviders,
                selectedProviderId = appState.recordPolishProviderId,
                providerLabel = { it.agentProviderLabel() },
                providerStatus = { provider ->
                    provider?.let {
                        if (it.isConfiguredForChat()) "已配置" else "请先补全模型和 API Key"
                    } ?: "未选择 Provider"
                },
                onProviderSelected = { providerId ->
                    onEvent(ProtatoEvent.SelectRecordPolishProvider(providerId))
                }
            )
        }
    }
}
