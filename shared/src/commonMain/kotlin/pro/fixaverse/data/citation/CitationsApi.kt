package pro.fixaverse.data.citation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.encodeURLPath
import pro.fixaverse.data.config.ApiConfig

class CitationsApi(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {
    suspend fun getDocument(documentId: String): CitationDocumentDto =
        httpClient.get(
            "${apiConfig.baseUrl}/citations/documents/${documentId.encodeURLPath()}",
        ).body()
}
