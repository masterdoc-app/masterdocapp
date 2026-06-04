package pro.masterdoc.domain.chat

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
}
