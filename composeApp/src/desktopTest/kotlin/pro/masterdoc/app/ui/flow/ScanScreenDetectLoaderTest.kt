package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.MasterdocDetectLoadingOverlay
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.app.ui.theme.MasterdocTheme
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore

@OptIn(ExperimentalTestApi::class)
class ScanScreenDetectLoaderTest {

    @Test
    fun detectLoader_appears_whenIsDetecting_onScanScreen() = runComposeUiTest {
        val state = EquipmentSelectionStore.State(
            isDetecting = true,
            detectProgress = "Ищем в базе знаний…",
        )

        setContent {
            MasterdocTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LiteAppHead(
                            title = "Masterdoc",
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
                    MasterdocDetectLoadingOverlay(
                        visible = state.isDetecting,
                        hint = state.detectProgress,
                    )
                }
            }
        }

        onNodeWithTag(MasterdocTestTags.DETECT_LOADING_OVERLAY).assertIsDisplayed()
        onNodeWithTag(MasterdocTestTags.DETECT_LOADING_TITLE).assertIsDisplayed()
        onNodeWithTag(MasterdocTestTags.DETECT_LOADING_HINT).assertIsDisplayed()
    }

    @Test
    fun detectLoader_hidden_whenNotDetecting_onScanScreen() = runComposeUiTest {
        val state = EquipmentSelectionStore.State(isDetecting = false)

        setContent {
            MasterdocTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    ScanMainPane(
                        state = state,
                        cameraError = null,
                        onCameraClick = {},
                        onListClick = {},
                    )
                    MasterdocDetectLoadingOverlay(
                        visible = state.isDetecting,
                        hint = state.detectProgress,
                    )
                }
            }
        }

        onNodeWithTag(MasterdocTestTags.DETECT_LOADING_OVERLAY).assertDoesNotExist()
    }
}
