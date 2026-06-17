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
        label = "Голос в браузере",
        hint = "Пока используйте текстовый ввод",
        onMicClick = { onError("Голосовой ввод на Web пока недоступен") },
        modifier = modifier,
        isListening = false,
    )
}
