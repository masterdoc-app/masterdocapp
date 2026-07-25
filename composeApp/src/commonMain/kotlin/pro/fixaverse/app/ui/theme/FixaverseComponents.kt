package pro.fixaverse.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pro.fixaverse.design.theme.FixaverseLiteTokens

/** Paper gradient used in lite `.convo` screens. */
@Composable
fun Modifier.fixaverseConvoBackground(): Modifier {
    val top = MaterialTheme.colorScheme.background
    val bottom = MaterialTheme.colorScheme.surfaceVariant
    return background(Brush.verticalGradient(listOf(top, bottom)))
}

/** Поле ввода сообщения — тот же Paper, что и [MaterialTheme.colorScheme.background]. */
@Composable
fun fixaverseChatInputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    disabledContainerColor = MaterialTheme.colorScheme.background,
    errorContainerColor = MaterialTheme.colorScheme.background,
    focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
)

@Composable
fun FixaverseScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Composable
fun FixaverseMonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text.uppercase(),
        style = style,
        color = color,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** lite `.app-head` — station context bar. */
@Composable
fun LiteAppHead(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    /** Anchored overflow menu (three dots). Prefer over [onMenuClick] + standalone [DropdownMenu]. */
    menuAnchor: (@Composable () -> Unit)? = null,
    subtitleLive: Boolean = false,
    markLetter: String? = null,
    markColor: androidx.compose.ui.graphics.Color = FixaversePalette.Marian,
) {
    val fonts = LocalFixaverseFontFamilies.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = FixaversePalette.Rule,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = FixaverseDimens.Space16, vertical = FixaverseDimens.Space10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FixaverseDimens.Space10),
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(FixaverseDimens.NavIconTouch)
                    .testTag(FixaverseTestTags.APP_HEAD_BACK)
                    .clickable(
                        onClick = onBack,
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(FixaverseDimens.NavIconSize),
                )
            }
        } else if (markLetter != null) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(markColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = markLetter,
                    style = FixaverseLiteTextStyles.serifEmphasis(fonts).copy(
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        color = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank() || subtitleLive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FixaverseDimens.Space8),
                ) {
                    if (subtitleLive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(FixaverseColors.Success, CircleShape),
                        )
                    }
                    if (subtitle.isNotBlank()) {
                        FixaverseMonoLabel(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        when {
            menuAnchor != null -> menuAnchor()
            onMenuClick != null -> {
                Column(
                    modifier = Modifier
                        .clickable(onClick = onMenuClick)
                        .padding(FixaverseDimens.Space4),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                    CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

/** Large scan CTA — lite listen-panel rings + flare fill. */
@Composable
fun LiteCameraHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val buttonSize = FixaverseDimens.CameraHeroSize
    val ringSize = FixaverseDimens.CameraHeroRing
    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .border(1.5.dp, FixaverseLiteTokens.FlareBorder.copy(alpha = 0.22f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(ringSize * 0.82f)
                .border(1.5.dp, FixaverseLiteTokens.FlareBorder.copy(alpha = 0.35f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .background(
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                )
                .border(3.dp, FixaverseLiteTokens.FlareTint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                FixaverseLoadingIndicator(modifier = Modifier.size(32.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Сканировать камерой",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp),
                )
            }
        }
    }
}

@Composable
fun LiteMicHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = FixaverseDimens.MicHeroSize,
) {
    Box(
        modifier = modifier
            .size(size)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Микрофон",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.48f),
            )
        }
    }
}

@Composable
fun LiteListenPanel(
    label: String,
    hint: String,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = FixaversePalette.Rule,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = FixaverseDimens.Space14, vertical = FixaverseDimens.Space18),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space8),
    ) {
        LiteMicHeroButton(onClick = onMicClick, size = 56.dp)
        FixaverseMonoLabel(
            text = if (isListening) label else label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun FixaverseSelectableCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = LiteOptionShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(FixaverseDimens.Space16),
        )
    }
}

/** lite `.opt` — guided troubleshooting step. */
@Composable
fun LiteOptionCard(
    letter: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val fonts = LocalFixaverseFontFamilies.current
    Surface(
        modifier = modifier.fillMaxWidth(FixaverseDimens.OptMaxWidthFraction),
        shape = LiteOptionShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FixaverseDimens.Space11, vertical = FixaverseDimens.Space9),
            horizontalArrangement = Arrangement.spacedBy(FixaverseDimens.Space9),
        ) {
            Surface(
                shape = RoundedCornerShapeCompat4,
                color = FixaverseColors.AccentMuted,
                border = BorderStroke(1.dp, FixaverseLiteTokens.FlareBorder),
            ) {
                Text(
                    text = letter,
                    style = FixaverseLiteTextStyles.optLetter(fonts),
                    modifier = Modifier.padding(horizontal = FixaverseDimens.Space5, vertical = 1.dp),
                )
            }
            Text(
                text = body,
                style = FixaverseLiteTextStyles.optBody(fonts),
            )
        }
    }
}

