package pro.masterdoc.data.casereport

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import pro.masterdoc.data.casereport.dto.CaseReportDto
import pro.masterdoc.data.casereport.dto.CreateCaseReportRequestDto
import pro.masterdoc.data.casereport.dto.PaginatedCaseReportsResponseDto
import pro.masterdoc.data.config.ApiConfig
import pro.masterdoc.domain.case.CaseReport
import pro.masterdoc.domain.case.CaseReportSubmitRequest
import pro.masterdoc.domain.case.PaginatedCaseReports
import pro.masterdoc.domain.case.TranscriptTurn
import pro.masterdoc.data.casereport.dto.TranscriptTurnDto

class CaseReportsApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {
    suspend fun createReport(request: CaseReportSubmitRequest): CaseReport {
        val response = httpClient.post("${apiConfig.baseUrl}/case-reports") {
            setBody(request.toDto())
        }
        if (!response.status.isSuccess()) {
            error("Case report API ${response.status.value}: ${response.bodyAsText().take(300)}")
        }
        return response.body<CaseReportDto>().toDomain()
    }

    suspend fun listReports(
        page: Int = 0,
        size: Int = 20,
        assistantId: Int? = null,
    ): PaginatedCaseReports =
        httpClient.get("${apiConfig.baseUrl}/case-reports") {
            parameter("page", page)
            parameter("size", size)
            assistantId?.let { parameter("assistant_id", it) }
        }.body<PaginatedCaseReportsResponseDto>().toDomain()
}

private fun CaseReportSubmitRequest.toDto() = CreateCaseReportRequestDto(
    assistantId = assistantId,
    conversationId = conversationId,
    result = result,
    transcript = transcript.map { TranscriptTurnDto(ask = it.ask, answer = it.answer) },
)

private fun CaseReportDto.toDomain() = CaseReport(
    id = id,
    createdAt = createdAt,
    assistantId = assistantId,
    conversationId = conversationId,
    result = result,
    transcript = transcript.map { TranscriptTurn(ask = it.ask, answer = it.answer) },
)

private fun PaginatedCaseReportsResponseDto.toDomain() = PaginatedCaseReports(
    items = items.map { it.toDomain() },
    page = page,
    size = size,
    total = total,
    hasMore = hasMore,
)
