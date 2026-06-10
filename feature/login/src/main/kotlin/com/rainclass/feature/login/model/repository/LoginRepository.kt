package com.rainclass.feature.login.model.repository

import com.rainclass.feature.login.model.api.LoginApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class LoginRepository(
  private val api: LoginApi,
  private val httpClient: OkHttpClient
) {
  data class QRCodeResult(val uuid: String, val pngBytes: ByteArray)

  private val uuidRegex = Regex("""src\s*=\s*"/connect/qrcode/([^"]+)"""")
  private val wxCodeRegex = Regex("""wx_code='([^']*)'""")

  private var oauthState: String = ""

  suspend fun getQRCode(): QRCodeResult = withContext(Dispatchers.IO) {
    api.triggerCsrf()

    val oauthInfo = api.getWxOauthInfo()
    val appId = oauthInfo.data.appId
    val redirectUri = oauthInfo.data.redirectUri
    oauthState = oauthInfo.data.state

    if (appId.isBlank() || redirectUri.isBlank() || oauthState.isBlank()) {
      throw Exception("微信 OAuth 参数缺失")
    }

    val qrPageUrl = "https://open.weixin.qq.com/connect/qrconnect?" +
      "appid=$appId" +
      "&redirect_uri=${URLEncoder.encode(redirectUri, "UTF-8")}" +
      "&response_type=code" +
      "&scope=snsapi_login" +
      "&state=$oauthState"

    val pageContent = httpGet(qrPageUrl)
    val uuid = uuidRegex.find(pageContent)?.groupValues?.get(1)
      ?: throw Exception("无法从 QR 页面提取 UUID")

    val pngBytes = httpGetBytes("https://open.weixin.qq.com/connect/qrcode/$uuid")
    if (pngBytes.isEmpty()) {
      throw Exception("QR 码图片下载失败")
    }

    QRCodeResult(uuid, pngBytes)
  }

  suspend fun pollForScan(uuid: String): Boolean = withContext(Dispatchers.IO) {
    val content = httpGet("https://lp.open.weixin.qq.com/connect/l/qrconnect?uuid=$uuid")
    val wxCode = wxCodeRegex.find(content)?.groupValues?.get(1) ?: ""

    if (wxCode.isNotEmpty()) {
      val sessionUrl = "https://changjiang.yuketang.cn/api/v3/user/login/wechat-web-callback" +
        "?path=${URLEncoder.encode("/v2/api/web/passport", "UTF-8")}" +
        "&code=$wxCode" +
        "&state=$oauthState"

      val request = Request.Builder()
        .url(sessionUrl)
        .get()
        .header("xtbz", "ykt")
        .build()

      httpClient.newCall(request).execute().use { }
      true
    } else {
      false
    }
  }

  private fun httpGet(url: String): String {
    val request = Request.Builder().url(url).get().build()
    return httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw Exception("GET 请求失败: HTTP ${response.code}")
      }
      response.body?.string() ?: ""
    }
  }

  private fun httpGetBytes(url: String): ByteArray {
    val request = Request.Builder().url(url).get().build()
    return httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw Exception("GET 请求失败: HTTP ${response.code}")
      }
      response.body?.bytes() ?: ByteArray(0)
    }
  }
}
