package pro.fixaverse.presentation.root

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.fixaverse.data.assistant.MockAssistantsRepository
import pro.fixaverse.data.chat.MockChatRepository
import pro.fixaverse.domain.assistant.Assistant
import pro.fixaverse.presentation.chat.ChatStoreFactory
import pro.fixaverse.presentation.chat.DefaultChatComponent
import pro.fixaverse.presentation.equipment.EquipmentSelectionStore
import pro.fixaverse.presentation.equipment.EquipmentSelectionStoreFactory
import pro.fixaverse.data.casereport.LoggingCaseReportsRepository
import pro.fixaverse.presentation.report.ReportListStoreFactory
import pro.fixaverse.presentation.summary.SummaryStoreFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultRootComponentBackTest {

    @Test
    fun onBack_fromChatDescribe_returnsToScan() {
        val root = createRoot()
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()

        assertIs<FlowChild.ChatDescribe>(root.stack.value.active.instance)

        root.onBack()

        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
        assertEquals(null, root.chat.equipmentStore.state.selectedAssistant)
    }

    @Test
    fun onBack_fromChatDescribe_doesNotReopenChatAfterScanReturns() {
        val root = createRoot()
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
        root.onBack()

        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
        root.onEquipmentReady()

        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
    }

    @Test
    fun onBack_onScan_isNoOp() {
        val root = createRoot()

        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
        assertEquals(0, root.stack.value.backStack.size)

        root.onBack()

        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
        assertEquals(0, root.stack.value.backStack.size)
    }

    @Test
    fun onBack_fromSummary_returnsToChatDescribe() {
        val root = createRoot()
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
        root.onOpenSummary()

        assertIs<FlowChild.Summary>(root.stack.value.active.instance)

        root.onBack()

        assertIs<FlowChild.ChatDescribe>(root.stack.value.active.instance)
    }

    @Test
    fun onBack_fromKnowledgeBase_returnsToPreviousScreen() {
        val root = createRoot()
        root.chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.Select(Assistant(id = 1, name = "Холодильники")),
        )
        root.onEquipmentReady()
        root.onOpenKnowledgeBase()

        assertIs<FlowChild.KnowledgeBase>(root.stack.value.active.instance)

        root.onBack()

        assertIs<FlowChild.ChatDescribe>(root.stack.value.active.instance)
    }

    @Test
    fun onCameraCancelled_returnsToScan() {
        val root = createRoot()
        root.onOpenCamera()

        assertIs<FlowChild.Camera>(root.stack.value.active.instance)

        root.onCameraCancelled()

        assertIs<FlowChild.Scan>(root.stack.value.active.instance)
    }

    private fun createRoot(): DefaultRootComponent {
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
                caseReportsRepository = LoggingCaseReportsRepository(),
            ),
            reportListStoreFactory = ReportListStoreFactory(
                storeFactory = storeFactory,
                caseReportsRepository = LoggingCaseReportsRepository(),
            ),
        )
        lifecycle.start()
        lifecycle.resume()
        return root
    }
}
