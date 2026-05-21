package pro.masterdoc.data.assistant

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import pro.masterdoc.data.assistant.dto.AssistantDto
import pro.masterdoc.data.config.ApiConfig
import pro.masterdoc.domain.assistant.Assistant

class AssistantsApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {
    suspend fun listAssistants(): List<Assistant> =
        httpClient.get("${apiConfig.baseUrl}/assistants")
            .body<List<AssistantDto>>()
            .map { Assistant(id = it.id, name = it.name) }
}
