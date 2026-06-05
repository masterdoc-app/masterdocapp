package pro.masterdoc.app.ui.flow

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
import pro.masterdoc.app.ui.chat.MasterdocChatInputField
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocPrimaryButton
import pro.masterdoc.app.ui.theme.MasterdocSecondaryButton
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.data.voice.VoiceApi
import pro.masterdoc.platform.PlatformCapabilities
import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.chat.ChatStore
import pro.masterdoc.presentation.chat.canFinishCase
import pro.masterdoc.presentation.root.RootComponent

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

    val stationTitle = equipmentState.selectedAssistant?.name?.let { "Masterdoc · $it" } ?: "Masterdoc"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        LiteAppHead(
            title = stationTitle,
            subtitle = when {
                voiceBusy -> "Голос · распознаю"
                else -> "Чат · подсказки Onyx"
            },
            onBack = root::onBack,
            menuAnchor = liteFlowMenuAnchor(menu),
            subtitleLive = voiceBusy,
        )

        ChatConversationPane(
            component = chat,
            modifier = Modifier.weight(1f),
        )

        if (textMode) {
            MasterdocChatInputField(
                value = chatState.input,
                onValueChange = { chat.store.accept(ChatStore.Intent.InputChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MasterdocDimens.Space14)
                    .testTag(MasterdocTestTags.CHAT_DESCRIBE_INPUT),
                isSending = chatState.isSending,
                onSend = { chat.store.accept(ChatStore.Intent.SendClicked) },
                placeholder = "Опишите, что происходит…",
                minLines = 2,
                maxLines = 4,
            )
            MasterdocPrimaryButton(
                text = if (chatState.isSending) "Отправляем…" else "Отправить",
                onClick = { chat.store.accept(ChatStore.Intent.SendClicked) },
                modifier = Modifier
                    .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space8)
                    .testTag(MasterdocTestTags.CHAT_DESCRIBE_SEND),
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

        MasterdocSecondaryButton(
            text = if (textMode) "Голосом" else "Ввести текстом",
            onClick = { textMode = !textMode },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MasterdocDimens.Space14),
            fillMaxWidth = true,
            enabled = !(textMode && chatState.isSending) && !voiceBusy,
        )

        if (canFinishCase) {
            MasterdocPrimaryButton(
                text = "Завершить кейс",
                onClick = {
                    if (chatState.input.isNotBlank()) {
                        chat.store.accept(ChatStore.Intent.SendClicked)
                    }
                    root.onOpenSummary()
                },
                modifier = Modifier
                    .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space12)
                    .testTag(MasterdocTestTags.CHAT_FINISH_CASE),
                enabled = !voiceBusy,
            )
        }
    }
}
