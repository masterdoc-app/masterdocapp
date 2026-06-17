package pro.fixaverse.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun fixaverseTypography(fonts: FixaverseFontFamilies): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fonts.serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        fontSize = 32.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = FixaverseColors.TextPrimary,
    ),
    headlineLarge = TextStyle(
        fontFamily = fonts.serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fonts.serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.25).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.08).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.08).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.5.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = fonts.mono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.12.em,
    ),
    labelSmall = TextStyle(
        fontFamily = fonts.mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.5.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.1.em,
    ),
)

/** App-in-phone styles from lite wireframes (bubbles, opts, fault card). */
object FixaverseLiteTextStyles {
    fun monoEyebrow(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.16.em,
        color = FixaverseColors.Accent,
    )

    fun bubbleUser(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        color = FixaverseColors.UserBubbleContent,
    )

    fun bubbleAssistant(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        color = FixaverseColors.TextPrimary,
    )

    fun bubbleWho(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.14.em,
        color = FixaverseColors.Accent,
    )

    fun optBody(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = FixaverseColors.TextPrimary,
    )

    fun optLetter(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = FixaverseColors.Accent,
    )

    fun serifEmphasis(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = FixaverseColors.TextPrimary,
    )

    fun faultHeadline(fonts: FixaverseFontFamilies) = TextStyle(
        fontFamily = fonts.serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = FixaverseColors.TextPrimary,
    )
}
