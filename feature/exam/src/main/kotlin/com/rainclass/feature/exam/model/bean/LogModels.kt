package com.rainclass.feature.exam.model.bean

enum class LogLevel { INFO, SUCCESS, WARNING, ERROR }

data class LogEntry(
  val time: String,
  val level: LogLevel,
  val message: String
)
