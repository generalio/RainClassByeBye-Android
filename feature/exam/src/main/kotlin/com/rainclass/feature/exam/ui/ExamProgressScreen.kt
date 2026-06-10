package com.rainclass.feature.exam.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rainclass.core.config.designsystem.component.RainClassTopBar
import com.rainclass.core.config.designsystem.component.StatusChip
import com.rainclass.core.config.ui.ProgressCard
import com.rainclass.feature.exam.model.bean.ExamStatus
import com.rainclass.feature.exam.viewmodel.ExamViewModel

@Composable
fun ExamProgressScreen(
    cid: Long,
    examId: Long,
    isResume: Boolean,
    viewModel: ExamViewModel,
    onBackClick: () -> Unit
) {
    val progress by viewModel.progress.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(cid, examId) {
        viewModel.startExam(cid, examId, isResume)
    }

    LaunchedEffect(progress.logs.size) {
        if (progress.logs.isNotEmpty()) {
            listState.animateScrollToItem(progress.logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            RainClassTopBar(
                title = progress.examTitle.ifEmpty { "自动答题" },
                onBackClick = onBackClick,
                actions = {
                    if (progress.isRunning) {
                        IconButton(onClick = { viewModel.cancel() }) {
                            Icon(Icons.Default.Stop, contentDescription = "停止", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (statusText, statusColor) = when (progress.status) {
                    ExamStatus.PENDING -> "等待中" to MaterialTheme.colorScheme.onSurfaceVariant
                    ExamStatus.RUNNING -> "进行中" to MaterialTheme.colorScheme.primary
                    ExamStatus.INTERRUPTED -> "已中断" to MaterialTheme.colorScheme.error
                    ExamStatus.PARTIAL -> "部分完成" to MaterialTheme.colorScheme.tertiary
                    ExamStatus.READY_TO_SUBMIT -> "待交卷" to MaterialTheme.colorScheme.secondary
                    ExamStatus.COMPLETED -> "已完成" to MaterialTheme.colorScheme.primary
                }
                StatusChip(text = statusText, color = statusColor)
            }

            ProgressCard(
                current = progress.answeredCount,
                total = progress.totalProblems,
                failed = progress.failedCount
            )

            Text("执行日志", style = MaterialTheme.typography.titleSmall)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(progress.logs) { entry ->
                    LogItem(entry = entry)
                }
            }
        }
    }
}
