package com.rainclass.core.domain.usecase

import com.rainclass.core.model.UnauthenticatedException
import com.rainclass.core.model.UserData
import com.rainclass.core.network.api.RainClassApi

class GetUserInfoUseCase(private val api: RainClassApi) {
    suspend operator fun invoke(): Result<UserData> = runCatching {
        val response = api.getUserInfo()
        if (response.code != 0) {
            throw UnauthenticatedException(response.msg.ifEmpty { "登录已过期，请重新登录" })
        }
        response.data
    }
}
