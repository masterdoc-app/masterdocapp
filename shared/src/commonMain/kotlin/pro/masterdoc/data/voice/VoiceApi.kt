package pro.masterdoc.data.voice

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import pro.masterdoc.data.config.ApiConfig

class VoiceApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {
    suspend fun transcribe(
        wavBytes: ByteArray,
        fileName: String = "recording.wav",
    ): TranscribeVoiceResponseDto {
        val response = httpClient.post("${apiConfig.baseUrl}/v1/voice/transcribe") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "audio",
                            wavBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "audio/wav")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            },
                        )
                    },
                ),
            )
        }
        return response.body()
    }
}
