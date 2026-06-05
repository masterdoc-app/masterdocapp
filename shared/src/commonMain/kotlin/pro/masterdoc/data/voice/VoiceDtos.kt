package pro.masterdoc.data.voice

import kotlinx.serialization.Serializable

@Serializable
data class TranscribeVoiceResponseDto(
    val text: String,
)
