package com.rainclass.feature.exam.model.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.1f,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int = 2048,
    val stream: Boolean = false,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: kotlinx.serialization.json.JsonElement
)

@Serializable
data class ChatCompletionResponse(
    val id: String = "",
    val model: String = "",
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ChoiceMessage = ChoiceMessage(),
    @SerialName("finish_reason") val finishReason: String = ""
)

@Serializable
data class ChoiceMessage(
    val role: String = "",
    val content: String = ""
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)
