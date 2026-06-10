package com.rainclass.core.network.api

import com.rainclass.core.model.*
import retrofit2.Response
import retrofit2.http.*

interface ExamApi {
    @GET("login")
    suspend fun examLogin(
        @Query("exam_id") examId: Long,
        @Query("user_id") userId: Long,
        @Query("crypt") crypt: String,
        @Query("next") next: String = "/exam_room"
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
