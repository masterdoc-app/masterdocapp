package pro.masterdoc.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

fun masterdocShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(MasterdocDimens.Radius8),
    small = RoundedCornerShape(MasterdocDimens.Radius8),
    medium = RoundedCornerShape(MasterdocDimens.Radius12),
    large = RoundedCornerShape(MasterdocDimens.Radius16),
    extraLarge = RoundedCornerShape(MasterdocDimens.Radius16),
)

/** Chat bubble: rounded-16 with flat corner toward the edge (Onyx-style). */
val UserMessageShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 16.dp,
    bottomStart = 4.dp,
)

val AssistantMessageShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 4.dp,
    bottomStart = 16.dp,
)
