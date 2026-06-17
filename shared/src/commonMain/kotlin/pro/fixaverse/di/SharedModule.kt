package pro.fixaverse.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.dsl.module
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.assistant.AssistantsApi
import pro.fixaverse.data.assistant.AssistantsRepository
import pro.fixaverse.data.assistant.HttpAssistantsRepository
import pro.fixaverse.data.assistant.MockAssistantsRepository
import pro.fixaverse.data.citation.CitationsApi
import pro.fixaverse.data.chat.ChatApi
import pro.fixaverse.data.chat.ChatDataMode
import pro.fixaverse.data.chat.ChatRepository
import pro.fixaverse.data.chat.HttpChatRepository
import pro.fixaverse.data.chat.MockChatRepository
import pro.fixaverse.data.chat.defaultChatDataMode
import pro.fixaverse.data.config.ApiConfig
import pro.fixaverse.data.config.apiBaseUrl
import pro.fixaverse.presentation.chat.ChatComponent
import pro.fixaverse.presentation.chat.ChatStoreFactory
import pro.fixaverse.presentation.chat.DefaultChatComponent
import pro.fixaverse.presentation.equipment.EquipmentSelectionStoreFactory
import pro.fixaverse.presentation.root.DefaultRootComponent
import pro.fixaverse.presentation.root.RootComponent
import pro.fixaverse.data.casereport.CaseReportsApi
import pro.fixaverse.data.casereport.CaseReportsRepository
import pro.fixaverse.data.casereport.HttpCaseReportsRepository
import pro.fixaverse.data.casereport.LoggingCaseReportsRepository
import pro.fixaverse.data.voice.VoiceApi
import pro.fixaverse.presentation.report.ReportListStoreFactory
import pro.fixaverse.presentation.summary.SummaryStoreFactory

val sharedModule = module {
    single<StoreFactory> { DefaultStoreFactory() }
    single { HttpClientFactory().create() }
    single { ApiConfig(baseUrl = apiBaseUrl()) }

    factory { AssistantsApi(httpClient = get(), apiConfig = get()) }
    factory { ChatApi(httpClient = get(), apiConfig = get()) }
    factory { CitationsApi(httpClient = get(), apiConfig = get()) }
    factory { CaseReportsApi(httpClient = get(), apiConfig = get()) }
    factory { VoiceApi(httpClient = get(), apiConfig = get()) }

    single<AssistantsRepository> {
        when (defaultChatDataMode()) {
            ChatDataMode.Mock -> MockAssistantsRepository()
            ChatDataMode.Http -> HttpAssistantsRepository(api = get())
        }
    }

    single<ChatRepository> {
        when (defaultChatDataMode()) {
            ChatDataMode.Mock -> MockChatRepository()
            ChatDataMode.Http -> HttpChatRepository(api = get())
        }
    }

    factory { EquipmentSelectionStoreFactory(storeFactory = get(), repository = get()) }
    factory { ChatStoreFactory(storeFactory = get(), repository = get()) }
    single<CaseReportsRepository> {
        when (defaultChatDataMode()) {
            ChatDataMode.Mock -> LoggingCaseReportsRepository()
            ChatDataMode.Http -> HttpCaseReportsRepository(api = get())
        }
    }

    factory { SummaryStoreFactory(storeFactory = get(), caseReportsRepository = get()) }
    factory { ReportListStoreFactory(storeFactory = get(), caseReportsRepository = get()) }

    factory<ChatComponent> { params ->
        DefaultChatComponent(
            componentContext = params.get(),
            storeFactory = get(),
            equipmentStoreFactory = get(),
        )
    }

    factory<RootComponent> { params ->
        DefaultRootComponent(
            componentContext = params.get(),
            chatFactory = { chatContext ->
                get<ChatComponent> { org.koin.core.parameter.parametersOf(chatContext) }
            },
            summaryStoreFactory = get(),
            reportListStoreFactory = get(),
        )
    }
}
