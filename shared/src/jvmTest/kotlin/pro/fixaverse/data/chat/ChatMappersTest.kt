package pro.fixaverse.data.chat

import pro.fixaverse.data.chat.dto.OnyxChatMessageDto
import pro.fixaverse.domain.chat.ChatMessageStatus
import pro.fixaverse.domain.chat.ChatRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatMappersTest {

    @Test
    fun toChatRole_mapsUserAndAssistant() {
        assertEquals(ChatRole.User, "user".toChatRole())
        assertEquals(ChatRole.User, "USER".toChatRole())
        assertEquals(ChatRole.Assistant, "assistant".toChatRole())
        assertEquals(ChatRole.Assistant, "Assistant".toChatRole())
    }

    @Test
    fun toChatRole_unknownOrSystemReturnsNull() {
        assertNull("system".toChatRole())
        assertNull("tool".toChatRole())
        assertNull("".toChatRole())
    }

    @Test
    fun toDomain_mapsUserAndAssistantMessages() {
        val user =
            OnyxChatMessageDto(
                messageId = 1,
                messageType = "user",
                message = "Не морозит",
                timeSent = "2026-01-01T12:00:00Z",
            ).toDomain()

        assertEquals("1", user?.id)
        assertEquals(ChatRole.User, user?.role)
        assertEquals("Не морозит", user?.content)
        assertEquals("2026-01-01T12:00:00Z", user?.createdAt)
        assertEquals(ChatMessageStatus.Sent, user?.status)

        val assistant =
            OnyxChatMessageDto(
                messageId = 2,
                messageType = "assistant",
                message = "Проверьте датчик",
            ).toDomain()

        assertEquals(ChatRole.Assistant, assistant?.role)
        assertEquals("Проверьте датчик", assistant?.content)
    }

    @Test
    fun toDomain_systemOrBlankMessageReturnsNull() {
        assertNull(
            OnyxChatMessageDto(
                messageId = 3,
                messageType = "system",
                message = "hidden prompt",
            ).toDomain(),
        )
        assertNull(
            OnyxChatMessageDto(
                messageId = 4,
                messageType = "user",
                message = "   ",
            ).toDomain(),
        )
    }

    @Test
    fun toDomain_citationsPassThrough() {
        val citations = mapOf("1" to "doc-42", "2" to "doc-99")
        val message =
            OnyxChatMessageDto(
                messageId = 5,
                messageType = "assistant",
                message = "См. [1]",
                citations = citations,
            ).toDomain()

        assertEquals(citations, message?.citations)
    }

    @Test
    fun toDomain_nullCitationsBecomeEmptyMap() {
        val message =
            OnyxChatMessageDto(
                messageId = 6,
                messageType = "user",
                message = "Вопрос",
                citations = null,
            ).toDomain()

        assertEquals(emptyMap(), message?.citations)
    }
}
