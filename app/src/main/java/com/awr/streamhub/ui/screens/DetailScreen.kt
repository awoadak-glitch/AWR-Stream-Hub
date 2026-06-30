package com.awr.streamhub.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.awr.streamhub.data.models.Episode
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*
import com.awr.streamhub.viewmodel.DetailState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailState,
    onBack: () -> Unit,
    onPlay: (Episode) -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val item = state.item
    if (item == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        return
    }

    val accent = accentForType(item.type.name)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteToggle) {
                        Text(
                            if (state.isFavorite) "♥" else "♡",
                            fontSize = 22.sp,
                            color = if (state.isFavorite) Red else TextSoft
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Bg
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // ── Cover / Backdrop ──────────────────────────────────────────────
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Cover image
                    val coverUrl = item.cover.takeIf { it.isNotEmpty() } ?: item.image
                    if (coverUrl.isNotEmpty()) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(accent.copy(.6f), Bg)))
                        )
                    }

                    // Gradient overlay
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Bg)
                                )
                            )
                    )

                    // Loading indicator
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Gold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // ── Main Info ─────────────────────────────────────────────────────
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Type badge
                    Pill("${item.type.emoji} ${item.type.label}", accent.copy(.25f), accent)

                    // Title
                    Text(
                        item.title,
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 30.sp
                    )

                    // Original title if different
                    if (item.titleEn.isNotEmpty() && item.titleEn != item.title) {
                        Text(
                            item.titleEn,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    // Meta pills row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        if (item.rating.isNotEmpty()) Pill("★ ${item.rating}", Gold.copy(.15f), Gold)
                        if (item.releaseDate.isNotEmpty()) Pill(item.releaseDate)
                        if (item.status.isNotEmpty()) Pill(item.status)
                        if (item.totalEpisodes > 0) Pill("${item.totalEpisodes} Episodes")
                        item.genres.take(3).forEach { Pill(it) }
                    }

                    // Description
                    var expanded by remember { mutableStateOf(false) }
                    Text(
                        item.description,
                        color = TextSoft,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                    if (!expanded && item.description.length > 150) {
                        Text(
                            "Read more",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { expanded = true }
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // CTA buttons
                    if (item.episodes.isNotEmpty()) {
                        val firstEp = item.episodes.first()
                        AWRButton(
                            text = "▶  Watch Episode 1",
                            onClick = { onPlay(firstEp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (item.totalEpisodes == 1 || item.type.name == "MOVIE") {
                        // Movie - play directly
                        AWRButton(
                            text = "▶  Watch Now",
                            onClick = {
                                onPlay(
                                    Episode(id = item.id, number = 1, title = item.title)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // AI Subtitle button
                    AWROutlinedButton(
                        text = "🤖 AI Translate Subtitles",
                        onClick = { /* AI subtitle feature */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Episodes List ──────────────────────────────────────────────────
            if (item.episodes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    SectionTitle("Episodes", "▶")
                    Spacer(Modifier.height(10.dp))
                }

                items(item.episodes) { episode ->
                    EpisodeRow(
                        episode = episode,
                        accent = accent,
                        onClick = { onPlay(episode) }
                    )
                }
            }

            // ── Error ──────────────────────────────────────────────────────────
            state.error?.let { err ->
                item {
                    Spacer(Modifier.height(12.dp))
                    ErrorBanner(message = "Some details unavailable: $err")
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, accent: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Color.White.copy(.06f))
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode number badge
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (episode.image != null) {
                    AsyncImage(
                        model = episode.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        "${episode.number}",
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    episode.title?.takeIf { it.isNotEmpty() }
                        ?: "Episode ${episode.number}",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (episode.airDate != null) {
                    Text(episode.airDate, color = TextMuted, fontSize = 11.sp)
                }
            }

            // Play icon
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = accent, fontSize = 13.sp)
            }
        }
    }
}
