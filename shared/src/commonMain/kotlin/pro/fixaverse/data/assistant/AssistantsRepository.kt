package pro.fixaverse.data.assistant

import pro.fixaverse.domain.assistant.Assistant

interface AssistantsRepository {
    suspend fun listAssistants(): Result<List<Assistant>>
    suspend fun detectAssistant(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
        candidateNames: List<String> = emptyList(),
        onProgress: suspend (String) -> Unit = {},
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
        candidateNames: List<String>,
        onProgress: suspend (String) -> Unit,
    ): Result<String?> = runCatching {
        api.detectAssistantStreaming(
            imageBytes = imageBytes,
            fileName = fileName,
            contentType = contentType,
            candidateNames = candidateNames,
            onProgress = onProgress,
        )
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
        candidateNames: List<String>,
        onProgress: suspend (String) -> Unit,
    ): Result<String?> {
        onProgress("Смотрим на фото…")
        return Result.success("Холодильники")
    }
}
