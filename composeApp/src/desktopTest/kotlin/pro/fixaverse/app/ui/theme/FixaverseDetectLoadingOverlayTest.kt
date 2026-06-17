package pro.fixaverse.app.ui.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FixaverseDetectLoadingOverlayTest {

    @Test
    fun overlay_visible_whenDetecting() = runComposeUiTest {
        setContent {
            FixaverseTheme {
                FixaverseDetectLoadingOverlay(visible = true)
            }
        }
        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_OVERLAY).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_TITLE).assertIsDisplayed()
    }

    @Test
    fun overlay_hidden_whenNotDetecting() = runComposeUiTest {
        setContent {
            FixaverseTheme {
                FixaverseDetectLoadingOverlay(visible = false)
            }
        }
        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_OVERLAY).assertDoesNotExist()
    }
}
