package com.rainclass.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rainclass.core.designsystem.component.RainClassTopBar
import com.rainclass.core.model.AppSettings

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var edited by remember(settings) { mutableStateOf(settings) }

    Scaffold(
        topBar = { RainClassTopBar(title = "设置", onBackClick = onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AI 模型配置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = edited.apiKey,
                onValueChange = { edited = edited.copy(apiKey = it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            OutlinedTextField(
                value = edited.baseUrl,
                onValueChange = { edited = edited.copy(baseUrl = it) },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = edited.model,
                onValueChange = { edited = edited.copy(model = it) },
                label = { Text("模型名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()
            Text("参数配置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = edited.temperature.toString(),
                onValueChange = { edited = edited.copy(temperature = it.toFloatOrNull() ?: 0.1f) },
                label = { Text("Temperature") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = edited.maxCompletionTokens.toString(),
                onValueChange = { edited = edited.copy(maxCompletionTokens = it.toIntOrNull() ?: 2048) },
                label = { Text("Max Completion Tokens") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = edited.requestTimeoutSeconds.toString(),
                onValueChange = { edited = edited.copy(requestTimeoutSeconds = it.toLongOrNull() ?: 120L) },
                label = { Text("请求超时 (秒)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = edited.workers.toString(),
                onValueChange = { edited = edited.copy(workers = it.toIntOrNull() ?: 20) },
                label = { Text("Worker 数量") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("自动交卷", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = edited.submitPaper,
                    onCheckedChange = { edited = edited.copy(submitPaper = it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.updateSettings(edited) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("保存设置", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
