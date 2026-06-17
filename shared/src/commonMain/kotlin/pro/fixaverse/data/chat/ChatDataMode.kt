package pro.fixaverse.data.chat

expect fun defaultChatDataMode(): ChatDataMode

/** Ktor Wasm fetch cannot reliably read NDJSON streams (TypeError: network error). */
expect fun useStreamingChatTransport(): Boolean

enum class ChatDataMode {
    Mock,
    Http,
}
