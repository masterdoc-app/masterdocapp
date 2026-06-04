package pro.masterdoc.presentation.summary

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pro.masterdoc.data.casereport.CaseReportsRepository
import pro.masterdoc.domain.case.CaseReportSubmitRequest
import pro.masterdoc.domain.case.TranscriptTurn

class SummaryStoreFactory(
    private val storeFactory: StoreFactory,
    private val caseReportsRepository: CaseReportsRepository,
) {
    fun create(): SummaryStore =
        object : SummaryStore, com.arkivanov.mvikotlin.core.store.Store<SummaryStore.Intent, SummaryStore.State, Nothing> by storeFactory.create(
            name = "SummaryStore",
            initialState = SummaryStore.State(),
            executorFactory = { SummaryExecutor(caseReportsRepository) },
            reducer = SummaryReducer,
        ) {}
}

private sealed interface SummaryMsg {
    data class Report(val text: String) : SummaryMsg
    data object Submitting : SummaryMsg
    data object Submitted : SummaryMsg
    data class SubmitFailed(val error: String) : SummaryMsg
    data object Reset : SummaryMsg
}

private object SummaryReducer : Reducer<SummaryStore.State, SummaryMsg> {
    override fun SummaryStore.State.reduce(msg: SummaryMsg): SummaryStore.State = when (msg) {
        is SummaryMsg.Report -> copy(report = msg.text, submitError = null)
        SummaryMsg.Submitting -> copy(isSubmitting = true, submitError = null)
        SummaryMsg.Submitted -> copy(isSubmitting = false, isSubmitted = true, submitError = null)
        is SummaryMsg.SubmitFailed -> copy(isSubmitting = false, submitError = msg.error)
        SummaryMsg.Reset -> SummaryStore.State()
    }
}

private class SummaryExecutor(
    private val caseReportsRepository: CaseReportsRepository,
) : CoroutineExecutor<SummaryStore.Intent, Nothing, SummaryStore.State, SummaryMsg, Nothing>() {

    private var assistantId: Int? = null
    private var conversationId: String? = null
    private var transcript: List<TranscriptTurn> = emptyList()

    override fun executeIntent(intent: SummaryStore.Intent) {
        when (intent) {
            is SummaryStore.Intent.ReportChanged -> dispatch(SummaryMsg.Report(intent.text))
            is SummaryStore.Intent.BindSessionContext -> {
                assistantId = intent.assistantId
                conversationId = intent.conversationId
                transcript = intent.transcript
            }
            SummaryStore.Intent.SubmitReport -> submitCurrent()
            SummaryStore.Intent.Reset -> {
                assistantId = null
                conversationId = null
                transcript = emptyList()
                dispatch(SummaryMsg.Reset)
            }
        }
    }

    private fun submitCurrent() {
        val report = state().report.trim()
        val id = assistantId
        when {
            id == null -> dispatch(SummaryMsg.SubmitFailed("Сначала выберите станцию"))
            report.length < 3 -> dispatch(SummaryMsg.SubmitFailed("Напишите отчёт (минимум 3 символа)"))
            else -> {
                dispatch(SummaryMsg.Submitting)
                scope.launch {
                    caseReportsRepository.submit(
                        CaseReportSubmitRequest(
                            assistantId = id,
                            conversationId = conversationId,
                            result = report,
                            transcript = transcript,
                        ),
                    )
                        .onSuccess { dispatch(SummaryMsg.Submitted) }
                        .onFailure { error ->
                            dispatch(
                                SummaryMsg.SubmitFailed(
                                    error.message?.takeIf { it.isNotBlank() }
                                        ?: "Не удалось отправить отчёт",
                                ),
                            )
                        }
                }
            }
        }
    }
}
