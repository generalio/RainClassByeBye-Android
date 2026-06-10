package com.rainclass.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.core.domain.usecase.GetUserInfoUseCase
import com.rainclass.core.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: UserData? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val getUserInfo: GetUserInfoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadUserInfo() }

    fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            getUserInfo().fold(
                onSuccess = { _uiState.value = HomeUiState(user = it, isLoading = false) },
                onFailure = { _uiState.value = HomeUiState(error = it.message, isLoading = false) }
            )
        }
    }
}
