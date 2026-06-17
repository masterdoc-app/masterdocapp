package pro.fixaverse.app.ui.flow

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pro.fixaverse.data.voice.VoiceApi

@Composable
expect fun ChatDescribeVoicePanel(
    voiceApi: VoiceApi?,
    onTranscript: (String) -> Unit,
    onBusyChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier = Modifier,
)
