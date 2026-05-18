package pro.masterdoc.data.chat

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pro.masterdoc.data.chat.dto.MessagesResponseDto
import pro.masterdoc.data.chat.dto.SendMessageRequestDto
import pro.masterdoc.data.chat.dto.SendMessageResponseDto
import pro.masterdoc.data.config.ApiConfig

class ChatApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {
    suspend fun getMessages(conversationId: String?): MessagesResponseDto =
        httpClient.get("${apiConfig.baseUrl}/v1/chat/messages") {
            conversationId?.let { parameter("conversationId", it) }
        }.body()

    suspend fun sendMessage(
        content: String,
        conversationId: String?,
    ): SendMessageResponseDto =
        httpClient.post("${apiConfig.baseUrl}/v1/chat/messages") {
            setBody(SendMessageRequestDto(content = content, conversationId = conversationId))
        }.body()
}
