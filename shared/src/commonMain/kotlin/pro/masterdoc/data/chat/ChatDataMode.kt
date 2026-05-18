package pro.masterdoc.data.chat

import pro.masterdoc.data.config.OnyxBuildConfig

/** Switch to [ChatDataMode.Http] when the backend is available. */
enum class ChatDataMode {
    Mock,
    Http,
}

fun defaultChatDataMode(): ChatDataMode =
    if (OnyxBuildConfig.PAT.isNotBlank()) ChatDataMode.Http else ChatDataMode.Mock
