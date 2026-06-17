package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import pro.fixaverse.app.ui.chat.ChatAssistantTimeline
import pro.fixaverse.app.ui.chat.ChatMarkdownText
import pro.fixaverse.app.ui.theme.AssistantMessageShape
import pro.fixaverse.app.ui.theme.AppBranding
import pro.fixaverse.app.ui.theme.FixaverseDimens
import pro.fixaverse.app.ui.theme.FixaverseLoadingIndicator
import pro.fixaverse.app.ui.theme.FixaverseMessageSurface
import pro.fixaverse.app.ui.theme.UserMessageShape
import pro.fixaverse.app.ui.theme.fixaverseConvoBackground
import pro.fixaverse.domain.chat.ChatMessageStatus
import pro.fixaverse.domain.chat.ChatRole
import pro.fixaverse.presentation.chat.ChatComponent
import pro.fixaverse.presentation.chat.ChatStore

@Composable
fun ChatConversationPane(
    component: ChatComponent,
    modifier: Modifier = Modifier,
    showAssistantWho: Boolean = true,
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
        scrollChatToEnd(listState, state.messages.size)
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoadingHistory) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                FixaverseLoadingIndicator()
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
                TextButton(onClick = { component.store.accept(ChatStore.Intent.RetryLoad) }) {
                    Text("Повторить")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .fixaverseConvoBackground()
                .padding(horizontal = FixaverseDimens.Space14, vertical = FixaverseDimens.Space10),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space10),
        ) {
            if (state.messages.isEmpty()) {
                item("greeting") {
                    ChatGreetingBubble(showWho = showAssistantWho)
                }
            }
            items(state.messages, key = { "${it.id}:${it.role}" }) { message ->
                FlowChatMessageBubble(message = message, showWho = showAssistantWho)
            }
        }
    }
}

@Composable
private fun ChatGreetingBubble(showWho: Boolean) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        FixaverseMessageSurface(isUser = false, shape = AssistantMessageShape) {
            Column(modifier = Modifier.padding(FixaverseDimens.Space12)) {
                if (showWho) {
                    Text(
                        text = AppBranding.ASSISTANT_LABEL,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(
                    text = "Привет. Что со станцией?",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FlowChatMessageBubble(
    message: pro.fixaverse.domain.chat.ChatMessage,
    showWho: Boolean,
) {
    val isUser = message.role == ChatRole.User
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) UserMessageShape else AssistantMessageShape

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        FixaverseMessageSurface(isUser = isUser, shape = shape) {
            Column(modifier = Modifier.padding(FixaverseDimens.Space12)) {
                if (!isUser) {
                    if (showWho && message.content.isNotBlank()) {
                        Text(
                            text = AppBranding.ASSISTANT_LABEL,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = FixaverseDimens.Space5),
                        )
                    }
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
                        FixaverseLoadingIndicator(modifier = Modifier.size(16.dp))
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
                    )
                }
            }
        }
    }
}

private suspend fun scrollChatToEnd(listState: androidx.compose.foundation.lazy.LazyListState, messageCount: Int) {
    if (messageCount <= 0) return
    val targetIndex = messageCount
    repeat(8) {
        val laidOut = listState.layoutInfo.totalItemsCount
        if (laidOut > targetIndex) {
            runCatching { listState.scrollToItem(targetIndex) }
            return
        }
        delay(32)
    }
}
