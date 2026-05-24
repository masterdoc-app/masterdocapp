package pro.masterdoc.data.chat

import kotlinx.coroutines.runBlocking
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.data.config.ApiConfig
import pro.masterdoc.data.config.DEFAULT_API_BASE_URL
import pro.masterdoc.data.config.MasterdocBuildConfig
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hits Masterdoc API when [local.properties] has masterdoc.api.baseUrl.
 * Skipped in CI without URL.
 */
class MasterdocChatApiIntegrationTest {

    @Test
    fun sendMessage_returnsAssistantReply() {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return
        val baseUrl = System.getenv("MASTERDOC_API_BASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
            ?: MasterdocBuildConfig.API_BASE_URL.ifBlank { DEFAULT_API_BASE_URL }
        runBlocking {
            val httpClient = HttpClientFactory().create()
            val api = ChatApi(httpClient, ApiConfig(baseUrl))
            val repository = HttpChatRepository(api)

            val result = repository.send("Тест из JVM", conversationId = null, personaId = 1)
            assertTrue(result.isSuccess, result.exceptionOrNull()?.message)
            val chat = result.getOrThrow()
            assertTrue(chat.assistantMessage.content.isNotBlank())
            assertTrue(chat.conversationId.isNotBlank())
        }
    }
}
