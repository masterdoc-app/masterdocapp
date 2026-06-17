package pro.fixaverse.presentation.chat

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pro.fixaverse.data.chat.ChatException
import pro.fixaverse.data.chat.ChatRepository
import pro.fixaverse.data.chat.StreamingChatUpdate
import pro.fixaverse.domain.chat.ChatMessage
import pro.fixaverse.domain.chat.ChatMessageStatus
import pro.fixaverse.domain.chat.ChatRole
import pro.fixaverse.domain.chat.ChatTimelineStep
import pro.fixaverse.domain.chat.TimelineStepKind
import pro.fixaverse.domain.chat.TimelineStepStatus
import kotlin.random.Random

class ChatStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: ChatRepository,
) {
    fun create(): ChatStore = object : ChatStore,
        com.arkivanov.mvikotlin.core.store.Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> by storeFactory.create(
            name = "ChatStore",
            initialState = ChatStore.State(),
            executorFactory = { ChatExecutor(repository) },
            reducer = ChatReducer,
        ) {}
}

private sealed interface Msg {
    data class SetInput(val text: String) : Msg
    data class SetLoadingHistory(val loading: Boolean) : Msg
    data class SetSending(val sending: Boolean) : Msg
    data class SetError(val error: String?) : Msg
    data class AssistantBound(val personaId: Int, val name: String) : Msg
    data object SessionReset : Msg
    data class HistoryLoaded(
        val conversationId: String?,
        val messages: List<ChatMessage>,
    ) : Msg
    data class AppendOptimisticUser(val message: ChatMessage) : Msg
    data class StartSend(
        val userMessage: ChatMessage,
        val streamingAssistantId: String,
        val conversationId: String?,
    ) : Msg
    data class StreamUpdate(
        val conversationId: String,
        val tempUserId: String,
        val streamingAssistantId: String,
        val update: StreamingChatUpdate,
    ) : Msg
    data class SendSucceeded(
        val conversationId: String,
        val tempUserId: String,
        val userMessage: ChatMessage,
        val assistantMessage: ChatMessage,
    ) : Msg
    data class SendFailed(val tempUserId: String, val streamingAssistantId: String?, val error: String) : Msg
}

private object ChatReducer : Reducer<ChatStore.State, Msg> {
    override fun ChatStore.State.reduce(msg: Msg): ChatStore.State = when (msg) {
        is Msg.SetInput -> copy(input = msg.text, error = null)
        is Msg.SetLoadingHistory -> copy(isLoadingHistory = msg.loading)
        is Msg.SetSending -> copy(isSending = msg.sending)
        is Msg.SetError -> copy(error = msg.error)
        is Msg.AssistantBound -> copy(
            personaId = msg.personaId,
            assistantName = msg.name,
            conversationId = null,
            messages = emptyList(),
            error = null,
        )
        Msg.SessionReset -> copy(
            conversationId = null,
            messages = emptyList(),
            error = null,
        )
        is Msg.HistoryLoaded -> copy(
            conversationId = msg.conversationId,
            messages = msg.messages,
            isLoadingHistory = false,
            error = null,
        )
        is Msg.AppendOptimisticUser -> copy(
            messages = messages + msg.message,
            input = "",
            isSending = true,
            error = null,
        )
        is Msg.StartSend -> copy(
            conversationId = msg.conversationId ?: conversationId,
            messages = messages + msg.userMessage + pendingAssistantMessage(msg.streamingAssistantId),
            input = "",
            isSending = true,
            error = null,
        )
        is Msg.StreamUpdate -> copy(
            conversationId = msg.conversationId,
            messages = messages
                .filterNot { it.id == msg.streamingAssistantId || it.id == msg.tempUserId }
                .plus(
                    msg.update.userMessage.copy(
                        id = msg.tempUserId,
                        status = ChatMessageStatus.Sending,
                    ),
                )
                .plus(msg.update.assistantMessage.copy(id = msg.streamingAssistantId)),
            isSending = true,
            error = null,
        )
        is Msg.SendSucceeded -> copy(
            conversationId = msg.conversationId,
            messages = messages
                .filterNot { it.id == msg.tempUserId }
                .filterNot { it.id.startsWith("streaming-") }
                .plus(msg.userMessage)
                .plus(msg.assistantMessage),
            isSending = false,
            error = null,
        )
        is Msg.SendFailed -> copy(
            messages = messages
                .filterNot { it.id.startsWith("streaming-") }
                .map { message ->
                    if (message.id == msg.tempUserId) {
                        message.copy(status = ChatMessageStatus.Failed)
                    } else {
                        message
                    }
                },
            isSending = false,
            error = msg.error,
        )
    }
}

