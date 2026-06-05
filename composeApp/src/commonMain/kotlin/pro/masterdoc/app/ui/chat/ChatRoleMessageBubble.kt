package pro.masterdoc.app.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pro.masterdoc.app.ui.theme.AssistantMessageShape
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocMessageSurface
import pro.masterdoc.app.ui.theme.UserMessageShape
import pro.masterdoc.domain.chat.ChatRole

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
        MasterdocMessageSurface(isUser = isUser, shape = shape) {
            Column(modifier = Modifier.padding(MasterdocDimens.Space12)) {
                if (!isUser && showAssistantWho) {
                    Text(
                        text = "MASTERDOC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = MasterdocDimens.Space5),
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
