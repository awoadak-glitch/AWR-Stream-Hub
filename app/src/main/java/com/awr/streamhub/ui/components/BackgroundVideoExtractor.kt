package com.awr.streamhub.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.awr.streamhub.utils.StreamSnifferWebViewClient

@Composable
fun BackgroundVideoExtractor(
    embedUrl: String,
    onDirectUrlExtracted: (String) -> Unit
) {
    var foundUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // الـ WebView مرئي للمستخدم ليتفاعل مع الموقع
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
                        mediaPlaybackRequiresUserGesture = false
                        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                    }
                    webViewClient = StreamSnifferWebViewClient { url ->
                        foundUrl = url
                    }
                    loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // الزر الذي يظهر فقط عند إيجاد الرابط
        if (foundUrl != null) {
            Button(
                onClick = { onDirectUrlExtracted(foundUrl!!) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text("شغل الفيلم في المشغل المحترف ▶")
            }
        }
    }
}
