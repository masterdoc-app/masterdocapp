package pro.masterdoc.app.ui.theme

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Paper gradient used in lite `.convo` screens. */
@Composable
fun Modifier.masterdocConvoBackground(): Modifier {
    val top = MaterialTheme.colorScheme.background
    val bottom = MaterialTheme.colorScheme.surfaceVariant
    return background(Brush.verticalGradient(listOf(top, bottom)))
}

/** Поле ввода сообщения — тот же Paper, что и [MaterialTheme.colorScheme.background]. */
@Composable
fun masterdocChatInputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    disabledContainerColor = MaterialTheme.colorScheme.background,
    errorContainerColor = MaterialTheme.colorScheme.background,
    focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
)

@Composable
fun MasterdocScreenTitle(
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
fun MasterdocMonoLabel(
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
    subtitleLive: Boolean = false,
    markLetter: String? = null,
    markColor: androidx.compose.ui.graphics.Color = MasterdocPalette.Marian,
) {
    val fonts = LocalMasterdocFontFamilies.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = MasterdocPalette.Rule,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = MasterdocDimens.Space16, vertical = MasterdocDimens.Space10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MasterdocDimens.Space10),
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(MasterdocDimens.NavIconTouch)
                    .testTag(MasterdocTestTags.APP_HEAD_BACK)
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
                    modifier = Modifier.size(MasterdocDimens.NavIconSize),
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
                    style = MasterdocLiteTextStyles.serifEmphasis(fonts).copy(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
            ) {
                if (subtitleLive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MasterdocColors.Success, CircleShape),
                    )
                }
                MasterdocMonoLabel(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (onMenuClick != null) {
            Column(
                modifier = Modifier
                    .clickable(onClick = onMenuClick)
                    .padding(MasterdocDimens.Space4),
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

/** Large scan CTA — lite listen-panel rings + flare fill. */
@Composable
fun LiteCameraHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val buttonSize = MasterdocDimens.CameraHeroSize
    val ringSize = MasterdocDimens.CameraHeroRing
    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .border(1.5.dp, MasterdocLiteTokens.FlareBorder.copy(alpha = 0.22f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(ringSize * 0.82f)
                .border(1.5.dp, MasterdocLiteTokens.FlareBorder.copy(alpha = 0.35f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .background(
                    color = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                )
                .border(3.dp, MasterdocLiteTokens.FlareTint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                MasterdocLoadingIndicator(modifier = Modifier.size(32.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Сканировать камерой",
                    tint = MaterialTheme.colorScheme.onSecondary,
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
    size: androidx.compose.ui.unit.Dp = MasterdocDimens.MicHeroSize,
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
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Микрофон",
                tint = MaterialTheme.colorScheme.onSecondary,
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
                    color = MasterdocPalette.Rule,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space18),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
    ) {
        LiteMicHeroButton(onClick = onMicClick, size = 56.dp)
        MasterdocMonoLabel(
            text = if (isListening) label else label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MasterdocSelectableCard(
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
            modifier = Modifier.padding(MasterdocDimens.Space16),
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
    val fonts = LocalMasterdocFontFamilies.current
    Surface(
        modifier = modifier.fillMaxWidth(MasterdocDimens.OptMaxWidthFraction),
        shape = LiteOptionShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MasterdocDimens.Space11, vertical = MasterdocDimens.Space9),
            horizontalArrangement = Arrangement.spacedBy(MasterdocDimens.Space9),
        ) {
            Surface(
                shape = RoundedCornerShapeCompat4,
                color = MasterdocColors.AccentMuted,
                border = BorderStroke(1.dp, MasterdocLiteTokens.FlareBorder),
            ) {
                Text(
                    text = letter,
                    style = MasterdocLiteTextStyles.optLetter(fonts),
                    modifier = Modifier.padding(horizontal = MasterdocDimens.Space5, vertical = 1.dp),
                )
            }
            Text(
                text = body,
                style = MasterdocLiteTextStyles.optBody(fonts),
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
    val container = if (flare) MasterdocColors.AccentMuted else MaterialTheme.colorScheme.surfaceVariant
    val border = if (flare) MasterdocLiteTokens.FlareBorder else MaterialTheme.colorScheme.outline
    val content = if (flare) MasterdocColors.Accent else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier,
        shape = LiteChipShape,
        color = container,
        border = BorderStroke(1.dp, border),
    ) {
        MasterdocMonoLabel(
            text = text,
            modifier = Modifier.padding(horizontal = MasterdocDimens.Space9, vertical = MasterdocDimens.Space4),
            style = MaterialTheme.typography.labelSmall,
            color = content,
        )
    }
}

@Composable
fun MasterdocPrimaryButton(
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = MasterdocDimens.Space18),
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
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        contentPadding = PaddingValues(vertical = 11.dp, horizontal = MasterdocDimens.Space18),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterdocDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = MasterdocDimens.Space24),
            shape = LiteOptionShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(MasterdocDimens.Space20),
                verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space12),
                content = content,
            )
        }
    }
}

@Composable
fun MasterdocPhotoSourceDialog(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
) {
    MasterdocDialog(onDismissRequest = onDismiss) {
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
        MasterdocSelectableCard(title = "Из галереи", onClick = onGallery)
        MasterdocSelectableCard(title = "С камеры", onClick = onCamera)
        MasterdocSecondaryButton(
            text = "Отмена",
            onClick = onDismiss,
            fillMaxWidth = true,
        )
    }
}

@Composable
fun MasterdocSecondaryButton(
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
fun MasterdocMessageSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    isUser: Boolean = false,
    content: @Composable () -> Unit,
) {
    val container = if (isUser) MasterdocColors.UserBubble else MasterdocColors.AssistantBubble
    val border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    Surface(
        modifier = modifier.fillMaxWidth(MasterdocDimens.BubbleMaxWidthFraction),
        shape = shape,
        color = container,
        border = border,
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun MasterdocLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondary,
        strokeWidth = 2.dp,
    )
}

/** Full-screen detect progress — lite flare rings + paper card (scan / equipment pick). */
@Composable
fun MasterdocDetectLoadingOverlay(
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
            .testTag(MasterdocTestTags.DETECT_LOADING_OVERLAY)
            .background(MasterdocLiteTokens.Ink.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(horizontal = MasterdocDimens.Space24),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MasterdocLiteTokens.FlareBorder),
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = MasterdocDimens.Space24,
                    vertical = MasterdocDimens.Space24,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space16),
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
                                MasterdocLiteTokens.FlareBorder.copy(alpha = 0.22f),
                                CircleShape,
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .size(ringMid)
                            .border(
                                1.5.dp,
                                MasterdocLiteTokens.FlareBorder.copy(alpha = 0.35f),
                                CircleShape,
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .size(ringInner)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .border(2.dp, MasterdocLiteTokens.FlareTint, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        MasterdocLoadingIndicator(modifier = Modifier.size(28.dp))
                    }
                }
                MasterdocMonoLabel(
                    text = title,
                    modifier = Modifier.testTag(MasterdocTestTags.DETECT_LOADING_TITLE),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
