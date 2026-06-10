package com.rainclass.feature.exam.model.repository

import com.rainclass.core.config.model.AppSettings
import com.rainclass.feature.exam.model.api.LLMApi
import com.rainclass.feature.exam.model.bean.ChatCompletionRequest
import com.rainclass.feature.exam.model.bean.ChatMessage
import com.rainclass.feature.exam.model.bean.ProblemsEntity
import com.rainclass.feature.exam.model.bean.ResponseFormat
import com.rainclass.feature.exam.model.bean.SolverAnswer
import kotlinx.serialization.json.*

class LLMSolver(
    private val llmApi: LLMApi,
    private val settings: AppSettings
) {
    private val systemPrompt = listOf(
        "你是一个雨课堂考试自动答题代理。",
        "你会收到题目 JSON 和若干题面图片 URL。",
        "你必须只返回一个 JSON 对象，不能返回 Markdown、代码块、解释、题目复述、LaTeX 推导或任何前后缀。",
        "输出的第一个字符必须是 {，最后一个字符必须是 }。",
        """返回格式固定为 {"problem_id":123,"result":["A"]}。""",
        "problem_id 必须填写题目里的 problem_id。",
        "不要把 JSON 再包成字符串，也不要省略 key 或字符串的双引号。",
        "选择题请优先返回选项 key，例如 A/B/C/D。",
        "多选题返回多个选项 key。",
        "填空题或主观题只返回最终答案，返回字符串数组，每个字符串对应一个答案，不要输出解题过程。",
        "如果你无法解析，就尽量给出最可能答案，但仍然必须输出合法 JSON。"
    ).joinToString("\n")

    fun modelName(): String = settings.model

    suspend fun solve(problem: ProblemsEntity): Triple<SolverAnswer, String, Exception?> {
        try {
            val messages = buildMessages(problem)

            // First attempt with JSON response format
            val request = ChatCompletionRequest(
                model = settings.model,
                messages = messages,
                temperature = settings.temperature,
                maxCompletionTokens = settings.maxCompletionTokens,
                responseFormat = ResponseFormat("json_object")
            )

            val response = llmApi.chatCompletion(
                authorization = "Bearer ${settings.apiKey}",
                request = request
            )

            val raw = response.choices.firstOrNull()?.message?.content ?: ""

            // Try structured parse
            val answer = AnswerParser.parseAnswer(raw, problem)
            if (answer != null) return Triple(answer, raw, null)

            // Repair attempt
            val repairMessages = messages + listOf(
                ChatMessage(role = "assistant", content = JsonPrimitive(raw)),
                ChatMessage(
                    role = "user",
                    content = JsonPrimitive(
                        "你上一个回答${if (raw.isBlank()) "为空字符串" else "不是纯 JSON"}，无法解析为合法 JSON。" +
                        "请基于同一道题重新输出一个 JSON 对象。" +
                        """只允许输出形如 {"problem_id":${problem.problemId},"result":["A"]} 的内容，不要解释，不要代码块，不要额外文本。"""
                    )
                )
            )

            val repairRequest = ChatCompletionRequest(
                model = settings.model,
                messages = repairMessages,
                temperature = settings.temperature,
                maxCompletionTokens = settings.maxCompletionTokens
            )

            val repairResponse = llmApi.chatCompletion(
                authorization = "Bearer ${settings.apiKey}",
                request = repairRequest
            )

            val repairedRaw = repairResponse.choices.firstOrNull()?.message?.content ?: ""
            val repairedAnswer = AnswerParser.parseAnswer(repairedRaw, problem)
            if (repairedAnswer != null) return Triple(repairedAnswer, repairedRaw, null)

            // Heuristic on original
            val heuristicAnswer = AnswerParser.parseHeuristicAnswer(raw, problem)
            if (heuristicAnswer != null) {
                val normalized = AnswerParser.normalizeForSubmission(problem, heuristicAnswer)
                if (normalized != null) return Triple(normalized, raw, null)
            }

            // Random fallback
            val fallback = AnswerParser.randomChoiceFallback(problem)
            if (fallback != null) {
                return Triple(fallback, repairedRaw.ifBlank { raw }, null)
            }

            return Triple(
                SolverAnswer(problem.problemId, emptyList()),
                raw,
                Exception("无法解析答案")
            )
        } catch (e: Exception) {
            // Try random fallback on error
            val fallback = AnswerParser.randomChoiceFallback(problem)
            if (fallback != null) return Triple(fallback, "", e)
            return Triple(SolverAnswer(problem.problemId, emptyList()), "", e)
        }
    }

    private fun buildMessages(problem: ProblemsEntity): List<ChatMessage> {
        val questionJson = Json.encodeToString(ProblemsEntity.serializer(), problem)
        val imageUrls = ImageUrlExtractor.extractImageUrls(problem)

        val contentParts = buildJsonArray {
            // Text content
            addJsonObject {
                put("type", "text")
                put("text", "只允许返回一个 JSON 对象，且第一个字符必须是 {，最后一个字符必须是 }。不要解释，不要复述题目，不要输出代码块。题目 JSON 如下：$questionJson")
            }
            // Image URLs
            for (url in imageUrls) {
                addJsonObject {
                    put("type", "image_url")
                    putJsonObject("image_url") {
                        put("url", url)
                    }
                }
            }
        }

        return listOf(
            ChatMessage(role = "system", content = JsonPrimitive(systemPrompt)),
            ChatMessage(role = "user", content = contentParts)
        )
    }
}
