package com.rainclass.feature.login.model.api

import com.rainclass.feature.login.model.bean.WxOauthResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginApi {
  @GET("v/course_meta/user_info")
  suspend fun triggerCsrf(): Response<Unit>

  @POST("api/v3/user/login/wechat-auth-param")
  suspend fun getWxOauthInfo(): WxOauthResponse
}
