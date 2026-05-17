package pro.masterdoc.presentation.home

import com.arkivanov.decompose.ComponentContext

class DefaultHomeComponent(
    componentContext: ComponentContext,
    storeFactory: HomeStoreFactory,
) : HomeComponent, ComponentContext by componentContext {
    override val store: HomeStore = storeFactory.create()
}
