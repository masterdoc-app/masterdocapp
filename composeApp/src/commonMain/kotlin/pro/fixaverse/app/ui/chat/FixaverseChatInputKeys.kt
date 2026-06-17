package pro.fixaverse.app.ui.chat

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Enter — отправить; Ctrl+Enter (или Cmd+Enter на macOS) — новая строка.
 */
fun Modifier.fixaverseChatInputKeys(
    input: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
): Modifier = onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) {
        return@onPreviewKeyEvent false
    }
    val insertNewLine = event.isCtrlPressed || event.isMetaPressed
    if (insertNewLine) {
        if (!isSending) {
            onInputChange(input + "\n")
        }
        return@onPreviewKeyEvent true
    }
    if (input.isNotBlank() && !isSending) {
        onSend()
    }
    true
}
