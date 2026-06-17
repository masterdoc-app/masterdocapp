package pro.fixaverse.data.assistant

import kotlinx.coroutines.runBlocking
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.integrationApiConfig
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hits Masterdoc API when [MASTERDOC_INTEGRATION]=1 and backend is reachable.
 */
class FixaverseAssistantsApiIntegrationTest {

    @Test
    fun listAssistants_returnsNonEmpty() {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return
        val apiConfig = integrationApiConfig()
        runBlocking {
            val api = AssistantsApi(HttpClientFactory().create(), apiConfig)
            val assistants = api.listAssistants()
            assertTrue(assistants.isNotEmpty(), "expected at least one assistant from ${apiConfig.baseUrl}")
        }
    }
}
