package pro.masterdoc.data.chat

/** Uses HTTP against Masterdoc API (platform default or local.properties). */
fun defaultChatDataMode(): ChatDataMode = ChatDataMode.Http

enum class ChatDataMode {
    Mock,
    Http,
}
