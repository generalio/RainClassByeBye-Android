package com.rainclass.core.network

import com.rainclass.core.network.api.RainClassApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class LoginHelper(
    private val api: RainClassApi,
    private val httpClient: OkHttpClient
) {
    data class QRCodeResult(val uuid: String, val pngBytes: ByteArray)

    private val uuidRegex = Regex("""src\s*=\s*"/connect/qrcode/([^"]+)"""")
    private val wxCodeRegex = Regex("""wx_code='([^']*)'""")

    private var oauthState: String = ""

    suspend fun getQRCode(): QRCodeResult = withContext(Dispatchers.IO) {
        // Step 1: Trigger CSRF cookie
        api.triggerCsrf()

        // Step 2: Get OAuth info
        val oauthInfo = api.getWxOauthInfo()
        oauthState = oauthInfo.data.state

        // Step 3: Get QR code page to extract UUID
        val qrPageUrl = "https://open.weixin.qq.com/connect/qrconnect?" +
            "appid=${oauthInfo.data.appId}" +
            "&redirect_uri=${URLEncoder.encode(oauthInfo.data.redirectUri, "UTF-8")}" +
            "&response_type=code" +
            "&scope=snsapi_login" +
            "&state=${oauthInfo.data.state}"

        val pageContent = httpGet(qrPageUrl)
        val uuid = uuidRegex.find(pageContent)?.groupValues?.get(1)
            ?: throw Exception("无法从 QR 页面提取 UUID")

        // Step 4: Download QR code PNG
        val pngUrl = "https://open.weixin.qq.com/connect/qrcode/$uuid"
        val pngBytes = httpGetBytes(pngUrl)

        if (pngBytes.isEmpty()) {
            throw Exception("QR 码图片下载失败")
        }

        QRCodeResult(uuid, pngBytes)
    }

    suspend fun pollForScan(uuid: String): Boolean = withContext(Dispatchers.IO) {
        val url = "https://lp.open.weixin.qq.com/connect/l/qrconnect?uuid=$uuid"
        val content = httpGet(url)
        val wxCode = wxCodeRegex.find(content)?.groupValues?.get(1) ?: ""

        if (wxCode.isNotEmpty()) {
            // Exchange wx_code for session
            val sessionUrl = "https://changjiang.yuketang.cn/api/v3/user/login/wechat-web-callback" +
                "?path=${URLEncoder.encode("/v2/api/web/passport", "UTF-8")}" +
                "&code=$wxCode" +
                "&state=$oauthState"

            val request = Request.Builder()
                .url(sessionUrl)
                .get()
                .header("xtbz", "ykt")
                .build()

            httpClient.newCall(request).execute().use { /* cookies saved by CookieJar */ }
            true
        } else {
            false
        }
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
