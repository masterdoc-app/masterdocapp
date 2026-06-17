package pro.fixaverse.data.chat

expect fun defaultChatDataMode(): ChatDataMode

enum class ChatDataMode {
    Mock,
    Http,
}
