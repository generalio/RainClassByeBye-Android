package com.rainclass.feature.exam.model.repository

import com.rainclass.feature.exam.model.bean.OptionsEntity
import com.rainclass.feature.exam.model.bean.ProblemsEntity
import com.rainclass.feature.exam.model.bean.SolverAnswer
import kotlinx.serialization.json.Json

object AnswerParser {
  private val json = Json { ignoreUnknownKeys = true; isLenient = true }

  private val trailingCommaRE = Regex(""",(\s*[}\]])""")
  private val bareJSONKeyRE = Regex("""([{,]\s*)([A-Za-z_][A-Za-z0-9_]*)(\s*:)""")
  private val resultFieldRE = Regex("""(?is)(?:["']?(?:result|answer)["']?)\s*[:=]\s*(\[[^\]]*]|"(?:\\.|[^"])*"|'(?:\\.|[^'])*'|[^,\r\n}]+)""")
  private val boxedAnswerRE = Regex("""\\boxed\s*\{([^{}]+)\}""")

  fun parseAnswer(raw: String, problem: ProblemsEntity): SolverAnswer? {
    parseStructuredAnswer(raw, problem.problemId)?.let { answer ->
      normalizeForSubmission(problem, answer)?.let { return it }
    }
    parseHeuristicAnswer(raw, problem)?.let { answer ->
      normalizeForSubmission(problem, answer)?.let { return it }
    }
    return null
  }

  fun parseStructuredAnswer(raw: String, expectedProblemId: Long): SolverAnswer? {
    for (answerText in answerTexts(raw)) {
      parseStructuredAnswerText(answerText, expectedProblemId)?.let { return it }
    }
    return null
  }

  fun parseHeuristicAnswer(raw: String, problem: ProblemsEntity): SolverAnswer? {
    val normalized = stripCodeFence(normalizePunctuation(raw)).trim()
    if (normalized.isEmpty()) return null

    // Try loose answer (regex for result field)
    parseLooseAnswer(normalized, problem.problemId)?.let { return it }

    // Try option answer
    parseOptionAnswer(normalized, problem)?.let { return it }

    // Try narrative answer
    parseNarrativeAnswer(normalized, problem)?.let { return it }

    return null
  }

  fun normalizeForSubmission(problem: ProblemsEntity, answer: SolverAnswer): SolverAnswer? {
    val result = normalizeResult(answer.result)
    if (result.isEmpty()) return null

    val normalized = SolverAnswer(problemId = problem.problemId, result = result)

    if (problem.options.isEmpty()) return normalized

    val keys = optionKeySet(problem.options)
    val choiceResult = normalizeChoiceResult(result, keys)
    if (choiceResult.isEmpty()) return null

    return normalized.copy(result = choiceResult)
  }

  fun randomChoiceFallback(problem: ProblemsEntity): SolverAnswer? {
    val keys = sortedOptionKeys(problem.options)
    if (keys.isEmpty()) return null
    return SolverAnswer(
      problemId = problem.problemId,
      result = listOf(keys.random())
    )
  }

  // Private helpers

  private fun answerTexts(raw: String): List<String> {
    val answerTexts = mutableListOf<String>()
    fun add(v: String) {
      val trimmed = v.trim()
      if (trimmed.isNotEmpty() && trimmed !in answerTexts) answerTexts.add(trimmed)
    }
    val normalized = stripCodeFence(normalizePunctuation(raw))
    add(normalized)
    extractJSONObjects(normalized).forEach { add(it) }

    tryUnquote(normalized)?.let { unquoted ->
      val u = stripCodeFence(normalizePunctuation(unquoted))
      add(u)
      extractJSONObjects(u).forEach { add(it) }
    }

    val repaired = repairJSONLike(normalized)
    add(repaired)
    extractJSONObjects(repaired).forEach { add(it) }

    tryUnquote(repaired)?.let { unquoted ->
      val u = stripCodeFence(normalizePunctuation(unquoted))
      add(u)
      extractJSONObjects(u).forEach { add(it) }
    }

    return answerTexts
  }

