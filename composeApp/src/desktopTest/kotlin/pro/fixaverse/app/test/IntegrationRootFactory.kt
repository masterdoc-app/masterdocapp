package pro.fixaverse.app.test

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.assistant.AssistantsApi
import pro.fixaverse.data.assistant.HttpAssistantsRepository
import pro.fixaverse.data.casereport.CaseReportsApi
import pro.fixaverse.data.casereport.HttpCaseReportsRepository
import pro.fixaverse.data.chat.ChatApi
import pro.fixaverse.data.chat.HttpChatRepository
import pro.fixaverse.data.integrationApiConfig
import pro.fixaverse.presentation.chat.ChatStoreFactory
import pro.fixaverse.presentation.chat.DefaultChatComponent
import pro.fixaverse.presentation.equipment.EquipmentSelectionStoreFactory
import pro.fixaverse.presentation.report.ReportListStoreFactory
import pro.fixaverse.presentation.root.DefaultRootComponent
import pro.fixaverse.presentation.summary.SummaryStoreFactory

/** Root with HTTP repositories for E2E against production/staging API. */
object IntegrationRootFactory {
    fun create(): DefaultRootComponent {
        val lifecycle = LifecycleRegistry()
        val storeFactory = DefaultStoreFactory()
        val httpClient = HttpClientFactory().create()
        val apiConfig = integrationApiConfig()
        val assistantsApi = AssistantsApi(httpClient, apiConfig)
        val chatApi = ChatApi(httpClient, apiConfig)
        val reportsApi = CaseReportsApi(httpClient, apiConfig)
        val equipmentStoreFactory = EquipmentSelectionStoreFactory(
            storeFactory = storeFactory,
            repository = HttpAssistantsRepository(assistantsApi),
        )
        val chatStoreFactory = ChatStoreFactory(
            storeFactory = storeFactory,
            repository = HttpChatRepository(chatApi),
        )
        val caseReportsRepository = HttpCaseReportsRepository(reportsApi)
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
