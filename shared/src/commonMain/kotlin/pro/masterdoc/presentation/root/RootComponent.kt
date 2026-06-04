package pro.masterdoc.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.report.ReportListStore
import pro.masterdoc.presentation.summary.SummaryStore

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
    fun onOpenFrequentIssues()
    fun onBack()
    fun onFinishAndRestart()
    fun onResetSession()
}
