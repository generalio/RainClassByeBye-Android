package com.rainclass.core.domain.usecase

import com.rainclass.core.model.CourseNode
import com.rainclass.core.network.api.RainClassApi

class GetCoursesUseCase(private val api: RainClassApi) {
    suspend operator fun invoke(): Result<List<CourseNode>> = runCatching {
        api.getCourseInfo().courseData.list
    }
}
