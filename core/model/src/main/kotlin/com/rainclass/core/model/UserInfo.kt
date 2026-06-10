package com.rainclass.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: UserData = UserData()
)

@Serializable
data class UserData(
    val id: String = "",
    val name: String = "",
    val school: String = "",
    @SerialName("school_number") val schoolNumber: String = "",
    val avatar: String = ""
)
