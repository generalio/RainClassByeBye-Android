package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

internal object SafeHomeworkDataSerializer : JsonTransformingSerializer<HomeworkData>(HomeworkData.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonObject) element else JsonObject(emptyMap())
    }
}

@Serializable
data class HomeworkInfoResponse(
    val code: Int = 0,
    val msg: String = "",
    @Serializable(with = SafeHomeworkDataSerializer::class)
    val data: HomeworkData = HomeworkData()
)

@Serializable
data class HomeworkData(
    @SerialName("course_chapter") val courseChapter: List<ChapterNode> = emptyList()
)

@Serializable
data class ChapterNode(
    val name: String = "",
    @SerialName("section_leaf_list") val sectionLeafList: List<LeafNode> = emptyList()
)

@Serializable
data class LeafNode(
    val id: Long = 0,
    val name: String = "",
    @SerialName("leaf_type") val leafType: Int = 0,
    @SerialName("leafinfo_id") val leafInfoId: Long = 0,
    @SerialName("is_assessed") val isAssessed: Boolean = false,
    @SerialName("start_time") val startTime: Long = 0,
    @SerialName("score_deadline") val scoreDeadline: Long = 0
)
