package pro.masterdoc.data.chat

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pro.masterdoc.domain.chat.ChatTimelineStep
import pro.masterdoc.domain.chat.TimelineStepKind
import pro.masterdoc.domain.chat.TimelineStepStatus

/**
 * Accumulates Onyx NDJSON/SSE packets into answer text + timeline steps (like Onyx AgentTimeline).
 */
internal class OnyxStreamAccumulator {
    private val steps = linkedMapOf<String, ChatTimelineStep>()
    private var answerText = StringBuilder()
    private var thinkingText = StringBuilder()
    private var thinkingDone = false
    private var activeThinkingId: String? = null
    private var debugPacketCount = 0

    fun onLine(line: String) {
        val trimmed = line.trim().removePrefix("data:").trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("{")) return

        val obj = runCatching { json.parseToJsonElement(trimmed).jsonObject }.getOrNull() ?: return

        if (obj.containsKey("answer")) {
            obj.string("answer")?.takeIf { it.isNotBlank() }?.let { answerText = StringBuilder(it) }
            return
        }

        val packetObj = obj["obj"]?.jsonObject ?: obj
        val type = packetObj.string("type") ?: return
        if (debugPacketCount < 8) {
            debugPacketCount++
            println("[OnyxStream] packet=$type")
        }

        when (type) {
            "reasoning_start" -> startThinking()
            "reasoning_delta" -> appendThinking(packetObj.string("reasoning").orEmpty())
            "reasoning_done" -> finishThinking()
            "message_start" -> {
                finishThinking()
                packetObj.string("content")?.takeIf { it.isNotBlank() }?.let {
                    answerText = StringBuilder(it)
                }
            }
            "message_delta" -> {
                finishThinking()
                answerText.append(packetObj.string("content").orEmpty())
            }
            "search_tool_start" -> addToolStep(
                id = "search-${steps.size}",
                label = if (packetObj.string("is_internet_search") == "true") {
                    "Поиск в интернете"
                } else {
                    "Поиск в базе знаний"
                },
                kind = TimelineStepKind.Search,
            )
            "python_tool_start" -> addToolStep("python", "Выполнение кода", TimelineStepKind.Tool)
            "open_url_start", "fetch_tool_start" -> addToolStep("fetch", "Открытие ссылок", TimelineStepKind.Tool)
            "image_generation_start" -> addToolStep("image", "Генерация изображения", TimelineStepKind.Tool)
            "custom_tool_start" -> addToolStep(
                id = "custom-${packetObj.string("tool_name") ?: steps.size}",
                label = packetObj.string("tool_name") ?: "Инструмент",
                kind = TimelineStepKind.Tool,
            )
            "deep_research_plan_start" -> addToolStep("plan", "Планирование", TimelineStepKind.Tool)
            "research_agent_start" -> addToolStep(
                "research",
                packetObj.string("research_task")?.take(40)?.let { "Исследование: $it…" } ?: "Агент исследования",
                TimelineStepKind.Tool,
            )
            "tool_call_argument_delta" -> addToolStep("tool-args", "Подготовка инструмента", TimelineStepKind.Tool)
            "memory_tool_start" -> addToolStep("memory", "Память", TimelineStepKind.Tool)
            "error" -> addToolStep(
                "error",
                packetObj.string("message") ?: "Ошибка",
                TimelineStepKind.Tool,
                TimelineStepStatus.Error,
            )
            "stop" -> {
                finishThinking()
                markAllToolsDone()
            }
        }
    }

    fun snapshot(answer: String = answerText.toString()): Snapshot {
        syncThinkingStep()
        return Snapshot(
            answer = answer,
            timeline = steps.values.toList(),
        )
    }

    private fun startThinking() {
        if (activeThinkingId != null) return
        val id = "thinking"
        activeThinkingId = id
        thinkingDone = false
        steps[id] = ChatTimelineStep(
            id = id,
            label = "Думаю…",
            kind = TimelineStepKind.Thinking,
            status = TimelineStepStatus.Active,
            detail = "",
        )
    }

    private fun appendThinking(text: String) {
        if (text.isEmpty()) return
        if (activeThinkingId == null) startThinking()
        thinkingText.append(text)
        syncThinkingStep()
    }

    private fun finishThinking() {
        if (activeThinkingId == null && thinkingText.isEmpty()) return
        thinkingDone = true
        syncThinkingStep()
    }

    private fun syncThinkingStep() {
        val id = activeThinkingId ?: return
        val label = thinkingText.lineSequence().firstOrNull { it.startsWith("#") }
            ?.removePrefix("#")?.trim()?.take(48)
            ?.takeIf { it.isNotBlank() }
            ?: "Думаю…"
        steps[id] = ChatTimelineStep(
            id = id,
            label = label,
            kind = TimelineStepKind.Thinking,
            status = if (thinkingDone) TimelineStepStatus.Done else TimelineStepStatus.Active,
            detail = thinkingText.toString().trim(),
        )
    }

    private fun addToolStep(
        id: String,
        label: String,
        kind: TimelineStepKind,
        status: TimelineStepStatus = TimelineStepStatus.Active,
    ) {
        finishThinking()
        if (steps.containsKey(id)) {
            steps[id] = steps[id]!!.copy(label = label, status = status)
        } else {
            steps[id] = ChatTimelineStep(id = id, label = label, kind = kind, status = status)
        }
    }

    private fun markAllToolsDone() {
        steps.entries.forEach { (id, step) ->
            if (step.kind != TimelineStepKind.Thinking && step.status == TimelineStepStatus.Active) {
                steps[id] = step.copy(status = TimelineStepStatus.Done)
            }
        }
    }

    data class Snapshot(
        val answer: String,
        val timeline: List<ChatTimelineStep>,
    )

    private fun JsonObject.string(key: String): String? =
        get(key)?.jsonPrimitive?.content

    private companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }
}
