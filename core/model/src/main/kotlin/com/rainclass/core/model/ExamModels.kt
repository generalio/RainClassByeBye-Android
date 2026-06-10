package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamGenTokenResponse(
    val success: Boolean = false,
    val data: ExamTokenData = ExamTokenData()
)

@Serializable
data class ExamTokenData(
    val token: String = "",
    @SerialName("exam_host") val examHost: String = "",
    @SerialName("user_id") val userId: Long = 0
)

@Serializable
data class ExamPaperQuestionsResponse(
    val errcode: Int = 0,
    val data: ExamPaperData = ExamPaperData()
)

@Serializable
data class ExamPaperData(
    val problems: List<ProblemsEntity> = emptyList(),
    val title: String = ""
)

@Serializable
data class ProblemsEntity(
    @SerialName("problem_id") val problemId: Long = 0,
    @SerialName("Body")
    val body: String = "",
    @SerialName("TypeText") val typeText: String = "",
    val index: Long = 0,
    val score: Double = 0.0,
    @SerialName("Options")
    val options: List<OptionsEntity> = emptyList()
)

@Serializable
data class OptionsEntity(
    val key: String = "",
    val value: String = ""
)

@Serializable
data class SubmitAnswerRequest(
    val results: List<SubmitAnswerResult>,
    @SerialName("exam_id") val examId: Long,
    val record: List<String> = emptyList()
)

@Serializable
data class SubmitAnswerResult(
    @SerialName("problem_id") val problemId: Long,
    val result: List<String>,
    val time: Long
)

@Serializable
data class SubmitPaperRequest(
    val results: List<SubmitPaperResult>,
    @SerialName("exam_id") val examId: String
)

@Serializable
data class SubmitPaperResult(
    @SerialName("problem_id") val problemId: Long,
    val result: List<String>,
    val time: Long,
    @SerialName("show_answer") val showAnswer: String = "",
    @SerialName("is_answered") val isAnswered: Boolean = true,
    @SerialName("is_save") val isSave: Boolean = true
)

@Serializable
data class ApiResponse(
    val errcode: Int = 0,
    val errmsg: String = ""
)

@Serializable
data class StartExamPaperResponse(
    val errcode: Int = 0,
    val data: StartExamPaperData = StartExamPaperData()
)

@Serializable
data class StartExamPaperData(
    @SerialName("has_limit") val hasLimit: Boolean = false,
    @SerialName("time_past") val timePast: Long = 0,
    @SerialName("time_left") val timeLeft: Long = 0
)

@Serializable
data class ExamGenTokenRequest(
    @SerialName("exam_id") val examId: String,
    @SerialName("classroom_id") val classroomId: String
)

@Serializable
data class StartExamPaperRequest(
    @SerialName("exam_id") val examId: String
)
