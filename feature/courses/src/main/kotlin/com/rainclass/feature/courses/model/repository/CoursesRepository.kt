package com.rainclass.feature.courses.model.repository

import com.rainclass.feature.courses.model.api.CoursesApi
import com.rainclass.feature.courses.model.bean.CourseNode

class CoursesRepository(private val api: CoursesApi) {
  suspend fun getCourses(): Result<List<CourseNode>> = runCatching {
    val response = api.getCourseInfo()
    if (response.code != 0) {
      throw Exception(response.msg.ifEmpty { "课程列表获取失败: code=${response.code}" })
    }
    if (response.errcode != 0) {
      throw Exception(response.errmsg.ifEmpty { "课程列表获取失败: errcode=${response.errcode}" })
    }
    response.courseData.list
  }
}
