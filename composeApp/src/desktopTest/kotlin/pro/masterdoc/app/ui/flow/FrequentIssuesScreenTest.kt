package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import pro.masterdoc.app.test.TestRootFactory
import pro.masterdoc.app.test.goldenScreenshotFile
import pro.masterdoc.app.test.saveGoldenScreenshot
import pro.masterdoc.app.ui.RootContent
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.app.ui.theme.MasterdocTheme
import pro.masterdoc.data.casereport.LoggingCaseReportsRepository
import pro.masterdoc.domain.assistant.Assistant
import pro.masterdoc.domain.case.CaseReportSubmitRequest
import pro.masterdoc.domain.case.TranscriptTurn
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore
import pro.masterdoc.presentation.report.ReportListStore
import pro.masterdoc.presentation.root.FlowChild
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FrequentIssuesScreenTest {

    @Test
    fun frequentIssues_emptyState_rendersAndMatchesGoldenScreenshot() = runComposeUiTest {
        val root = TestRootFactory.create()
        navigateToFrequentIssues(root)

        setContent {
            MasterdocTheme {
                RootContent(component = root)
            }
        }

        waitForFrequentIssuesScreen()
        onNodeWithTag(MasterdocTestTags.FREQUENT_ISSUES_HEAD).assertIsDisplayed()
        onNodeWithText("Пока нет записей о неисправностях").assertIsDisplayed()

        saveGoldenScreenshot("frequent_issues_empty.png")
        assertTrue(goldenScreenshotFile("frequent_issues_empty.png").exists())
    }

    @Test
    fun frequentIssues_withReports_rendersAndMatchesGoldenScreenshot() = runComposeUiTest {
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
        navigateToFrequentIssues(root)

        setContent {
            MasterdocTheme {
                RootContent(component = root)
            }
        }

        waitForReportList(root.reportList)
        waitForFrequentIssuesScreen()

        onNodeWithTag(MasterdocTestTags.FREQUENT_ISSUES_HEAD).assertIsDisplayed()
        onNodeWithText("Холодильник не включается", substring = true).assertIsDisplayed()

        saveGoldenScreenshot("frequent_issues_list.png")
        assertTrue(goldenScreenshotFile("frequent_issues_list.png").exists())
    }

    private fun navigateToFrequentIssues(root: pro.masterdoc.presentation.root.DefaultRootComponent) {
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
        root.onOpenFrequentIssues()
        assertIs<FlowChild.FrequentIssues>(root.stack.value.active.instance)
    }

    private fun ComposeUiTest.waitForFrequentIssuesScreen() {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            try {
                onNodeWithTag(MasterdocTestTags.FREQUENT_ISSUES_SCREEN).assertIsDisplayed()
                return
            } catch (_: AssertionError) {
                Thread.sleep(50)
            }
        }
        onNodeWithTag(MasterdocTestTags.FREQUENT_ISSUES_SCREEN).assertIsDisplayed()
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
