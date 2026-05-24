package pro.masterdoc.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.root.TabChild

@Composable
fun RootContent(component: RootComponent) {
    val pages by component.pages.subscribeAsState()
    val activeChild = pages.items.getOrNull(pages.selectedIndex)?.instance

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                NavigationBarItem(
                    selected = pages.selectedIndex == 0,
                    onClick = { component.onTabSelected(0) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                    label = { Text("Чат", style = MaterialTheme.typography.labelMedium) },
                    colors = navColors,
                )
                NavigationBarItem(
                    selected = pages.selectedIndex == 1,
                    onClick = { component.onTabSelected(1) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Поиск", style = MaterialTheme.typography.labelMedium) },
                    colors = navColors,
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
                is TabChild.Chat -> ChatTabContent(activeChild.component)
                is TabChild.Search -> SearchTabContent(activeChild.component)
                null -> Unit
            }
        }
    }
}
