package pro.masterdoc.data.chat

import kotlinx.coroutines.runBlocking
import pro.masterdoc.domain.chat.TimelineStepKind
import pro.masterdoc.domain.chat.TimelineStepStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnyxStreamAccumulatorTest {

    @Test
    fun accumulates_reasoning_search_and_answer() = runBlocking {
        val acc = OnyxStreamAccumulator()
        acc.onLine("""{"obj":{"type":"reasoning_start"}}""")
        acc.onLine("""{"obj":{"type":"reasoning_delta","reasoning":"# Анализ\nПроверяю"}}""")
        acc.onLine("""{"obj":{"type":"reasoning_done"}}""")
        acc.onLine("""{"obj":{"type":"search_tool_start","is_internet_search":false}}""")
        acc.onLine("""{"obj":{"type":"message_delta","content":"Ответ "}}""")
        acc.onLine("""{"obj":{"type":"message_delta","content":"готов."}}""")
        acc.onLine("""{"obj":{"type":"stop"}}""")

        val snap = acc.snapshot()
        assertEquals("Ответ готов.", snap.answer)
        assertTrue(snap.timeline.any { it.kind == TimelineStepKind.Thinking })
        assertTrue(snap.timeline.any { it.kind == TimelineStepKind.Search })
        assertEquals(
            TimelineStepStatus.Done,
            snap.timeline.first { it.kind == TimelineStepKind.Thinking }.status,
        )
    }
}
