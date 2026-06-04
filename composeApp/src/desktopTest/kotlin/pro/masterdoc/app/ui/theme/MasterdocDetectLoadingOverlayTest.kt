package pro.masterdoc.app.ui.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MasterdocDetectLoadingOverlayTest {

    @Test
    fun overlay_visible_whenDetecting() = runComposeUiTest {
        setContent {
            MasterdocTheme {
                MasterdocDetectLoadingOverlay(visible = true)
            }
        }
        onNodeWithTag(MasterdocTestTags.DETECT_LOADING_OVERLAY).assertIsDisplayed()
    }

    @Test
    fun overlay_hidden_whenNotDetecting() = runComposeUiTest {
        setContent {
            MasterdocTheme {
                MasterdocDetectLoadingOverlay(visible = false)
            }
        }
        onNodeWithTag(MasterdocTestTags.DETECT_LOADING_OVERLAY).assertDoesNotExist()
    }
}
