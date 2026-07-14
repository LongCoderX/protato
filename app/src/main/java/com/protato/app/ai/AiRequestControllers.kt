package com.protato.app.ai

import com.protato.app.AgentSettings
import com.protato.app.AppState
import com.protato.app.DailySummary
import com.protato.app.LlmProviderSettings
import com.protato.app.PomodoroRecord
import com.protato.app.RecordTemplate
import com.protato.app.domain.timer.PendingRecord
import com.protato.app.domain.todo.TodoDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal sealed interface AsyncRequestState<out T> {
    data object Idle : AsyncRequestState<Nothing>
    data object Loading : AsyncRequestState<Nothing>
    data class Success<T>(val value: T) : AsyncRequestState<T>
    data class Failed(val error: Throwable) : AsyncRequestState<Nothing>
}

internal class AsyncRequestController<T>(
    private val scope: CoroutineScope
) {
    private val mutableState = MutableStateFlow<AsyncRequestState<T>>(AsyncRequestState.Idle)
    val state: StateFlow<AsyncRequestState<T>> = mutableState.asStateFlow()

    private var requestId = 0L
    private var job: Job? = null

    fun submit(
        request: suspend () -> T,
        onSuccess: (T) -> Unit = {}
    ): Boolean {
        if (mutableState.value is AsyncRequestState.Loading) return false
        start(request, onSuccess)
        return true
    }

    fun submitLatest(
        request: suspend () -> T,
        onSuccess: (T) -> Unit = {}
    ) {
        job?.cancel()
        start(request, onSuccess)
    }

    fun cancel() {
        requestId += 1
        job?.cancel()
        job = null
        mutableState.value = AsyncRequestState.Idle
    }

    private fun start(request: suspend () -> T, onSuccess: (T) -> Unit) {
        val currentRequestId = ++requestId
        mutableState.value = AsyncRequestState.Loading
        job = scope.launch {
            try {
                val value = request()
                if (currentRequestId == requestId) {
                    mutableState.value = AsyncRequestState.Success(value)
                    onSuccess(value)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                if (currentRequestId == requestId) {
                    mutableState.value = AsyncRequestState.Failed(error)
                }
            }
        }
    }
}

internal data class AgentTurnRequest(
    val text: String,
    val userState: AppState,
    val agent: AgentSettings?,
    val provider: LlmProviderSettings?,
    val nickname: String,
    val handledTodoDraftKeys: Set<String>,
    val handledTodoEditKeys: Set<String>,
    val hasPendingActions: Boolean,
    val contextTodoId: String?
)

internal class AgentTurnController(scope: CoroutineScope) {
    private val requests = AsyncRequestController<AgentTurnResult>(scope)
    val state = requests.state

    fun submit(request: AgentTurnRequest, onSuccess: (AgentTurnResult) -> Unit): Boolean {
        return requests.submit(
            request = {
                runAgentTurn(
                    text = request.text,
                    userState = request.userState,
                    agent = request.agent,
                    provider = request.provider,
                    nickname = request.nickname,
                    handledTodoDraftKeys = request.handledTodoDraftKeys,
                    handledTodoEditKeys = request.handledTodoEditKeys,
                    hasPendingActions = request.hasPendingActions,
                    contextTodoId = request.contextTodoId
                )
            },
            onSuccess = onSuccess
        )
    }

    fun cancel() = requests.cancel()
}

internal data class TodoSuggestionResult(
    val suggestions: List<TodoDraft>,
    val errorText: String? = null
)

internal class TodoSuggestionController(scope: CoroutineScope) {
    private val requests = AsyncRequestController<TodoSuggestionResult>(scope)
    val state = requests.state

    fun submit(
        rawInput: String,
        agent: AgentSettings?,
        provider: LlmProviderSettings?,
        nickname: String
    ): Boolean = requests.submit(request = {
        if (agent == null || provider == null || !provider.isConfiguredForChat()) {
            TodoSuggestionResult(parseTodoSuggestions(rawInput))
        } else {
            runCatching {
                TodoSuggestionResult(generateTodoSuggestions(rawInput, agent, provider, nickname))
            }.getOrElse { error ->
                TodoSuggestionResult(
                    suggestions = parseTodoSuggestions(rawInput),
                    errorText = error.message ?: "生成失败"
                )
            }
        }
    })

    fun cancel() = requests.cancel()
}

internal data class TemplateSuggestionResult(
    val suggestion: RecordTemplate,
    val errorText: String? = null
)

internal class TemplateSuggestionController(scope: CoroutineScope) {
    private val requests = AsyncRequestController<TemplateSuggestionResult>(scope)
    val state = requests.state

    fun submit(
        rawInput: String,
        agent: AgentSettings?,
        provider: LlmProviderSettings?,
        nickname: String
    ): Boolean = requests.submit(request = {
        if (agent == null || provider == null || !provider.isConfiguredForChat()) {
            TemplateSuggestionResult(localTemplateSuggestion(rawInput))
        } else {
            runCatching {
                TemplateSuggestionResult(generateTemplateSuggestion(rawInput, agent, provider, nickname))
            }.getOrElse { error ->
                TemplateSuggestionResult(
                    suggestion = localTemplateSuggestion(rawInput),
                    errorText = error.message ?: "生成失败"
                )
            }
        }
    })

    fun cancel() = requests.cancel()
}

internal class DailySummaryController(scope: CoroutineScope) {
    private val requests = AsyncRequestController<DailySummary>(scope)
    val state = requests.state

    fun submit(
        dayKey: String,
        records: List<PomodoroRecord>,
        templates: List<RecordTemplate>,
        providers: List<LlmProviderSettings>,
        agents: List<AgentSettings>,
        nickname: String,
        onSuccess: (DailySummary) -> Unit
    ): Boolean = requests.submit(
        request = {
            runCatching {
                generateDailySummary(dayKey, records, templates, providers, agents, nickname)
            }.getOrElse { localDailySummary(dayKey, records, nickname) }
        },
        onSuccess = onSuccess
    )

    fun cancel() = requests.cancel()
}

internal class RecordChatController(scope: CoroutineScope) {
    private val requests = AsyncRequestController<String>(scope)
    val state = requests.state

    fun submit(
        record: PomodoroRecord,
        template: RecordTemplate,
        agent: AgentSettings,
        provider: LlmProviderSettings,
        nickname: String,
        messages: List<LlmChatMessage>,
        onSuccess: (String) -> Unit
    ): Boolean = requests.submit(
        request = { requestRecordChat(record, template, agent, provider, nickname, messages) },
        onSuccess = onSuccess
    )

    fun cancel() = requests.cancel()
}

internal class RecordCompletionController(scope: CoroutineScope) {
    private val encouragementRequests = AsyncRequestController<String>(scope)
    private val polishRequests = AsyncRequestController<String>(scope)
    val encouragementState = encouragementRequests.state
    val polishState = polishRequests.state

    fun loadEncouragement(
        pendingRecord: PendingRecord,
        agent: AgentSettings,
        provider: LlmProviderSettings,
        nickname: String,
        onSuccess: (String) -> Unit
    ) {
        encouragementRequests.submitLatest(
            request = {
                runCatching {
                    generateAiEncouragement(pendingRecord, agent, provider, nickname)
                }.getOrElse { localEncouragement(pendingRecord, nickname) }
            },
            onSuccess = onSuccess
        )
    }

    fun polish(
        rawText: String,
        pendingRecord: PendingRecord,
        template: RecordTemplate,
        answers: Map<String, String>,
        provider: LlmProviderSettings,
        nickname: String,
        onSuccess: (String) -> Unit
    ): Boolean = polishRequests.submit(
        request = {
            runCatching {
                requestRecordPolish(rawText, pendingRecord, template, answers, provider, nickname)
            }.getOrElse { rawText }
        },
        onSuccess = onSuccess
    )

    fun cancel() {
        encouragementRequests.cancel()
        polishRequests.cancel()
    }
}
