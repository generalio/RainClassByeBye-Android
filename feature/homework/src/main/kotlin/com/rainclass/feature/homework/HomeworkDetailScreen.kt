package com.rainclass.feature.homework

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.rainclass.core.designsystem.component.ErrorMessage
import com.rainclass.core.designsystem.component.LoadingIndicator
import com.rainclass.core.designsystem.component.RainClassTopBar
import com.rainclass.core.ui.InfoCard
import com.rainclass.core.ui.InfoRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeworkDetailScreen(
    cid: Long,
    leafId: Long,
    viewModel: HomeworkViewModel,
    onBackClick: () -> Unit,
    onStartExam: (Long, Long) -> Unit // cid, examId
) {
    val uiState by viewModel.detailState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(cid, leafId) { viewModel.loadDetail(cid, leafId) }

    Scaffold(
        topBar = { RainClassTopBar(title = "作业详情", onBackClick = onBackClick) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.error != null -> ErrorMessage(uiState.error!!, modifier = Modifier.padding(padding))
            uiState.detail != null -> {
                val detail = uiState.detail!!
                val cover = uiState.cover
                val canStart = cover != null && detail.contentInfo.leafTypeId > 0

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(title = detail.name) {
                        InfoRow("CID", cid.toString())
                        InfoRow("Leaf ID", detail.id.toString())
                        InfoRow("Exam ID", detail.contentInfo.leafTypeId.toString())
                        if (detail.publishTime > 0) InfoRow("发布时间", dateFormat.format(Date(detail.publishTime.toLong())))
                        if (detail.scoreDeadline > 0) InfoRow("截止时间", dateFormat.format(Date(detail.scoreDeadline.toLong())))
                        InfoRow("是否锁定", if (detail.isLocked) "是" else "否")
                        InfoRow("是否计分", if (detail.isScore) "是" else "否")
                    }

                    if (cover != null) {
                        InfoCard(title = "考试信息") {
                            InfoRow("题目数", cover.problemCount.toString())
                            InfoRow("总分", cover.totalScore.toString())
                            if (cover.startTime > 0) InfoRow("考试开始", dateFormat.format(Date(cover.startTime)))
                            if (cover.deadline > 0) InfoRow("考试截止", dateFormat.format(Date(cover.deadline)))
                        }
                    } else {
                        InfoCard(title = "考试信息") {
                            InfoRow("状态", uiState.startBlockedReason ?: "考试信息不可用")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onStartExam(cid, detail.contentInfo.leafTypeId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        enabled = canStart
                    ) {
                        Text("开始自动答题", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
