package pro.masterdoc.data.assistant

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.json.Json
import pro.masterdoc.data.HttpClientFactory
import pro.masterdoc.data.assistant.dto.AssistantDto
import pro.masterdoc.data.assistant.dto.DetectAssistantResponse
import pro.masterdoc.data.assistant.dto.DetectStreamResultDto
import pro.masterdoc.data.chat.OnyxStreamAccumulator
import pro.masterdoc.data.config.ApiConfig
import pro.masterdoc.domain.assistant.Assistant
import pro.masterdoc.domain.assistant.DetectAnswerMatcher

class AssistantsApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
    streamHttpClient: HttpClient = HttpClientFactory().createRaw(),
) {
    private val streamClient = streamHttpClient
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
        if (!response.status.isSuccess()) {
            val detail = response.bodyAsText().take(300).ifBlank { "нет текста ошибки" }
            error("Сервер ${response.status.value}: $detail")
        }
        val name = response.body<DetectAssistantResponse>().assistant
        println("[masterdoc detect] response assistant=$name")
        return name
    }

    suspend fun detectAssistantStreaming(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
        candidateNames: List<String>,
        onProgress: suspend (String) -> Unit,
    ): String? {
        println(
            "[masterdoc detect] POST ${apiConfig.baseUrl}/assistants/detect/stream " +
                "bytes=${imageBytes.size} file=$fileName type=$contentType",
        )
        onProgress("Отправляем фото…")
        val progress = DetectProgressTracker()
        val accumulator = OnyxStreamAccumulator()
        var parsedAssistant: String? = null

        streamClient.preparePost("${apiConfig.baseUrl}/assistants/detect/stream") {
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
        }.execute { response ->
            if (!response.status.isSuccess()) {
                error("Сервер ${response.status.value}: ${response.bodyAsText().take(300)}")
            }
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.isBlank()) continue
                runCatching { json.decodeFromString<DetectStreamResultDto>(line) }
                    .getOrNull()
                    ?.detectResult
                    ?.assistant
                    ?.let {
                        parsedAssistant = it
                        continue
                    }
                accumulator.onLine(line)
                progress.onLine(line)?.let { onProgress(it) }
            }
        }

        parsedAssistant?.let {
            println("[masterdoc detect] stream response assistant=$it")
            return it
        }
        val answer = accumulator.snapshot().answer
        val matched = DetectAnswerMatcher.match(answer, candidateNames)
        println("[masterdoc detect] stream parsed assistant=$matched answer=${answer.take(120)}")
        return matched
    }

    private companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }
}
