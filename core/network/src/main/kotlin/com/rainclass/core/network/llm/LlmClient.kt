package com.rainclass.core.network.llm

import com.rainclass.core.config.model.AppSettings
import com.rainclass.core.config.model.LlmApiFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class LlmMessage(
  val role: String,
  val text: String,
  val imageUrls: List<String> = emptyList()
)

data class LlmCompletionRequest(
  val messages: List<LlmMessage>,
  val responseJson: Boolean = false
)

data class LlmCompletionResult(
  val text: String,
  val raw: String
)

class LlmClient(timeoutSeconds: Long) {
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
  }
  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun complete(
    settings: AppSettings,
    request: LlmCompletionRequest
  ): LlmCompletionResult = withContext(Dispatchers.IO) {
    when (settings.llmApiFormat) {
      LlmApiFormat.OPENAI_CHAT -> completeOpenAiChat(settings, request)
      LlmApiFormat.OPENAI_RESPONSES -> completeOpenAiResponses(settings, request)
      LlmApiFormat.ANTHROPIC_MESSAGES -> completeAnthropicMessages(settings, request)
      LlmApiFormat.GEMINI -> completeGemini(settings, request)
    }
  }

  suspend fun listModels(settings: AppSettings): List<String> = withContext(Dispatchers.IO) {
    when (settings.llmApiFormat) {
      LlmApiFormat.OPENAI_CHAT,
      LlmApiFormat.OPENAI_RESPONSES -> listBearerModels(settings)
      LlmApiFormat.ANTHROPIC_MESSAGES -> listAnthropicModels(settings)
      LlmApiFormat.GEMINI -> listGeminiModels(settings)
    }
  }

  private fun completeOpenAiChat(
    settings: AppSettings,
    request: LlmCompletionRequest
  ): LlmCompletionResult {
    val body = buildJsonObject {
      put("model", settings.model)
      put("temperature", settings.temperature)
      put("max_completion_tokens", settings.maxCompletionTokens)
      put("stream", false)
      if (request.responseJson) {
        putJsonObject("response_format") { put("type", "json_object") }
      }
      putJsonArray("messages") {
        request.messages.forEach { add(openAiChatMessage(it)) }
      }
    }

    val raw = postJson(
      url = endpoint(settings.baseUrl, "chat/completions"),
      body = body,
      headers = mapOf("Authorization" to "Bearer ${settings.apiKey}")
    )
    val text = json.parseToJsonElement(raw).jsonObject["choices"]
      ?.jsonArray
      ?.firstOrNull()
      ?.jsonObject
      ?.get("message")
      ?.jsonObject
      ?.get("content")
      ?.jsonPrimitive
      ?.contentOrNull
      .orEmpty()
    return LlmCompletionResult(text = text, raw = raw)
  }

  private fun completeOpenAiResponses(
    settings: AppSettings,
    request: LlmCompletionRequest
  ): LlmCompletionResult {
    val body = buildJsonObject {
      put("model", settings.model)
      put("temperature", settings.temperature)
      put("max_output_tokens", settings.maxCompletionTokens)
      val systemText = request.messages
        .filter { it.role == "system" }
        .joinToString("\n") { it.text }
      if (systemText.isNotBlank()) put("instructions", systemText)
      putJsonArray("input") {
        request.messages.filterNot { it.role == "system" }.forEach { message ->
          add(buildJsonObject {
            put("role", if (message.role == "assistant") "assistant" else message.role)
            put("content", openAiResponsesContent(message))
          })
        }
      }
    }

    val raw = postJson(
      url = endpoint(settings.baseUrl, "responses"),
      body = body,
      headers = mapOf("Authorization" to "Bearer ${settings.apiKey}")
    )
    return LlmCompletionResult(
      text = collectResponseOutputText(json.parseToJsonElement(raw)),
      raw = raw
    )
  }

  private fun completeAnthropicMessages(
    settings: AppSettings,
    request: LlmCompletionRequest
  ): LlmCompletionResult {
    val systemText = request.messages
      .filter { it.role == "system" }
      .joinToString("\n") { it.text }
    val chatMessages = request.messages.filterNot { it.role == "system" }

    val body = buildJsonObject {
      put("model", settings.model)
      put("max_tokens", settings.maxCompletionTokens)
      put("temperature", settings.temperature)
      if (systemText.isNotBlank()) put("system", systemText)
      putJsonArray("messages") {
        chatMessages.forEach { message ->
          add(buildJsonObject {
            put("role", if (message.role == "assistant") "assistant" else "user")
            putJsonArray("content") {
              add(textBlock(message.withImageUrlsAsText()))
            }
          })
        }
      }
    }

    val raw = postJson(
      url = endpoint(settings.baseUrl, "messages"),
      body = body,
      headers = mapOf(
        "x-api-key" to settings.apiKey,
        "anthropic-version" to ANTHROPIC_VERSION
      )
    )
    val text = json.parseToJsonElement(raw).jsonObject["content"]
      ?.jsonArray
      ?.mapNotNull { item ->
        val obj = item.jsonObject
        if (obj["type"]?.jsonPrimitive?.contentOrNull == "text") {
          obj["text"]?.jsonPrimitive?.contentOrNull
        } else {
          null
        }
      }
      ?.joinToString("\n")
      .orEmpty()
    return LlmCompletionResult(text = text, raw = raw)
  }

  private fun completeGemini(
    settings: AppSettings,
    request: LlmCompletionRequest
  ): LlmCompletionResult {
    val systemText = request.messages
      .filter { it.role == "system" }
      .joinToString("\n") { it.text }

    val body = buildJsonObject {
      if (systemText.isNotBlank()) {
        putJsonObject("system_instruction") {
          putJsonArray("parts") { add(textPart(systemText)) }
        }
      }
      putJsonArray("contents") {
        request.messages.filterNot { it.role == "system" }.forEach { message ->
          add(buildJsonObject {
            put("role", if (message.role == "assistant") "model" else "user")
            putJsonArray("parts") { add(textPart(message.withImageUrlsAsText())) }
          })
        }
      }
      putJsonObject("generationConfig") {
        put("temperature", settings.temperature)
        put("maxOutputTokens", settings.maxCompletionTokens)
        if (request.responseJson) {
          put("responseMimeType", "application/json")
        }
      }
    }

    val modelPath = if (settings.model.startsWith("models/")) {
      settings.model
    } else {
      "models/${settings.model}"
    }
    val raw = postJson(
      url = endpoint(
        baseUrl = settings.baseUrl,
        path = "$modelPath:generateContent?key=${settings.apiKey.urlEncode()}"
      ),
      body = body,
      headers = emptyMap()
    )
    val text = json.parseToJsonElement(raw).jsonObject["candidates"]
      ?.jsonArray
      ?.firstOrNull()
      ?.jsonObject
      ?.get("content")
      ?.jsonObject
      ?.get("parts")
      ?.jsonArray
      ?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
      ?.joinToString("\n")
      .orEmpty()
    return LlmCompletionResult(text = text, raw = raw)
  }

  private fun listBearerModels(settings: AppSettings): List<String> {
    val raw = getJson(
      url = endpoint(settings.baseUrl, "models"),
      headers = mapOf("Authorization" to "Bearer ${settings.apiKey}")
    )
    return json.parseToJsonElement(raw).jsonObject["data"]
      ?.jsonArray
      ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull }
      .orEmpty()
      .sorted()
  }

  private fun listAnthropicModels(settings: AppSettings): List<String> {
    val raw = getJson(
      url = endpoint(settings.baseUrl, "models"),
      headers = mapOf(
        "x-api-key" to settings.apiKey,
        "anthropic-version" to ANTHROPIC_VERSION
      )
    )
    return json.parseToJsonElement(raw).jsonObject["data"]
      ?.jsonArray
      ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull }
      .orEmpty()
      .sorted()
  }

  private fun listGeminiModels(settings: AppSettings): List<String> {
    val raw = getJson(
      url = endpoint(settings.baseUrl, "models?key=${settings.apiKey.urlEncode()}"),
      headers = emptyMap()
    )
    return json.parseToJsonElement(raw).jsonObject["models"]
      ?.jsonArray
      ?.mapNotNull { model ->
        val obj = model.jsonObject
        val methods = obj["supportedGenerationMethods"]?.jsonArray?.mapNotNull {
          it.jsonPrimitive.contentOrNull
        }.orEmpty()
        val name = obj["name"]?.jsonPrimitive?.contentOrNull
        if (methods.isEmpty() || methods.contains("generateContent")) {
          name?.removePrefix("models/")
        } else {
          null
        }
      }
      .orEmpty()
      .sorted()
  }

  private fun openAiChatMessage(message: LlmMessage): JsonObject {
    return buildJsonObject {
      put("role", message.role)
      if (message.imageUrls.isEmpty()) {
        put("content", message.text)
      } else {
        putJsonArray("content") {
          add(textBlock(message.text))
          message.imageUrls.forEach { url ->
            add(buildJsonObject {
              put("type", "image_url")
              putJsonObject("image_url") { put("url", url) }
            })
          }
        }
      }
    }
  }

  private fun openAiResponsesContent(message: LlmMessage): JsonArray {
    return buildJsonArray {
      add(buildJsonObject {
        put("type", if (message.role == "assistant") "output_text" else "input_text")
        put("text", message.text)
      })
      message.imageUrls.forEach { url ->
        add(buildJsonObject {
          put("type", "input_image")
          put("image_url", url)
        })
      }
    }
  }

  private fun collectResponseOutputText(element: JsonElement): String {
    return element.jsonObject["output"]
      ?.jsonArray
      .orEmpty()
      .flatMap { item ->
        item.jsonObject["content"]?.jsonArray.orEmpty().mapNotNull { content ->
          val obj = content.jsonObject
          if (obj["type"]?.jsonPrimitive?.contentOrNull == "output_text") {
            obj["text"]?.jsonPrimitive?.contentOrNull
          } else {
            null
          }
        }
      }
      .joinToString("\n")
  }

  private fun postJson(
    url: String,
    body: JsonObject,
    headers: Map<String, String>
  ): String {
    val request = Request.Builder()
      .url(url)
      .jsonHeaders(headers)
      .post(json.encodeToString(JsonObject.serializer(), body).toRequestBody(JSON_MEDIA_TYPE))
      .build()
    return execute(request)
  }

  private fun getJson(
    url: String,
    headers: Map<String, String>
  ): String {
    val request = Request.Builder()
      .url(url)
      .jsonHeaders(headers)
      .get()
      .build()
    return execute(request)
  }

  private fun execute(request: Request): String {
    client.newCall(request).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        throw IllegalStateException("HTTP ${response.code}: ${body.take(300)}")
      }
      return body
    }
  }

  private fun Request.Builder.jsonHeaders(headers: Map<String, String>): Request.Builder {
    header("Content-Type", "application/json")
    headers.forEach { (name, value) ->
      if (value.isNotBlank()) header(name, value)
    }
    return this
  }

  private fun endpoint(baseUrl: String, path: String): String {
    val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return normalizedBase + path.removePrefix("/")
  }

  private fun textBlock(text: String): JsonObject {
    return buildJsonObject {
      put("type", "text")
      put("text", text)
    }
  }

  private fun textPart(text: String): JsonObject {
    return buildJsonObject { put("text", text) }
  }

  private fun LlmMessage.withImageUrlsAsText(): String {
    if (imageUrls.isEmpty()) return text
    return buildString {
      append(text)
      append("\n题面图片 URL：")
      imageUrls.forEach { append("\n").append(it) }
    }
  }

  private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

  private companion object {
    val JSON_MEDIA_TYPE = "application/json".toMediaType()
    const val ANTHROPIC_VERSION = "2023-06-01"
  }
}
