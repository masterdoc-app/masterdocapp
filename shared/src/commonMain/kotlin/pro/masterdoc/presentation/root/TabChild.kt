package pro.masterdoc.presentation.root

import pro.masterdoc.presentation.home.HomeComponent
import pro.masterdoc.presentation.search.SearchComponent

sealed interface TabChild {
    data class Home(val component: HomeComponent) : TabChild
    data class Search(val component: SearchComponent) : TabChild
}
