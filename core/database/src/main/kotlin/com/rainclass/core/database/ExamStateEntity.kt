package com.rainclass.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_state")
data class ExamStateEntity(
  @PrimaryKey
  val id: String, // "${cid}_${examId}"
  val cid: Long,
  val examId: Long,
  val examTitle: String = "",
  val status: String = "pending",
  val totalProblems: Int = 0,
  val submittedPaper: Boolean = false,
  val lastError: String = "",
  val answeredJson: String = "{}",  // JSON map of problemId -> AnsweredRecord
  val failedJson: String = "{}",    // JSON map of problemId -> FailedRecord
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
