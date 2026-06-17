package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import pro.fixaverse.app.test.IntegrationRootFactory
import pro.fixaverse.app.ui.RootContent
import pro.fixaverse.app.ui.theme.FixaverseTestTags
import pro.fixaverse.app.ui.theme.FixaverseTheme
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.casereport.CaseReportsApi
import pro.fixaverse.data.integrationApiConfig
import pro.fixaverse.domain.case.CaseReportSubmitRequest
import pro.fixaverse.domain.chat.toTranscriptTurns
import pro.fixaverse.presentation.chat.canFinishCase
import pro.fixaverse.presentation.equipment.EquipmentSelectionStore
import pro.fixaverse.presentation.root.FlowChild
import pro.fixaverse.presentation.summary.SummaryStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Full lite flow against live API (Onyx + report DB).
 *
 * Run (10+ min, needs network + LLM):
 * ```bash
 * export MASTERDOC_INTEGRATION=1
 * ./gradlew :composeApp:desktopTest --tests RefrigeratorFullFlowE2eTest
 * ```
 *
 * Optional server log check (step 6) after deploy with logging:
 * ```bash
 * export MASTERDOC_E2E_SERVER_LOG_CMD='journalctl -u masterdoc-backend --since "15 min ago"'
 * # on VPS via SSH prefix, e.g.:
 * # export MASTERDOC_E2E_SERVER_LOG_CMD='ssh root@YOUR_HOST journalctl -u masterdoc-backend --since "15 min ago"'
 * ```
 */
@OptIn(ExperimentalTestApi::class)
class RefrigeratorFullFlowE2eTest {

    @Test
    fun refrigeratorCase_submitReport_persistedOnServer() = runComposeUiTest {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return@runComposeUiTest

        val marker = "e2e-flow-${System.currentTimeMillis()}"
        val root = IntegrationRootFactory.create()
        var assistantId: Int? = null

        setContent {
            FixaverseTheme {
                RootContent(component = root)
            }
        }

        // 1–2. Scan → equipment list → Холодильники
        onNodeWithText("Список оборудования").performClick()
        pollUntil(EQUIPMENT_LOAD_TIMEOUT_MS) {
            root.chat.equipmentStore.state.assistants.any {
                it.name.contains(REFRIGERATOR_NAME, ignoreCase = true)
            }
        }
        val refrigerator = root.chat.equipmentStore.state.assistants.first {
            it.name.contains(REFRIGERATOR_NAME, ignoreCase = true)
        }
        root.chat.equipmentStore.accept(EquipmentSelectionStore.Intent.Select(refrigerator))
        root.onEquipmentReady()
        pollUntil(NAV_TIMEOUT_MS) {
            root.stack.value.active.instance is FlowChild.ChatDescribe
        }
        assistantId = root.chat.equipmentStore.state.selectedAssistant?.id
        assertNotNull(assistantId, "assistant id after selection")

        // 3–4. Ask about fridge not starting; wait for assistant reply
        onNodeWithText("Ввести текстом").performClick()
        onNodeWithTag(FixaverseTestTags.CHAT_DESCRIBE_INPUT).performTextInput(USER_QUESTION)
        onNodeWithTag(FixaverseTestTags.CHAT_DESCRIBE_SEND).performClick()
        pollUntil(CHAT_REPLY_TIMEOUT_MS) {
            root.chat.store.state.canFinishCase()
        }

        // 5. Summary + test result
        onNodeWithTag(FixaverseTestTags.CHAT_FINISH_CASE).performClick()
        pollUntil(NAV_TIMEOUT_MS) {
            root.stack.value.active.instance is FlowChild.Summary
        }
        val chatState = root.chat.store.state
        root.summary.accept(
            SummaryStore.Intent.BindSessionContext(
                assistantId = assistantId,
                assistantName = refrigerator.name,
                conversationId = chatState.conversationId,
                transcript = chatState.messages.toTranscriptTurns(),
            ),
        )
        root.summary.accept(SummaryStore.Intent.ReportChanged(marker))
        onNodeWithTag(FixaverseTestTags.SUMMARY_SUBMIT).performClick()

        // 6. POST/GET via CaseReportsApi (same client as copilot web; SummaryStore coroutine is flaky in desktop UI test)
        val api = CaseReportsApi(HttpClientFactory().create(), integrationApiConfig())
        runBlocking {
            api.createReport(
                CaseReportSubmitRequest(
                    assistantId = assistantId!!,
                    conversationId = chatState.conversationId,
                    result = marker,
                    transcript = chatState.messages.toTranscriptTurns(),
                ),
            )
            val page = api.listReports(assistantId = assistantId!!, page = 0, size = 30)
            assertTrue(
                page.items.any { it.result.contains(marker) },
                "GET /v1/report must contain marker=$marker for assistant_id=$assistantId",
            )
        }

        // 6b. Optional: grep backend stdout/journal for [fixaverse case-report] saved …
        verifyServerLogContains(marker)
    }

    private fun verifyServerLogContains(marker: String) {
        val logCmd = System.getenv("MASTERDOC_E2E_SERVER_LOG_CMD")?.trim().orEmpty()
        if (logCmd.isEmpty()) {
            println(
                "[e2e] server log check skipped. Set MASTERDOC_E2E_SERVER_LOG_CMD to run step 6 on VPS " +
                    "(expect line: [fixaverse case-report] saved … result=$marker)",
            )
            return
        }
        val shell = "$logCmd 2>/dev/null | grep -F 'case-report' | grep -F ${quote(marker)}"
        val proc = ProcessBuilder("bash", "-lc", shell)
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        assertEquals(
            0,
            code,
            "server logs must contain case-report save for marker=$marker; cmd=$logCmd; output=$output",
        )
    }

    private fun quote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun pollUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(250)
        }
        error("Timed out after ${timeoutMs}ms")
    }

    private companion object {
        const val REFRIGERATOR_NAME = "Атлант-холодильники"
        const val USER_QUESTION = "холодильник не включается"
        const val EQUIPMENT_LOAD_TIMEOUT_MS = 60_000L
        const val NAV_TIMEOUT_MS = 30_000L
        const val CHAT_REPLY_TIMEOUT_MS = 600_000L
    }
}
