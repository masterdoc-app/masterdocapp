package pro.masterdoc.data.assistant

import pro.masterdoc.domain.assistant.Assistant

interface AssistantsRepository {
    suspend fun listAssistants(): Result<List<Assistant>>
    suspend fun detectAssistant(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Result<String?>
}

class HttpAssistantsRepository(
    private val api: AssistantsApi,
) : AssistantsRepository {
    override suspend fun listAssistants(): Result<List<Assistant>> =
        runCatching { api.listAssistants() }

    override suspend fun detectAssistant(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Result<String?> = runCatching {
        api.detectAssistant(imageBytes, fileName, contentType)
    }
}

class MockAssistantsRepository : AssistantsRepository {
    override suspend fun listAssistants(): Result<List<Assistant>> = Result.success(
        listOf(
            Assistant(id = 1, name = "Холодильники"),
            Assistant(id = 2, name = "Стиралки"),
        ),
    )

    override suspend fun detectAssistant(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Result<String?> = Result.success("Холодильники")
}
