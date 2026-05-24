package pro.masterdoc.data.assistant

import kotlinx.coroutines.runBlocking
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.data.config.ApiConfig
import pro.masterdoc.data.config.DEFAULT_API_BASE_URL
import pro.masterdoc.data.config.MasterdocBuildConfig
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hits Masterdoc API when [MASTERDOC_INTEGRATION]=1 and backend is reachable.
 */
class MasterdocAssistantsApiIntegrationTest {

    @Test
    fun listAssistants_returnsNonEmpty() {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return
        val baseUrl = System.getenv("MASTERDOC_API_BASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
            ?: MasterdocBuildConfig.API_BASE_URL.ifBlank { DEFAULT_API_BASE_URL }
        runBlocking {
            val api = AssistantsApi(HttpClientFactory().create(), ApiConfig(baseUrl))
            val assistants = api.listAssistants()
            assertTrue(assistants.isNotEmpty(), "expected at least one assistant from $baseUrl")
        }
    }
}
