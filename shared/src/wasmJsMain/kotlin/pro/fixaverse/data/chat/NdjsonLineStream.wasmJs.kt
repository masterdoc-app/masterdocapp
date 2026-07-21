@file:OptIn(ExperimentalWasmJsInterop::class)

package pro.fixaverse.data.chat

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Wasm Ktor [readUTF8Line] on long NDJSON streams throws TypeError: network error.
 * Browser Fetch + ReadableStream via [fetchNdjsonLinesJs].
 */
@JsFun(
    """
    (url, body, onLine, onSuccess, onFailure) => {
      (async () => {
        try {
          const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: body
          });
          if (!response.ok) {
            const err = await response.text();
            onFailure('Chat API ' + response.status + ': ' + err.slice(0, 300));
            return;
          }
          const reader = response.body.getReader();
          const decoder = new TextDecoder('utf-8');
          let carry = '';
          while (true) {
            const chunk = await reader.read();
            if (chunk.done) break;
            carry += decoder.decode(chunk.value);
            let idx;
            while ((idx = carry.indexOf('\n')) >= 0) {
              const line = carry.slice(0, idx).trim();
              carry = carry.slice(idx + 1);
              if (line) onLine(line);
            }
          }
          const tail = carry.trim();
          if (tail) onLine(tail);
          onSuccess();
        } catch (e) {
          onFailure(String(e));
        }
      })();
    }
    """,
)
private external fun fetchNdjsonLinesJs(
    url: String,
    body: String,
    onLine: (String) -> Unit,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
)

internal actual suspend fun ndjsonStreamPostPlatformSync(
    url: String,
    jsonBody: String,
    onLine: (String) -> Unit,
) = suspendCoroutine { cont ->
    fetchNdjsonLinesJs(
        url = url,
        body = jsonBody,
        onLine = onLine,
        onSuccess = { cont.resume(Unit) },
        onFailure = { message -> cont.resumeWithException(IllegalStateException(message)) },
    )
}
