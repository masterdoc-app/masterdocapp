package pro.masterdoc.data.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetectProgressTrackerTest {

    @Test
    fun tracks_reasoning_then_search_then_answer() {
        val tracker = DetectProgressTracker()
        assertEquals("Смотрим на фото…", tracker.onLine("""{"obj":{"type":"reasoning_start"}}"""))
        assertEquals("Анализ фото", tracker.onLine("""{"obj":{"type":"reasoning_delta","reasoning":"# Анализ фото"}}"""))
        assertEquals("Ищем в базе знаний…", tracker.onLine("""{"obj":{"type":"search_tool_start","is_internet_search":false}}"""))
        assertEquals("Определяем станцию…", tracker.onLine("""{"obj":{"type":"message_delta","content":"Атлант"}}"""))
    }
}
