package pro.fixaverse.data.voice

import kotlinx.serialization.Serializable

@Serializable
data class TranscribeVoiceResponseDto(
    val text: String,
)
