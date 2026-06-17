package pro.fixaverse.presentation.summary

import com.arkivanov.mvikotlin.core.store.Store
import pro.fixaverse.domain.case.TranscriptTurn

interface SummaryStore : Store<SummaryStore.Intent, SummaryStore.State, Nothing> {

    data class State(
        val report: String = "",
        val isSubmitting: Boolean = false,
        val isSubmitted: Boolean = false,
        val submitError: String? = null,
    )

    sealed interface Intent {
        data class ReportChanged(val text: String) : Intent
        data class BindSessionContext(
            val assistantId: Int?,
            val assistantName: String?,
            val conversationId: String?,
            val transcript: List<TranscriptTurn>,
        ) : Intent
        data object SubmitReport : Intent
        data object Reset : Intent
    }
}
