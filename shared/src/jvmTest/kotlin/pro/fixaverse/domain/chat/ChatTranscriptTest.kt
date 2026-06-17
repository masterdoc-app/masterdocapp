package pro.fixaverse.domain.chat

import pro.fixaverse.domain.case.TranscriptTurn
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatTranscriptTest {

    @Test
    fun toTranscriptTurns_pairsUserAndAssistant() {
        val turns = listOf(
            ChatMessage(id = "1", role = ChatRole.User, content = "Не морозит"),
            ChatMessage(id = "2", role = ChatRole.Assistant, content = "Проверьте датчик"),
            ChatMessage(id = "3", role = ChatRole.User, content = "Заменили"),
            ChatMessage(id = "4", role = ChatRole.Assistant, content = "Ок"),
        ).toTranscriptTurns()

        assertEquals(2, turns.size)
        assertEquals("Не морозит", turns[0].ask)
        assertEquals("Проверьте датчик", turns[0].answer)
        assertEquals("Заменили", turns[1].ask)
        assertEquals("Ок", turns[1].answer)
    }

    @Test
    fun toChatMessages_expandsTranscriptByRole() {
        val messages = listOf(
            TranscriptTurn(ask = "на экране ошибка 14", answer = "Ошибка 14 обычно — таймаут датчика."),
            TranscriptTurn(ask = "сброс не помог", answer = "Проверьте датчик приближения."),
        ).toChatMessages()

        assertEquals(4, messages.size)
        assertEquals(ChatRole.User, messages[0].role)
        assertEquals("на экране ошибка 14", messages[0].content)
        assertEquals(ChatRole.Assistant, messages[1].role)
        assertEquals(ChatRole.User, messages[2].role)
        assertEquals(ChatRole.Assistant, messages[3].role)
    }
}
