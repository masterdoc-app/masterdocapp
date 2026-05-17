package pro.masterdoc.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.presentation.home.DefaultHomeComponent
import pro.masterdoc.presentation.home.HomeComponent
import pro.masterdoc.presentation.home.HomeStoreFactory
import pro.masterdoc.presentation.root.DefaultRootComponent
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.root.TabChild
import pro.masterdoc.presentation.search.DefaultSearchComponent
import pro.masterdoc.presentation.search.SearchComponent
import pro.masterdoc.presentation.search.SearchStoreFactory

val sharedModule = module {
    single<StoreFactory> { DefaultStoreFactory() }
    single { HttpClientFactory().create() }

    factoryOf(::HomeStoreFactory)
    factoryOf(::SearchStoreFactory)

    factory<HomeComponent> { params ->
        DefaultHomeComponent(
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
            homeFactory = { childContext ->
                TabChild.Home(get { org.koin.core.parameter.parametersOf(childContext) })
            },
            searchFactory = { childContext ->
                TabChild.Search(get { org.koin.core.parameter.parametersOf(childContext) })
            },
        )
    }
}
