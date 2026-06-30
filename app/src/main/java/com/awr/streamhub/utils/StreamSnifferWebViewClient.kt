class StreamSnifferWebViewClient(
    private val onVideoUrlFound: (String) -> Unit
) : WebViewClient() {

    private val adKeywords = setOf(
        "adsterra", "popads", "exoclick", "doubleclick", "googleads", 
        "onclickads", "adserver", "banner", "popunder", "juicyads",
        "clisre", "p some", "ymtracking", "highcpmgate", "onclickperformance"
    )

    // للتأكد من عدم إرسال نفس الرابط مراراً وتكراراً
    private var lastCapturedUrl: String? = null

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

        // 1. مانع الإعلانات
        if (adKeywords.any { url.contains(it, ignoreCase = true) }) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
        }

        // 2. صيد الرابط المباشر مع إضافة تدقيق إضافي
        if ((url.contains(".m3u8") || url.contains(".mp4")) && url != lastCapturedUrl) {
            
            // فلترة إضافية: نركز على الروابط التي تحتوي "master" أو "index" لأنها غالباً هي روابط البث الأساسية
            if (url.contains("master") || url.contains("index") || url.endsWith(".mp4")) {
                lastCapturedUrl = url
                view?.post {
                    onVideoUrlFound(url)
                }
            }
        }

        return super.shouldInterceptRequest(view, request)
    }
}
