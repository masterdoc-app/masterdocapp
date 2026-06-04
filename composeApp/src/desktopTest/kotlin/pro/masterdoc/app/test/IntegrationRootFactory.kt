package pro.masterdoc.app.test

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.data.assistant.AssistantsApi
import pro.masterdoc.data.assistant.HttpAssistantsRepository
import pro.masterdoc.data.casereport.CaseReportsApi
import pro.masterdoc.data.casereport.HttpCaseReportsRepository
import pro.masterdoc.data.chat.ChatApi
import pro.masterdoc.data.chat.HttpChatRepository
import pro.masterdoc.data.integrationApiConfig
import pro.masterdoc.presentation.chat.ChatStoreFactory
import pro.masterdoc.presentation.chat.DefaultChatComponent
import pro.masterdoc.presentation.equipment.EquipmentSelectionStoreFactory
import pro.masterdoc.presentation.report.ReportListStoreFactory
import pro.masterdoc.presentation.root.DefaultRootComponent
import pro.masterdoc.presentation.summary.SummaryStoreFactory

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
