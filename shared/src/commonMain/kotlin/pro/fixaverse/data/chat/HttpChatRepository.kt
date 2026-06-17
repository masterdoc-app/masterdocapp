package pro.fixaverse.data.chat

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import pro.fixaverse.domain.chat.ChatMessage
import pro.fixaverse.domain.chat.ChatMessageStatus
import pro.fixaverse.domain.chat.ChatRole
import pro.fixaverse.domain.chat.TimelineStepStatus

class HttpChatRepository(
    private val api: ChatApi,
) : ChatRepository {
    override suspend fun loadHistory(conversationId: String?): Result<ChatHistory> =
        runCatching {
            if (conversationId.isNullOrBlank()) {
                return@runCatching ChatHistory(conversationId = null, messages = emptyList())
            }
            val session = api.getChatSession(conversationId)
            ChatHistory(
                conversationId = session.chatSessionId,
                messages = session.messages.mapNotNull { it.toDomain() },
            )
        }.mapError(::toUserMessage)

    override suspend fun send(
        text: String,
        conversationId: String?,
        personaId: Int,
        onStreamUpdate: (StreamingChatUpdate) -> Unit,
    ): Result<SendChatResult> = runCatching {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            error("Пустое сообщение")
        }

        val sessionId = conversationId?.takeIf { it.isNotBlank() }
            ?: api.createChatSession(personaId).chatSessionId

        val userMessage = ChatMessage(
            id = "user-${sessionId}-${trimmed.hashCode()}",
            role = ChatRole.User,
            content = trimmed,
            status = ChatMessageStatus.Sent,
        )
        val assistantId = "assistant-${sessionId}-${trimmed.hashCode()}"
        val accumulator = OnyxStreamAccumulator()

        api.sendChatMessageStream(trimmed, sessionId) { line ->
            accumulator.onLine(line)
            val snap = accumulator.snapshot()
            onStreamUpdate(
                StreamingChatUpdate(
                    conversationId = sessionId,
                    userMessage = userMessage,
                    assistantMessage = ChatMessage(
                        id = assistantId,
                        role = ChatRole.Assistant,
                        content = snap.answer,
                        timeline = snap.timeline,
                        isStreaming = true,
                    ),
                ),
            )
        }

        val final = accumulator.snapshot()
        if (final.answer.isBlank()) {
            error("Пустой ответ от Onyx")
        }

        SendChatResult(
            conversationId = sessionId,
            userMessage = userMessage,
            assistantMessage = ChatMessage(
                id = assistantId,
                role = ChatRole.Assistant,
                content = final.answer,
                timeline = final.timeline.map { step ->
                    if (step.status == TimelineStepStatus.Active) {
                        step.copy(status = TimelineStepStatus.Done)
                    } else {
                        step
                    }
                },
                isStreaming = false,
            ),
        )
    }.mapError(::toUserMessage)

    private fun toUserMessage(throwable: Throwable): String = when (throwable) {
        is ClientRequestException -> when (throwable.response.status.value) {
            401 -> "Ошибка авторизации API"
            403 -> "Доступ запрещён"
            else -> "Ошибка запроса (${throwable.response.status.value})"
        }
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
