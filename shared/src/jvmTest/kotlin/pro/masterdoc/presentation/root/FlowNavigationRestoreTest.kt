package pro.masterdoc.presentation.root

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.masterdoc.data.assistant.MockAssistantsRepository
import pro.masterdoc.data.casereport.LoggingCaseReportsRepository
import pro.masterdoc.data.chat.MockChatRepository
import pro.masterdoc.domain.assistant.Assistant
import pro.masterdoc.presentation.chat.ChatStoreFactory
import pro.masterdoc.presentation.chat.DefaultChatComponent
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore
import pro.masterdoc.presentation.equipment.EquipmentSelectionStoreFactory
import pro.masterdoc.presentation.report.ReportListStoreFactory
import pro.masterdoc.presentation.summary.SummaryStoreFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * Simulates a full web page refresh: navigation is written to [InMemoryFlowNavigationPersistence],
 * then a new [DefaultRootComponent] reads it and must open the same screen.
 */
class FlowNavigationRestoreTest {

    @Test
    fun refresh_restoresCameraScreen() {
        val persistence = InMemoryFlowNavigationPersistence()
        val first = createRoot(persistence)
        first.onOpenCamera()
        assertPersisted(persistence, FlowConfig.Camera)

        val restored = createRoot(persistence)
        assertIs<FlowChild.Camera>(restored.stack.value.active.instance)
    }

    @Test
    fun refresh_restoresChatDescribeScreen() {
        val persistence = InMemoryFlowNavigationPersistence()
        val first = createRoot(persistence)
        selectRefrigeratorsAndOpenChat(first)
        assertPersisted(persistence, FlowConfig.ChatDescribe)

        val restored = createRoot(persistence)
        assertIs<FlowChild.ChatDescribe>(restored.stack.value.active.instance)
        assertEquals(1, restored.chat.equipmentStore.state.selectedAssistant?.id)
        assertEquals("Холодильники", restored.chat.equipmentStore.state.selectedAssistant?.name)
    }

    @Test
    fun refresh_restoresSummaryScreen() {
        val persistence = InMemoryFlowNavigationPersistence()
        val first = createRoot(persistence)
        selectRefrigeratorsAndOpenChat(first)
        first.onOpenSummary()
        assertPersisted(persistence, FlowConfig.Summary)

        val restored = createRoot(persistence)
        assertIs<FlowChild.Summary>(restored.stack.value.active.instance)
        assertEquals(1, restored.chat.equipmentStore.state.selectedAssistant?.id)
    }

    @Test
    fun refresh_restoresFrequentIssuesScreen() {
        val persistence = InMemoryFlowNavigationPersistence()
        val first = createRoot(persistence)
        selectRefrigeratorsAndOpenChat(first)
        first.onOpenFrequentIssues()
        assertPersisted(persistence, FlowConfig.FrequentIssues)

        val restored = createRoot(persistence)
        assertIs<FlowChild.FrequentIssues>(restored.stack.value.active.instance)
        assertEquals(1, restored.chat.equipmentStore.state.selectedAssistant?.id)
    }

    @Test
    fun refresh_fromScan_staysOnScan() {
        val persistence = InMemoryFlowNavigationPersistence()
        val first = createRoot(persistence)
        assertIs<FlowChild.Scan>(first.stack.value.active.instance)
        assertPersisted(persistence, FlowConfig.Scan)

        val restored = createRoot(persistence)
        assertIs<FlowChild.Scan>(restored.stack.value.active.instance)
    }

    @Test
    fun finishAndRestart_clearsPersistence() {
        val persistence = InMemoryFlowNavigationPersistence()
        val root = createRoot(persistence)
        selectRefrigeratorsAndOpenChat(root)
        root.onFinishAndRestart()

        assertEquals(null, persistence.snapshot)
        val restored = createRoot(persistence)
        assertIs<FlowChild.Scan>(restored.stack.value.active.instance)
    }

    private fun assertPersisted(persistence: InMemoryFlowNavigationPersistence, active: FlowConfig) {
        val snapshot = persistence.snapshot
        assertNotNull(snapshot, "navigation snapshot must be written before refresh")
        assertEquals(active, snapshot.stack.last())
    }

    private fun selectRefrigeratorsAndOpenChat(root: DefaultRootComponent) {
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
    }

    private fun createRoot(persistence: FlowNavigationPersistence): DefaultRootComponent {
        val lifecycle = LifecycleRegistry()
        val storeFactory = DefaultStoreFactory()
        val equipmentStoreFactory = EquipmentSelectionStoreFactory(
            storeFactory = storeFactory,
            repository = MockAssistantsRepository(),
        )
        val chatStoreFactory = ChatStoreFactory(
            storeFactory = storeFactory,
            repository = MockChatRepository(),
        )
        val caseReports = LoggingCaseReportsRepository()
        val root = DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle),
            chatFactory = { ctx ->
                DefaultChatComponent(
                    componentContext = ctx,
                    storeFactory = chatStoreFactory,
                    equipmentStoreFactory = equipmentStoreFactory,
                )
            },
            summaryStoreFactory = SummaryStoreFactory(
                storeFactory = storeFactory,
                caseReportsRepository = caseReports,
            ),
            reportListStoreFactory = ReportListStoreFactory(
                storeFactory = storeFactory,
                caseReportsRepository = caseReports,
            ),
            flowNavigationPersistence = persistence,
        )
        lifecycle.start()
        lifecycle.resume()
        return root
    }
}
