package com.rainclass.feature.exam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.core.database.AppDatabase
import com.rainclass.core.database.ExamStateEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExamStatusViewModel(
  private val database: AppDatabase
) : ViewModel() {
  val states: StateFlow<List<ExamStateEntity>> = database.examStateDao()
    .observeAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun delete(state: ExamStateEntity) {
    viewModelScope.launch {
      database.examStateDao().delete(state)
    }
  }
}
