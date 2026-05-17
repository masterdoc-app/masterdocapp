package pro.masterdoc.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.root.TabChild

@Composable
fun RootContent(component: RootComponent) {
    val pages by component.pages.subscribeAsState()
    val activeChild = pages.items.getOrNull(pages.selectedIndex)?.instance

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pages.selectedIndex == 0,
                    onClick = { component.onTabSelected(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Главная") },
                )
                NavigationBarItem(
                    selected = pages.selectedIndex == 1,
                    onClick = { component.onTabSelected(1) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Поиск") },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (activeChild) {
                is TabChild.Home -> HomeTabContent(activeChild.component)
                is TabChild.Search -> SearchTabContent(activeChild.component)
                null -> Unit
            }
        }
    }
}
