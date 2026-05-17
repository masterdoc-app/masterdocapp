package pro.masterdoc.presentation.root

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value

interface RootComponent {
    val pages: Value<ChildPages<TabConfig, TabChild>>

    fun onTabSelected(index: Int)
}
