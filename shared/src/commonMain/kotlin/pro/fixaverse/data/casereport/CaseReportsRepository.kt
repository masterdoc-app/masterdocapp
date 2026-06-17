package pro.fixaverse.data.casereport

import pro.fixaverse.domain.case.CaseReport
import pro.fixaverse.domain.case.CaseReportSubmitRequest
import pro.fixaverse.domain.case.PaginatedCaseReports

interface CaseReportsRepository {
    suspend fun submit(request: CaseReportSubmitRequest): Result<Unit>
    suspend fun list(assistantId: Int, page: Int, size: Int): Result<PaginatedCaseReports>
}

class HttpCaseReportsRepository(
    private val api: CaseReportsApi,
) : CaseReportsRepository {
    override suspend fun submit(request: CaseReportSubmitRequest): Result<Unit> =
        runCatching { api.createReport(request) }.map { }

    override suspend fun list(assistantId: Int, page: Int, size: Int): Result<PaginatedCaseReports> =
        runCatching { api.listReports(assistantId = assistantId, page = page, size = size) }
}

class LoggingCaseReportsRepository : CaseReportsRepository {
    private val saved = mutableListOf<CaseReport>()
    private var nextId = 1L

    override suspend fun submit(request: CaseReportSubmitRequest): Result<Unit> {
        val report = CaseReport(
            id = "mock-${nextId++}",
            createdAt = "2026-06-03T12:00:00",
            assistantId = request.assistantId,
            conversationId = request.conversationId,
            result = request.result,
            transcript = request.transcript,
        )
        saved.add(0, report)
        println(
            "[fixaverse case-report] mock submit assistant=${request.assistantId} " +
                "turns=${request.transcript.size} result=${request.result.take(120)}",
        )
        return Result.success(Unit)
    }

    override suspend fun list(assistantId: Int, page: Int, size: Int): Result<PaginatedCaseReports> {
        val filtered = saved.filter { it.assistantId == assistantId }
        val from = page * size
        val slice = filtered.drop(from).take(size)
        return Result.success(
            PaginatedCaseReports(
                items = slice,
                page = page,
                size = size,
                total = filtered.size,
                hasMore = from + slice.size < filtered.size,
            ),
        )
    }
}
