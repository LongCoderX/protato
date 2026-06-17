package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.DEFAULT_NICKNAME

internal fun reduceSettingsEvent(
    state: AppState,
    event: SettingsProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.UpdateNickname -> {
            StateReduction(state.copy(nickname = event.nickname.ifBlank { DEFAULT_NICKNAME }))
        }

        is ProtatoEvent.UpdateFontScale -> {
            StateReduction(state.copy(fontScale = event.fontScale.coerceIn(0.85f, 1.3f)))
        }

        is ProtatoEvent.UpdateThemeChoice -> {
            StateReduction(state.copy(themeChoice = event.themeChoice))
        }

        is ProtatoEvent.UpdateWebDavSync -> {
            StateReduction(state.copy(webDevSync = event.settings))
        }

        is ProtatoEvent.UpsertLlmProvider -> {
            val exists = state.llmProviders.any { it.id == event.provider.id }
            val providers = if (exists) {
                state.llmProviders.map { provider ->
                    if (provider.id == event.provider.id) event.provider else provider
                }
            } else {
                state.llmProviders + event.provider
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
    }
}
