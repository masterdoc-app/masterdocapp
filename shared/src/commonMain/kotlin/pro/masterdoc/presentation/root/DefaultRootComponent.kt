package pro.masterdoc.presentation.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import pro.masterdoc.domain.chat.ChatRole
import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.chat.ChatStore
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore
import pro.masterdoc.presentation.summary.SummaryStore
import pro.masterdoc.presentation.summary.SummaryStoreFactory

class DefaultRootComponent(
    componentContext: ComponentContext,
    chatFactory: (ComponentContext) -> ChatComponent,
    private val summaryStoreFactory: SummaryStoreFactory,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<FlowConfig>()

    override val stack: Value<ChildStack<FlowConfig, FlowChild>> =
        childStack(
            source = navigation,
            initialStack = { listOf(FlowConfig.Scan) },
            saveStack = { null },
            restoreStack = { null },
            handleBackButton = true,
        ) { config, _ ->
            when (config) {
                FlowConfig.Scan -> FlowChild.Scan
                FlowConfig.Camera -> FlowChild.Camera
                FlowConfig.ChatDescribe -> FlowChild.ChatDescribe
                FlowConfig.ChatGuide -> FlowChild.ChatGuide
                FlowConfig.Summary -> FlowChild.Summary
            }
        }

    override val chat: ChatComponent by lazy { chatFactory(this) }
    override val summary: SummaryStore by lazy { summaryStoreFactory.create() }

    @OptIn(DelicateDecomposeApi::class)
    override fun onEquipmentReady() {
        val selected = chat.equipmentStore.state.selectedAssistant ?: return
        chat.store.accept(ChatStore.Intent.BindAssistant(selected.id, selected.name))
        navigation.push(FlowConfig.ChatDescribe)
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun onOpenCamera() {
        navigation.push(FlowConfig.Camera)
    }

    override fun onCameraPhotoResult(imageBytes: ByteArray, fileName: String, contentType: String) {
        navigation.pop()
        chat.equipmentStore.accept(
            EquipmentSelectionStore.Intent.DetectFromPhoto(
                imageBytes = imageBytes,
                fileName = fileName,
                contentType = contentType,
            ),
        )
    }

    override fun onCameraCancelled() {
        navigation.pop()
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun onOpenChatGuide() {
        navigation.push(FlowConfig.ChatGuide)
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun onOpenSummary() {
        prefillSummaryFromSession()
        navigation.push(FlowConfig.Summary)
    }

    private fun prefillSummaryFromSession() {
        val equipment = chat.equipmentStore.state.selectedAssistant
        val chatState = chat.store.state
        summary.accept(
            SummaryStore.Intent.BindSessionContext(
                assistantId = equipment?.id,
                assistantName = equipment?.name,
                conversationId = chatState.conversationId,
            ),
        )
        val userText = chatState.messages
            .filter { it.role == ChatRole.User }
            .joinToString("\n") { it.content.trim() }
            .trim()
        val assistantText = chatState.messages
            .filter { it.role == ChatRole.Assistant }
            .lastOrNull()
            ?.content
            ?.trim()
            .orEmpty()
        summary.accept(
            SummaryStore.Intent.PrefillFromSession(
                reported = userText,
                resolved = assistantText,
            ),
        )
    }

    override fun onBack() {
        if (stack.value.backStack.isNotEmpty()) {
            navigation.pop()
        }
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun onFinishAndRestart() {
        onResetSession()
        navigation.popTo(0)
    }

    override fun onResetSession() {
        chat.store.accept(ChatStore.Intent.ResetSession)
        chat.equipmentStore.accept(EquipmentSelectionStore.Intent.ClearSelection)
        summary.accept(SummaryStore.Intent.Reset)
    }
}
