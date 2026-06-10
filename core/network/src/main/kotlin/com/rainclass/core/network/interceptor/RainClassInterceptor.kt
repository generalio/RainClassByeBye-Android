package com.rainclass.core.network.interceptor

import com.rainclass.core.network.cookie.PersistentCookieStore
import okhttp3.Interceptor
import okhttp3.Response

class RainClassInterceptor(private val cookieStore: PersistentCookieStore) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val builder = request.newBuilder()
      .header("xtbz", "ykt")

    if (request.method == "POST") {
      cookieStore.getCsrfToken()?.let { csrf ->
        builder.header("X-CSRFToken", csrf)
      }
    }

    return chain.proceed(builder.build())
  }
}
