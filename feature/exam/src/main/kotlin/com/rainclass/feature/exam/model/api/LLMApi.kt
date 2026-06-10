package com.rainclass.feature.exam.model.api

import com.rainclass.feature.exam.model.bean.ChatCompletionRequest
import com.rainclass.feature.exam.model.bean.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LLMApi {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
