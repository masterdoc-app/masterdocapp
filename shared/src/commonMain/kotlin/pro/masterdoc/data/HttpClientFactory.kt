package pro.masterdoc.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pro.masterdoc.data.config.onyxPatOrNull

class HttpClientFactory {
    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        onyxPatOrNull()?.let { pat ->
            install(DefaultRequest) {
                header(HttpHeaders.Authorization, "Bearer $pat")
            }
        }
    }
}
