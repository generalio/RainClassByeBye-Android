package com.rainclass.feature.home.model.api

import com.rainclass.feature.home.model.bean.UserInfoResponse
import retrofit2.http.GET

interface HomeApi {
    @GET("api/v3/user/basic-info")
    suspend fun getUserInfo(): UserInfoResponse
}
