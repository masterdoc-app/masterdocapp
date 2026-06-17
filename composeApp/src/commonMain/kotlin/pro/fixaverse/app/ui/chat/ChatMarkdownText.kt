package pro.fixaverse.app.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.mikepenz.markdown.m3.Markdown
import pro.fixaverse.data.citation.parseCitationUri
import pro.fixaverse.data.citation.preprocessCitationMarkdown

@Composable
fun ChatMarkdownText(
    content: String,
    citations: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    var activeCitation by remember { mutableStateOf<Pair<String, String>?>(null) }
    val parentHandler = LocalUriHandler.current
    val handler = remember(citations, parentHandler) {
        object : UriHandler {
            override fun openUri(uri: String) {
                val index = parseCitationUri(uri)
                if (index != null) {
                    activeCitation = index to citations[index].orEmpty()
                    return
                }
                if (uri.isBlank()) return
                parentHandler.openUri(uri)
            }
        }
    }

    CompositionLocalProvider(LocalUriHandler provides handler) {
        Markdown(
            content = preprocessCitationMarkdown(content),
            modifier = modifier,
        )
    }

    activeCitation?.let { (index, documentId) ->
        ChatCitationDialog(
            citationIndex = index,
            documentId = documentId,
            onDismiss = { activeCitation = null },
        )
    }
}
