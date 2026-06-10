package com.rainclass.core.domain.usecase

import com.rainclass.core.model.CourseNode
import com.rainclass.core.network.api.RainClassApi

class GetCoursesUseCase(private val api: RainClassApi) {
    suspend operator fun invoke(): Result<List<CourseNode>> = runCatching {
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
