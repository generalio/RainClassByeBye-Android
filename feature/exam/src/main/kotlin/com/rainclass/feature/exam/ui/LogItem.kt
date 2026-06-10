package com.rainclass.feature.exam.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rainclass.feature.exam.model.bean.LogEntry
import com.rainclass.feature.exam.model.bean.LogLevel

@Composable
fun LogItem(
    entry: LogEntry,
    modifier: Modifier = Modifier
) {
    val color = when (entry.level) {
        LogLevel.INFO -> MaterialTheme.colorScheme.onSurface
        LogLevel.SUCCESS -> Color(0xFF2E7D32)
        LogLevel.WARNING -> Color(0xFFE65100)
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = entry.time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = "[${entry.level.name.take(4).padEnd(4)}]",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.weight(1f)
        )
    }
}
