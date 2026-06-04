package pro.masterdoc.domain.case

data class CaseReport(
    val id: String,
    val createdAt: String,
    val assistantId: Int,
    val conversationId: String?,
    val result: String,
    val transcript: List<TranscriptTurn>,
)

data class PaginatedCaseReports(
    val items: List<CaseReport>,
    val page: Int,
    val size: Int,
    val total: Int,
    val hasMore: Boolean,
)
