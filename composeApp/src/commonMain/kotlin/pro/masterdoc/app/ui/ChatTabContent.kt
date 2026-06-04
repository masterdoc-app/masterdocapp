package pro.masterdoc.app.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyListState
import pro.masterdoc.app.ui.chat.ChatAssistantTimeline
import pro.masterdoc.app.ui.chat.masterdocChatInputKeys
import pro.masterdoc.app.ui.chat.ChatMarkdownText
import pro.masterdoc.app.ui.theme.AssistantMessageShape
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocLoadingIndicator
import pro.masterdoc.app.ui.theme.MasterdocMessageSurface
import pro.masterdoc.app.ui.theme.LiteFieldShape
import pro.masterdoc.app.ui.theme.UserMessageShape
import pro.masterdoc.domain.chat.ChatMessageStatus
import pro.masterdoc.domain.chat.ChatRole
import pro.masterdoc.presentation.chat.ChatComponent
import pro.masterdoc.presentation.chat.ChatStore
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore

@Composable
fun ChatTabContent(component: ChatComponent) {
    val equipmentState by component.equipmentStore.states.collectAsState(
        initial = component.equipmentStore.state,
    )
    val selected = equipmentState.selectedAssistant

    if (selected == null) {
        EquipmentSelectionContent(component.equipmentStore)
        return
    }

    LaunchedEffect(selected.id) {
        component.store.accept(ChatStore.Intent.BindAssistant(selected.id, selected.name))
    }

    ChatConversationContent(
        component = component,
        assistantName = selected.name,
        onChangeEquipment = {
            component.store.accept(ChatStore.Intent.ResetSession)
            component.equipmentStore.accept(EquipmentSelectionStore.Intent.ClearSelection)
        },
    )
}

@Composable
private fun ChatConversationContent(
    component: ChatComponent,
    assistantName: String,
    onChangeEquipment: () -> Unit,
) {
    val state by component.store.states.collectAsState(initial = component.store.state)
    val listState = rememberLazyListState()

    val lastMessage = state.messages.lastOrNull()
    LaunchedEffect(
        state.messages.size,
        lastMessage?.id,
        lastMessage?.content?.length,
        lastMessage?.timeline?.size,
        lastMessage?.isStreaming,
    ) {
        listState.scrollToLastMessage(state.messages.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = onChangeEquipment,
                label = {
                    Text(
                        assistantName,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            )
            TextButton(onClick = onChangeEquipment) {
                Text(
                    "Сменить",
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        if (state.isLoadingHistory) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                MasterdocLoadingIndicator()
            }
        }

        state.error?.let { error ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { component.store.accept(ChatStore.Intent.RetryLoad) },
                ) {
                    Text("Повторить")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { "${it.id}:${it.role}" }) { message ->
                ChatMessageBubble(message = message)
            }
        }

        ChatInputBar(
            input = state.input,
            isSending = state.isSending,
            onInputChange = { component.store.accept(ChatStore.Intent.InputChanged(it)) },
            onSend = { component.store.accept(ChatStore.Intent.SendClicked) },
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: pro.masterdoc.domain.chat.ChatMessage,
) {
    val isUser = message.role == ChatRole.User
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) UserMessageShape else AssistantMessageShape

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        MasterdocMessageSurface(
            isUser = isUser,
            shape = shape,
        ) {
            Column(modifier = Modifier.padding(MasterdocDimens.Space12)) {
                if (!isUser) {
                    ChatAssistantTimeline(
                        steps = message.timeline,
                        isStreaming = message.isStreaming,
                    )
                }
                if (message.content.isNotBlank() || isUser) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        ChatMarkdownText(content = message.content)
                    }
                } else if (message.isStreaming) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MasterdocLoadingIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            text = "Ожидаю ответ…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (message.status == ChatMessageStatus.Failed) {
                    Text(
                        text = "Не отправлено",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MasterdocDimens.Space12),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .masterdocChatInputKeys(
                        input = input,
                        isSending = isSending,
                        onInputChange = onInputChange,
                        onSend = onSend,
                    ),
                placeholder = {
                    Text(
                        "Сообщение…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                maxLines = 4,
                enabled = !isSending,
                shape = LiteFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            Button(
                onClick = onSend,
                enabled = input.isNotBlank() && !isSending,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(
                    if (isSending) "…" else "→",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/** [animateScrollToItem] can crash on Wasm when layout lags behind state updates. */
private suspend fun LazyListState.scrollToLastMessage(messageCount: Int) {
    if (messageCount <= 0) return
    val targetIndex = messageCount - 1
    repeat(8) {
        val laidOut = layoutInfo.totalItemsCount
        if (laidOut > targetIndex) {
            runCatching { scrollToItem(targetIndex) }
            return
        }
        delay(32)
    }
}
