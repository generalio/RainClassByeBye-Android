package com.rainclass.feature.courses.model.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

internal object SafeCourseDataSerializer : JsonTransformingSerializer<CourseData>(CourseData.serializer()) {
  override fun transformDeserialize(element: JsonElement): JsonElement {
    return if (element is JsonObject) element else JsonObject(emptyMap())
  }
}

@Serializable
data class CourseInfoResponse(
  val code: Int = 0,
  val msg: String = "",
  val errcode: Int = 0,
  val errmsg: String = "",
  @SerialName("data")
  @Serializable(with = SafeCourseDataSerializer::class)
  val courseData: CourseData = CourseData()
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
