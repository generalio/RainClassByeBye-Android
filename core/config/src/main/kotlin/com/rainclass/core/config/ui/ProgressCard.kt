package com.rainclass.core.config.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressCard(
  current: Int,
  total: Int,
  failed: Int,
  modifier: Modifier = Modifier,
  title: String = "答题进度"
) {
  val progress by animateFloatAsState(
    targetValue = if (total > 0) current.toFloat() / total else 0f,
    label = "progress"
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(12.dp))
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "已完成: $current / $total",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary
        )
        if (failed > 0) {
          Text(
            text = "失败: $failed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    }
  }
}
