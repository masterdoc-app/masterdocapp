package pro.masterdoc.presentation.summary

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pro.masterdoc.domain.case.CaseOutcomePayload

class SummaryStoreFactory(
    private val storeFactory: StoreFactory,
    private val outcomeSubmitter: CaseOutcomeSubmitter = LoggingCaseOutcomeSubmitter(),
) {
    fun create(): SummaryStore =
        object : SummaryStore, com.arkivanov.mvikotlin.core.store.Store<SummaryStore.Intent, SummaryStore.State, Nothing> by storeFactory.create(
            name = "SummaryStore",
            initialState = SummaryStore.State(),
            executorFactory = { SummaryExecutor(outcomeSubmitter) },
            reducer = SummaryReducer,
        ) {}
}

private sealed interface SummaryMsg {
    data class Reported(val text: String) : SummaryMsg
    data class Resolved(val text: String) : SummaryMsg
    data class Cause(val text: String) : SummaryMsg
    data class NextSteps(val text: String) : SummaryMsg
    data class Prefilled(
        val reported: String,
        val resolved: String,
        val cause: String,
        val nextSteps: String,
    ) : SummaryMsg
    data class SessionBound(
        val assistantId: Int?,
        val assistantName: String?,
        val conversationId: String?,
    ) : SummaryMsg
    data class PayloadReady(val payload: CaseOutcomePayload) : SummaryMsg
    data object Saved : SummaryMsg
    data object Submitting : SummaryMsg
    data object Submitted : SummaryMsg
    data class SubmitFailed(val error: String) : SummaryMsg
    data object Reset : SummaryMsg
}

private object SummaryReducer : Reducer<SummaryStore.State, SummaryMsg> {
    override fun SummaryStore.State.reduce(msg: SummaryMsg): SummaryStore.State = when (msg) {
        is SummaryMsg.Reported -> copy(reported = msg.text, submitError = null)
        is SummaryMsg.Resolved -> copy(resolved = msg.text, submitError = null)
        is SummaryMsg.Cause -> copy(cause = msg.text, submitError = null)
        is SummaryMsg.NextSteps -> copy(nextSteps = msg.text, submitError = null)
        is SummaryMsg.Prefilled -> copy(
            reported = msg.reported.ifBlank { reported },
            resolved = msg.resolved.ifBlank { resolved },
            cause = msg.cause.ifBlank { cause },
            nextSteps = msg.nextSteps.ifBlank { nextSteps },
        )
        is SummaryMsg.SessionBound -> this
        is SummaryMsg.PayloadReady -> copy(pendingPayload = msg.payload, submitError = null)
        SummaryMsg.Saved -> copy(isSaved = true)
        SummaryMsg.Submitting -> copy(isSubmitting = true, submitError = null)
        SummaryMsg.Submitted -> copy(isSubmitting = false, isSubmitted = true, submitError = null)
        is SummaryMsg.SubmitFailed -> copy(isSubmitting = false, submitError = msg.error)
        SummaryMsg.Reset -> SummaryStore.State()
    }
}

private class SummaryExecutor(
    private val outcomeSubmitter: CaseOutcomeSubmitter,
) : CoroutineExecutor<SummaryStore.Intent, Nothing, SummaryStore.State, SummaryMsg, Nothing>() {

    private var assistantId: Int? = null
    private var assistantName: String? = null
    private var conversationId: String? = null

    override fun executeIntent(intent: SummaryStore.Intent) {
        when (intent) {
            is SummaryStore.Intent.ReportedChanged -> dispatch(SummaryMsg.Reported(intent.text))
            is SummaryStore.Intent.ResolvedChanged -> dispatch(SummaryMsg.Resolved(intent.text))
            is SummaryStore.Intent.CauseChanged -> dispatch(SummaryMsg.Cause(intent.text))
            is SummaryStore.Intent.NextStepsChanged -> dispatch(SummaryMsg.NextSteps(intent.text))
            is SummaryStore.Intent.PrefillFromSession -> dispatch(
                SummaryMsg.Prefilled(
                    reported = intent.reported,
                    resolved = intent.resolved,
                    cause = intent.cause,
                    nextSteps = intent.nextSteps,
                ),
            )
            is SummaryStore.Intent.BindSessionContext -> {
                assistantId = intent.assistantId
                assistantName = intent.assistantName
                conversationId = intent.conversationId
                dispatch(
                    SummaryMsg.SessionBound(
                        assistantId = intent.assistantId,
                        assistantName = intent.assistantName,
                        conversationId = intent.conversationId,
                    ),
                )
            }
            SummaryStore.Intent.Save -> {
                dispatch(SummaryMsg.PayloadReady(buildPayload(state())))
                dispatch(SummaryMsg.Saved)
            }
            SummaryStore.Intent.SubmitCase -> submitCurrent()
            SummaryStore.Intent.Reset -> {
                assistantId = null
                assistantName = null
                conversationId = null
                dispatch(SummaryMsg.Reset)
            }
        }
    }

    private fun submitCurrent() {
        val payload = buildPayload(state())
        if (!isPayloadComplete(payload)) {
            dispatch(SummaryMsg.SubmitFailed("Заполните итог: что сделали и причину сбоя"))
            return
        }
        dispatch(SummaryMsg.PayloadReady(payload))
        dispatch(SummaryMsg.Submitting)
        scope.launch {
            outcomeSubmitter.submit(payload)
                .onSuccess { dispatch(SummaryMsg.Submitted) }
                .onFailure { error ->
                    dispatch(
                        SummaryMsg.SubmitFailed(
                            error.message?.takeIf { it.isNotBlank() } ?: "Не удалось отправить итог",
                        ),
                    )
                }
        }
    }

    private fun buildPayload(state: SummaryStore.State): CaseOutcomePayload = CaseOutcomePayload(
        assistantId = assistantId,
        assistantName = assistantName,
        conversationId = conversationId,
        reported = state.reported.trim(),
        resolved = state.resolved.trim(),
        cause = state.cause.trim(),
        nextSteps = state.nextSteps.trim(),
    )

    private fun isPayloadComplete(payload: CaseOutcomePayload): Boolean =
        payload.resolved.length >= 3 && payload.cause.length >= 3
}
