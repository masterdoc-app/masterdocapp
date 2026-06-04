package pro.masterdoc.presentation.root

import kotlinx.serialization.Serializable
import pro.masterdoc.domain.assistant.Assistant

@Serializable
data class FlowNavigationSnapshot(
    val stack: List<FlowConfig>,
    val assistantId: Int? = null,
    val assistantName: String? = null,
) {
    fun sanitizedStack(): List<FlowConfig> {
        val normalized = stack.ifEmpty { return listOf(FlowConfig.Scan) }
        if (normalized == listOf(FlowConfig.Scan)) return normalized
        val needsAssistant = normalized.any { it.needsSelectedAssistant() }
        if (needsAssistant && assistantId == null) return listOf(FlowConfig.Scan)
        if (needsAssistant && assistantName.isNullOrBlank()) return listOf(FlowConfig.Scan)
        return normalized
    }

    fun selectedAssistant(): Assistant? {
        val id = assistantId ?: return null
        val name = assistantName?.trim().orEmpty()
        if (name.isEmpty()) return null
        return Assistant(id = id, name = name)
    }
}

private fun FlowConfig.needsSelectedAssistant(): Boolean = when (this) {
    FlowConfig.ChatDescribe,
    FlowConfig.Summary,
    FlowConfig.FrequentIssues,
    -> true
    FlowConfig.Scan,
    FlowConfig.Camera,
    -> false
}
