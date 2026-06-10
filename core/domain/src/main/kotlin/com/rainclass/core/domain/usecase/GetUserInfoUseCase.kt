package com.rainclass.core.domain.usecase

import com.rainclass.core.model.UserData
import com.rainclass.core.network.api.RainClassApi

class GetUserInfoUseCase(private val api: RainClassApi) {
    suspend operator fun invoke(): Result<UserData> = runCatching {
        api.getUserInfo().data
    }
}
