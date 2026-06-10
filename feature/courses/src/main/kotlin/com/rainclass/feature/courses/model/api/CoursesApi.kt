package com.rainclass.feature.courses.model.api

import com.rainclass.feature.courses.model.bean.CourseInfoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CoursesApi {
  @GET("v2/api/web/courses/list")
  suspend fun getCourseInfo(@Query("identity") identity: Int = 2): CourseInfoResponse
}
