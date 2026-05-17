package pro.masterdoc.presentation.home

import com.arkivanov.mvikotlin.core.store.Store

interface HomeStore : Store<HomeStore.Intent, HomeStore.State, Nothing> {
    data class State(
        val title: String = "Главная",
        val subtitle: String = "Masterdoc",
    )

    sealed interface Intent
}
