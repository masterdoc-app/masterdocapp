package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.arkivanov.mvikotlin.extensions.coroutines.states
import org.koin.mp.KoinPlatformTools
import pro.fixaverse.app.ui.chat.FixaverseChatInputField
import pro.fixaverse.app.ui.theme.LiteAppHead
import pro.fixaverse.app.ui.theme.AppBranding
import pro.fixaverse.app.ui.theme.FixaverseDimens
import pro.fixaverse.app.ui.theme.FixaversePrimaryButton
import pro.fixaverse.app.ui.theme.FixaverseSecondaryButton
import pro.fixaverse.app.ui.theme.FixaverseTestTags
import pro.fixaverse.data.voice.VoiceApi
import pro.fixaverse.platform.PlatformCapabilities
import pro.fixaverse.presentation.chat.ChatComponent
import pro.fixaverse.presentation.chat.ChatStore
import pro.fixaverse.presentation.chat.canFinishCase
import pro.fixaverse.presentation.root.RootComponent

@Composable
fun ChatDescribeScreenContent(
    root: RootComponent,
    chat: ChatComponent,
) {
    val equipmentState by chat.equipmentStore.states.collectAsState(initial = chat.equipmentStore.state)
    val chatState by chat.store.states.collectAsState(initial = chat.store.state)
    val canFinishCase = chatState.canFinishCase()
    val menu = rememberLiteFlowMenuState(root)
    var textMode by remember { mutableStateOf(false) }
    val voiceApi = remember {
        KoinPlatformTools.defaultContext().getOrNull()?.get<VoiceApi>()
    }
    var voiceBusy by remember { mutableStateOf(false) }

    val stationTitle = AppBranding.screenTitle(equipmentState.selectedAssistant?.name)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        LiteAppHead(
            title = stationTitle,
            subtitle = if (voiceBusy) "Голос · распознаю" else "",
            onBack = root::onBack,
            menuAnchor = liteFlowMenuAnchor(menu),
            subtitleLive = voiceBusy,
        )

        ChatConversationPane(
            component = chat,
            modifier = Modifier.weight(1f),
        )

        if (textMode) {
            FixaverseChatInputField(
                value = chatState.input,
                onValueChange = { chat.store.accept(ChatStore.Intent.InputChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FixaverseDimens.Space14)
                    .testTag(FixaverseTestTags.CHAT_DESCRIBE_INPUT),
                isSending = chatState.isSending,
                onSend = { chat.store.accept(ChatStore.Intent.SendClicked) },
                placeholder = "Опишите, что происходит…",
                minLines = 2,
                maxLines = 4,
            )
            FixaversePrimaryButton(
                text = if (chatState.isSending) "Отправляем…" else "Отправить",
                onClick = { chat.store.accept(ChatStore.Intent.SendClicked) },
                modifier = Modifier
                    .padding(horizontal = FixaverseDimens.Space14, vertical = FixaverseDimens.Space8)
                    .testTag(FixaverseTestTags.CHAT_DESCRIBE_SEND),
                enabled = chatState.input.isNotBlank() && !chatState.isSending,
            )
        } else if (PlatformCapabilities.supportsMicrophone) {
            ChatDescribeVoicePanel(
                voiceApi = voiceApi,
                onTranscript = { text ->
                    chat.store.accept(ChatStore.Intent.InputChanged(text))
                    chat.store.accept(ChatStore.Intent.SendClicked)
                },
                onBusyChanged = { voiceBusy = it },
                onError = { err ->
                    if (err != null) textMode = true
                },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            ChatDescribeVoicePanel(
                voiceApi = null,
                onTranscript = {},
                onBusyChanged = {},
                onError = { textMode = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FixaverseSecondaryButton(
            text = if (textMode) "Голосом" else "Ввести текстом",
            onClick = { textMode = !textMode },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FixaverseDimens.Space14),
            fillMaxWidth = true,
            enabled = !(textMode && chatState.isSending) && !voiceBusy,
        )

        if (canFinishCase) {
            FixaversePrimaryButton(
                text = "Завершить кейс",
                onClick = {
                    if (chatState.input.isNotBlank()) {
                        chat.store.accept(ChatStore.Intent.SendClicked)
                    }
                    root.onOpenSummary()
                },
                modifier = Modifier
                    .padding(horizontal = FixaverseDimens.Space14, vertical = FixaverseDimens.Space12)
                    .testTag(FixaverseTestTags.CHAT_FINISH_CASE),
                enabled = !voiceBusy,
            )
        }
    }
}
