package pro.fixaverse.data.citation

import kotlin.test.Test
import kotlin.test.assertEquals

class ChatCitationMarkdownTest {
    @Test
    fun preprocessCitationMarkdown_rewritesEmptyOnyxLinks() {
        val input = "причина [[1]]() и ещё [[2]]()"
        val expected =
            "причина [1](https://copilot.fixaverse.ru/.citation/1) и ещё [2](https://copilot.fixaverse.ru/.citation/2)"
        assertEquals(expected, preprocessCitationMarkdown(input))
    }

    @Test
    fun preprocessCitationMarkdown_rewritesSingleBracketLinks() {
        assertEquals(
            "текст [3](https://copilot.fixaverse.ru/.citation/3)",
            preprocessCitationMarkdown("текст [3]()"),
        )
    }

    @Test
    fun parseCitationUri_readsFixaverseScheme() {
        assertEquals("3", parseCitationUri("fixaverse://citation/3"))
        assertEquals("3", parseCitationUri("https://copilot.fixaverse.ru/.citation/3"))
    }
}
