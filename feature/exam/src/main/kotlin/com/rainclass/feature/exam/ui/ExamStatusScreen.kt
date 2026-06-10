package com.rainclass.feature.exam.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rainclass.core.database.ExamStateEntity
import com.rainclass.core.config.designsystem.component.RainClassTopBar
import com.rainclass.core.config.designsystem.component.StatusChip
import com.rainclass.feature.exam.viewmodel.ExamStatusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExamStatusScreen(
  viewModel: ExamStatusViewModel,
  onBackClick: () -> Unit,
  onResume: (Long, Long) -> Unit
) {
  val states by viewModel.states.collectAsState()

  Scaffold(
    topBar = { RainClassTopBar(title = "任务状态", onBackClick = onBackClick) }
  ) { padding ->
    if (states.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("暂无任务记录", style = MaterialTheme.typography.titleMedium)
      }
    } else {
      LazyColumn(
        modifier = Modifier.padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(states, key = { it.id }) { state ->
          ExamStateItem(
            state = state,
            onResume = { onResume(state.cid, state.examId) },
            onDelete = { viewModel.delete(state) }
          )
        }
      }
    }
  }
}

@Composable
private fun ExamStateItem(
  state: ExamStateEntity,
  onResume: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
  val answered = remember(state.answeredJson) { countProblemRecords(state.answeredJson) }
  val failed = remember(state.failedJson) { countProblemRecords(state.failedJson) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = state.examTitle.ifBlank { "Exam ${state.examId}" },
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = "CID ${state.cid}  Exam ${state.examId}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        StatusChip(text = statusText(state.status), color = statusColor(state.status))
      }

      Text(
        text = "进度 $answered/${state.totalProblems}  失败 $failed  更新 ${dateFormat.format(Date(state.updatedAt))}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        IconButton(onClick = onResume) {
          Icon(Icons.Default.PlayArrow, contentDescription = "恢复任务")
        }
        IconButton(onClick = onDelete) {
          Icon(
            Icons.Default.Delete,
            contentDescription = "删除任务",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    }
  }
}

private fun countProblemRecords(json: String): Int {
  return Regex(""""problemId"\s*:""").findAll(json).count()
}

@Composable
private fun statusColor(status: String) = when (status) {
  "completed" -> MaterialTheme.colorScheme.primary
  "running" -> MaterialTheme.colorScheme.secondary
  "partial", "ready_to_submit" -> MaterialTheme.colorScheme.tertiary
  "interrupted" -> MaterialTheme.colorScheme.error
  else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun statusText(status: String): String = when (status) {
  "completed" -> "已完成"
  "running" -> "进行中"
  "partial" -> "部分完成"
  "ready_to_submit" -> "待交卷"
  "interrupted" -> "已中断"
  else -> "等待中"
}
