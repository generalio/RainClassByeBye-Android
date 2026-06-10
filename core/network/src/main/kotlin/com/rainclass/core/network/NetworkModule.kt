package com.rainclass.core.network

import android.content.Context
import com.rainclass.core.network.cookie.PersistentCookieStore
import com.rainclass.core.network.interceptor.ExamInterceptor
import com.rainclass.core.network.interceptor.RainClassInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
  }

  fun provideCookieStore(context: Context): PersistentCookieStore {
    return PersistentCookieStore(context)
  }

  fun provideCookieClient(cookieStore: PersistentCookieStore): OkHttpClient {
    return OkHttpClient.Builder()
      .cookieJar(cookieStore)
      .build()
  }

  fun provideRainClassRetrofit(cookieStore: PersistentCookieStore): Retrofit {
    val client = OkHttpClient.Builder()
      .cookieJar(cookieStore)
      .addInterceptor(RainClassInterceptor(cookieStore))
      .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .followRedirects(true)
      .build()

    return Retrofit.Builder()
      .baseUrl("https://changjiang.yuketang.cn/")
      .client(client)
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
  }

  fun provideExamRetrofit(cookieStore: PersistentCookieStore): Retrofit {
    val client = OkHttpClient.Builder()
      .cookieJar(cookieStore)
      .addInterceptor(ExamInterceptor())
      .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .followRedirects(false)
      .build()

    return Retrofit.Builder()
      .baseUrl("https://changjiang-exam.yuketang.cn/")
      .client(client)
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
  }

}
