package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.ui.chat.masterdocChatInputKeys
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.masterdocChatInputFieldColors
import pro.masterdoc.app.ui.theme.LiteFieldShape
import pro.masterdoc.app.ui.theme.LiteListenPanel
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocPrimaryButton
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.app.ui.theme.MasterdocSecondaryButton
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
    var isListening by remember { mutableStateOf(false) }

    val stationTitle = equipmentState.selectedAssistant?.name?.let { "Masterdoc · $it" } ?: "Masterdoc"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        LiteAppHead(
            title = stationTitle,
            subtitle = if (isListening) "Голос · активен" else "Чат · подсказки Onyx",
            onBack = root::onBack,
            menuAnchor = liteFlowMenuAnchor(menu),
            subtitleLive = isListening,
        )

        ChatConversationPane(
            component = chat,
            modifier = Modifier.weight(1f),
        )

        if (textMode) {
            OutlinedTextField(
                value = chatState.input,
                onValueChange = { chat.store.accept(ChatStore.Intent.InputChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MasterdocDimens.Space14)
                    .testTag(MasterdocTestTags.CHAT_DESCRIBE_INPUT)
                    .masterdocChatInputKeys(
                        input = chatState.input,
                        isSending = chatState.isSending,
                        onInputChange = { chat.store.accept(ChatStore.Intent.InputChanged(it)) },
                        onSend = { chat.store.accept(ChatStore.Intent.SendClicked) },
                    ),
                placeholder = { Text("Опишите, что происходит…") },
                minLines = 2,
                maxLines = 4,
                shape = LiteFieldShape,
                colors = masterdocChatInputFieldColors(),
            )
            MasterdocPrimaryButton(
                text = if (chatState.isSending) "Отправляем…" else "Отправить",
                onClick = { chat.store.accept(ChatStore.Intent.SendClicked) },
                modifier = Modifier
                    .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space8)
                    .testTag(MasterdocTestTags.CHAT_DESCRIBE_SEND),
                enabled = chatState.input.isNotBlank() && !chatState.isSending,
            )
        } else {
            LiteListenPanel(
                label = "Слушаю · шумоподавление",
                hint = if (isListening) "Распознаю…" else "Нажмите микрофон",
                onMicClick = {
                    isListening = !isListening
                    if (!isListening && chatState.input.isBlank()) {
                        textMode = true
                    }
                },
                isListening = isListening,
            )
        }

        MasterdocSecondaryButton(
            text = if (textMode) "Голосом" else "Ввести текстом",
            onClick = { textMode = !textMode },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MasterdocDimens.Space14),
            fillMaxWidth = true,
            enabled = !(textMode && chatState.isSending),
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
            )
        }
    }
}
