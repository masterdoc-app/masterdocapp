package pro.masterdoc.data.chat

import kotlinx.coroutines.delay
import pro.masterdoc.domain.chat.ChatMessage
import pro.masterdoc.domain.chat.ChatRole
import pro.masterdoc.domain.chat.ChatTimelineStep
import pro.masterdoc.domain.chat.TimelineStepKind
import pro.masterdoc.domain.chat.TimelineStepStatus
import kotlin.random.Random

/**
 * In-memory mock for UI/dev without a backend.
 */
class MockChatRepository : ChatRepository {

    private var conversationId: String = "mock-conversation-1"
    private val messages = mutableListOf<ChatMessage>()

    override suspend fun loadHistory(conversationId: String?): Result<ChatHistory> {
        delay(MOCK_NETWORK_DELAY_MS)
        if (messages.isEmpty()) {
            messages += listOf(
                assistantMessage(
                    id = "mock-welcome",
                    content = "Здравствуйте! Я помощник Masterdoc по холодильникам Атлант. " +
                        "Спросите про настройки, уход, шум, лёд или неисправности.",
                ),
                assistantMessage(
                    id = "mock-hint",
                    content = "Например: «Почему много инея?» или «Какой режим для отпуска?»",
                ),
            )
        }
        conversationId?.let { this.conversationId = it }
        return Result.success(
            ChatHistory(
                conversationId = this.conversationId,
                messages = messages.toList(),
            ),
        )
    }

    override suspend fun send(
        text: String,
        conversationId: String?,
        personaId: Int,
        onStreamUpdate: (StreamingChatUpdate) -> Unit,
    ): Result<SendChatResult> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(ChatException("Пустое сообщение"))
        }
        conversationId?.let { this.conversationId = it }

        val userMessage = ChatMessage(
            id = "mock-user-${Random.nextLong()}",
            role = ChatRole.User,
            content = trimmed,
        )
        val assistantId = "mock-assistant-${Random.nextLong()}"
        val answer = replyFor(trimmed)

        val thinking = ChatTimelineStep(
            id = "thinking",
            label = "Думаю…",
            kind = TimelineStepKind.Thinking,
            status = TimelineStepStatus.Active,
            detail = "Анализирую вопрос по базе знаний Атлант…",
        )
        onStreamUpdate(
            StreamingChatUpdate(
                conversationId = this.conversationId,
                userMessage = userMessage,
                assistantMessage = ChatMessage(
                    id = assistantId,
                    role = ChatRole.Assistant,
                    content = "",
                    timeline = listOf(thinking),
                    isStreaming = true,
                ),
            ),
        )
        delay(350)

        val search = ChatTimelineStep(
            id = "search",
            label = "Поиск в базе знаний",
            kind = TimelineStepKind.Search,
            status = TimelineStepStatus.Active,
        )
        onStreamUpdate(
            StreamingChatUpdate(
                conversationId = this.conversationId,
                userMessage = userMessage,
                assistantMessage = ChatMessage(
                    id = assistantId,
                    role = ChatRole.Assistant,
                    content = "",
                    timeline = listOf(
                        thinking.copy(status = TimelineStepStatus.Done),
                        search,
                    ),
                    isStreaming = true,
                ),
            ),
        )
        delay(400)

        var partial = ""
        answer.chunked(12).forEach { chunk ->
            partial += chunk
            onStreamUpdate(
                StreamingChatUpdate(
                    conversationId = this.conversationId,
                    userMessage = userMessage,
                    assistantMessage = ChatMessage(
                        id = assistantId,
                        role = ChatRole.Assistant,
                        content = partial,
                        timeline = listOf(
                            thinking.copy(status = TimelineStepStatus.Done),
                            search.copy(status = TimelineStepStatus.Done),
                        ),
                        isStreaming = true,
                    ),
                ),
            )
            delay(80)
        }

        val assistantMessage = ChatMessage(
            id = assistantId,
            role = ChatRole.Assistant,
            content = answer,
            timeline = listOf(
                thinking.copy(status = TimelineStepStatus.Done),
                search.copy(status = TimelineStepStatus.Done),
            ),
            isStreaming = false,
        )
        messages += userMessage
        messages += assistantMessage

        return Result.success(
            SendChatResult(
                conversationId = this.conversationId,
                userMessage = userMessage,
                assistantMessage = assistantMessage,
            ),
        )
    }

    private fun assistantMessage(id: String, content: String) = ChatMessage(
        id = id,
        role = ChatRole.Assistant,
        content = content,
    )

    private fun replyFor(userText: String): String {
        val lower = userText.lowercase()
        return when {
            "иней" in lower || "инея" in lower || "лёд" in lower || "лед" in lower ->
                "Проверьте, плотно ли закрывается дверь и не мешает ли что-то уплотнителю. " +
                    "Частая причина инея — длительное открытие двери или высокая влажность. " +
                    "Если иней быстро нарастает снова — лучше вызвать сервис."
            "шум" in lower || "гудит" in lower ->
                "Лёгкий гул компрессора — норма. Обратите внимание, не касается ли корпус стены " +
                    "и стоит ли холодильник ровно. Сильный или новый шум — зафиксируйте на видео и обратитесь в сервис."
            "отпуск" in lower || "уезжа" in lower ->
                "Для длительного отсутствия: опорожните продукты, выключите или включите режим отпуска " +
                    "(см. вашу модель в инструкции), оставьте двери приоткрытыми, если рекомендовано производителем."
            else ->
                "Понял вопрос: «$userText». Для точной диагностики укажите модель холодильника " +
                    "и что именно наблюдаете (температура, индикаторы, звуки). Пока это демо-ответ без реального API."
        }
    }

    private companion object {
        const val MOCK_NETWORK_DELAY_MS = 400L
    }
}
