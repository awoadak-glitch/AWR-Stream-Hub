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
import com.awr.streamhub.data.models.SubtitleTrack
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
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var selectedSubtitle by remember { mutableStateOf("off") }

    // Build ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Load source when available
    LaunchedEffect(state.videoInfo) {
        val info = state.videoInfo ?: return@LaunchedEffect
        val bestSource = info.sources
            .sortedByDescending {
                when {
                    it.quality.contains("1080") -> 4
                    it.quality.contains("720") -> 3
                    it.quality.contains("480") -> 2
                    it.quality.contains("auto") -> 5
                    else -> 1
                }
            }
            .firstOrNull() ?: return@LaunchedEffect

        val mediaItemBuilder = ExoMediaItem.Builder()
            .setUri(bestSource.url)

        if (bestSource.isM3U8) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.prepare()
    }

    // Track progress
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(1L)
            isPlaying = exoPlayer.isPlaying
            if (currentPosition > 0 && duration > 0) {
                onProgress(currentPosition, duration)
            }
        }
    }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        // ── ExoPlayer View ──────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Loading Overlay ─────────────────────────────────────────────────
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Gold)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading stream...", color = TextSoft, fontSize = 14.sp)
                }
            }
        }

        // ── Error Overlay ───────────────────────────────────────────────────
        state.error?.let { err ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("⚠", fontSize = 40.sp)
                    Text("Stream unavailable", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(err, color = TextMuted, fontSize = 12.sp)
                    AWRButton("Go Back", onBack)
                }
            }
        }

        // ── Controls Overlay ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showControls && state.isLoading.not() && state.error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                // Top gradient + header
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.TopStart)
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(.8f), Color.Transparent)))
                )

                // Back button
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            mediaItem.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            "Episode $episodeNumber",
                            color = Color.White.copy(.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Center play/pause
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(.55f))
                        .clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause()
                            else exoPlayer.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPlaying) "⏸" else "▶",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }

                // Bottom gradient + controls
                val bottomBg = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.9f)))
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(bottomBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Progress bar
                    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                    Slider(
                        value = progress,
                        onValueChange = { newVal ->
                            exoPlayer.seekTo((newVal * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Color.White.copy(.3f)
                        )
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${formatMs(currentPosition)} / ${formatMs(duration)}",
                            color = Color.White.copy(.8f),
                            fontSize = 12.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Subtitle button
                            if (state.videoInfo?.subtitles?.isNotEmpty() == true) {
                                TextButton(onClick = { showSubtitleMenu = true }) {
                                    Text("CC", color = if (selectedSubtitle != "off") Gold else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Next episode
                            TextButton(onClick = onNextEpisode) {
                                Text("Next ▶▶", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Subtitle Menu ────────────────────────────────────────────────────
        if (showSubtitleMenu) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(.7f))
                    .clickable { showSubtitleMenu = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Subtitle", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        HorizontalDivider(color = Color.White.copy(.1f))

                        SubtitleOption("Off", selectedSubtitle == "off") {
                            selectedSubtitle = "off"
                            showSubtitleMenu = false
                        }

                        state.videoInfo?.subtitles?.forEach { sub ->
                            SubtitleOption(sub.label, selectedSubtitle == sub.lang) {
                                selectedSubtitle = sub.lang
                                showSubtitleMenu = false
                                // TODO: Load subtitle into ExoPlayer
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitleOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Gold.copy(.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (isSelected) Gold else TextSoft, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        if (isSelected) Text("✓", color = Gold, fontSize = 14.sp)
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
