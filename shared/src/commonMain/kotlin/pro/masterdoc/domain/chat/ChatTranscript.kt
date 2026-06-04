package pro.masterdoc.domain.chat

import pro.masterdoc.domain.case.TranscriptTurn

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
