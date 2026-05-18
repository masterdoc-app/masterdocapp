package pro.masterdoc.presentation.chat

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pro.masterdoc.data.chat.ChatException
import pro.masterdoc.data.chat.ChatRepository
import pro.masterdoc.domain.chat.ChatMessage
import pro.masterdoc.domain.chat.ChatMessageStatus
import pro.masterdoc.domain.chat.ChatRole
import kotlin.random.Random

class ChatStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: ChatRepository,
) {
    fun create(): ChatStore = object : ChatStore,
        com.arkivanov.mvikotlin.core.store.Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> by storeFactory.create(
            name = "ChatStore",
            initialState = ChatStore.State(),
            bootstrapper = SimpleBootstrapper(Action.LoadHistory),
            executorFactory = { ChatExecutor(repository) },
            reducer = ChatReducer,
        ) {}
}

private sealed interface Action {
    data object LoadHistory : Action
}

private sealed interface Msg {
    data class SetInput(val text: String) : Msg
    data class SetLoadingHistory(val loading: Boolean) : Msg
    data class SetSending(val sending: Boolean) : Msg
    data class SetError(val error: String?) : Msg
    data class HistoryLoaded(
        val conversationId: String?,
        val messages: List<ChatMessage>,
    ) : Msg
    data class AppendOptimisticUser(val message: ChatMessage) : Msg
    data class SendSucceeded(
        val conversationId: String,
        val tempUserId: String,
        val userMessage: ChatMessage,
        val assistantMessage: ChatMessage,
    ) : Msg
    data class SendFailed(val tempUserId: String, val error: String) : Msg
}

private object ChatReducer : Reducer<ChatStore.State, Msg> {
    override fun ChatStore.State.reduce(msg: Msg): ChatStore.State = when (msg) {
        is Msg.SetInput -> copy(input = msg.text, error = null)
        is Msg.SetLoadingHistory -> copy(isLoadingHistory = msg.loading)
        is Msg.SetSending -> copy(isSending = msg.sending)
        is Msg.SetError -> copy(error = msg.error)
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
        is Msg.SendSucceeded -> copy(
            conversationId = msg.conversationId,
            messages = messages
                .filterNot { it.id == msg.tempUserId }
                .plus(msg.userMessage)
                .plus(msg.assistantMessage),
            isSending = false,
            error = null,
        )
        is Msg.SendFailed -> copy(
            messages = messages.map { message ->
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
) : CoroutineExecutor<ChatStore.Intent, Action, ChatStore.State, Msg, ChatStore.Label>() {

    override fun executeAction(action: Action) {
        when (action) {
            Action.LoadHistory -> loadHistory()
        }
    }

    override fun executeIntent(intent: ChatStore.Intent) {
        when (intent) {
            is ChatStore.Intent.InputChanged -> dispatch(Msg.SetInput(intent.text))
            ChatStore.Intent.SendClicked -> sendMessage()
            ChatStore.Intent.RetryLoad,
            ChatStore.Intent.Refresh,
            -> loadHistory()
        }
    }

    private fun loadHistory() {
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
        val text = current.input.trim()
        if (text.isEmpty() || current.isSending) return

        val tempId = "local-${Random.nextLong()}"
        val optimistic = ChatMessage(
            id = tempId,
            role = ChatRole.User,
            content = text,
            status = ChatMessageStatus.Sending,
        )
        dispatch(Msg.AppendOptimisticUser(optimistic))

        scope.launch {
            repository.send(text, current.conversationId)
                .onSuccess { result ->
                    dispatch(
                        Msg.SendSucceeded(
                            conversationId = result.conversationId,
                            tempUserId = tempId,
                            userMessage = result.userMessage,
                            assistantMessage = result.assistantMessage,
                        ),
                    )
                    publish(ChatStore.Label.ScrollToBottom())
                }
                .onFailure { error ->
                    dispatch(Msg.SendFailed(tempUserId = tempId, error = error.toUserMessage()))
                }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ChatException -> userMessage
        else -> message ?: "Неизвестная ошибка"
    }
}
