package pro.fixaverse.data.citation

import kotlinx.serialization.Serializable

@Serializable
data class CitationDocumentDto(
    @kotlinx.serialization.SerialName("document_id")
    val documentId: String,
    val title: String,
    val excerpt: String,
)
