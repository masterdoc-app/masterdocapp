package pro.masterdoc.app.ui.flow

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
import pro.masterdoc.app.test.TestRootFactory
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.app.ui.theme.MasterdocTheme
import pro.masterdoc.domain.assistant.Assistant
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore
import pro.masterdoc.presentation.root.FlowChild

@OptIn(ExperimentalTestApi::class)
class FlowScreensBackButtonTest {

    @Test
    fun scanScreen_root_hasNoBackButton() = runComposeUiTest {
        val root = TestRootFactory.create()
        setContent {
            MasterdocTheme {
                ScanScreenContent(
                    root = root,
                    equipmentStore = root.chat.equipmentStore,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).assertDoesNotExist()
    }

    @Test
    fun equipmentList_displaysBackAndReturnsToScan() = runComposeUiTest {
        val root = TestRootFactory.create()
        setContent {
            MasterdocTheme {
                ScanScreenContent(root, root.chat.equipmentStore)
            }
        }
        onNodeWithText("Список оборудования").performClick()
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).performClick()
        onNodeWithText("Сканировать QR или шильдик").assertIsDisplayed()
    }

    @Test
    fun chatDescribe_back_returnsToScan() = runComposeUiTest {
        val root = TestRootFactory.create()
        navigateToChatDescribe(root)
        setContent {
            MasterdocTheme {
                ChatDescribeScreenContent(root, root.chat)
            }
        }
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).performClick()
        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
    }

    @Test
    fun summary_back_returnsToChatDescribe() = runComposeUiTest {
        val root = TestRootFactory.create()
        navigateToChatDescribe(root)
        root.onOpenSummary()
        setContent {
            MasterdocTheme {
                SummaryScreenContent(root, root.summary)
            }
        }
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).performClick()
        assertIs<FlowChild.ChatDescribe>(root.stack.value.active.instance)
    }

    @Test
    fun cameraScreen_displaysBackAndReturnsToScan() = runComposeUiTest {
        val root = TestRootFactory.create()
        root.onOpenCamera()
        setContent {
            MasterdocTheme {
                CameraScreenContent(root, autoOpenCamera = false)
            }
        }
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).assertIsDisplayed()
        onNodeWithTag(MasterdocTestTags.APP_HEAD_BACK).performClick()
        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
    }

    private fun navigateToChatDescribe(root: pro.masterdoc.presentation.root.DefaultRootComponent) {
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
    }
}
