package com.rainclass.core.common

import com.rainclass.core.model.ProblemsEntity
import kotlinx.serialization.json.Json

object ImageUrlExtractor {
    private val imgRegex = Regex("""(?i)<img[^>]*src\s*=\s*\\?["']([^"']*?)\\?["']""")

    fun extractImageUrls(problem: ProblemsEntity): List<String> {
        val seen = mutableSetOf<String>()
        val urls = mutableListOf<String>()

        // From body
        collectUrls(problem.body, seen, urls)

        // From full JSON serialization (catches URLs in options etc.)
        try {
            val json = Json.encodeToString(ProblemsEntity.serializer(), problem)
            collectUrls(json, seen, urls)
        } catch (_: Exception) {}

        return urls
    }

    private fun collectUrls(content: String, seen: MutableSet<String>, urls: MutableList<String>) {
        imgRegex.findAll(content).forEach { match ->
            val url = match.groupValues.getOrElse(1) { "" }.trim()
            if (url.isNotEmpty() && seen.add(url)) {
                urls.add(url)
            }
        }
    }
}
