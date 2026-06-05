package pro.masterdoc.data.assistant.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantDto(
    val id: Int,
    val name: String,
)

@Serializable
data class DetectAssistantResponse(
    val assistant: String? = null,
)

@Serializable
data class DetectStreamResultDto(
    @SerialName("detect_result") val detectResult: DetectStreamAssistantDto? = null,
)

@Serializable
data class DetectStreamAssistantDto(
    val assistant: String? = null,
)
