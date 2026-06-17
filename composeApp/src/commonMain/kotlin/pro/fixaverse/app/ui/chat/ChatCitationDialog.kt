package pro.fixaverse.app.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.CancellationException
import org.koin.compose.koinInject
import pro.fixaverse.app.ui.theme.FixaverseDialog
import pro.fixaverse.app.ui.theme.FixaverseLoadingIndicator
import pro.fixaverse.app.ui.theme.FixaverseSecondaryButton
import pro.fixaverse.data.citation.CitationsApi

@Composable
fun ChatCitationDialog(
    citationIndex: String,
    documentId: String,
    onDismiss: () -> Unit,
) {
    val citationsApi = koinInject<CitationsApi>()
    var title by remember(documentId) { mutableStateOf("Источник [$citationIndex]") }
    var excerpt by remember(documentId) { mutableStateOf<String?>(null) }
    var error by remember(documentId) { mutableStateOf<String?>(null) }
    var loading by remember(documentId) { mutableStateOf(true) }

    LaunchedEffect(documentId) {
        loading = true
        error = null
        excerpt = null
        if (documentId.isBlank()) {
            error = "Источник для цитаты [$citationIndex] не найден"
            loading = false
            return@LaunchedEffect
        }
        try {
            val doc = citationsApi.getDocument(documentId)
            title = doc.title.ifBlank { title }
            excerpt = doc.excerpt.trim()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "Не удалось загрузить фрагмент документа"
        } finally {
            loading = false
        }
    }

    FixaverseDialog(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        when {
            loading -> FixaverseLoadingIndicator()
            error != null -> Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = excerpt.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        FixaverseSecondaryButton(
            text = "Закрыть",
            onClick = onDismiss,
            fillMaxWidth = true,
        )
    }
}
