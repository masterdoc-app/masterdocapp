package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.LiteFieldShape
import pro.masterdoc.app.ui.theme.LiteListenPanel
import pro.masterdoc.app.ui.theme.MasterdocColors
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocPrimaryButton
import pro.masterdoc.app.ui.theme.MasterdocSecondaryButton
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.summary.SummaryStore

@Composable
fun SummaryScreenContent(
    root: RootComponent,
    summary: SummaryStore,
) {
    val menu = rememberLiteFlowMenuState(root)
    val state by summary.states.collectAsState(initial = summary.state)
    var textMode by remember { mutableStateOf(true) }
    var isListening by remember { mutableStateOf(false) }
    val canFinish = state.resolved.trim().length >= 3 && state.cause.trim().length >= 3

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            root.onFinishAndRestart()
        }
    }

    LiteFlowDropdownMenu(menu)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        LiteAppHead(
            title = "Запись о сбое",
            subtitle = when {
                state.isSubmitted -> "Итог отправлен"
                state.isSaved -> "Черновик сохранён"
                else -> "Итог устранения · обязательно"
            },
            onBack = root::onBack,
            onMenuClick = menu.onOpen,
            markLetter = "M",
            markColor = if (state.isSaved) MasterdocColors.Success else MaterialTheme.colorScheme.tertiary,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MasterdocDimens.Space16, vertical = MasterdocDimens.Space14),
            verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space10),
        ) {
            SummaryField(
                label = "Сообщили",
                value = state.reported,
                onValueChange = { summary.accept(SummaryStore.Intent.ReportedChanged(it)) },
                enabled = textMode,
            )
            SummaryField(
                label = "Сделали",
                value = state.resolved,
                onValueChange = { summary.accept(SummaryStore.Intent.ResolvedChanged(it)) },
                enabled = textMode,
                required = true,
            )
            SummaryField(
                label = "Причина",
                value = state.cause,
                onValueChange = { summary.accept(SummaryStore.Intent.CauseChanged(it)) },
                enabled = textMode,
                required = true,
            )
            SummaryField(
                label = "Далее",
                value = state.nextSteps,
                onValueChange = { summary.accept(SummaryStore.Intent.NextStepsChanged(it)) },
                enabled = textMode,
            )
        }

        if (!textMode) {
            LiteListenPanel(
                label = "Итог · голос",
                hint = if (isListening) "Записываю…" else "Опишите результат",
                onMicClick = { isListening = !isListening },
                isListening = isListening,
            )
        }

        MasterdocSecondaryButton(
            text = if (textMode) "Голосом" else "Текстом",
            onClick = { textMode = !textMode },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MasterdocDimens.Space14),
            fillMaxWidth = true,
        )

        state.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = MasterdocDimens.Space14),
            )
        }

        RowActions(
            isSaved = state.isSaved,
            isSubmitting = state.isSubmitting,
            canFinish = canFinish,
            onSave = { summary.accept(SummaryStore.Intent.Save) },
            onDone = { summary.accept(SummaryStore.Intent.SubmitCase) },
        )
    }
}

@Composable
private fun SummaryField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    required: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space4)) {
        Text(
            text = buildString {
                append(label.uppercase())
                if (required) append(" *")
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (enabled) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = LiteFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        } else {
            Text(text = value.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RowActions(
    isSaved: Boolean,
    isSubmitting: Boolean,
    canFinish: Boolean,
    onSave: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space12),
        verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
    ) {
        if (!isSaved) {
            MasterdocSecondaryButton(
                text = "Сохранить в базу",
                onClick = onSave,
                fillMaxWidth = true,
            )
        }
        MasterdocPrimaryButton(
            text = if (isSubmitting) "Отправляем…" else "Готово · отправить итог",
            onClick = onDone,
            enabled = canFinish && !isSubmitting,
        )
    }
}
