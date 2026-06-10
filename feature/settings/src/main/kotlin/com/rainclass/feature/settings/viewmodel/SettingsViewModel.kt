package com.rainclass.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.core.config.datastore.SettingsDataStore
import com.rainclass.core.config.model.AppSettings
import com.rainclass.core.network.llm.LlmClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModelListUiState(
  val isLoading: Boolean = false,
  val models: List<String> = emptyList(),
  val message: String? = null,
  val error: String? = null
)

class SettingsViewModel(
  private val settingsDataStore: SettingsDataStore
) : ViewModel() {
  val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

  private val _modelListState = MutableStateFlow(ModelListUiState())
  val modelListState: StateFlow<ModelListUiState> = _modelListState.asStateFlow()

  fun updateSettings(settings: AppSettings) {
    viewModelScope.launch { settingsDataStore.updateSettings(settings) }
  }

  fun fetchModels(settings: AppSettings) {
    if (settings.apiKey.isBlank()) {
      _modelListState.value = ModelListUiState(error = "请先填写 API Key")
      return
    }
    if (settings.baseUrl.isBlank()) {
      _modelListState.value = ModelListUiState(error = "请先填写 Base URL")
      return
    }

    viewModelScope.launch {
      _modelListState.value = ModelListUiState(isLoading = true)
      runCatching {
        LlmClient(settings.requestTimeoutSeconds).listModels(settings)
      }.onSuccess { models ->
        _modelListState.value = ModelListUiState(
          models = models,
          message = if (models.isEmpty()) {
            "没有获取到可用模型，请检查服务是否支持模型列表接口"
          } else {
            "已获取 ${models.size} 个模型"
          }
        )
      }.onFailure { e ->
        _modelListState.value = ModelListUiState(error = e.message ?: "获取模型失败")
      }
    }
  }

  fun clearModelListState() {
    _modelListState.value = ModelListUiState()
  }
}