  private fun parseStructuredAnswerText(content: String, expectedProblemId: Long): SolverAnswer? {
    return try {
      val answer = json.decodeFromString<SolverAnswer>(content)
      if (answer.result.isNotEmpty()) {
        SolverAnswer(expectedProblemId, normalizeResult(answer.result))
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun parseLooseAnswer(raw: String, expectedProblemId: Long): SolverAnswer? {
    val match = resultFieldRE.find(raw) ?: return null
    val value = match.groupValues.getOrNull(1) ?: return null
    val result = parseLooseResultValue(value)
    if (result.isEmpty()) return null
    return SolverAnswer(expectedProblemId, result)
  }

  private fun parseOptionAnswer(raw: String, problem: ProblemsEntity): SolverAnswer? {
    val keys = optionKeySet(problem.options)
    if (keys.isEmpty()) return null
    val normalized = raw.trim()
    if (normalized.isEmpty()) return null

    // Try direct option tokens
    if (!normalized.contains(" ")) {
      val result = extractOptionKeys(normalized, keys)
      if (result.isNotEmpty()) return SolverAnswer(problem.problemId, result)
    }

    // Try lines with answer cues
    for (line in reverseNonEmptyLines(normalized)) {
      if (!containsAnswerCue(line)) continue
      val result = extractOptionKeys(line, keys)
      if (result.isNotEmpty()) return SolverAnswer(problem.problemId, result)
    }
    return null
  }

  private fun parseNarrativeAnswer(raw: String, problem: ProblemsEntity): SolverAnswer? {
    // Try boxed answers
    val boxed = extractBoxedAnswers(raw)
    if (boxed.isNotEmpty()) return SolverAnswer(problem.problemId, boxed)

    // Try narrative extraction
    val marked = extractNarrativeAnswer(raw)
    if (marked.isNotEmpty()) return SolverAnswer(problem.problemId, listOf(marked))

    // Freeform
    val freeform = sanitizeNarrative(raw)
    if (freeform.isEmpty()) return null
    return SolverAnswer(problem.problemId, listOf(freeform))
  }

  private fun parseLooseResultValue(raw: String): List<String> {
    var s = raw.trim().trim('{', '}').trim().trim('[', ']', '(', ')').trim()
    if (s.isEmpty()) return emptyList()

    tryUnquote(s)?.let { s = it.trim() }
    s = s.replace("'", "\"").trim('"').trim()
    if (s.isEmpty()) return emptyList()

    s = s.replace("，", ",").replace("、", ",").replace(";", ",")
      .replace("/", ",").replace("\n", ",").replace("\r", ",")

    return s.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }.distinct()
  }

  fun stripCodeFence(raw: String): String {
    var trimmed = raw.trim()
    if (trimmed.startsWith("```")) {
      val nl = trimmed.indexOf('\n')
      if (nl >= 0) trimmed = trimmed.substring(nl + 1)
    }
    trimmed = trimmed.trim()
    if (trimmed.endsWith("```")) trimmed = trimmed.removeSuffix("```")
    return trimmed.trim()
  }

  fun normalizePunctuation(raw: String): String {
    return raw
      .replace("“", "\"").replace("”", "\"")
      .replace("‘", "'").replace("’", "'")
      .replace("：", ":").replace("，", ",")
      .replace("（", "(").replace("）", ")")
      .replace("【", "[").replace("】", "]")
      .replace("｛", "{").replace("｝", "}")
  }

  fun repairJSONLike(raw: String): String {
    var repaired = raw.trim()
    repaired = repaired.replace("\\\"", "\"")
    repaired = repaired.replace("'", "\"")
    repaired = bareJSONKeyRE.replace(repaired) { m ->
      "${m.groupValues[1]}\"${m.groupValues[2]}\"${m.groupValues[3]}"
    }
    repaired = trailingCommaRE.replace(repaired) { m -> m.groupValues[1] }
    return repaired
  }

  fun extractJSONObjects(raw: String): List<String> {
    val objects = mutableListOf<String>()
    var start = -1
    var depth = 0
    var inString = false
    var escaped = false

    for (i in raw.indices) {
      val ch = raw[i]
      if (escaped) { escaped = false; continue }
      if (ch == '\\' && inString) { escaped = true; continue }
      if (ch == '"') { inString = !inString; continue }
      if (inString) continue
      when (ch) {
        '{' -> { if (depth == 0) start = i; depth++ }
        '}' -> {
          if (depth > 0) {
            depth--
            if (depth == 0 && start >= 0) {
              objects.add(raw.substring(start, i + 1))
              start = -1
            }
          }
        }
      }
    }
    return objects
  }

  private fun tryUnquote(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.length < 2) return null
    if (trimmed.first() != '"' && trimmed.first() != '`') return null
    if (trimmed.last() != trimmed.first()) return null
    return trimmed.substring(1, trimmed.length - 1)
      .replace("\\\"", "\"")
      .replace("\\n", "\n")
  }

  private fun normalizeResult(input: List<String>): List<String> {
    val seen = mutableSetOf<String>()
    return input.map { it.trim().trim('"') }.filter { it.isNotEmpty() && seen.add(it) }
  }

  private fun optionKeySet(options: List<OptionsEntity>): Set<String> {
    return options.map { it.key.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()
  }

  private fun sortedOptionKeys(options: List<OptionsEntity>): List<String> {
    return optionKeySet(options).sorted()
  }

  private fun normalizeChoiceResult(result: List<String>, keys: Set<String>): List<String> {
    val seen = mutableSetOf<String>()
    return result.flatMap { extractOptionKeys(it, keys) }.filter { seen.add(it) }
  }

  private fun extractOptionKeys(raw: String, keys: Set<String>): List<String> {
    var s = raw.uppercase()
    for (word in listOf("答案", "最终", "应选", "故选", "选择", "选项", "RESULT")) {
      s = s.replace(word, " ")
    }
    for (ch in listOf(":", "：", "=", "(", ")", "[", "]", "{", "}", "，", ",", "、", "/", ";", "；", "\n", "\r", "\t")) {
      s = s.replace(ch, " ")
    }
    val seen = mutableSetOf<String>()
    return s.split(" ").map { it.trim() }.filter { it.isNotEmpty() && it in keys && seen.add(it) }
  }

  private fun reverseNonEmptyLines(raw: String): List<String> {
    return raw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.reversed()
  }

  private fun containsAnswerCue(raw: String): Boolean {
    return raw.contains("答案") || raw.contains("最终") || raw.contains("故选") ||
      raw.contains("应选") || raw.contains("结果") || raw.lowercase().contains("result")
  }

  private fun extractBoxedAnswers(raw: String): List<String> {
    return boxedAnswerRE.findAll(raw).mapNotNull { match ->
      sanitizeNarrative(match.groupValues.getOrElse(1) { "" }).takeIf { it.isNotEmpty() }
    }.distinct().toList()
  }

  private fun extractNarrativeAnswer(raw: String): String {
    for (line in reverseNonEmptyLines(raw)) {
      if (!containsAnswerCue(line)) continue
      var answerText = line
      for (sep in listOf("：", ":", "=")) {
        val idx = answerText.lastIndexOf(sep)
        if (idx >= 0 && idx + sep.length < answerText.length) {
          answerText = answerText.substring(idx + sep.length)
          break
        }
      }
      val weiIdx = answerText.lastIndexOf("为")
      if (weiIdx >= 0 && weiIdx + "为".length < answerText.length) {
        answerText = answerText.substring(weiIdx + "为".length)
      }
      val result = sanitizeNarrative(answerText)
      if (result.isNotEmpty()) return result
    }
    return ""
  }

  private fun sanitizeNarrative(raw: String): String {
    val s = raw.trim().trim('"', '\'').trim()
    if (s.isEmpty()) return ""
    return s.split(Regex("\\s+")).joinToString(" ")
  }
}
