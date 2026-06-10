package com.rainclass.feature.login.model.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WxOauthResponse(
    val data: WxOauthData = WxOauthData()
)

@Serializable
data class WxOauthData(
    @SerialName("appId") val appId: String = "",
    val state: String = "",
    @SerialName("redirectUri") val redirectUri: String = ""
)
