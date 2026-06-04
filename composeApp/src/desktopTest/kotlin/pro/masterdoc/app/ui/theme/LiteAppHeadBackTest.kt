package pro.masterdoc.app.ui.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LiteAppHeadBackTest {

    @Test
    fun backControl_invokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            MasterdocTheme {
                LiteAppHead(
                    title = "Masterdoc · SMT-12",
                    subtitle = "Описание сбоя",
                    onBack = { clicked = true },
                )
            }
        }

        assertFalse(clicked)
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).performClick()
        assertTrue(clicked)
    }
}
