package pro.masterdoc.data.casereport

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val response = httpClient.post("${apiConfig.baseUrl}/report") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request.toDto()))
        }
        if (!response.status.isSuccess()) {
            error("Case report API ${response.status.value}: ${response.bodyAsText().take(300)}")
        }
        return json.decodeFromString<CaseReportDto>(response.bodyAsText()).toDomain()
    }

    suspend fun listReports(
        assistantId: Int,
        page: Int = 0,
        size: Int = 20,
    ): PaginatedCaseReports {
        val response = httpClient.get("${apiConfig.baseUrl}/report") {
            parameter("assistant_id", assistantId)
            parameter("page", page)
            parameter("size", size)
        }
        if (!response.status.isSuccess()) {
            error("Case report API ${response.status.value}: ${response.bodyAsText().take(300)}")
        }
        return json.decodeFromString<PaginatedCaseReportsResponseDto>(response.bodyAsText()).toDomain()
    }

    private companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
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
