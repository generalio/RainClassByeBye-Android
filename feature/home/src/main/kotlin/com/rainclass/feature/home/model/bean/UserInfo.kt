package com.rainclass.feature.home.model.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

internal object SafeUserDataSerializer : JsonTransformingSerializer<UserData>(UserData.serializer()) {
  override fun transformDeserialize(element: JsonElement): JsonElement {
    return if (element is JsonObject) element else JsonObject(emptyMap())
  }
}

@Serializable
data class UserInfoResponse(
  val code: Int = 0,
  val msg: String = "",
  @Serializable(with = SafeUserDataSerializer::class)
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
