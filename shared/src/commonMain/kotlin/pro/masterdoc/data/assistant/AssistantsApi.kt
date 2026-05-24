package pro.masterdoc.data.assistant

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import pro.masterdoc.data.assistant.dto.AssistantDto
import pro.masterdoc.data.assistant.dto.DetectAssistantResponse
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

    suspend fun detectAssistant(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): String? {
        println(
            "[masterdoc detect] POST ${apiConfig.baseUrl}/assistants/detect " +
                "bytes=${imageBytes.size} file=$fileName type=$contentType",
        )
        val response = httpClient.post("${apiConfig.baseUrl}/assistants/detect") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image",
                            imageBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            },
                        )
                    },
                ),
            )
        }
        val name = response.body<DetectAssistantResponse>().assistant
        println("[masterdoc detect] response assistant=$name")
        return name
    }
}
