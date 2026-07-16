package com.protato.app.ui.screens.settings

import com.protato.app.AppState
import com.protato.app.state.ProtatoEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.protato.app.ui.components.SecondaryHeader

@Composable
internal fun SettingsScreen(
    appState: AppState,
    appVersionName: String,
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
                subtitle = "个人资料与显示",
                onBack = onBack
            )
        }
        item {
            ProfileSettingsCard(
                nickname = appState.nickname,
                avatarStyle = appState.avatarStyle,
                onNicknameChange = { nickname ->
                    onEvent(ProtatoEvent.UpdateNickname(nickname))
                },
                onAvatarStyleChange = { avatarStyle ->
                    onEvent(ProtatoEvent.UpdateAvatarStyle(avatarStyle))
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
            ProjectVersionCard(
                appVersionName = appVersionName
            )
        }
    }
}
