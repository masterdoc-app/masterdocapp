package pro.fixaverse.data.chat

import kotlinx.coroutines.runBlocking
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.integrationApiConfig
import kotlin.test.Test
import kotlin.test.assertTrue

class StreamingChatIntegrationTest {

    @Test
    fun streamingMessage_printsTimelineAndAnswer() = runBlocking {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return@runBlocking

        val api = ChatApi(HttpClientFactory().create(), integrationApiConfig())
        val repository = HttpChatRepository(api)

        var lastTimeline = emptyList<String>()
        val result = repository.send(
            text = "Почему стиральная машина не отжимает?",
            conversationId = null,
            personaId = 1,
            onStreamUpdate = { update ->
                lastTimeline = update.assistantMessage.timeline.map { it.label }
                println(
                    "[stream] answer_len=${update.assistantMessage.content.length} " +
                        "timeline=$lastTimeline streaming=${update.assistantMessage.isStreaming}",
                )
            },
        )

        assertTrue(result.isSuccess, result.exceptionOrNull()?.message)
        val chat = result.getOrThrow()
        println("[final] ${chat.assistantMessage.content.take(200)}…")
        assertTrue(chat.assistantMessage.content.isNotBlank())
        assertTrue(lastTimeline.isNotEmpty(), "expected timeline steps during stream")
    }
}
