package com.rainclass.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ExamInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("xtbz", "cloud")
            .header("x-client", "web")
            .build()
        return chain.proceed(request)
    }
}
