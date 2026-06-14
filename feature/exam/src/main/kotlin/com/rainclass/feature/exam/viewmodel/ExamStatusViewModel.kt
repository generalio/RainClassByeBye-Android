package com.rainclass.feature.exam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.core.database.AppDatabase
import com.rainclass.core.database.ExamStateEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExamStatusViewModel(
  private val database: AppDatabase
) : ViewModel() {
  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  private var refreshJob: Job? = null

  val states: StateFlow<List<ExamStateEntity>> = database.examStateDao()
    .observeAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun refresh() {
    if (_isRefreshing.value) return
    refreshJob?.cancel()
    refreshJob = viewModelScope.launch {
      _isRefreshing.value = true
      delay(350)
      _isRefreshing.value = false
    }
  }

  fun delete(state: ExamStateEntity) {
    viewModelScope.launch {
      database.examStateDao().delete(state)
    }
  }
}
