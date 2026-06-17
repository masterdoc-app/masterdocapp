package pro.fixaverse.presentation.chat

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pro.fixaverse.domain.chat.ChatMessage
import pro.fixaverse.domain.chat.ChatMessageStatus
import pro.fixaverse.domain.chat.ChatRole

class ChatStoreFinishCaseTest {

    @Test
    fun canFinishCase_false_whenSending() {
        val state = ChatStore.State(
            isSending = true,
            messages = listOf(
                user("u1"),
                assistant("a1", isStreaming = true),
            ),
        )
        assertFalse(state.canFinishCase())
    }

    @Test
    fun canFinishCase_false_whenAssistantStillStreaming() {
        val state = ChatStore.State(
            isSending = false,
            messages = listOf(
                user("u1"),
                assistant("a1", isStreaming = true),
            ),
        )
        assertFalse(state.canFinishCase())
    }

    @Test
    fun canFinishCase_true_afterStreamCompleted() {
        val state = ChatStore.State(
            isSending = false,
            messages = listOf(
                user("u1"),
                assistant("a1", isStreaming = false),
            ),
        )
        assertTrue(state.canFinishCase())
    }

    @Test
    fun canFinishCase_false_beforeAnyUserMessage() {
        assertFalse(ChatStore.State().canFinishCase())
    }

    private fun user(id: String) = ChatMessage(
        id = id,
        role = ChatRole.User,
        content = "не включается",
        status = ChatMessageStatus.Sent,
    )

    private fun assistant(id: String, isStreaming: Boolean) = ChatMessage(
        id = id,
        role = ChatRole.Assistant,
        content = if (isStreaming) "" else "проверьте питание",
        isStreaming = isStreaming,
    )
}
