package pro.fixaverse.presentation.report

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.fixaverse.data.casereport.CaseReportsRepository
import pro.fixaverse.domain.case.CaseReport
import pro.fixaverse.domain.case.CaseReportSubmitRequest
import pro.fixaverse.domain.case.PaginatedCaseReports
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReportListStoreTest {

    @Test
    fun load_populatesItems() {
        val repository = FakeCaseReportsRepository(
            pages = mapOf(
                0 to PaginatedCaseReports(
                    items = listOf(report("a")),
                    page = 0,
                    size = 20,
                    total = 1,
                    hasMore = false,
                ),
            ),
        )
        val store = ReportListStoreFactory(DefaultStoreFactory(), repository).create()
        store.accept(ReportListStore.Intent.BindAssistant(1, "Станция"))
        store.accept(ReportListStore.Intent.Load)

        awaitIdle(store)
        assertEquals(1, store.state.items.size)
        assertEquals("a", store.state.items.first().id)
        assertFalse(store.state.isLoading)
    }

    @Test
    fun loadMore_appendsNextPage() {
        val repository = FakeCaseReportsRepository(
            pages = mapOf(
                0 to PaginatedCaseReports(
                    items = listOf(report("a")),
                    page = 0,
                    size = 1,
                    total = 2,
                    hasMore = true,
                ),
                1 to PaginatedCaseReports(
                    items = listOf(report("b")),
                    page = 1,
                    size = 1,
                    total = 2,
                    hasMore = false,
                ),
            ),
        )
        val store = ReportListStoreFactory(DefaultStoreFactory(), repository).create()
        store.accept(ReportListStore.Intent.BindAssistant(1, "Станция"))
        store.accept(ReportListStore.Intent.Load)
        awaitIdle(store)
        store.accept(ReportListStore.Intent.LoadMore)
        awaitIdle(store)

        assertEquals(listOf("a", "b"), store.state.items.map { it.id })
        assertFalse(store.state.hasMore)
    }

    private fun awaitIdle(store: ReportListStore) {
        repeat(50) {
            if (!store.state.isLoading && !store.state.isLoadingMore) return
            Thread.sleep(20)
        }
    }

    private fun report(id: String) = CaseReport(
        id = id,
        createdAt = "2026-06-03T10:00:00",
        assistantId = 1,
        conversationId = null,
        result = "Подробный результат отчёта $id для базы знаний",
        transcript = emptyList(),
    )
}

private class FakeCaseReportsRepository(
    private val pages: Map<Int, PaginatedCaseReports>,
) : CaseReportsRepository {
    override suspend fun submit(request: CaseReportSubmitRequest): Result<Unit> = Result.success(Unit)

    override suspend fun list(assistantId: Int, page: Int, size: Int): Result<PaginatedCaseReports> =
        Result.success(
            pages[page] ?: PaginatedCaseReports(
                items = emptyList(),
                page = page,
                size = size,
                total = 0,
                hasMore = false,
            ),
        )
}
