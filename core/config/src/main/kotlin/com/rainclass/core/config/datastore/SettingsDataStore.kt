package com.rainclass.core.config.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rainclass.core.config.model.AppSettings
import com.rainclass.core.config.model.LlmApiFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

  private object Keys {
    val API_KEY = stringPreferencesKey("api_key")
    val BASE_URL = stringPreferencesKey("base_url")
    val MODEL = stringPreferencesKey("model")
    val LLM_API_FORMAT = stringPreferencesKey("llm_api_format")
    val TEMPERATURE = floatPreferencesKey("temperature")
    val MAX_TOKENS = intPreferencesKey("max_completion_tokens")
    val TIMEOUT = longPreferencesKey("request_timeout_seconds")
    val WORKERS = intPreferencesKey("workers")
    val SUBMIT_PAPER = booleanPreferencesKey("submit_paper")
  }

  val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
    AppSettings(
      apiKey = prefs[Keys.API_KEY] ?: "",
      baseUrl = prefs[Keys.BASE_URL] ?: "https://dashscope.aliyuncs.com/compatible-mode/v1",
      model = prefs[Keys.MODEL] ?: "qwen3.7-plus",
      llmApiFormat = prefs[Keys.LLM_API_FORMAT]
        ?.let { runCatching { LlmApiFormat.valueOf(it) }.getOrNull() }
        ?: LlmApiFormat.OPENAI_CHAT,
      temperature = prefs[Keys.TEMPERATURE] ?: 0.1f,
      maxCompletionTokens = prefs[Keys.MAX_TOKENS] ?: 2048,
      requestTimeoutSeconds = prefs[Keys.TIMEOUT] ?: 120L,
      workers = prefs[Keys.WORKERS] ?: 20,
      submitPaper = prefs[Keys.SUBMIT_PAPER] ?: false
    )
  }

  suspend fun updateSettings(settings: AppSettings) {
    context.dataStore.edit { prefs ->
      prefs[Keys.API_KEY] = settings.apiKey
      prefs[Keys.BASE_URL] = settings.baseUrl
      prefs[Keys.MODEL] = settings.model
      prefs[Keys.LLM_API_FORMAT] = settings.llmApiFormat.name
      prefs[Keys.TEMPERATURE] = settings.temperature
      prefs[Keys.MAX_TOKENS] = settings.maxCompletionTokens
      prefs[Keys.TIMEOUT] = settings.requestTimeoutSeconds
      prefs[Keys.WORKERS] = settings.workers
      prefs[Keys.SUBMIT_PAPER] = settings.submitPaper
    }
  }

  suspend fun updateApiKey(apiKey: String) {
    context.dataStore.edit { it[Keys.API_KEY] = apiKey }
  }

  suspend fun updateModel(model: String) {
    context.dataStore.edit { it[Keys.MODEL] = model }
  }

  suspend fun updateBaseUrl(baseUrl: String) {
    context.dataStore.edit { it[Keys.BASE_URL] = baseUrl }
  }
}
