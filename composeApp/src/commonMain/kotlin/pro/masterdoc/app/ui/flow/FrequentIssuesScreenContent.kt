package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.MasterdocTestTags
import pro.masterdoc.app.ui.theme.LiteOptionShape
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocLoadingIndicator
import pro.masterdoc.app.ui.theme.masterdocConvoBackground
import pro.masterdoc.app.ui.theme.masterdocReportDateLabel
import pro.masterdoc.domain.case.CaseReport
import pro.masterdoc.presentation.report.ReportListStore
import pro.masterdoc.presentation.root.RootComponent

@Composable
fun FrequentIssuesScreenContent(
    root: RootComponent,
    reportList: ReportListStore,
) {
    val state by reportList.states.collectAsState(initial = reportList.state)
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layout.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.hasMore && !state.isLoading && !state.isLoadingMore) {
            reportList.accept(ReportListStore.Intent.LoadMore)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(MasterdocTestTags.FREQUENT_ISSUES_SCREEN),
    ) {
        LiteAppHead(
            title = state.assistantName?.let { "Masterdoc · $it" } ?: "Masterdoc",
            subtitle = "Частые неисправности",
            onBack = root::onBack,
            modifier = Modifier.testTag(MasterdocTestTags.FREQUENT_ISSUES_HEAD),
        )

        state.error?.let { error ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MasterdocDimens.Space14, vertical = MasterdocDimens.Space8),
            ) {
                Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { reportList.accept(ReportListStore.Intent.Retry) }) {
                    Text("Повторить")
                }
            }
        }

        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    MasterdocLoadingIndicator()
                }
            }
            state.items.isEmpty() && !state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .masterdocConvoBackground(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Пока нет записей о неисправностях",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .masterdocConvoBackground(),
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = MasterdocDimens.Space14,
                        vertical = MasterdocDimens.Space10,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
                ) {
                    items(state.items, key = { it.id }) { report ->
                        FrequentIssueCard(report = report)
                    }
                    if (state.isLoadingMore) {
                        item("loading-more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(MasterdocDimens.Space12),
                                contentAlignment = Alignment.Center,
                            ) {
                                MasterdocLoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequentIssueCard(report: CaseReport) {
    val preview = report.result.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { report.result }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = LiteOptionShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(MasterdocDimens.Space14),
            verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
        ) {
            Text(
                text = preview,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = masterdocReportDateLabel(report.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
