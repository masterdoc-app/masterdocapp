package pro.masterdoc.data.chat

import kotlinx.coroutines.runBlocking
import pro.masterdoc.domain.chat.ChatRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockChatRepositoryTest {

    private val repository = MockChatRepository()

    @Test
    fun loadHistory_returnsWelcomeMessages() = runBlocking {
        val history = repository.loadHistory(conversationId = null).getOrThrow()

        assertEquals("mock-conversation-1", history.conversationId)
        assertTrue(history.messages.size >= 2)
        assertEquals(ChatRole.Assistant, history.messages.first().role)
    }

    @Test
    fun send_appendsUserAndAssistantMessages() = runBlocking {
        repository.loadHistory(null).getOrThrow()

        val result = repository.send("Почему много инея?", conversationId = null, personaId = 1).getOrThrow()

        assertEquals(ChatRole.User, result.userMessage.role)
        assertEquals(ChatRole.Assistant, result.assistantMessage.role)
        assertTrue(result.assistantMessage.content.contains("иней", ignoreCase = true))

        val history = repository.loadHistory(null).getOrThrow()
        assertTrue(history.messages.any { it.content.contains("иней") })
    }
}
