package pro.masterdoc.domain.case

data class CaseReportSubmitRequest(
    val assistantId: Int,
    val conversationId: String?,
    val result: String,
    val transcript: List<TranscriptTurn>,
)
