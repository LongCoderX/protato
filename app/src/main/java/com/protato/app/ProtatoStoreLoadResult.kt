package com.protato.app

sealed interface ProtatoStoreLoadResult {
    val state: AppState

    data class Missing(
        override val state: AppState = AppState()
    ) : ProtatoStoreLoadResult

    data class Success(
        override val state: AppState
    ) : ProtatoStoreLoadResult

    data class Recovered(
        override val state: AppState,
        val messages: List<String>
    ) : ProtatoStoreLoadResult

    data class Failed(
        val error: Throwable,
        override val state: AppState = AppState()
    ) : ProtatoStoreLoadResult
}

sealed interface AppStateSaveResult {
    data object Success : AppStateSaveResult

    data class Failed(val error: Throwable) : AppStateSaveResult
}
