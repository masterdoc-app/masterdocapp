package pro.masterdoc.presentation.chat

import com.arkivanov.decompose.ComponentContext

class DefaultChatComponent(
    componentContext: ComponentContext,
    storeFactory: ChatStoreFactory,
) : ChatComponent, ComponentContext by componentContext {
    override val store: ChatStore = storeFactory.create()
}
