package pro.fixaverse.presentation.search

import com.arkivanov.mvikotlin.core.store.Store

interface SearchStore : Store<SearchStore.Intent, SearchStore.State, Nothing> {
    data class State(
        val title: String = "Поиск",
        val subtitle: String = "Мануалы и документация",
    )

    sealed interface Intent
}
