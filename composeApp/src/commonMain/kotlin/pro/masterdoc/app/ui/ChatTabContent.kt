package pro.masterdoc.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
                label = { Text(assistantName) },
            )
            TextButton(onClick = onChangeEquipment) {
                Text("Сменить")
            }
        }

        if (state.isLoadingHistory) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
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
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bubbleColor,
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    ChatAssistantTimeline(
                        steps = message.timeline,
                        isStreaming = message.isStreaming,
                    )
                }
                if (message.content.isNotBlank() || isUser) {
                    Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
                } else if (message.isStreaming) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Сообщение…") },
            maxLines = 4,
            enabled = !isSending,
        )
        TextButton(
            onClick = onSend,
            enabled = input.isNotBlank() && !isSending,
        ) {
            Text(if (isSending) "…" else "Отправить")
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
