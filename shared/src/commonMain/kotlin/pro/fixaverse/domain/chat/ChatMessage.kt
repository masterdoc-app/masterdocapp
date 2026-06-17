package pro.fixaverse.domain.chat

enum class ChatRole {
    User,
    Assistant,
}

enum class ChatMessageStatus {
    Sent,
    Sending,
    Failed,
}

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val createdAt: String? = null,
    val status: ChatMessageStatus = ChatMessageStatus.Sent,
    val timeline: List<ChatTimelineStep> = emptyList(),
    val isStreaming: Boolean = false,
    /** Citation index (e.g. "1") → Onyx document id. */
    val citations: Map<String, String> = emptyMap(),
)
