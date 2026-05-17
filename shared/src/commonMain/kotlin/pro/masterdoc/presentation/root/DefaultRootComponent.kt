package pro.masterdoc.presentation.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val homeFactory: (ComponentContext) -> TabChild.Home,
    private val searchFactory: (ComponentContext) -> TabChild.Search,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = PagesNavigation<TabConfig>()

    override val pages: Value<ChildPages<TabConfig, TabChild>> = childPages(
        source = navigation,
        serializer = TabConfig.serializer(),
        initialPages = {
            Pages(
                items = listOf(TabConfig.Home, TabConfig.Search),
                selectedIndex = 0,
            )
        },
        childFactory = { config, childContext ->
            when (config) {
                TabConfig.Home -> homeFactory(childContext)
                TabConfig.Search -> searchFactory(childContext)
            }
        },
    )

    override fun onTabSelected(index: Int) {
        navigation.select(index)
    }
}
