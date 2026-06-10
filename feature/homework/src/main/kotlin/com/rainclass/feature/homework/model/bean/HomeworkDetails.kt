package com.rainclass.feature.homework.model.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkDetailsResponse(
  val data: HomeworkDetailData = HomeworkDetailData()
)

@Serializable
data class HomeworkDetailData(
  val id: Long = 0,
  val name: String = "",
  @SerialName("publish_time") val publishTime: Double = 0.0,
  @SerialName("score_deadline") val scoreDeadline: Double = 0.0,
  @SerialName("is_locked") val isLocked: Boolean = false,
  @SerialName("is_score") val isScore: Boolean = false,
  @SerialName("is_assessed") val isAssessed: Boolean = false,
  @SerialName("content_info") val contentInfo: ContentInfo = ContentInfo()
)

@Serializable
data class ContentInfo(
  @SerialName("leaf_type_id") val leafTypeId: Long = 0
)
