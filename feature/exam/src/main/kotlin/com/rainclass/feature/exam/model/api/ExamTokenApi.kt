package com.rainclass.feature.exam.model.api

import com.rainclass.feature.exam.model.bean.ExamGenTokenRequest
import com.rainclass.feature.exam.model.bean.ExamGenTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ExamTokenApi {
    @POST("v/exam/gen_token")
    suspend fun examGenToken(@Body request: ExamGenTokenRequest): ExamGenTokenResponse
}
