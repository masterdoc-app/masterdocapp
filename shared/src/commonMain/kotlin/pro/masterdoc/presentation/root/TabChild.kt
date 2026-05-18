package pro.masterdoc.presentation.root

import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.search.SearchComponent

sealed interface TabChild {
    data class Chat(val component: ChatComponent) : TabChild
    data class Search(val component: SearchComponent) : TabChild
}
