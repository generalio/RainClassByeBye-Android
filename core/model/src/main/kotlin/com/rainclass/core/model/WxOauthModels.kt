package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WxOauthResponse(
    val data: WxOauthData = WxOauthData()
)

@Serializable
data class WxOauthData(
    @SerialName("app_id") val appId: String = "",
    val state: String = "",
    @SerialName("redirect_uri") val redirectUri: String = ""
)
