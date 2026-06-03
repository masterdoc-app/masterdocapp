package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.LiteChip
import pro.masterdoc.app.ui.theme.LiteFieldShape
import pro.masterdoc.app.ui.theme.LiteMicHeroButton
import pro.masterdoc.app.ui.theme.LiteOptionCard
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocPrimaryButton
import pro.masterdoc.domain.chat.ChatRole
import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.chat.ChatStore
import pro.masterdoc.presentation.root.RootComponent

@Composable
fun ChatGuideScreenContent(
    root: RootComponent,
    chat: ChatComponent,
) {
    val menu = rememberLiteFlowMenuState(root)
    val equipmentState by chat.equipmentStore.states.collectAsState(initial = chat.equipmentStore.state)
    val chatState by chat.store.states.collectAsState(initial = chat.store.state)
    val assistantName = equipmentState.selectedAssistant?.name ?: "Станция"
    val lastAssistant = chatState.messages.lastOrNull { it.role == ChatRole.Assistant }

    LiteFlowDropdownMenu(menu)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        LiteAppHead(
            title = assistantName,
            subtitle = "Подключено · подсказки",
            onBack = root::onBack,
            onMenuClick = menu.onOpen,
            subtitleLive = true,
        )

        Column(modifier = Modifier.weight(1f)) {
            ChatConversationPane(
                component = chat,
                modifier = Modifier.weight(1f),
                showAssistantWho = true,
            )

            if (lastAssistant != null && lastAssistant.content.isNotBlank()) {
                Column(
                    modifier = Modifier.padding(horizontal = MasterdocDimens.Space14),
                    verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space7),
                ) {
                    LiteOptionCard(
                        letter = "A",
                        body = "Сброс — удерживайте START 3 сек, затем CLEAR.",
                    )
                    LiteOptionCard(
                        letter = "B",
                        body = "Проверьте датчик приближения на направляющей.",
                    )
                    LiteChip(text = "Поиск в базе · ${chatState.messages.size} сообщ.", flare = true)
                }
            }
        }

        GuideInputBar(
            input = chatState.input,
            isSending = chatState.isSending,
            onInputChange = { chat.store.accept(ChatStore.Intent.InputChanged(it)) },
            onSend = { chat.store.accept(ChatStore.Intent.SendClicked) },
        )

        MasterdocPrimaryButton(
            text = "Завершить кейс",
            onClick = root::onOpenSummary,
            modifier = Modifier.padding(
                horizontal = MasterdocDimens.Space14,
                vertical = MasterdocDimens.Space12,
            ),
        )
    }
}

@Composable
private fun GuideInputBar(
    input: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MasterdocDimens.Space10),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && input.isNotBlank() && !isSending) {
                            onSend()
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text("Опишите, что происходит") },
                maxLines = 2,
                shape = LiteFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            LiteMicHeroButton(
                onClick = {
                    if (input.isNotBlank() && !isSending) onSend()
                },
                enabled = !isSending,
                size = MasterdocDimens.MicHeroSize,
            )
        }
    }
}
