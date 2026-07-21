package pro.fixaverse.data.chat

internal actual suspend fun ndjsonStreamPostPlatformSync(
    url: String,
    jsonBody: String,
    onLine: (String) -> Unit,
) = ndjsonStreamPostDefault(url, jsonBody, onLine)
