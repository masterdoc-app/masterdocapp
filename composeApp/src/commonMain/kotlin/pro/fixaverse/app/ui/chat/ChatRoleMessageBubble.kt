package pro.fixaverse.app.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pro.fixaverse.app.ui.theme.AssistantMessageShape
import pro.fixaverse.app.ui.theme.AppBranding
import pro.fixaverse.app.ui.theme.FixaverseDimens
import pro.fixaverse.app.ui.theme.FixaverseMessageSurface
import pro.fixaverse.app.ui.theme.UserMessageShape
import pro.fixaverse.domain.chat.ChatRole

/** Read-only chat bubble for a single role + text (knowledge base transcript, etc.). */
@Composable
fun ChatRoleMessageBubble(
    role: ChatRole,
    content: String,
    modifier: Modifier = Modifier,
    showAssistantWho: Boolean = true,
) {
    val isUser = role == ChatRole.User
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) UserMessageShape else AssistantMessageShape

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = alignment) {
        FixaverseMessageSurface(isUser = isUser, shape = shape) {
            Column(modifier = Modifier.padding(FixaverseDimens.Space12)) {
                if (!isUser && showAssistantWho) {
                    Text(
                        text = AppBranding.ASSISTANT_LABEL,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = FixaverseDimens.Space5),
                    )
                }
                if (isUser) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    ChatMarkdownText(content = content)
                }
            }
        }
    }
}
