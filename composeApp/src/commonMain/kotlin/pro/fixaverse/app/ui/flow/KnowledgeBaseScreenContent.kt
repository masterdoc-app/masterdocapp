package pro.fixaverse.app.ui.flow

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
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.fixaverse.app.ui.chat.ChatRoleMessageBubble
import pro.fixaverse.app.ui.theme.LiteAppHead
import pro.fixaverse.app.ui.theme.LiteOptionShape
import pro.fixaverse.app.ui.theme.AppBranding
import pro.fixaverse.app.ui.theme.FixaverseDimens
import pro.fixaverse.app.ui.theme.FixaverseLoadingIndicator
import pro.fixaverse.app.ui.theme.FixaverseTestTags
import pro.fixaverse.app.ui.theme.fixaverseConvoBackground
import pro.fixaverse.app.ui.theme.fixaverseReportDateLabel
import pro.fixaverse.domain.case.CaseReport
import pro.fixaverse.domain.chat.toChatMessages
import pro.fixaverse.presentation.report.ReportListStore
import pro.fixaverse.presentation.root.RootComponent

@Composable
fun KnowledgeBaseScreenContent(
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
            .testTag(FixaverseTestTags.KNOWLEDGE_BASE_SCREEN),
    ) {
        LiteAppHead(
            title = AppBranding.screenTitle(state.assistantName),
            subtitle = "База знаний",
            onBack = root::onBack,
            modifier = Modifier.testTag(FixaverseTestTags.KNOWLEDGE_BASE_HEAD),
        )

        state.error?.let { error ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FixaverseDimens.Space14, vertical = FixaverseDimens.Space8),
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
                    FixaverseLoadingIndicator()
                }
            }
            state.items.isEmpty() && !state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .fixaverseConvoBackground(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Пока нет записей в базе знаний",
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
                        .fixaverseConvoBackground(),
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = FixaverseDimens.Space14,
                        vertical = FixaverseDimens.Space10,
                    ),
                    verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space8),
                ) {
                    items(state.items, key = { it.id }) { report ->
                        KnowledgeBaseReportCard(report = report)
                    }
                    if (state.isLoadingMore) {
                        item("loading-more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(FixaverseDimens.Space12),
                                contentAlignment = Alignment.Center,
                            ) {
                                FixaverseLoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KnowledgeBaseReportCard(report: CaseReport) {
    val transcriptMessages = report.transcript.toChatMessages()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = LiteOptionShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(FixaverseDimens.Space14),
            verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space10),
        ) {
            Text(
                text = "РЕЗУЛЬТАТ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = report.result.trim(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = fixaverseReportDateLabel(report.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (transcriptMessages.isNotEmpty()) {
                Text(
                    text = "ПЕРЕПИСКА",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space8)) {
                    transcriptMessages.forEach { message ->
                        ChatRoleMessageBubble(
                            role = message.role,
                            content = message.content,
                        )
                    }
                }
            }
        }
    }
}
