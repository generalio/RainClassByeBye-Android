package com.rainclass.feature.login.viewmodel

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainclass.feature.login.model.repository.LoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
  val qrCodeBitmap: ImageBitmap? = null,
  val isLoading: Boolean = false,
  val isScanning: Boolean = false,
  val isLoggedIn: Boolean = false,
  val error: String? = null,
  val statusText: String = "点击获取二维码"
)

class LoginViewModel(
  private val repository: LoginRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState

  private var currentUuid: String? = null

  fun loadQRCode() {
    viewModelScope.launch {
      _uiState.value = LoginUiState(isLoading = true, statusText = "正在获取二维码...")
      try {
        val result = repository.getQRCode()
        currentUuid = result.uuid
        val bitmap = withContext(Dispatchers.Default) {
          BitmapFactory.decodeByteArray(result.pngBytes, 0, result.pngBytes.size)
        }
        _uiState.value = LoginUiState(
          qrCodeBitmap = bitmap?.asImageBitmap(),
          isScanning = true,
          statusText = "请使用微信扫描二维码"
        )
        startPolling()
      } catch (e: Exception) {
        _uiState.value = LoginUiState(
          error = "获取二维码失败: ${e.message}",
          statusText = "获取失败，请重试"
        )
      }
    }
  }

  private fun startPolling() {
    viewModelScope.launch {
      val uuid = currentUuid ?: return@launch
      while (_uiState.value.isScanning) {
        try {
          val scanned = repository.pollForScan(uuid)
          if (scanned) {
            _uiState.value = _uiState.value.copy(
              isScanning = false,
              isLoggedIn = true,
              statusText = "登录成功！"
            )
            return@launch
          }
        } catch (_: Exception) { }
        delay(3000)
      }
    }
  }
}
