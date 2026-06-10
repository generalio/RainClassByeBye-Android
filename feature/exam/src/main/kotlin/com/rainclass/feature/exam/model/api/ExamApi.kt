package com.rainclass.feature.exam.model.api

import com.rainclass.feature.exam.model.bean.ApiResponse
import com.rainclass.feature.exam.model.bean.ExamPaperQuestionsResponse
import com.rainclass.feature.exam.model.bean.StartExamPaperRequest
import com.rainclass.feature.exam.model.bean.StartExamPaperResponse
import com.rainclass.feature.exam.model.bean.SubmitAnswerRequest
import com.rainclass.feature.exam.model.bean.SubmitPaperRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExamApi {
    @GET("login")
    suspend fun examLogin(
        @Query("exam_id") examId: Long,
        @Query("user_id") userId: Long,
        @Query("crypt") crypt: String,
        @Query("next") next: String,
        @Query("language") language: String = "zh"
    ): Response<Unit>

    @GET("start/{examId}")
    suspend fun startExam(
        @Path("examId") examId: Long,
        @Query("isFrom") isFrom: Int = 2
    ): Response<Unit>

    @POST("exam_room/start_paper")
    suspend fun startExamPaper(@Body request: StartExamPaperRequest): StartExamPaperResponse

    @GET("exam_room/show_paper")
    suspend fun getExamPaperQuestion(@Query("exam_id") examId: Long): ExamPaperQuestionsResponse

    @POST("exam_room/answer_problem")
    suspend fun submitAnswer(@Body request: SubmitAnswerRequest): ApiResponse

    @POST("exam_room/submit_paper")
    suspend fun submitPaper(@Body request: SubmitPaperRequest): ApiResponse
}
