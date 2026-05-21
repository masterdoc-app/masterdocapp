package pro.masterdoc.domain.chat

enum class TimelineStepStatus {
    Active,
    Done,
    Error,
}

enum class TimelineStepKind {
    Thinking,
    Search,
    Tool,
    Answer,
}

data class ChatTimelineStep(
    val id: String,
    val label: String,
    val kind: TimelineStepKind,
    val status: TimelineStepStatus = TimelineStepStatus.Active,
    /** Reasoning / plan text (thinking). */
    val detail: String = "",
)
