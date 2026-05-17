package pro.masterdoc.presentation.search

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

class SearchStoreFactory(
    private val storeFactory: StoreFactory,
) {
    fun create(): SearchStore = object : SearchStore,
        com.arkivanov.mvikotlin.core.store.Store<SearchStore.Intent, SearchStore.State, Nothing> by storeFactory.create(
            name = "SearchStore",
            initialState = SearchStore.State(),
            executorFactory = ::SearchExecutor,
        ) {}
}

private class SearchExecutor :
    CoroutineExecutor<SearchStore.Intent, Nothing, SearchStore.State, Nothing, Nothing>()
