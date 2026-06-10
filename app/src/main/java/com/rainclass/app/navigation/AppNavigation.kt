package com.rainclass.app.navigation

import com.rainclass.core.navigation3.RainRoute
import kotlinx.serialization.Serializable

@Serializable object Login : RainRoute
@Serializable object Home : RainRoute
@Serializable object Courses : RainRoute
@Serializable data class HomeworkList(val cid: Long) : RainRoute
@Serializable data class HomeworkDetail(val cid: Long, val leafId: Long) : RainRoute
@Serializable data class ExamProgress(val cid: Long, val examId: Long, val isResume: Boolean = false) : RainRoute
@Serializable object Settings : RainRoute
@Serializable object Status : RainRoute
