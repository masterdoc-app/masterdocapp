package pro.fixaverse.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pro.fixaverse.presentation.chat.ChatComponent
import pro.fixaverse.presentation.report.ReportListStore
import pro.fixaverse.presentation.summary.SummaryStore

interface RootComponent {
    val stack: Value<ChildStack<FlowConfig, FlowChild>>
    val chat: ChatComponent
    val summary: SummaryStore
    val reportList: ReportListStore

    fun onEquipmentReady()
    fun onOpenCamera()
    fun onCameraPhotoResult(imageBytes: ByteArray, fileName: String, contentType: String)
    fun onCameraCancelled()
    fun onOpenSummary()
    fun onOpenKnowledgeBase()
    fun onBack()
    fun onFinishAndRestart()
    fun onResetSession()
}
