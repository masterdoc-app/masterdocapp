package pro.masterdoc.data.chat

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException

class HttpChatRepository(
    private val api: ChatApi,
) : ChatRepository {
    override suspend fun loadHistory(conversationId: String?): Result<ChatHistory> =
        runCatching {
            val response = api.getMessages(conversationId)
            ChatHistory(
                conversationId = response.conversationId ?: conversationId,
                messages = response.messages.map { it.toDomain() },
            )
        }.mapError(::toUserMessage)

    override suspend fun send(text: String, conversationId: String?): Result<SendChatResult> =
        runCatching {
            val response = api.sendMessage(content = text.trim(), conversationId = conversationId)
            SendChatResult(
                conversationId = response.conversationId,
                userMessage = response.userMessage.toDomain(),
                assistantMessage = response.assistantMessage.toDomain(),
            )
        }.mapError(::toUserMessage)

    private fun toUserMessage(throwable: Throwable): String = when (throwable) {
        is ClientRequestException -> "Ошибка запроса (${throwable.response.status.value})"
        is ServerResponseException -> "Ошибка сервера (${throwable.response.status.value})"
        else -> if (throwable.message.orEmpty().contains("connection", ignoreCase = true)) {
            "Нет соединения с сервером"
        } else {
            throwable.message ?: "Неизвестная ошибка"
        }
    }
}

private inline fun <T> Result<T>.mapError(transform: (Throwable) -> String): Result<T> =
    fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(ChatException(transform(it))) },
    )
