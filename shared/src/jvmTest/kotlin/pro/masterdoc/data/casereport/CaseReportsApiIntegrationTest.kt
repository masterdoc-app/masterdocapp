package pro.masterdoc.data.casereport

import kotlinx.coroutines.runBlocking
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.data.integrationApiConfig
import pro.masterdoc.domain.case.CaseReportSubmitRequest
import pro.masterdoc.domain.case.TranscriptTurn
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * POST/GET /v1/report against live API (same [CaseReportsApi] as copilot web after wasm JSON fix).
 *
 * ```bash
 * export MASTERDOC_INTEGRATION=1
 * ./gradlew :shared:jvmTest --tests CaseReportsApiIntegrationTest
 * ```
 */
class CaseReportsApiIntegrationTest {

    @Test
    fun createReport_thenList_containsMarker() {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return
        val marker = "jvm-report-${System.currentTimeMillis()}"
        val api = CaseReportsApi(HttpClientFactory().create(), integrationApiConfig())
        runBlocking {
            api.createReport(
                CaseReportSubmitRequest(
                    assistantId = 2,
                    conversationId = "integration-test",
                    result = marker,
                    transcript = listOf(
                        TranscriptTurn(
                            ask = "холодильник гудит",
                            answer = "проверьте установку",
                        ),
                    ),
                ),
            )
            val page = api.listReports(assistantId = 2, page = 0, size = 20)
            assertTrue(
                page.items.any { it.result.contains(marker) },
                "GET /v1/report must return marker=$marker",
            )
        }
    }
}