private val RoundedCornerShapeCompat4 = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)

@Composable
fun LiteChip(
    text: String,
    modifier: Modifier = Modifier,
    flare: Boolean = false,
) {
    val container = if (flare) FixaverseColors.AccentMuted else MaterialTheme.colorScheme.surfaceVariant
    val border = if (flare) FixaverseLiteTokens.FlareBorder else MaterialTheme.colorScheme.outline
    val content = if (flare) FixaverseColors.Accent else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier,
        shape = LiteChipShape,
        color = container,
        border = BorderStroke(1.dp, border),
    ) {
        FixaverseMonoLabel(
            text = text,
            modifier = Modifier.padding(horizontal = FixaverseDimens.Space9, vertical = FixaverseDimens.Space4),
            style = MaterialTheme.typography.labelSmall,
            color = content,
        )
    }
}

@Composable
fun FixaversePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = LiteButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = FixaverseDimens.Space18),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun LiteFlareButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = LiteButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        contentPadding = PaddingValues(vertical = 11.dp, horizontal = FixaverseDimens.Space18),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixaverseDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = FixaverseDimens.Space24),
            shape = LiteOptionShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(FixaverseDimens.Space20),
                verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space12),
                content = content,
            )
        }
    }
}

@Composable
fun FixaversePhotoSourceDialog(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
) {
    FixaverseDialog(onDismissRequest = onDismiss) {
        Text(
            text = "Добавить фото",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Выберите источник изображения",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FixaverseSelectableCard(title = "Из галереи", onClick = onGallery)
        FixaverseSelectableCard(title = "С камеры", onClick = onCamera)
        FixaverseSecondaryButton(
            text = "Отмена",
            onClick = onDismiss,
            fillMaxWidth = true,
        )
    }
}

@Composable
fun FixaverseSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        shape = LiteButtonShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FixaverseMessageSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    isUser: Boolean = false,
    content: @Composable () -> Unit,
) {
    val container = if (isUser) FixaverseColors.UserBubble else FixaverseColors.AssistantBubble
    val border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    Surface(
        modifier = modifier.fillMaxWidth(FixaverseDimens.BubbleMaxWidthFraction),
        shape = shape,
        color = container,
        border = border,
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun FixaverseLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp,
    )
}

/** Full-screen detect progress — lite flare rings + paper card (scan / equipment pick). */
@Composable
fun FixaverseDetectLoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String = "Распознаём станцию…",
    hint: String = "",
) {
    if (!visible) return

    val ringOuter = 96.dp
    val ringMid = ringOuter * 0.82f
    val ringInner = 64.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(FixaverseTestTags.DETECT_LOADING_OVERLAY)
            .background(FixaverseLiteTokens.Ink.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(horizontal = FixaverseDimens.Space24),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, FixaverseLiteTokens.FlareBorder),
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = FixaverseDimens.Space24,
                    vertical = FixaverseDimens.Space24,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space16),
            ) {
                Box(
                    modifier = Modifier.size(ringOuter),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(ringOuter)
                            .border(
                                1.5.dp,
                                FixaverseLiteTokens.FlareBorder.copy(alpha = 0.22f),
                                CircleShape,
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .size(ringMid)
                            .border(
                                1.5.dp,
                                FixaverseLiteTokens.FlareBorder.copy(alpha = 0.35f),
                                CircleShape,
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .size(ringInner)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(2.dp, FixaverseLiteTokens.FlareTint, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        FixaverseLoadingIndicator(modifier = Modifier.size(28.dp))
                    }
                }
                FixaverseMonoLabel(
                    text = title,
                    modifier = Modifier.testTag(FixaverseTestTags.DETECT_LOADING_TITLE),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (hint.isNotBlank()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag(FixaverseTestTags.DETECT_LOADING_HINT),
                    )
                }
            }
        }
    }
}
