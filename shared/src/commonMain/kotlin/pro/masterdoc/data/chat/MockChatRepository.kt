package pro.masterdoc.data.chat

import kotlinx.coroutines.delay
import pro.masterdoc.domain.chat.ChatMessage
import pro.masterdoc.domain.chat.ChatRole
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

    override suspend fun send(text: String, conversationId: String?): Result<SendChatResult> {
        delay(MOCK_NETWORK_DELAY_MS)
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
        val assistantMessage = assistantMessage(
            id = "mock-assistant-${Random.nextLong()}",
            content = replyFor(trimmed),
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
