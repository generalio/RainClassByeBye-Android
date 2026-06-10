package com.rainclass.core.config.model

enum class LlmApiFormat(
  val label: String,
  val defaultBaseUrl: String
) {
  OPENAI_CHAT("OpenAI Chat Completions", "https://dashscope.aliyuncs.com/compatible-mode/v1"),
  OPENAI_RESPONSES("OpenAI Responses", "https://api.openai.com/v1"),
  ANTHROPIC_MESSAGES("Anthropic Messages", "https://api.anthropic.com/v1"),
  GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta")
}

data class AppSettings(
  val apiKey: String = "",
  val baseUrl: String = "https://dashscope.aliyuncs.com/compatible-mode/v1",
  val model: String = "qwen3.7-plus",
  val llmApiFormat: LlmApiFormat = LlmApiFormat.OPENAI_CHAT,
  val temperature: Float = 0.1f,
  val maxCompletionTokens: Int = 2048,
  val requestTimeoutSeconds: Long = 120,
  val workers: Int = 20,
  val submitPaper: Boolean = false
)
