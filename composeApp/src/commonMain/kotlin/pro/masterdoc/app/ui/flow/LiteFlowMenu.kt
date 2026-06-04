package pro.masterdoc.app.ui.flow

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pro.masterdoc.presentation.root.FlowChild
import pro.masterdoc.presentation.root.RootComponent

@Composable
fun rememberLiteFlowMenuState(
    root: RootComponent,
    canFinishCase: Boolean = false,
): LiteFlowMenuState {
    var expanded by remember { mutableStateOf(false) }
    val stack by root.stack.subscribeAsState()
    val active = stack.active.instance
    return LiteFlowMenuState(
        expanded = expanded,
        onDismiss = { expanded = false },
        onOpen = { expanded = true },
        onChangeEquipment = {
            expanded = false
            root.onResetSession()
            root.onFinishAndRestart()
        },
        onFinishCase = {
            expanded = false
            if (active !is FlowChild.Summary) {
                root.onOpenSummary()
            }
        },
        onRestart = {
            expanded = false
            root.onFinishAndRestart()
        },
        showFinish = active is FlowChild.ChatDescribe && canFinishCase,
    )
}

data class LiteFlowMenuState(
    val expanded: Boolean,
    val onDismiss: () -> Unit,
    val onOpen: () -> Unit,
    val onChangeEquipment: () -> Unit,
    val onFinishCase: () -> Unit,
    val onRestart: () -> Unit,
    val showFinish: Boolean,
)

@Composable
fun LiteFlowDropdownMenu(state: LiteFlowMenuState) {
    DropdownMenu(
        expanded = state.expanded,
        onDismissRequest = state.onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text("Сменить оборудование") },
            onClick = state.onChangeEquipment,
        )
        if (state.showFinish) {
            DropdownMenuItem(
                text = { Text("Завершить кейс") },
                onClick = state.onFinishCase,
            )
        }
        DropdownMenuItem(
            text = { Text("Начать заново") },
            onClick = state.onRestart,
        )
    }
}
