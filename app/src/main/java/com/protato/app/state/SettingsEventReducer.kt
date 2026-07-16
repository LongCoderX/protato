package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.DEFAULT_NICKNAME
import com.protato.app.sanitizeLegacyApiKey

internal fun reduceSettingsEvent(
    state: AppState,
    event: SettingsProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.UpdateNickname -> {
            StateReduction(state.copy(nickname = event.nickname.ifBlank { DEFAULT_NICKNAME }))
        }

        is ProtatoEvent.UpdateAvatarStyle -> {
            StateReduction(state.copy(avatarStyle = event.avatarStyle))
        }

        is ProtatoEvent.UpdateFontScale -> {
            StateReduction(state.copy(fontScale = event.fontScale.coerceIn(0.85f, 1.3f)))
        }

        is ProtatoEvent.UpdateThemeChoice -> {
            StateReduction(state.copy(themeChoice = event.themeChoice))
        }

        is ProtatoEvent.UpdateWebDavBackup -> {
            StateReduction(state.copy(webDavBackup = event.settings))
        }

        is ProtatoEvent.UpsertLlmProvider -> {
            val sanitizedProvider = event.provider.copy(
                apiKey = sanitizeLegacyApiKey(event.provider.apiKey)
            )
            val exists = state.llmProviders.any { it.id == event.provider.id }
            val providers = if (exists) {
                state.llmProviders.map { provider ->
                    if (provider.id == event.provider.id) sanitizedProvider else provider
                }
            } else {
                state.llmProviders + sanitizedProvider
            }
            StateReduction(state.copy(llmProviders = providers))
        }

        is ProtatoEvent.DeleteLlmProvider -> {
            StateReduction(
                state.copy(
                    llmProviders = state.llmProviders.filterNot { it.id == event.provider.id },
                    agents = state.agents.map { agent ->
                        if (agent.providerId == event.provider.id) {
                            agent.copy(providerId = "")
                        } else {
                            agent
                        }
                    }
                )
            )
        }

        is ProtatoEvent.SelectRecordPolishProvider -> {
            StateReduction(state.copy(recordPolishProviderId = event.providerId))
        }

        is ProtatoEvent.UpdateAgent -> {
            val agents = state.agents.map { agent ->
                if (agent.id == event.agent.id) event.agent else agent
            }.let { current ->
                if (current.any { it.id == event.agent.id }) current else current + event.agent
            }
            StateReduction(state.copy(agents = agents))
        }
    }
}
