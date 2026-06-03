package pro.masterdoc.data.chat

expect fun defaultChatDataMode(): ChatDataMode

enum class ChatDataMode {
    Mock,
    Http,
}
