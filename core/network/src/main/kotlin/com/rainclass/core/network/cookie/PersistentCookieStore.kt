package com.rainclass.core.network.cookie

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieStore(context: Context) : CookieJar {
  private val prefs: SharedPreferences = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
  private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

  init { loadFromPrefs() }

  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
    val host = url.host
    cookieStore.getOrPut(host) { mutableListOf() }.apply {
      // Remove existing cookies with same name
      cookies.forEach { newCookie ->
        removeAll { it.name == newCookie.name }
      }
      addAll(cookies)
    }
    persistToPrefs()
  }

  override fun loadForRequest(url: HttpUrl): List<Cookie> {
    return cookieStore[url.host]?.filter { !it.expiresAt.let { exp -> exp < System.currentTimeMillis() } } ?: emptyList()
  }

  fun getCsrfToken(): String? {
    return cookieStore["changjiang.yuketang.cn"]?.find { it.name == "csrftoken" }?.value
  }

  fun clearAll() {
    cookieStore.clear()
    prefs.edit().clear().apply()
  }

  fun hasCookies(): Boolean = cookieStore.isNotEmpty()

  private fun persistToPrefs() {
    val editor = prefs.edit()
    editor.clear()
    cookieStore.forEach { (host, cookies) ->
      val cookieStrings = cookies.map { encodeCookie(it) }
      editor.putStringSet("cookies_$host", cookieStrings.toSet())
    }
    editor.apply()
  }

  private fun loadFromPrefs() {
    prefs.all.forEach { (key, value) ->
      if (key.startsWith("cookies_") && value is Set<*>) {
        val host = key.removePrefix("cookies_")
        val cookies = value.mapNotNull { str ->
          (str as? String)?.let { decodeCookie(it, host) }
        }.toMutableList()
        if (cookies.isNotEmpty()) cookieStore[host] = cookies
      }
    }
  }

  private fun encodeCookie(cookie: Cookie): String {
    return "${cookie.name}=${cookie.value}|${cookie.domain}|${cookie.path}|${cookie.expiresAt}|${cookie.secure}|${cookie.httpOnly}"
  }

  private fun decodeCookie(encoded: String, host: String): Cookie? {
    return try {
      val parts = encoded.split("|")
      if (parts.size < 6) return null
      val nameValue = parts[0].split("=", limit = 2)
      if (nameValue.size < 2) return null
      Cookie.Builder()
        .name(nameValue[0])
        .value(nameValue[1])
        .domain(parts[1].ifEmpty { host })
        .path(parts[2].ifEmpty { "/" })
        .expiresAt(parts[3].toLongOrNull() ?: Long.MAX_VALUE)
        .apply {
          if (parts[4] == "true") secure()
          if (parts[5] == "true") httpOnly()
        }
        .build()
    } catch (_: Exception) { null }
  }
}
