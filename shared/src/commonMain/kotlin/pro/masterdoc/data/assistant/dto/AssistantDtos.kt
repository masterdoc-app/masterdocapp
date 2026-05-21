package pro.masterdoc.data.assistant.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssistantDto(
    val id: Int,
    val name: String,
)
