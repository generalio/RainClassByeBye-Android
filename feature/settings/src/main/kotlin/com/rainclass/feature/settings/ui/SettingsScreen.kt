package com.rainclass.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rainclass.core.config.designsystem.component.RainClassTopBar
import com.rainclass.core.config.model.AppSettings
import com.rainclass.core.config.model.LlmApiFormat
import com.rainclass.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  onBackClick: () -> Unit
) {
  val settings by viewModel.settings.collectAsState()
  val modelListState by viewModel.modelListState.collectAsState()
  var edited by remember(settings) { mutableStateOf(settings) }
  var formatExpanded by remember { mutableStateOf(false) }
  var modelExpanded by remember { mutableStateOf(false) }

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
      SettingsSection(title = "AI 模型配置") {
        ExposedDropdownMenuBox(
          expanded = formatExpanded,
          onExpandedChange = { formatExpanded = it }
        ) {
          OutlinedTextField(
            value = edited.llmApiFormat.label,
            onValueChange = {},
            label = { Text("接口格式") },
            modifier = Modifier
              .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth(),
            trailingIcon = {
              ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded)
            },
            readOnly = true,
            singleLine = true
          )
          ExposedDropdownMenu(
            expanded = formatExpanded,
            onDismissRequest = { formatExpanded = false }
          ) {
            LlmApiFormat.entries.forEach { format ->
              DropdownMenuItem(
                text = { Text(format.label) },
                onClick = {
                  val shouldUseDefaultBaseUrl = edited.baseUrl.isBlank() ||
                    edited.baseUrl == edited.llmApiFormat.defaultBaseUrl
                  edited = edited.copy(
                    llmApiFormat = format,
                    baseUrl = if (shouldUseDefaultBaseUrl) {
                      format.defaultBaseUrl
                    } else {
                      edited.baseUrl
                    }
                  )
                  modelExpanded = false
                  viewModel.clearModelListState()
                  formatExpanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = edited.apiKey,
          onValueChange = {
            edited = edited.copy(apiKey = it)
            modelExpanded = false
            viewModel.clearModelListState()
          },
          label = { Text("API Key") },
          modifier = Modifier.fillMaxWidth(),
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true
        )

        OutlinedTextField(
          value = edited.baseUrl,
          onValueChange = {
            edited = edited.copy(baseUrl = it)
            modelExpanded = false
            viewModel.clearModelListState()
          },
          label = { Text("Base URL") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        ExposedDropdownMenuBox(
          expanded = modelExpanded,
          onExpandedChange = {
            if (modelListState.models.isNotEmpty()) modelExpanded = it
          }
        ) {
          OutlinedTextField(
            value = edited.model,
            onValueChange = { edited = edited.copy(model = it) },
            label = { Text("模型名称") },
            modifier = Modifier
              .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
              .fillMaxWidth(),
            trailingIcon = {
              if (modelListState.models.isNotEmpty()) {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
              }
            },
            singleLine = true
          )
          ExposedDropdownMenu(
            expanded = modelExpanded,
            onDismissRequest = { modelExpanded = false }
          ) {
            modelListState.models.forEach { model ->
              DropdownMenuItem(
                text = { Text(model) },
                onClick = {
                  edited = edited.copy(model = model)
                  modelExpanded = false
                }
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = { viewModel.fetchModels(edited) },
            enabled = !modelListState.isLoading,
            modifier = Modifier.weight(1f)
          ) {
            Text(if (modelListState.isLoading) "获取中..." else "获取模型")
          }
          Button(
            onClick = { viewModel.updateSettings(edited) },
            modifier = Modifier.weight(1f)
          ) {
            Text("保存设置")
          }
        }

        modelListState.message?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
        modelListState.error?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }

      SettingsSection(title = "参数配置") {
        OutlinedTextField(
          value = edited.temperature.toString(),
          onValueChange = {
            edited = edited.copy(temperature = it.toFloatOrNull() ?: 0.1f)
          },
          label = { Text("Temperature") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true
        )

        OutlinedTextField(
          value = edited.maxCompletionTokens.toString(),
          onValueChange = {
            edited = edited.copy(maxCompletionTokens = it.toIntOrNull() ?: 2048)
          },
          label = { Text("Max Completion Tokens") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true
        )

        OutlinedTextField(
          value = edited.requestTimeoutSeconds.toString(),
          onValueChange = {
            edited = edited.copy(
              requestTimeoutSeconds = it.toLongOrNull() ?: 120L
            )
          },
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
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("自动交卷", style = MaterialTheme.typography.bodyLarge)
          Switch(
            checked = edited.submitPaper,
            onCheckedChange = { edited = edited.copy(submitPaper = it) }
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SettingsSection(
  title: String,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
      )
      content()
    }
  }
}
