package pro.fixaverse.data.chat

expect fun defaultChatDataMode(): ChatDataMode

/** Wasm uses Fetch ReadableStream; other targets use Ktor. */
expect fun useStreamingChatTransport(): Boolean

enum class ChatDataMode {
    Mock,
    Http,
}
