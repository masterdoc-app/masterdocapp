package pro.fixaverse.presentation.search

import com.arkivanov.decompose.ComponentContext

class DefaultSearchComponent(
    componentContext: ComponentContext,
    storeFactory: SearchStoreFactory,
) : SearchComponent, ComponentContext by componentContext {
    override val store: SearchStore = storeFactory.create()
}
