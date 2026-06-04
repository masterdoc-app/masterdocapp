package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.LiteFieldShape
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocPrimaryButton
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.presentation.root.RootComponent
import pro.masterdoc.presentation.summary.SummaryStore

@Composable
fun SummaryScreenContent(
    root: RootComponent,
    summary: SummaryStore,
) {
    val menu = rememberLiteFlowMenuState(root)
    val state by summary.states.collectAsState(initial = summary.state)
    val canSubmit = state.report.trim().length >= 3

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            root.onFinishAndRestart()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        LiteAppHead(
            title = "Итог",
            subtitle = when {
                state.isSubmitted -> "Отправлено"
                state.isSubmitting -> "Отправляем…"
                else -> "Опишите результат"
            },
            onBack = root::onBack,
            menuAnchor = liteFlowMenuAnchor(menu),
            subtitleLive = state.isSubmitting,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MasterdocDimens.Space16, vertical = MasterdocDimens.Space14),
        ) {
            Text(
                text = "РЕЗУЛЬТАТ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = state.report,
                onValueChange = { summary.accept(SummaryStore.Intent.ReportChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = MasterdocDimens.Space8)
                    .testTag(MasterdocTestTags.SUMMARY_RESULT_INPUT),
                placeholder = { Text("Что сделали и чем закончилось…") },
                minLines = 8,
                shape = LiteFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }

        state.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MasterdocDimens.Space14),
            )
        }

        MasterdocPrimaryButton(
            text = if (state.isSubmitting) "Отправляем…" else "Отправить",
            onClick = { summary.accept(SummaryStore.Intent.SubmitReport) },
            modifier = Modifier
                .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space12)
                .testTag(MasterdocTestTags.SUMMARY_SUBMIT),
            enabled = canSubmit && !state.isSubmitting,
        )
    }
}
