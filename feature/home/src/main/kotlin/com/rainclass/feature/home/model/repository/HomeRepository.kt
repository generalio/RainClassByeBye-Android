package com.rainclass.feature.home.model.repository

import com.rainclass.core.config.model.UnauthenticatedException
import com.rainclass.feature.home.model.api.HomeApi
import com.rainclass.feature.home.model.bean.UserData

class HomeRepository(private val api: HomeApi) {
  suspend fun getUserInfo(): Result<UserData> = runCatching {
    val response = api.getUserInfo()
    if (response.code != 0) {
      throw UnauthenticatedException(response.msg.ifEmpty { "登录已过期，请重新登录" })
    }
    response.data
  }
}
