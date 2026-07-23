package pro.fixaverse.data.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnyxSendResponseParserTest {
    @Test
    fun blankRawReturnsBlankAnswer() {
        assertEquals("", OnyxSendResponseParser.parse("").answer)
        assertEquals("", OnyxSendResponseParser.parse("   \n  ").answer)
    }

    @Test
    fun fullJsonWithAnswerShortCircuits() {
        val raw = """
            {"answer":"Принято!","message_id":94,"chat_session_id":null,"error_msg":null}
        """.trimIndent()

        val parsed = OnyxSendResponseParser.parse(raw)

        assertTrue(parsed.answer.contains("Принято"))
        assertEquals(94, parsed.messageId)
    }

    @Test
    fun sseMessageDeltasAccumulateAnswer() {
        val raw = """
            data: {"obj":{"type":"message_delta","content":"Hel"}}
            data: {"obj":{"type":"message_delta","content":"lo"}}
        """.trimIndent()

        assertEquals("Hello", OnyxSendResponseParser.parse(raw).answer)
    }

    @Test
    fun ssePreservesErrorMsg() {
        val raw = """
            data: {"error_msg":"upstream timeout","message_id":7}
            data: {"obj":{"type":"message_delta","content":"partial"}}
        """.trimIndent()

        val parsed = OnyxSendResponseParser.parse(raw)

        assertEquals("partial", parsed.answer)
        assertEquals("upstream timeout", parsed.errorMsg)
        assertEquals(7, parsed.messageId)
    }

    @Test
    fun sseAnswerFieldReturnsEarly() {
        val raw = """
            data: {"obj":{"type":"message_delta","content":"Hel"}}
            data: {"answer":"Complete","message_id":5}
            data: {"obj":{"type":"message_delta","content":"lo"}}
        """.trimIndent()

        val parsed = OnyxSendResponseParser.parse(raw)

        assertEquals("Complete", parsed.answer)
        assertEquals(5, parsed.messageId)
    }
}
