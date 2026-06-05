package pro.masterdoc.domain.assistant

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
}
