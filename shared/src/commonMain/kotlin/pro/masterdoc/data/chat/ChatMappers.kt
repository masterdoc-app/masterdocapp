package pro.masterdoc.data.chat

import pro.masterdoc.data.chat.dto.ChatMessageDto
import pro.masterdoc.domain.chat.ChatMessage
import pro.masterdoc.domain.chat.ChatMessageStatus
import pro.masterdoc.domain.chat.ChatRole
internal fun ChatMessageDto.toDomain(status: ChatMessageStatus = ChatMessageStatus.Sent): ChatMessage =
    ChatMessage(
        id = id,
        role = role.toChatRole(),
        content = content,
        createdAt = createdAt,
        status = status,
    )

internal fun String.toChatRole(): ChatRole = when (lowercase()) {
    "user" -> ChatRole.User
    "assistant" -> ChatRole.Assistant
    else -> ChatRole.Assistant
}

internal fun ChatRole.toApiRole(): String = when (this) {
    ChatRole.User -> "user"
    ChatRole.Assistant -> "assistant"
}
