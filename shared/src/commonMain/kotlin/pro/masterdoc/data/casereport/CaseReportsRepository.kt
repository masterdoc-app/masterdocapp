package pro.masterdoc.data.casereport

import pro.masterdoc.domain.case.CaseReportSubmitRequest
import pro.masterdoc.domain.case.PaginatedCaseReports

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
    override suspend fun submit(request: CaseReportSubmitRequest): Result<Unit> {
        println(
            "[masterdoc case-report] mock submit assistant=${request.assistantId} " +
                "turns=${request.transcript.size} result=${request.result.take(120)}",
        )
        return Result.success(Unit)
    }

    override suspend fun list(assistantId: Int, page: Int, size: Int): Result<PaginatedCaseReports> =
        Result.success(
            PaginatedCaseReports(items = emptyList(), page = page, size = size, total = 0, hasMore = false),
        )
}
