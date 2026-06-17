package pro.fixaverse.domain.chat

import pro.fixaverse.domain.case.TranscriptTurn

/** Pairs user questions with the following assistant reply for case-report storage. */
fun List<ChatMessage>.toTranscriptTurns(): List<TranscriptTurn> {
    val turns = mutableListOf<TranscriptTurn>()
    var pendingAsk: String? = null
    for (message in this) {
        when (message.role) {
            ChatRole.User -> {
                if (pendingAsk != null) {
                    turns.add(TranscriptTurn(ask = pendingAsk, answer = ""))
                }
                pendingAsk = message.content.trim()
            }
            ChatRole.Assistant -> {
                turns.add(
                    TranscriptTurn(
                        ask = pendingAsk.orEmpty(),
                        answer = message.content.trim(),
                    ),
                )
                pendingAsk = null
            }
        }
    }
    if (pendingAsk != null) {
        turns.add(TranscriptTurn(ask = pendingAsk, answer = ""))
    }
    return turns.filter { it.ask.isNotBlank() || it.answer.isNotBlank() }
}

/** Expands stored transcript turns into ordered user/assistant messages for read-only display. */
fun List<TranscriptTurn>.toChatMessages(): List<ChatMessage> {
    val messages = mutableListOf<ChatMessage>()
    var index = 0
    for (turn in this) {
        if (turn.ask.isNotBlank()) {
            messages.add(
                ChatMessage(
                    id = "transcript-$index",
                    role = ChatRole.User,
                    content = turn.ask.trim(),
                ),
            )
            index++
        }
        if (turn.answer.isNotBlank()) {
            messages.add(
                ChatMessage(
                    id = "transcript-$index",
                    role = ChatRole.Assistant,
                    content = turn.answer.trim(),
                ),
            )
            index++
        }
    }
    return messages
}
