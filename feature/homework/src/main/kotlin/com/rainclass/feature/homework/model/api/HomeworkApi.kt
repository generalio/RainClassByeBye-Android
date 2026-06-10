package com.rainclass.feature.homework.model.api

import com.rainclass.feature.homework.model.bean.HomeworkCoverResponse
import com.rainclass.feature.homework.model.bean.HomeworkDetailsResponse
import com.rainclass.feature.homework.model.bean.HomeworkInfoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeworkApi {
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
}
