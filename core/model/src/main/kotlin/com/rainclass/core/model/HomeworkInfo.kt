package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkInfoResponse(
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
    @SerialName("is_assessed") val isAssessed: Boolean = false,
    @SerialName("start_time") val startTime: Long = 0,
    @SerialName("score_deadline") val scoreDeadline: Long = 0
)
