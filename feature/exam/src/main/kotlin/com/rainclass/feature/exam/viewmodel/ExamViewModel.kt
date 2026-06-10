package com.rainclass.feature.exam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.feature.exam.model.repository.ExamProgress
import com.rainclass.feature.exam.model.repository.ExamRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExamViewModel(
  private val examRunnerFactory: () -> ExamRunner
) : ViewModel() {
  private var runner: ExamRunner? = null

  private val _progress = MutableStateFlow(ExamProgress())
  val progress: StateFlow<ExamProgress> = _progress

  fun startExam(cid: Long, examId: Long, isResume: Boolean = false) {
    val newRunner = examRunnerFactory()
    runner = newRunner

    viewModelScope.launch {
      newRunner.progress.collect { _progress.value = it }
    }

    viewModelScope.launch {
      newRunner.execute(cid, examId, isResume)
    }
  }

  fun cancel() {
    runner?.cancel()
  }

  override fun onCleared() {
    super.onCleared()
    runner?.cancel()
  }
}
