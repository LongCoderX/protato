package com.protato.app.state

import com.protato.app.AppState

internal fun reduceRecordEvent(
    state: AppState,
    event: RecordProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.UpdateRecord -> {
            StateReduction(
                state.copy(
                    records = state.records.map { record ->
                        if (record.id == event.record.id) event.record else record
                    }
                )
            )
        }

        is ProtatoEvent.SaveDailySummary -> {
            val summaries = listOf(event.summary) + state.dailySummaries.filterNot {
                it.dayKey == event.summary.dayKey
            }
            StateReduction(state.copy(dailySummaries = summaries))
        }
    }
}
