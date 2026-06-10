package com.rainclass.core.network.api

import com.rainclass.core.model.*
import retrofit2.http.*

interface RainClassApi {
    @GET("v/course_meta/user_info")
    suspend fun triggerCsrf(): retrofit2.Response<Unit>

    @POST("api/v3/user/login/wechat-auth-param")
    suspend fun getWxOauthInfo(): WxOauthResponse

    @GET("api/v3/user/basic-info")
    suspend fun getUserInfo(): UserInfoResponse

    @GET("v2/api/web/courses/list")
    suspend fun getCourseInfo(@Query("identity") identity: Int = 2): CourseInfoResponse

    @GET("mooc-api/v1/lms/learn/course/chapter")
    suspend fun getHomeworkInfo(
        @Query("cid") cid: Long,
        @Query("classroom_id") classroomId: Long
    ): HomeworkInfoResponse

    @GET("mooc-api/v1/lms/learn/leaf_info/{cid}/{leafId}/")
    suspend fun getHomeworkDetails(
        @Path("cid") cid: Long,
        @Path("leafId") leafId: Long,
        @Header("classroom-id") classroomIdHeader: String
    ): HomeworkDetailsResponse

    @GET("v/exam/cover")
    suspend fun getHomeworkCover(
        @Query("exam_id") examId: Long,
        @Query("classroom_id") classroomId: Long
    ): HomeworkCoverResponse

    @POST("v/exam/gen_token")
    suspend fun examGenToken(@Body request: ExamGenTokenRequest): ExamGenTokenResponse
}
