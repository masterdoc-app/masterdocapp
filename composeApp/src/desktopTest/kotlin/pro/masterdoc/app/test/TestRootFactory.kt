package pro.masterdoc.app.test

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.masterdoc.data.assistant.MockAssistantsRepository
import pro.masterdoc.data.chat.MockChatRepository
import pro.masterdoc.presentation.chat.ChatStoreFactory
import pro.masterdoc.presentation.chat.DefaultChatComponent
import pro.masterdoc.presentation.equipment.EquipmentSelectionStoreFactory
import pro.masterdoc.presentation.root.DefaultRootComponent
import pro.masterdoc.data.casereport.LoggingCaseReportsRepository
import pro.masterdoc.presentation.summary.SummaryStoreFactory

object TestRootFactory {
    fun create(): DefaultRootComponent {
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
        )
        lifecycle.start()
        lifecycle.resume()
        return root
    }
}
