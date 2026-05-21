package pro.masterdoc.data.chat

import pro.masterdoc.data.chat.dto.OnyxChatMessageDto
import pro.masterdoc.domain.chat.ChatMessage
import pro.masterdoc.domain.chat.ChatMessageStatus
import pro.masterdoc.domain.chat.ChatRole

internal fun OnyxChatMessageDto.toDomain(status: ChatMessageStatus = ChatMessageStatus.Sent): ChatMessage? {
    val role = messageType.toChatRole() ?: return null
    if (message.isBlank()) return null
    return ChatMessage(
        id = messageId.toString(),
        role = role,
        content = message,
        createdAt = timeSent,
        status = status,
    )
}

internal fun String.toChatRole(): ChatRole? = when (lowercase()) {
    "user" -> ChatRole.User
    "assistant" -> ChatRole.Assistant
    else -> null
}
