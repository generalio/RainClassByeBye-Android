package com.rainclass.feature.homework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.core.domain.usecase.GetHomeworkUseCase
import com.rainclass.core.model.ChapterNode
import com.rainclass.core.model.HomeworkCoverData
import com.rainclass.core.model.HomeworkDetailData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeworkListUiState(
    val chapters: List<ChapterNode> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class HomeworkDetailUiState(
    val detail: HomeworkDetailData? = null,
    val cover: HomeworkCoverData? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val startBlockedReason: String? = null
)

class HomeworkViewModel(
    private val getHomework: GetHomeworkUseCase
) : ViewModel() {
    private val _listState = MutableStateFlow(HomeworkListUiState())
    val listState: StateFlow<HomeworkListUiState> = _listState

    private val _detailState = MutableStateFlow(HomeworkDetailUiState())
    val detailState: StateFlow<HomeworkDetailUiState> = _detailState

    fun loadList(cid: Long) {
        viewModelScope.launch {
            _listState.value = HomeworkListUiState(isLoading = true)
            getHomework.getList(cid).fold(
                onSuccess = { _listState.value = HomeworkListUiState(chapters = it, isLoading = false) },
                onFailure = { _listState.value = HomeworkListUiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun loadDetail(cid: Long, leafId: Long) {
        viewModelScope.launch {
            _detailState.value = HomeworkDetailUiState(isLoading = true)
            val detailResult = getHomework.getDetails(cid, leafId)
            detailResult.fold(
                onSuccess = { detail ->
                    val coverResult = getHomework.getCover(cid, detail.contentInfo.leafTypeId)
                    coverResult.fold(
                        onSuccess = { cover ->
                            _detailState.value = HomeworkDetailUiState(detail = detail, cover = cover, isLoading = false)
                        },
                        onFailure = {
                            _detailState.value = HomeworkDetailUiState(
                                detail = detail,
                                isLoading = false,
                                startBlockedReason = it.message ?: "考试信息获取失败"
                            )
                        }
                    )
                },
                onFailure = { _detailState.value = HomeworkDetailUiState(error = it.message, isLoading = false) }
            )
        }
    }
}
