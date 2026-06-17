package pro.fixaverse.app.ui.chat

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import pro.fixaverse.app.ui.theme.LiteFieldShape
import pro.fixaverse.app.ui.theme.fixaverseChatInputFieldColors
import pro.fixaverse.platform.PlatformCapabilities

/**
 * Chat input backed by local [TextFieldValue] so mobile IME composition (word + space)
 * is not wiped by async store round-trips.
 */
@Composable
fun FixaverseChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSending: Boolean = false,
    onSend: (() -> Unit)? = null,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }

    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        }
    }

    var fieldModifier = modifier
    if (PlatformCapabilities.supportsEnterToSend && onSend != null) {
        fieldModifier = fieldModifier.fixaverseChatInputKeys(
            input = textFieldValue.text,
            isSending = isSending,
            onInputChange = onValueChange,
            onSend = onSend,
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue.text)
        },
        modifier = fieldModifier,
        enabled = enabled,
        placeholder = { Text(placeholder) },
        minLines = minLines,
        maxLines = maxLines,
        shape = LiteFieldShape,
        colors = fixaverseChatInputFieldColors(),
    )
}
