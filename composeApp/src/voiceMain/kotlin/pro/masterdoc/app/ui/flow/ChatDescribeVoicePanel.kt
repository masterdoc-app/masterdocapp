package pro.masterdoc.app.ui.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import pro.masterdoc.app.ui.theme.LiteListenPanel
import pro.masterdoc.app.voice.encodeRecordingAsWav
import pro.masterdoc.data.voice.VoiceApi
import space.kodio.compose.rememberRecorderState
import space.kodio.core.security.AudioPermissionManager

@Composable
actual fun ChatDescribeVoicePanel(
    voiceApi: VoiceApi?,
    onTranscript: (String) -> Unit,
    onBusyChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var voiceError by remember { mutableStateOf<String?>(null) }
    val recorderState = rememberRecorderState(
        onRecordingComplete = { recording ->
            val api = voiceApi
            if (api == null) {
                voiceError = "Голосовой API недоступен"
                onError(voiceError)
                return@rememberRecorderState
            }
            scope.launch {
                onBusyChanged(true)
                voiceError = null
                onError(null)
                try {
                    val wavBytes = encodeRecordingAsWav(recording)
                    if (wavBytes.isEmpty()) {
                        voiceError = "Пустая запись"
                        onError(voiceError)
                        return@launch
                    }
                    val text = api.transcribe(wavBytes).text.trim()
                    if (text.isNotEmpty()) {
                        onTranscript(text)
                    } else {
                        voiceError = "Не удалось распознать речь"
                        onError(voiceError)
                    }
                } catch (e: Exception) {
                    voiceError = e.message ?: "Ошибка распознавания"
                    onError(voiceError)
                } finally {
                    onBusyChanged(false)
                }
            }
        },
    )
    val isListening = recorderState.isRecording
    val voiceBusy = recorderState.isProcessing
    SideEffect {
        onBusyChanged(voiceBusy)
    }

    val hint = when {
        voiceError != null -> voiceError!!
        voiceBusy -> "Распознаю…"
        isListening -> "Нажмите ещё раз, чтобы остановить"
        recorderState.permissionState != AudioPermissionManager.State.Granted ->
            "Разрешите доступ к микрофону"
        else -> "Нажмите микрофон"
    }

    LiteListenPanel(
        label = if (isListening) "Слушаю · шумоподавление" else "Голосовой ввод",
        hint = hint,
        onMicClick = {
            if (voiceBusy) return@LiteListenPanel
            scope.launch {
                voiceError = null
                onError(null)
                if (recorderState.permissionState != AudioPermissionManager.State.Granted) {
                    recorderState.requestPermissionAsync()
                    if (recorderState.permissionState != AudioPermissionManager.State.Granted) {
                        voiceError = "Нет доступа к микрофону"
                        onError(voiceError)
                    }
                    return@launch
                }
                recorderState.toggleAsync()
            }
        },
        modifier = modifier,
        isListening = isListening,
    )
}
