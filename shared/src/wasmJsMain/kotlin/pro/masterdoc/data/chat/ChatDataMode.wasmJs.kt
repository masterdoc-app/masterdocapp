package pro.masterdoc.data.chat

/** Local web dev works without API for UI flow testing. */
actual fun defaultChatDataMode(): ChatDataMode = ChatDataMode.Mock
