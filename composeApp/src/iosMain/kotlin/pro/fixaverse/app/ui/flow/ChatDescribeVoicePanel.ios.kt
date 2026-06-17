package pro.fixaverse.app.ui.flow

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pro.fixaverse.app.ui.theme.LiteListenPanel
import pro.fixaverse.data.voice.VoiceApi

@Composable
actual fun ChatDescribeVoicePanel(
    voiceApi: VoiceApi?,
    onTranscript: (String) -> Unit,
    onBusyChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier,
) {
    onBusyChanged(false)
    LiteListenPanel(
        label = "Голос (iOS)",
        hint = "Скоро — пока введите текстом",
        onMicClick = { onError("Голосовой ввод на iOS пока недоступен") },
        modifier = modifier,
        isListening = false,
    )
}
