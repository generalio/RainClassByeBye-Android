package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ExamStatus {
    PENDING, RUNNING, INTERRUPTED, PARTIAL, READY_TO_SUBMIT, COMPLETED
}

@Serializable
data class SolverAnswer(
    @SerialName("problem_id") val problemId: Long = 0,
    val result: List<String> = emptyList()
)

@Serializable
data class AnsweredRecord(
    val problemId: Long,
    val problemIndex: Long,
    val problemType: String,
    val result: List<String>,
    val model: String,
    val modelRawOutput: String,
    val submittedAtUnixMs: Long
)

@Serializable
data class FailedRecord(
    val problemId: Long,
    val attempts: Int,
    val lastError: String,
    val updatedAt: Long
)
