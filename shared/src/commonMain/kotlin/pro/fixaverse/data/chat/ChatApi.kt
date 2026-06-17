package pro.fixaverse.data.chat

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pro.fixaverse.data.chat.dto.CreateChatSessionRequestDto
import pro.fixaverse.data.chat.dto.CreateChatSessionResponseDto
import pro.fixaverse.data.chat.dto.GetChatSessionResponseDto
import pro.fixaverse.data.chat.dto.SendOnyxChatMessageRequestDto
import pro.fixaverse.data.chat.dto.SendOnyxChatMessageResponseDto
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.config.ApiConfig

class ChatApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
    streamHttpClient: HttpClient = HttpClientFactory().createRaw(),
) {
    private val streamClient = streamHttpClient
    suspend fun createChatSession(personaId: Int): CreateChatSessionResponseDto =
        httpClient.post("${apiConfig.baseUrl}/chat/sessions") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CreateChatSessionRequestDto(personaId = personaId)))
        }.decodeBody()

    suspend fun getChatSession(sessionId: String): GetChatSessionResponseDto =
        httpClient.get("${apiConfig.baseUrl}/chat/sessions/$sessionId").decodeBody()

    suspend fun sendChatMessageStream(
        message: String,
        sessionId: String,
        onLine: suspend (String) -> Unit,
    ) {
        streamClient.preparePost("${apiConfig.baseUrl}/chat/sessions/$sessionId/messages") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SendOnyxChatMessageRequestDto(
                        message = message,
                        chatSessionId = sessionId,
                        stream = true,
                    ),
                ),
            )
        }.execute { response ->
            if (response.status.value !in 200..299) {
                error("Chat API ${response.status.value}: ${response.bodyAsText().take(300)}")
            }
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.isNotBlank()) {
                    onLine(line)
                }
            }
        }
    }

    suspend fun sendChatMessage(
        message: String,
        sessionId: String,
    ): SendOnyxChatMessageResponseDto {
        val raw = httpClient.post("${apiConfig.baseUrl}/chat/sessions/$sessionId/messages") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SendOnyxChatMessageRequestDto(
                        message = message,
                        chatSessionId = sessionId,
                        stream = false,
                    ),
                ),
            )
        }.bodyAsText()
        return OnyxSendResponseParser.parse(raw)
    }

    private suspend inline fun <reified T> io.ktor.client.statement.HttpResponse.decodeBody(): T =
        json.decodeFromString(bodyAsText())

    private companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
