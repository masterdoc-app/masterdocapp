package pro.masterdoc.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

fun masterdocShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(MasterdocDimens.RadiusOpt),
    small = RoundedCornerShape(MasterdocDimens.RadiusOpt),
    medium = RoundedCornerShape(MasterdocDimens.RadiusBubble),
    large = RoundedCornerShape(MasterdocDimens.RadiusBubble),
    extraLarge = RoundedCornerShape(MasterdocDimens.RadiusField),
)

val UserMessageShape = RoundedCornerShape(
    topStart = MasterdocDimens.RadiusBubble,
    topEnd = MasterdocDimens.RadiusBubble,
    bottomEnd = MasterdocDimens.RadiusBubble,
    bottomStart = MasterdocDimens.RadiusBubbleTail,
)

val AssistantMessageShape = RoundedCornerShape(
    topStart = MasterdocDimens.RadiusBubble,
    topEnd = MasterdocDimens.RadiusBubble,
    bottomEnd = MasterdocDimens.RadiusBubbleTail,
    bottomStart = MasterdocDimens.RadiusBubble,
)

val LiteOptionShape = RoundedCornerShape(MasterdocDimens.RadiusOpt)
val LiteButtonShape = RoundedCornerShape(MasterdocDimens.RadiusButton)
val LiteFieldShape = RoundedCornerShape(MasterdocDimens.RadiusField)
val LiteChipShape = RoundedCornerShape(MasterdocDimens.RadiusChip)
