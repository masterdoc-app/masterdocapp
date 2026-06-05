package pro.masterdoc.data.assistant

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Maps Onyx NDJSON stream packets to a single user-facing detect status line.
 */
class DetectProgressTracker {
    private var message = ""
    private var searchActive = false

    fun onLine(line: String): String? {
        val trimmed = line.trim().removePrefix("data:").trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("{")) return null

        val obj = runCatching { json.parseToJsonElement(trimmed).jsonObject }.getOrNull() ?: return null
        val packetObj = obj["obj"]?.jsonObject ?: obj
        val type = packetObj.string("type") ?: return null

        val next = when (type) {
            "reasoning_start" -> "Смотрим на фото…"
            "reasoning_delta" -> {
                val heading = packetObj.string("reasoning")
                    ?.lineSequence()
                    ?.firstOrNull { it.startsWith("#") }
                    ?.removePrefix("#")
                    ?.trim()
                    ?.take(56)
                heading?.takeIf { it.isNotBlank() } ?: message.ifBlank { "Смотрим на фото…" }
            }
            "reasoning_done" -> message.ifBlank { "Смотрим на фото…" }
            "search_tool_start" -> {
                searchActive = true
                if (packetObj.string("is_internet_search") == "true") {
                    "Ищем в интернете…"
                } else {
                    "Ищем в базе знаний…"
                }
            }
            "research_agent_start" -> {
                searchActive = true
                packetObj.string("research_task")?.take(48)?.let { "Исследуем: $it…" }
                    ?: "Исследуем документацию…"
            }
            "deep_research_plan_start" -> "Планируем поиск…"
            "tool_call_argument_delta" -> message.ifBlank { "Подготавливаем поиск…" }
            "message_start", "message_delta" -> {
                if (searchActive) {
                    searchActive = false
                    "Определяем станцию…"
                } else {
                    message.ifBlank { "Определяем станцию…" }
                }
            }
            "stop" -> message.ifBlank { "Завершаем…" }
            else -> null
        }

        if (next == null || next == message) return null
        message = next
        return message
    }

    fun currentMessage(): String = message

    private fun JsonObject.string(key: String): String? =
        get(key)?.jsonPrimitive?.content

    private companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }
}