private class ChatExecutor(
    private val repository: ChatRepository,
) : CoroutineExecutor<ChatStore.Intent, Nothing, ChatStore.State, Msg, ChatStore.Label>() {

    override fun executeIntent(intent: ChatStore.Intent) {
        when (intent) {
            is ChatStore.Intent.InputChanged -> dispatch(Msg.SetInput(intent.text))
            ChatStore.Intent.SendClicked -> sendMessage()
            ChatStore.Intent.RetryLoad,
            ChatStore.Intent.Refresh,
            -> loadHistory()
            is ChatStore.Intent.BindAssistant -> {
                dispatch(Msg.AssistantBound(intent.id, intent.name))
                loadHistory()
            }
            ChatStore.Intent.ResetSession -> dispatch(Msg.SessionReset)
        }
    }

    private fun loadHistory() {
        if (state().personaId == null) return
        val conversationId = state().conversationId
        dispatch(Msg.SetLoadingHistory(true))
        dispatch(Msg.SetError(null))
        scope.launch {
            repository.loadHistory(conversationId)
                .onSuccess { history ->
                    dispatch(
                        Msg.HistoryLoaded(
                            conversationId = history.conversationId,
                            messages = history.messages,
                        ),
                    )
                }
                .onFailure { error ->
                    dispatch(Msg.SetLoadingHistory(false))
                    dispatch(Msg.SetError(error.toUserMessage()))
                }
        }
    }

    private fun sendMessage() {
        val current = state()
        val personaId = current.personaId ?: return
        val text = current.input.trim()
        if (text.isEmpty() || current.isSending) return

        val tempId = "local-${Random.nextLong()}"
        val streamingAssistantId = "streaming-$tempId"
        val optimistic = ChatMessage(
            id = tempId,
            role = ChatRole.User,
            content = text,
            status = ChatMessageStatus.Sending,
        )
        dispatch(
            Msg.StartSend(
                userMessage = optimistic,
                streamingAssistantId = streamingAssistantId,
                conversationId = current.conversationId,
            ),
        )

        scope.launch {
            repository.send(
                text = text,
                conversationId = current.conversationId,
                personaId = personaId,
                onStreamUpdate = { update ->
                    dispatch(
                        Msg.StreamUpdate(
                            conversationId = update.conversationId,
                            tempUserId = tempId,
                            streamingAssistantId = streamingAssistantId,
                            update = update.copy(
                                assistantMessage = update.assistantMessage.copy(id = streamingAssistantId),
                            ),
                        ),
                    )
                },
            )
                .onSuccess { result ->
                    dispatch(
                        Msg.SendSucceeded(
                            conversationId = result.conversationId,
                            tempUserId = tempId,
                            userMessage = result.userMessage,
                            assistantMessage = result.assistantMessage,
                        ),
                    )
                }
                .onFailure { error ->
                    dispatch(
                        Msg.SendFailed(
                            tempUserId = tempId,
                            streamingAssistantId = streamingAssistantId,
                            error = error.toUserMessage(),
                        ),
                    )
                }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ChatException -> userMessage
        else -> message ?: "Неизвестная ошибка"
    }
}

private fun pendingAssistantMessage(id: String): ChatMessage = ChatMessage(
    id = id,
    role = ChatRole.Assistant,
    content = "",
    isStreaming = true,
    timeline = listOf(
        ChatTimelineStep(
            id = "search",
            label = "Поиск в документах",
            kind = TimelineStepKind.Search,
            status = TimelineStepStatus.Active,
        ),
    ),
)
