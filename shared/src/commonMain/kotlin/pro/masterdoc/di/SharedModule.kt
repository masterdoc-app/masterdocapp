package pro.masterdoc.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import pro.masterdoc.data.HttpClientFactory
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
import pro.masterdoc.presentation.root.DefaultRootComponent
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.root.TabChild
import pro.masterdoc.presentation.search.DefaultSearchComponent
import pro.masterdoc.presentation.search.SearchComponent
import pro.masterdoc.presentation.search.SearchStoreFactory

val sharedModule = module {
    single<StoreFactory> { DefaultStoreFactory() }
    single { HttpClientFactory().create() }
    single { ApiConfig(baseUrl = apiBaseUrl()) }

    factory { ChatApi(httpClient = get(), apiConfig = get()) }
    single<ChatRepository> {
        when (defaultChatDataMode()) {
            ChatDataMode.Mock -> MockChatRepository()
            ChatDataMode.Http -> HttpChatRepository(api = get())
        }
    }

    factoryOf(::ChatStoreFactory)
    factoryOf(::SearchStoreFactory)

    factory<ChatComponent> { params ->
        DefaultChatComponent(
            componentContext = params.get(),
            storeFactory = get(),
        )
    }

    factory<SearchComponent> { params ->
        DefaultSearchComponent(
            componentContext = params.get(),
            storeFactory = get(),
        )
    }

    factory<RootComponent> { params ->
        DefaultRootComponent(
            componentContext = params.get(),
            chatFactory = { childContext ->
                TabChild.Chat(get { org.koin.core.parameter.parametersOf(childContext) })
            },
            searchFactory = { childContext ->
                TabChild.Search(get { org.koin.core.parameter.parametersOf(childContext) })
            },
        )
    }
}
