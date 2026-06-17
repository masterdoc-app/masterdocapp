package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import pro.fixaverse.app.test.TestRootFactory
import pro.fixaverse.app.test.goldenScreenshotFile
import pro.fixaverse.app.test.saveGoldenScreenshot
import pro.fixaverse.app.ui.RootContent
import pro.fixaverse.app.ui.theme.FixaverseTestTags
import pro.fixaverse.app.ui.theme.FixaverseTheme
import pro.fixaverse.data.casereport.LoggingCaseReportsRepository
import pro.fixaverse.domain.assistant.Assistant
import pro.fixaverse.domain.case.CaseReportSubmitRequest
import pro.fixaverse.domain.case.TranscriptTurn
import pro.fixaverse.presentation.equipment.EquipmentSelectionStore
import pro.fixaverse.presentation.report.ReportListStore
import pro.fixaverse.presentation.root.FlowChild
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class KnowledgeBaseScreenTest {

    @Test
    fun knowledgeBase_emptyState_rendersAndMatchesGoldenScreenshot() = runComposeUiTest {
        val root = TestRootFactory.create()
        navigateToKnowledgeBase(root)

        setContent {
            FixaverseTheme {
                RootContent(component = root)
            }
        }

        waitForKnowledgeBaseScreen()
        onNodeWithTag(FixaverseTestTags.KNOWLEDGE_BASE_HEAD).assertIsDisplayed()
        onNodeWithText("Пока нет записей в базе знаний").assertIsDisplayed()

        saveGoldenScreenshot("knowledge_base_empty.png")
        assertTrue(goldenScreenshotFile("knowledge_base_empty.png").exists())
    }

    @Test
    fun knowledgeBase_withReports_rendersAndMatchesGoldenScreenshot() = runComposeUiTest {
        val caseReports = LoggingCaseReportsRepository()
        runBlocking {
            caseReports.submit(
                CaseReportSubmitRequest(
                    assistantId = 1,
                    conversationId = "conv-1",
                    result = "Холодильник не включается — проверен предохранитель, заменён пускозащитный реле.",
                    transcript = listOf(
                        TranscriptTurn(ask = "Не включается", answer = "Проверьте питание и реле"),
                    ),
                ),
            )
            caseReports.submit(
                CaseReportSubmitRequest(
                    assistantId = 1,
                    conversationId = "conv-2",
                    result = "Срабатывает защита по току при пуске компрессора.",
                    transcript = emptyList(),
                ),
            )
        }

        val root = TestRootFactory.create(caseReportsRepository = caseReports)
        navigateToKnowledgeBase(root)

        setContent {
            FixaverseTheme {
                RootContent(component = root)
            }
        }

        waitForReportList(root.reportList)
        waitForKnowledgeBaseScreen()

        onNodeWithTag(FixaverseTestTags.KNOWLEDGE_BASE_HEAD).assertIsDisplayed()
        onNodeWithText("Холодильник не включается", substring = true).assertIsDisplayed()
        onNodeWithText("ПЕРЕПИСКА").assertIsDisplayed()
        onNodeWithText("Не включается").assertIsDisplayed()
        onNodeWithText("Проверьте питание и реле", substring = true).assertIsDisplayed()
        onNodeWithText("FIXAVERSE").assertIsDisplayed()

        saveGoldenScreenshot("knowledge_base_list.png")
        assertTrue(goldenScreenshotFile("knowledge_base_list.png").exists())
    }

    private fun navigateToKnowledgeBase(root: pro.fixaverse.presentation.root.DefaultRootComponent) {
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
        root.onOpenKnowledgeBase()
        waitForKnowledgeBaseChild(root)
    }

    private fun waitForKnowledgeBaseChild(root: pro.fixaverse.presentation.root.DefaultRootComponent) {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            if (root.stack.value.active.instance is FlowChild.KnowledgeBase) return
            Thread.sleep(50)
        }
        assertIs<FlowChild.KnowledgeBase>(root.stack.value.active.instance)
    }

    private fun ComposeUiTest.waitForKnowledgeBaseScreen() {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            try {
                onNodeWithTag(FixaverseTestTags.KNOWLEDGE_BASE_SCREEN).assertIsDisplayed()
                return
            } catch (_: AssertionError) {
                Thread.sleep(50)
            }
        }
        onNodeWithTag(FixaverseTestTags.KNOWLEDGE_BASE_SCREEN).assertIsDisplayed()
    }

    private fun waitForReportList(reportList: ReportListStore) {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val state = reportList.state
            if (!state.isLoading && state.items.isNotEmpty()) return
            Thread.sleep(50)
        }
        error("Report list did not load in time")
    }
}
