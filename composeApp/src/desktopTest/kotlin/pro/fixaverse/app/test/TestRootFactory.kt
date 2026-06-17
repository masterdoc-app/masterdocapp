package pro.fixaverse.app.test

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.fixaverse.data.assistant.MockAssistantsRepository
import pro.fixaverse.data.chat.MockChatRepository
import pro.fixaverse.presentation.chat.ChatStoreFactory
import pro.fixaverse.presentation.chat.DefaultChatComponent
import pro.fixaverse.presentation.equipment.EquipmentSelectionStoreFactory
import pro.fixaverse.presentation.root.DefaultRootComponent
import pro.fixaverse.data.casereport.CaseReportsRepository
import pro.fixaverse.data.casereport.LoggingCaseReportsRepository
import pro.fixaverse.presentation.report.ReportListStoreFactory
import pro.fixaverse.presentation.summary.SummaryStoreFactory

object TestRootFactory {
    fun create(caseReportsRepository: CaseReportsRepository = LoggingCaseReportsRepository()): DefaultRootComponent {
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
                caseReportsRepository = caseReportsRepository,
            ),
            reportListStoreFactory = ReportListStoreFactory(
                storeFactory = storeFactory,
                caseReportsRepository = caseReportsRepository,
            ),
        )
        lifecycle.start()
        lifecycle.resume()
        return root
    }
}
