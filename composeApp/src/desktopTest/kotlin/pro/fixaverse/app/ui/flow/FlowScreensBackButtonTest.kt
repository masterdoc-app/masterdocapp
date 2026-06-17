package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertIs
import pro.fixaverse.app.test.TestRootFactory
import pro.fixaverse.app.ui.theme.FixaverseTestTags
import pro.fixaverse.app.ui.theme.FixaverseTheme
import pro.fixaverse.domain.assistant.Assistant
import pro.fixaverse.presentation.equipment.EquipmentSelectionStore
import pro.fixaverse.presentation.root.FlowChild

@OptIn(ExperimentalTestApi::class)
class FlowScreensBackButtonTest {

    @Test
    fun scanScreen_root_hasNoBackButton() = runComposeUiTest {
        val root = TestRootFactory.create()
        setContent {
            FixaverseTheme {
                ScanScreenContent(
                    root = root,
                    equipmentStore = root.chat.equipmentStore,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).assertDoesNotExist()
    }

    @Test
    fun equipmentList_displaysBackAndReturnsToScan() = runComposeUiTest {
        val root = TestRootFactory.create()
        setContent {
            FixaverseTheme {
                ScanScreenContent(root, root.chat.equipmentStore)
            }
        }
        onNodeWithText("Список оборудования").performClick()
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).performClick()
        onNodeWithText("Сканировать QR или шильдик").assertIsDisplayed()
    }

    @Test
    fun chatDescribe_back_returnsToScan() = runComposeUiTest {
        val root = TestRootFactory.create()
        navigateToChatDescribe(root)
        setContent {
            FixaverseTheme {
                ChatDescribeScreenContent(root, root.chat)
            }
        }
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).performClick()
        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
    }

    @Test
    fun summary_back_returnsToChatDescribe() = runComposeUiTest {
        val root = TestRootFactory.create()
        navigateToChatDescribe(root)
        root.onOpenSummary()
        setContent {
            FixaverseTheme {
                SummaryScreenContent(root, root.summary)
            }
        }
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).performClick()
        assertIs<FlowChild.ChatDescribe>(root.stack.value.active.instance)
    }

    @Test
    fun cameraScreen_displaysBackAndReturnsToScan() = runComposeUiTest {
        val root = TestRootFactory.create()
        root.onOpenCamera()
        setContent {
            FixaverseTheme {
                CameraScreenContent(root, autoOpenCamera = false)
            }
        }
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(FixaverseTestTags.APP_HEAD_BACK).performClick()
        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
    }

    private fun navigateToChatDescribe(root: pro.fixaverse.presentation.root.DefaultRootComponent) {
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
    }
}
