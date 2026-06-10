package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseInfoResponse(
    @SerialName("course_data") val courseData: CourseData = CourseData()
)

@Serializable
data class CourseData(
    val list: List<CourseNode> = emptyList()
)

@Serializable
data class CourseNode(
    @SerialName("classroom_id") val classroomId: Long = 0,
    val name: String = "",
    val course: CourseBasic = CourseBasic(),
    val teacher: Teacher = Teacher(),
    @SerialName("students_count") val studentsCount: Int = 0
)

@Serializable
data class CourseBasic(val name: String = "")

@Serializable
data class Teacher(val name: String = "")
