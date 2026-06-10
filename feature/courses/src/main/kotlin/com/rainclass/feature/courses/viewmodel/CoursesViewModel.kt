package com.rainclass.feature.courses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.feature.courses.model.bean.CourseNode
import com.rainclass.feature.courses.model.repository.CoursesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CoursesUiState(
    val courses: List<CourseNode> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CoursesViewModel(
    private val repository: CoursesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CoursesUiState(isLoading = true)
            repository.getCourses().fold(
                onSuccess = { _uiState.value = CoursesUiState(courses = it, isLoading = false) },
                onFailure = { _uiState.value = CoursesUiState(error = it.message, isLoading = false) }
            )
        }
    }
}
