package pro.masterdoc.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.dsl.module
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.data.assistant.AssistantsApi
import pro.masterdoc.data.assistant.AssistantsRepository
import pro.masterdoc.data.assistant.HttpAssistantsRepository
import pro.masterdoc.data.assistant.MockAssistantsRepository
import pro.masterdoc.data.chat.ChatApi
import pro.masterdoc.data.chat.ChatDataMode
import pro.masterdoc.data.chat.ChatRepository
import pro.masterdoc.data.chat.HttpChatRepository
import pro.masterdoc.data.chat.MockChatRepository
import pro.masterdoc.data.chat.defaultChatDataMode
import pro.masterdoc.data.config.ApiConfig
import pro.masterdoc.data.config.apiBaseUrl
import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.chat.ChatStoreFactory
import pro.masterdoc.presentation.chat.DefaultChatComponent
import pro.masterdoc.presentation.equipment.EquipmentSelectionStoreFactory
import pro.masterdoc.presentation.root.DefaultRootComponent
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.summary.SummaryStoreFactory

val sharedModule = module {
    single<StoreFactory> { DefaultStoreFactory() }
    single { HttpClientFactory().create() }
    single { ApiConfig(baseUrl = apiBaseUrl()) }

    factory { AssistantsApi(httpClient = get(), apiConfig = get()) }
    factory { ChatApi(httpClient = get(), apiConfig = get()) }

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
    factory { SummaryStoreFactory(storeFactory = get()) }

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
        )
    }
}
