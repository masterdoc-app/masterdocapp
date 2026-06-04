package pro.masterdoc.presentation.report

import com.arkivanov.mvikotlin.core.store.Store
import pro.masterdoc.domain.case.CaseReport

interface ReportListStore : Store<ReportListStore.Intent, ReportListStore.State, Nothing> {

    data class State(
        val assistantId: Int? = null,
        val assistantName: String? = null,
        val items: List<CaseReport> = emptyList(),
        val page: Int = 0,
        val hasMore: Boolean = false,
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data class BindAssistant(val id: Int, val name: String) : Intent
        data object Load : Intent
        data object LoadMore : Intent
        data object Retry : Intent
        data object Reset : Intent
    }
}
