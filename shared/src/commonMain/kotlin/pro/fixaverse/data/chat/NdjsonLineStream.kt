package pro.fixaverse.data.chat

import io.ktor.client.HttpClient
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.fixaverse.data.HttpClientFactory

/** Platform-specific NDJSON line reader (Fetch on Wasm, Ktor elsewhere). */
internal expect suspend fun ndjsonStreamPostPlatformSync(
    url: String,
    jsonBody: String,
    onLine: (String) -> Unit,
)

internal suspend fun ndjsonStreamPost(
    url: String,
    jsonBody: String,
    onLine: suspend (String) -> Unit,
) = coroutineScope {
    val channel = Channel<String>(capacity = 64)
    val producer = launch {
        try {
            ndjsonStreamPostPlatformSync(url, jsonBody) { line ->
                channel.trySend(line)
            }
        } finally {
            channel.close()
        }
    }
    for (line in channel) {
        onLine(line)
    }
    producer.join()
}

internal suspend fun ndjsonStreamPostKtor(
    httpClient: HttpClient,
    url: String,
    jsonBody: String,
    onLine: (String) -> Unit,
) {
    httpClient.preparePost(url) {
        contentType(ContentType.Application.Json)
        setBody(jsonBody)
    }.execute { response ->
        if (response.status.value !in 200..299) {
            error("Chat API ${response.status.value}: ${response.bodyAsText().take(300)}")
        }
        val channel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.isNotBlank()) {
                onLine(line)
            }
        }
    }
}

internal suspend fun ndjsonStreamPostDefault(
    url: String,
    jsonBody: String,
    onLine: (String) -> Unit,
) {
    ndjsonStreamPostKtor(HttpClientFactory().createRaw(), url, jsonBody, onLine)
}
