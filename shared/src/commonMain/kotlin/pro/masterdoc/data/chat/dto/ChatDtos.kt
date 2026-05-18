package pro.masterdoc.data.chat.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val role: String,
    val content: String,
    @SerialName("createdAt")
    val createdAt: String? = null,
)

@Serializable
data class MessagesResponseDto(
    @SerialName("conversationId")
    val conversationId: String? = null,
    val messages: List<ChatMessageDto> = emptyList(),
)

@Serializable
data class SendMessageRequestDto(
    val content: String,
    @SerialName("conversationId")
    val conversationId: String? = null,
)

@Serializable
data class SendMessageResponseDto(
    @SerialName("conversationId")
    val conversationId: String,
    @SerialName("userMessage")
    val userMessage: ChatMessageDto,
    @SerialName("assistantMessage")
    val assistantMessage: ChatMessageDto,
)
