package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import pro.fixaverse.app.ui.theme.LiteAppHead
import pro.fixaverse.app.ui.theme.FixaverseDetectLoadingOverlay
import pro.fixaverse.app.ui.theme.FixaverseTestTags
import pro.fixaverse.app.ui.theme.FixaverseTheme
import pro.fixaverse.presentation.equipment.EquipmentSelectionStore

@OptIn(ExperimentalTestApi::class)
class ScanScreenDetectLoaderTest {

    @Test
    fun detectLoader_appears_whenIsDetecting_onScanScreen() = runComposeUiTest {
        val state = EquipmentSelectionStore.State(
            isDetecting = true,
            detectProgress = "Ищем в базе знаний…",
        )

        setContent {
            FixaverseTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LiteAppHead(
                            title = "Fixaverse",
                            subtitle = "Скан · выбор станции",
                            onMenuClick = null,
                        )
                        ScanMainPane(
                            state = state,
                            cameraError = null,
                            onCameraClick = {},
                            onListClick = {},
                        )
                    }
                    FixaverseDetectLoadingOverlay(
                        visible = state.isDetecting,
                        hint = state.detectProgress,
                    )
                }
            }
        }

        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_OVERLAY).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_TITLE).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_HINT).assertIsDisplayed()
    }

    @Test
    fun detectLoader_hidden_whenNotDetecting_onScanScreen() = runComposeUiTest {
        val state = EquipmentSelectionStore.State(isDetecting = false)

        setContent {
            FixaverseTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    ScanMainPane(
                        state = state,
                        cameraError = null,
                        onCameraClick = {},
                        onListClick = {},
                    )
                    FixaverseDetectLoadingOverlay(
                        visible = state.isDetecting,
                        hint = state.detectProgress,
                    )
                }
            }
        }

        onNodeWithTag(FixaverseTestTags.DETECT_LOADING_OVERLAY).assertDoesNotExist()
    }
}
