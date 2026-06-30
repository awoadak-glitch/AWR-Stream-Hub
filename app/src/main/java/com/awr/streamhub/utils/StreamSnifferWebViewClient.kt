package com.awr.streamhub.utils

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

class StreamSnifferWebViewClient(
    private val onVideoUrlFound: (String) -> Unit
) : WebViewClient() {

    // القائمة السوداء لمنع النوافذ المنبثقة والإعلانات المزعجة في سيرفرات الـ Embed
    private val adKeywords = setOf(
        "adsterra", "popads", "exoclick", "doubleclick", "googleads", 
        "onclickads", "adserver", "banner", "popunder", "juicyads",
        "clisre", "p some", "ymtracking", "highcpmgate", "onclickperformance"
    )

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

        // 1️⃣ مانع الإعلانات: إذا كان الرابط يحتوي على أي كلمة إعلانية، يتم حظره فوراً بإرجاع استجابة فارغة
        if (adKeywords.any { url.contains(it, ignoreCase = true) }) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
        }

        // 2️⃣ صيد الرابط المباشر: إذا طلب الموقع ملف البث المتكيف HLS أو رابط mp4 مباشر
        if (url.contains(".m3u8") || url.contains("master.m3u8") || url.contains("index.m3u8") || url.contains(".mp4")) {
            view?.post {
                onVideoUrlFound(url) // إرسال الرابط النظيف فوراً للمشغل الأصلي
            }
        }

        return super.shouldInterceptRequest(view, request)
    }
}
