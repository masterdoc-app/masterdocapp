package pro.masterdoc.domain.case

/**
 * Outcome of a completed case — will be sent to the API once the endpoint exists.
 */
data class CaseOutcomePayload(
    val assistantId: Int?,
    val assistantName: String?,
    val conversationId: String?,
    val reported: String,
    val resolved: String,
    val cause: String,
    val nextSteps: String,
)
