package pro.fixaverse.domain.assistant

import kotlin.test.Test
import kotlin.test.assertEquals

class DetectAnswerMatcherTest {

    @Test
    fun matches_assistant_name_in_answer() {
        val names = listOf("Атлант-стиралки", "Атлант-холодильники")
        assertEquals(
            "Атлант-холодильники",
            DetectAnswerMatcher.match("Фото относится к чату «Атлант-холодильники».", names),
        )
    }

    @Test
    fun matches_unique_prefix_fragment() {
        val names = listOf("Стол холодильный", "Насос 1К", "Насос 1ЦНСг")
        assertEquals("Стол холодильный", DetectAnswerMatcher.match("Стол", names))
    }

    @Test
    fun returns_null_for_ambiguous_prefix() {
        val names = listOf("Насос 1К", "Насос 1ЦНСг")
        assertEquals(null, DetectAnswerMatcher.match("Насос", names))
    }
}
