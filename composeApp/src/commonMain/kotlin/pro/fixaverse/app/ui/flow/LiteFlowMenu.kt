package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.fixaverse.app.ui.theme.LiteOptionShape
import pro.fixaverse.app.ui.theme.FixaverseDimens
import pro.fixaverse.presentation.root.FlowChild
import pro.fixaverse.presentation.root.RootComponent

@Composable
fun rememberLiteFlowMenuState(root: RootComponent): LiteFlowMenuState {
    var expanded by remember { mutableStateOf(false) }
    val stack by root.stack.subscribeAsState()
    val active = stack.active.instance
    val equipmentState by root.chat.equipmentStore.states.collectAsState(
        initial = root.chat.equipmentStore.state,
    )
    val hasAssistant = equipmentState.selectedAssistant != null
    return LiteFlowMenuState(
        expanded = expanded,
        onDismiss = { expanded = false },
        onOpen = { expanded = true },
        onOpenKnowledgeBase = {
            expanded = false
            root.onOpenKnowledgeBase()
        },
        canOpenKnowledgeBase = hasAssistant,
        showMenu = active !is FlowChild.KnowledgeBase,
    )
}

data class LiteFlowMenuState(
    val expanded: Boolean,
    val onDismiss: () -> Unit,
    val onOpen: () -> Unit,
    val onOpenKnowledgeBase: () -> Unit,
    val canOpenKnowledgeBase: Boolean,
    /** Three-dot menu in the top bar (hidden on the knowledge-base screen itself). */
    val showMenu: Boolean,
)

/** Three-dot trigger + [DropdownMenu] anchored to the top-bar button. */
@Composable
fun LiteFlowMenuAnchor(
    state: LiteFlowMenuState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LiteFlowMenuDots(onClick = state.onOpen)
        DropdownMenu(
            expanded = state.expanded,
            onDismissRequest = state.onDismiss,
            shape = LiteOptionShape,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            DropdownMenuItem(
                text = { Text("База знаний") },
                onClick = state.onOpenKnowledgeBase,
                enabled = state.canOpenKnowledgeBase,
            )
        }
    }
}

@Composable
fun liteFlowMenuAnchor(menu: LiteFlowMenuState): (@Composable () -> Unit)? =
    if (menu.showMenu) {
        { LiteFlowMenuAnchor(menu) }
    } else {
        null
    }

@Composable
private fun LiteFlowMenuDots(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(FixaverseDimens.Space4),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
            )
        }
    }
}
