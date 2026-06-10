package com.rainclass.feature.homework.model.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkCoverResponse(
    val msg: String = "",
    val status: Int = 0,
    val success: Boolean = false,
    val data: HomeworkCoverData = HomeworkCoverData()
)

@Serializable
data class HomeworkCoverData(
    @SerialName("problem_count") val problemCount: Int = 0,
    @SerialName("total_score") val totalScore: Double = 0.0,
    @SerialName("start_time") val startTime: Long = 0,
    val deadline: Long = 0
)
