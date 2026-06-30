package com.awr.streamhub.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*
import com.awr.streamhub.viewmodel.PlayerState
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaItem: MediaItem,
    episodeNumber: Int,
    state: PlayerState,
    onBack: () -> Unit,
    onProgress: (Long, Long) -> Unit,
    onNextEpisode: () -> Unit
) {
    val context = LocalContext.current
    
    // متغيرات الحالة
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var selectedSubtitle by remember { mutableStateOf("off") }
    
    // متغيرات القنص والتبديل
    var extractedVideoUrl by remember { mutableStateOf<String?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) } // التبديل بين WebView والمشغل

    val embedUrl = remember(mediaItem.id, episodeNumber) {
        if (episodeNumber > 0) "https://vidsrc.to/embed/tv/${mediaItem.id}/1/$episodeNumber"
        else "https://vidsrc.to/embed/movie/${mediaItem.id}"
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // تجهيز المشغل فقط عند التبديل
    LaunchedEffect(isPlayerReady, extractedVideoUrl) {
        if (isPlayerReady && extractedVideoUrl != null) {
            val mediaItemBuilder = ExoMediaItem.Builder()
                .setUri(extractedVideoUrl!!)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
            exoPlayer.setMediaItem(mediaItemBuilder.build())
            exoPlayer.prepare()
        }
    }

    // تتبع التقدم
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            if (isPlayerReady) {
                currentPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(1L)
                isPlaying = exoPlayer.isPlaying
                if (currentPosition > 0 && duration > 0) onProgress(currentPosition, duration)
            }
        }
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // 1. حالة الـ WebView (قبل بدء المشغل)
        if (!isPlayerReady) {
            BackgroundVideoExtractor(embedUrl = embedUrl) { url ->
                extractedVideoUrl = url
            }
            
            // زر التشغيل يظهر فوق الـ WebView عند إيجاد الرابط
            if (extractedVideoUrl != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Button(
                        onClick = { isPlayerReady = true },
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text("بدء المشاهدة الآن ▶", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        } 
        
        // 2. حالة المشغل (بعد التبديل)
        else {
            Box(Modifier.fillMaxSize().clickable { showControls = !showControls }) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(-1, -1)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // ── Controls Overlay ──────────────────────────────────────────────
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(Modifier.fillMaxSize()) {
                        // Top UI
                        Box(Modifier.fillMaxWidth().height(100.dp).align(Alignment.TopStart).background(Brush.verticalGradient(listOf(Color.Black.copy(.8f), Color.Transparent))))
                        
                        Row(Modifier.align(Alignment.TopStart).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                            Column {
                                Text(mediaItem.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (episodeNumber > 0) Text("Episode $episodeNumber", color = Color.White.copy(.7f), fontSize = 12.sp)
                            }
                        }

                        // Center Play/Pause
                        Box(Modifier.align(Alignment.Center).size(64.dp).clip(CircleShape).background(Color.Black.copy(.55f)).clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }, contentAlignment = Alignment.Center) {
                            Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 28.sp)
                        }

                        // Bottom Controls
                        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.9f)))).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                            Slider(value = progress, onValueChange = { exoPlayer.seekTo((it * duration).toLong()) }, colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold))
                            
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${formatMs(currentPosition)} / ${formatMs(duration)}", color = Color.White.copy(.8f), fontSize = 12.sp)
                                Row {
                                    if (state.videoInfo?.subtitles?.isNotEmpty() == true) TextButton(onClick = { showSubtitleMenu = true }) { Text("CC", color = Gold, fontSize = 13.sp) }
                                    if (episodeNumber > 0) TextButton(onClick = onNextEpisode) { Text("Next ▶▶", color = Color.White, fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Loading & Error Overlay
        if (!isPlayerReady && extractedVideoUrl == null) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(.6f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Gold)
                    Spacer(Modifier.height(12.dp))
                    Text("جاري البحث عن الرابط...", color = TextSoft, fontSize = 14.sp)
                }
            }
        }
        
        // Subtitle Menu
        if (showSubtitleMenu) {
            // ... (نفس كود القائمة الموجود لديك)
        }
    }
}

// دالة تنسيق الوقت
private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
