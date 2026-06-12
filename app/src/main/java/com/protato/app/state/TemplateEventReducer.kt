package com.protato.app.state

import com.protato.app.AppState
import com.protato.app.defaultTemplate

internal fun reduceTemplateEvent(
    state: AppState,
    event: TemplateProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.SelectTemplate -> {
            StateReduction(state.copy(selectedTemplateId = event.templateId))
        }

        is ProtatoEvent.UpsertTemplate -> {
            val exists = state.templates.any { it.id == event.template.id }
            val templates = if (exists) {
                state.templates.map { template ->
                    if (template.id == event.template.id) event.template else template
                }
            } else {
                listOf(event.template) + state.templates
            }
            StateReduction(
                state.copy(
                    templates = templates,
                    selectedTemplateId = event.template.id
                )
            )
        }

        is ProtatoEvent.DeleteTemplate -> {
            val templates = state.templates.filterNot { it.id == event.template.id }.ifEmpty {
                listOf(defaultTemplate())
            }
            StateReduction(
                state.copy(
                    templates = templates,
                    selectedTemplateId = templates.first().id
                )
            )
        }
    }
}
