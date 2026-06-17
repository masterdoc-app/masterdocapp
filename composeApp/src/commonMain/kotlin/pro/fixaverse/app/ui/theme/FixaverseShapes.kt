package pro.fixaverse.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

fun fixaverseShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(FixaverseDimens.RadiusOpt),
    small = RoundedCornerShape(FixaverseDimens.RadiusOpt),
    medium = RoundedCornerShape(FixaverseDimens.RadiusBubble),
    large = RoundedCornerShape(FixaverseDimens.RadiusBubble),
    extraLarge = RoundedCornerShape(FixaverseDimens.RadiusField),
)

val UserMessageShape = RoundedCornerShape(
    topStart = FixaverseDimens.RadiusBubble,
    topEnd = FixaverseDimens.RadiusBubble,
    bottomEnd = FixaverseDimens.RadiusBubble,
    bottomStart = FixaverseDimens.RadiusBubbleTail,
)

val AssistantMessageShape = RoundedCornerShape(
    topStart = FixaverseDimens.RadiusBubble,
    topEnd = FixaverseDimens.RadiusBubble,
    bottomEnd = FixaverseDimens.RadiusBubbleTail,
    bottomStart = FixaverseDimens.RadiusBubble,
)

val LiteOptionShape = RoundedCornerShape(FixaverseDimens.RadiusOpt)
val LiteButtonShape = RoundedCornerShape(FixaverseDimens.RadiusButton)
val LiteFieldShape = RoundedCornerShape(FixaverseDimens.RadiusField)
val LiteChipShape = RoundedCornerShape(FixaverseDimens.RadiusChip)
