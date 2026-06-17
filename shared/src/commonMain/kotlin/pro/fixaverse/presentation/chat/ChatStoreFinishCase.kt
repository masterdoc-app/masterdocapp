package pro.fixaverse.presentation.chat

import pro.fixaverse.domain.chat.ChatMessageStatus
import pro.fixaverse.domain.chat.ChatRole

/** True when chat REST stream is idle and at least one user turn has finished. */
fun ChatStore.State.canFinishCase(): Boolean {
    if (isSending || isLoadingHistory) return false
    if (!messages.any { it.role == ChatRole.User && it.status != ChatMessageStatus.Sending }) {
        return false
    }
    if (messages.any { it.isStreaming }) return false
    return true
}
