package com.rainclass.feature.homework.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rainclass.core.config.designsystem.component.ErrorMessage
import com.rainclass.core.config.designsystem.component.LoadingIndicator
import com.rainclass.core.config.designsystem.component.RainClassTopBar
import com.rainclass.core.config.designsystem.component.StatusChip
import com.rainclass.feature.homework.model.bean.LeafNode
import com.rainclass.feature.homework.viewmodel.HomeworkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun HomeworkListScreen(
    cid: Long,
    viewModel: HomeworkViewModel,
    onBackClick: () -> Unit,
    onHomeworkClick: (Long, Long) -> Unit // cid, leafId
) {
    val uiState by viewModel.listState.collectAsState()

    LaunchedEffect(cid) { viewModel.loadList(cid) }

    Scaffold(
        topBar = { RainClassTopBar(title = "作业列表", onBackClick = onBackClick) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, modifier = Modifier.padding(padding))
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.chapters.forEach { chapter ->
                        item {
                            Text(
                                text = chapter.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(chapter.sectionLeafList) { leaf ->
                            HomeworkItem(leaf = leaf, onClick = { onHomeworkClick(cid, leaf.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworkItem(leaf: LeafNode, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(leaf.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StatusChip(
                    text = if (leaf.isAssessed) "已完成" else "未完成",
                    color = if (leaf.isAssessed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (leaf.leafType == 5) "作业" else "教学活动",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (leaf.scoreDeadline > 0) {
                    Text(
                        text = "截止: ${dateFormat.format(Date(leaf.scoreDeadline))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
