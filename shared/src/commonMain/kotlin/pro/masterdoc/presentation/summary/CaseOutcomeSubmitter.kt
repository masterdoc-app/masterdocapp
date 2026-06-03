package pro.masterdoc.presentation.summary

import pro.masterdoc.domain.case.CaseOutcomePayload

/**
 * Sends completed case outcomes to the backend (HTTP wiring comes later).
 */
interface CaseOutcomeSubmitter {
    suspend fun submit(payload: CaseOutcomePayload): Result<Unit>
}

/**
 * Dev stub — logs payload until POST /v1/cases/outcome exists.
 */
class LoggingCaseOutcomeSubmitter : CaseOutcomeSubmitter {
    override suspend fun submit(payload: CaseOutcomePayload): Result<Unit> {
        println(
            "[masterdoc case] submit pending API: " +
                "assistant=${payload.assistantName}(${payload.assistantId}) " +
                "conversation=${payload.conversationId} " +
                "reported=${payload.reported.take(80)}… " +
                "resolved=${payload.resolved.take(80)}…",
        )
        return Result.success(Unit)
    }
}
