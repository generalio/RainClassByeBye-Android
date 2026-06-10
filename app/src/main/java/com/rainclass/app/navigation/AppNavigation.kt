package com.rainclass.app.navigation

import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Home
@Serializable object Courses
@Serializable data class HomeworkList(val cid: Long)
@Serializable data class HomeworkDetail(val cid: Long, val leafId: Long)
@Serializable data class ExamProgress(val cid: Long, val examId: Long, val isResume: Boolean = false)
@Serializable object Settings
@Serializable object Status
