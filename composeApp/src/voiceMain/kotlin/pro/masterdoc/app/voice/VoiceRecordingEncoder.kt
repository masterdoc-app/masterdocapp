package pro.masterdoc.app.voice

import kotlinx.io.files.Path
import space.kodio.core.AudioRecording
import space.kodio.core.io.files.AudioFileFormat
import java.io.File

internal suspend fun encodeRecordingAsWav(recording: AudioRecording): ByteArray {
    val tmp = File.createTempFile("masterdoc-voice-", ".wav")
    try {
        recording.saveAs(Path(tmp.absolutePath), AudioFileFormat.Wav)
        return tmp.readBytes()
    } finally {
        tmp.delete()
    }
}
