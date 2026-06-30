package com.awr.streamhub.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.awr.streamhub.utils.StreamSnifferWebViewClient

@Composable
fun BackgroundVideoExtractor(
    embedUrl: String,
    onDirectUrlExtracted: (String) -> Unit
) {
    // نستخدم AndroidView لعرض الـ WebView الذي سيتفاعل معه المستخدم مباشرة
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    // السماح بتشغيل الوسائط
                    mediaPlaybackRequiresUserGesture = false 
                    
                    // محاكاة متصفح سطح المكتب لتجنب قيود إعلانات الموبايل
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                }

                // الـ Sniffer الخاص بك سيستمر في مراقبة الطلبات في الخلفية
                webViewClient = StreamSnifferWebViewClient { extractedVideoUrl ->
                    // عند نجاح عملية الصيد، نمرر الرابط للخارج (الـ PlayerScreen سيهتم بإخفاء هذا المكون)
                    onDirectUrlExtracted(extractedVideoUrl)
                }
                
                loadUrl(embedUrl)
            }
        },
        modifier = Modifier.fillMaxSize() // الآن أصبح مرئياً ويغطي كامل مساحة الشاشة
    )
}
