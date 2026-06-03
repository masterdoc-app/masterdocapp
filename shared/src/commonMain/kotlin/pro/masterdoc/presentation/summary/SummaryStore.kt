package pro.masterdoc.presentation.summary

import com.arkivanov.mvikotlin.core.store.Store
import pro.masterdoc.domain.case.CaseOutcomePayload

interface SummaryStore : Store<SummaryStore.Intent, SummaryStore.State, Nothing> {

    data class State(
        val reported: String = "",
        val resolved: String = "",
        val cause: String = "",
        val nextSteps: String = "",
        val isSaved: Boolean = false,
        val pendingPayload: CaseOutcomePayload? = null,
        val isSubmitting: Boolean = false,
        val isSubmitted: Boolean = false,
        val submitError: String? = null,
    )

    sealed interface Intent {
        data class ReportedChanged(val text: String) : Intent
        data class ResolvedChanged(val text: String) : Intent
        data class CauseChanged(val text: String) : Intent
        data class NextStepsChanged(val text: String) : Intent
        data class PrefillFromSession(
            val reported: String,
            val resolved: String,
            val cause: String = "",
            val nextSteps: String = "",
        ) : Intent
        data class BindSessionContext(
            val assistantId: Int?,
            val assistantName: String?,
            val conversationId: String?,
        ) : Intent
        data object Save : Intent
        data object SubmitCase : Intent
        data object Reset : Intent
    }
}
