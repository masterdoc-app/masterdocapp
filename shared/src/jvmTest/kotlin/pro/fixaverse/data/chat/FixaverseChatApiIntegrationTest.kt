package pro.fixaverse.data.chat

import kotlinx.coroutines.runBlocking
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.integrationApiConfig
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hits Masterdoc API when [local.properties] has masterdoc.api.baseUrl.
 * Skipped in CI without URL.
 */
class FixaverseChatApiIntegrationTest {

    @Test
    fun sendMessage_returnsAssistantReply() {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return
        val apiConfig = integrationApiConfig()
        runBlocking {
            val httpClient = HttpClientFactory().create()
            val api = ChatApi(httpClient, apiConfig)
            val repository = HttpChatRepository(api)

            val result = repository.send("Тест из JVM", conversationId = null, personaId = 1)
            assertTrue(result.isSuccess, result.exceptionOrNull()?.message)
            val chat = result.getOrThrow()
            assertTrue(chat.assistantMessage.content.isNotBlank())
            assertTrue(chat.conversationId.isNotBlank())
        }
    }
}
