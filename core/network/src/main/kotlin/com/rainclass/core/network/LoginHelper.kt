package com.rainclass.core.network

import com.rainclass.core.network.api.RainClassApi
import okhttp3.OkHttpClient
import okhttp3.Request

class LoginHelper(
    private val api: RainClassApi,
    private val httpClient: OkHttpClient
) {
    data class QRCodeResult(val uuid: String, val pngBytes: ByteArray)

    private val uuidRegex = Regex("""src\s*=\s*"/connect/qrcode/([^"]+)"""")
    private val wxCodeRegex = Regex("""wx_code='([^']*)'""")

    suspend fun getQRCode(): QRCodeResult {
        // Step 1: Trigger CSRF
        api.triggerCsrf()

        // Step 2: Get OAuth info
        val oauthInfo = api.getWxOauthInfo()

        // Step 3: Get QR code page to extract UUID
        val qrPageUrl = "https://open.weixin.qq.com/connect/qrconnect?" +
            "appid=${oauthInfo.data.appId}&state=${oauthInfo.data.state}" +
            "&redirect_uri=${java.net.URLEncoder.encode(oauthInfo.data.redirectUri, "UTF-8")}" +
            "&response_type=code&scope=snsapi_login"

        val pageContent = httpGet(qrPageUrl)
        val uuid = uuidRegex.find(pageContent)?.groupValues?.get(1)
            ?: throw Exception("无法从 QR 页面提取 UUID")

        // Step 4: Download QR code PNG
        val pngUrl = "https://open.weixin.qq.com/connect/qrcode/$uuid"
        val pngBytes = httpGetBytes(pngUrl)

        return QRCodeResult(uuid, pngBytes)
    }

    suspend fun pollForScan(uuid: String): Boolean {
        val url = "https://lp.open.weixin.qq.com/connect/l/qrconnect?uuid=$uuid"
        val content = httpGet(url)
        val wxCode = wxCodeRegex.find(content)?.groupValues?.get(1) ?: ""

        if (wxCode.isNotEmpty()) {
            // Step: Exchange wx_code for session
            val sessionUrl = "https://changjiang.yuketang.cn/api/v3/user/login/wechat-web-callback" +
                "?code=$wxCode&state="
            httpGet(sessionUrl) // The cookies will be automatically saved by CookieJar
            return true
        }
        return false
    }

    private fun httpGet(url: String): String {
        val request = Request.Builder().url(url).get().build()
        return httpClient.newCall(request).execute().use { response ->
            response.body?.string() ?: ""
        }
    }

    private fun httpGetBytes(url: String): ByteArray {
        val request = Request.Builder().url(url).get().build()
        return httpClient.newCall(request).execute().use { response ->
            response.body?.bytes() ?: ByteArray(0)
        }
    }
}
