package com.rainclass.core.config.model

data class AppSettings(
    val apiKey: String = "",
    val baseUrl: String = "https://dashscope.aliyuncs.com/compatible-mode/v1",
    val model: String = "qwen3.7-plus",
    val temperature: Float = 0.1f,
    val maxCompletionTokens: Int = 2048,
    val requestTimeoutSeconds: Long = 120,
    val workers: Int = 20,
    val submitPaper: Boolean = false
)
