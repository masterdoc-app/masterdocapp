package pro.masterdoc.presentation.home

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

class HomeStoreFactory(
    private val storeFactory: StoreFactory,
) {
    fun create(): HomeStore = object : HomeStore,
        com.arkivanov.mvikotlin.core.store.Store<HomeStore.Intent, HomeStore.State, Nothing> by storeFactory.create(
            name = "HomeStore",
            initialState = HomeStore.State(),
            executorFactory = ::HomeExecutor,
        ) {}
}

private class HomeExecutor :
    CoroutineExecutor<HomeStore.Intent, Nothing, HomeStore.State, Nothing, Nothing>()
