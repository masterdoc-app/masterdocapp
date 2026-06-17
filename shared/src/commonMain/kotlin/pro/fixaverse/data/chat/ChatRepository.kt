package pro.fixaverse.data.chat

import pro.fixaverse.domain.chat.ChatMessage

data class ChatHistory(
    val conversationId: String?,
    val messages: List<ChatMessage>,
)

data class SendChatResult(
    val conversationId: String,
    val userMessage: ChatMessage,
    val assistantMessage: ChatMessage,
)

data class StreamingChatUpdate(
    val conversationId: String,
    val userMessage: ChatMessage,
    val assistantMessage: ChatMessage,
)

interface ChatRepository {
    suspend fun loadHistory(conversationId: String?): Result<ChatHistory>
    suspend fun send(
        text: String,
        conversationId: String?,
        personaId: Int,
        onStreamUpdate: (StreamingChatUpdate) -> Unit = {},
    ): Result<SendChatResult>
}

class ChatException(val userMessage: String) : Exception(userMessage)
