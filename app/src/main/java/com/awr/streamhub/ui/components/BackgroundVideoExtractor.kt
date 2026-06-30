package com.awr.streamhub.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.awr.streamhub.utils.StreamSnifferWebViewClient

@Composable
fun BackgroundVideoExtractor(
    embedUrl: String,
    onDirectUrlExtracted: (String) -> Unit
) {
    var isUrlFound by remember { mutableStateOf(false) }

    if (!isUrlFound) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    // إعطاء أبعاد ضئيلة جداً لكي يعتبره النظام شغالاً في الواجهة ولا يوقفه
                    layoutParams = ViewGroup.LayoutParams(1, 1) 
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false // للسماح للموقع ببدء تشغيل الفيديو تلقائياً في الخلفية لاستخراج الرابط
                        
                        // خداع الحماية كأننا متصفح كروم رسمي على الكمبيوتر لتجنب إعلانات الموبايل الموجهة
                        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                    }

                    // ربط الـ WebView بالصائد المخصص لنا
                    webViewClient = StreamSnifferWebViewClient { extractedVideoUrl ->
                        if (!isUrlFound) {
                            isUrlFound = true
                            onDirectUrlExtracted(extractedVideoUrl) // تمرير الرابط النظيف المكتشف
                        }
                    }
                }
            },
            modifier = Modifier
                .size(1.dp)
                .alpha(0.01f), // شبه شفاف تماماً وغير مرئي نهائياً للمستخدم
            update = { webView ->
                webView.loadUrl(embedUrl)
            }
        )
    }
}
