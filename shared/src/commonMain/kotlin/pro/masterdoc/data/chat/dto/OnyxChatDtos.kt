package pro.masterdoc.data.chat.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateChatSessionRequestDto(
    @SerialName("persona_id")
    val personaId: Int = 0,
)

@Serializable
data class CreateChatSessionResponseDto(
    @SerialName("chat_session_id")
    val chatSessionId: String,
)

@Serializable
data class OnyxChatMessageDto(
    @SerialName("message_id")
    val messageId: Int,
    @SerialName("message_type")
    val messageType: String,
    val message: String,
    @SerialName("time_sent")
    val timeSent: String? = null,
)

@Serializable
data class GetChatSessionResponseDto(
    @SerialName("chat_session_id")
    val chatSessionId: String,
    val messages: List<OnyxChatMessageDto> = emptyList(),
)

@Serializable
data class SendOnyxChatMessageRequestDto(
    val message: String,
    @SerialName("chat_session_id")
    val chatSessionId: String,
    val stream: Boolean = false,
)

@Serializable
data class SendOnyxChatMessageResponseDto(
    val answer: String = "",
    @SerialName("message_id")
    val messageId: Int? = null,
    @SerialName("chat_session_id")
    val chatSessionId: String? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null,
)
