package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkCoverResponse(
    val data: HomeworkCoverData = HomeworkCoverData()
)

@Serializable
data class HomeworkCoverData(
    @SerialName("problem_count") val problemCount: Int = 0,
    @SerialName("total_score") val totalScore: Int = 0,
    @SerialName("start_time") val startTime: Long = 0,
    val deadline: Long = 0
)
