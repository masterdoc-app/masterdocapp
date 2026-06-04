package pro.masterdoc.presentation.report

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pro.masterdoc.data.casereport.CaseReportsRepository
import pro.masterdoc.domain.case.CaseReport

class ReportListStoreFactory(
    private val storeFactory: StoreFactory,
    private val caseReportsRepository: CaseReportsRepository,
) {
    fun create(): ReportListStore =
        object : ReportListStore,
            com.arkivanov.mvikotlin.core.store.Store<ReportListStore.Intent, ReportListStore.State, Nothing> by storeFactory.create(
                name = "ReportListStore",
                initialState = ReportListStore.State(),
                executorFactory = { ReportListExecutor(caseReportsRepository) },
                reducer = ReportListReducer,
            ) {}
}

private const val PAGE_SIZE = 20

private sealed interface Msg {
    data class Bound(val id: Int, val name: String) : Msg
    data object Loading : Msg
    data object LoadingMore : Msg
    data class Loaded(val items: List<CaseReport>, val page: Int, val hasMore: Boolean, val append: Boolean) : Msg
    data class Failed(val error: String) : Msg
    data object Reset : Msg
}

private object ReportListReducer : Reducer<ReportListStore.State, Msg> {
    override fun ReportListStore.State.reduce(msg: Msg): ReportListStore.State = when (msg) {
        is Msg.Bound -> copy(assistantId = msg.id, assistantName = msg.name, error = null)
        Msg.Loading -> copy(isLoading = true, isLoadingMore = false, error = null)
        Msg.LoadingMore -> copy(isLoadingMore = true, error = null)
        is Msg.Loaded -> copy(
            items = if (msg.append) items + msg.items else msg.items,
            page = msg.page,
            hasMore = msg.hasMore,
            isLoading = false,
            isLoadingMore = false,
            error = null,
        )
        is Msg.Failed -> copy(isLoading = false, isLoadingMore = false, error = msg.error)
        Msg.Reset -> ReportListStore.State()
    }
}

private class ReportListExecutor(
    private val repository: CaseReportsRepository,
) : CoroutineExecutor<ReportListStore.Intent, Nothing, ReportListStore.State, Msg, Nothing>() {

    override fun executeIntent(intent: ReportListStore.Intent) {
        when (intent) {
            is ReportListStore.Intent.BindAssistant -> dispatch(Msg.Bound(intent.id, intent.name))
            ReportListStore.Intent.Load -> load(page = 0, append = false)
            ReportListStore.Intent.LoadMore -> {
                val current = state()
                if (!current.isLoading && !current.isLoadingMore && current.hasMore) {
                    load(page = current.page + 1, append = true)
                }
            }
            ReportListStore.Intent.Retry -> load(page = 0, append = false)
            ReportListStore.Intent.Reset -> dispatch(Msg.Reset)
        }
    }

    private fun load(page: Int, append: Boolean) {
        val assistantId = state().assistantId
        if (assistantId == null) {
            dispatch(Msg.Failed("Сначала выберите станцию"))
            return
        }
        dispatch(if (append) Msg.LoadingMore else Msg.Loading)
        scope.launch {
            repository.list(assistantId = assistantId, page = page, size = PAGE_SIZE)
                .onSuccess { pageResult ->
                    dispatch(
                        Msg.Loaded(
                            items = pageResult.items,
                            page = pageResult.page,
                            hasMore = pageResult.hasMore,
                            append = append,
                        ),
                    )
                }
                .onFailure { error ->
                    dispatch(
                        Msg.Failed(
                            error.message?.takeIf { it.isNotBlank() }
                                ?: "Не удалось загрузить отчёты",
                        ),
                    )
                }
        }
    }
}
