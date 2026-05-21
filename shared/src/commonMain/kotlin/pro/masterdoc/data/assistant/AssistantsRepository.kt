package pro.masterdoc.data.assistant

import pro.masterdoc.domain.assistant.Assistant

interface AssistantsRepository {
    suspend fun listAssistants(): Result<List<Assistant>>
}

class HttpAssistantsRepository(
    private val api: AssistantsApi,
) : AssistantsRepository {
    override suspend fun listAssistants(): Result<List<Assistant>> =
        runCatching { api.listAssistants() }
}

class MockAssistantsRepository : AssistantsRepository {
    override suspend fun listAssistants(): Result<List<Assistant>> = Result.success(
        listOf(
            Assistant(id = 1, name = "Холодильники"),
            Assistant(id = 2, name = "Стиралки"),
        ),
    )
}
