package pro.fixaverse.presentation.chat

import com.arkivanov.mvikotlin.core.store.Store
import pro.fixaverse.domain.chat.ChatMessage

interface ChatStore : Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> {

    data class State(
        val messages: List<ChatMessage> = emptyList(),
        val input: String = "",
        val isLoadingHistory: Boolean = false,
        val isSending: Boolean = false,
        val error: String? = null,
        val conversationId: String? = null,
        val personaId: Int? = null,
        val assistantName: String? = null,
    )

    sealed interface Intent {
        data class InputChanged(val text: String) : Intent
        data object SendClicked : Intent
        data object RetryLoad : Intent
        data object Refresh : Intent
        data class BindAssistant(val id: Int, val name: String) : Intent
        data object ResetSession : Intent
    }

    sealed interface Label {
        data class ScrollToBottom(val animate: Boolean = true) : Label
    }
}
