package pro.fixaverse.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pro.fixaverse.app.ui.flow.CameraScreenContent
import pro.fixaverse.app.ui.flow.ChatDescribeScreenContent
import pro.fixaverse.app.ui.flow.ScanScreenContent
import pro.fixaverse.app.ui.flow.KnowledgeBaseScreenContent
import pro.fixaverse.app.ui.flow.SummaryScreenContent
import pro.fixaverse.presentation.root.FlowChild
import pro.fixaverse.presentation.root.RootComponent

@Composable
fun RootContent(component: RootComponent) {
    val stack by component.stack.subscribeAsState()
    val active = stack.active.instance

    when (active) {
        FlowChild.Scan -> ScanScreenContent(
            root = component,
            equipmentStore = component.chat.equipmentStore,
            modifier = Modifier.fillMaxSize(),
        )
        FlowChild.Camera -> CameraScreenContent(
            root = component,
            modifier = Modifier.fillMaxSize(),
        )
        FlowChild.ChatDescribe -> ChatDescribeScreenContent(
            root = component,
            chat = component.chat,
        )
        FlowChild.Summary -> SummaryScreenContent(
            root = component,
            summary = component.summary,
        )
        FlowChild.KnowledgeBase -> KnowledgeBaseScreenContent(
            root = component,
            reportList = component.reportList,
        )
    }
}
