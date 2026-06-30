package com.awr.streamhub.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
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
import com.awr.streamhub.data.local.WatchHistoryEntity
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*
import com.awr.streamhub.viewmodel.HomeState
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    history: List<WatchHistoryEntity>,
    onItemClick: (MediaItem) -> Unit,
    onContinueWatching: (WatchHistoryEntity) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // ── Top Header ──────────────────────────────────────────────────────
        item {
            HomeHeader()
        }

        // ── Hero Banner ─────────────────────────────────────────────────────
        item {
            if (state.isLoading && state.banner.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .background(Panel2)
                ) {
                    val shimmer by rememberInfiniteTransition(label = "b").animateFloat(
                        .2f, .5f,
                        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "ba"
                    )
                    Box(Modifier.fillMaxSize().background(Panel3.copy(shimmer)))
                }
            } else if (state.banner.isNotEmpty()) {
                HeroBanner(items = state.banner, onItemClick = onItemClick)
            }
        }

        // ── Error ───────────────────────────────────────────────────────────
        state.error?.let { err ->
            item { ErrorBanner(message = err, onRetry = onRefresh) }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Continue Watching ────────────────────────────────────────────────
        if (history.isNotEmpty()) {
            item { Spacer(Modifier.height(24.dp)) }
            item {
                SectionTitle("Continue Watching", "▶")
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history) { entry ->
                        ContinueWatchingCard(
                            title = entry.title,
                            image = entry.image,
                            episodeNumber = entry.episodeNumber,
                            progress = entry.progressPercent,
                            onClick = { onContinueWatching(entry) }
                        )
                    }
                }
            }
        }

        // ── Trending Anime ───────────────────────────────────────────────────
        item { Spacer(Modifier.height(26.dp)) }
        item { SectionTitle("Trending Anime", "🔥") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            MediaRow(
                items = state.trendingAnime,
                isLoading = state.isLoading,
                onItemClick = onItemClick
            )
        }

        // ── Popular Anime ────────────────────────────────────────────────────
        item { Spacer(Modifier.height(26.dp)) }
        item { SectionTitle("Popular Anime", "◈") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            MediaRow(
                items = state.popularAnime,
                isLoading = state.isLoading,
                onItemClick = onItemClick
            )
        }

        // ── Top Movies ───────────────────────────────────────────────────────
        item { Spacer(Modifier.height(26.dp)) }
        item { SectionTitle("Top Movies", "▣") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            MediaRow(
                items = state.trendingMovies,
                isLoading = state.isLoading,
                onItemClick = onItemClick,
                cardWidth = 150.dp,
                cardHeight = 220.dp
            )
        }

        // ── K-Drama Hot ──────────────────────────────────────────────────────
        item { Spacer(Modifier.height(26.dp)) }
        item { SectionTitle("K-Drama Hot", "◆") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            MediaRow(
                items = state.hotDrama,
                isLoading = state.isLoading,
                onItemClick = onItemClick
            )
        }

        // ── Recently Added ────────────────────────────────────────────────────
        if (state.recentAnime.isNotEmpty()) {
            item { Spacer(Modifier.height(26.dp)) }
            item { SectionTitle("Recently Added", "🆕") }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                MediaRow(
                    items = state.recentAnime,
                    isLoading = state.isLoading,
                    onItemClick = onItemClick
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

// ─── Home Header ─────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "AWR Stream Hub",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Anime • Movies • K-Drama",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        AWRBadge(size = 46.dp)
    }
}

// ─── Hero Banner (Auto-scroll Pager) ─────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroBanner(items: List<MediaItem>, onItemClick: (MediaItem) -> Unit) {
    val pagerState = rememberPagerState { items.size }

    // Auto-scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000)
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(Modifier.fillMaxWidth().height(480.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            BannerPage(item = items[page], onClick = { onItemClick(items[page]) })
        }

        // Page indicators
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(items.size) { index ->
                val isSelected = index == pagerState.currentPage
                Box(
                    Modifier
                        .height(4.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Gold else Color.White.copy(.35f))
                )
            }
        }
    }
}

@Composable
private fun BannerPage(item: MediaItem, onClick: () -> Unit) {
    val accent = accentForType(item.type.name)

    Box(
        Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
    ) {
        // Background cover image
        if (item.cover.isNotEmpty() || item.image.isNotEmpty()) {
            AsyncImage(
                model = item.cover.takeIf { it.isNotEmpty() } ?: item.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(accent.copy(.7f), Bg)))
            )
        }

        // Multi-layer gradient overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(.2f),
                            Color.Transparent,
                            Color.Black.copy(.7f),
                            Color.Black.copy(.95f)
                        )
                    )
                )
        )

        // Content bottom
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 70.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Type badge
            Pill(
                text = "${item.type.emoji} ${item.type.label}",
                bg = accent.copy(.3f),
                fg = accent
            )

            // Title
            Text(
                item.title,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 32.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Meta row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.rating.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("★", color = Gold, fontSize = 14.sp)
                        Text(item.rating, color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                if (item.releaseDate.isNotEmpty()) {
                    Text("•", color = TextMuted)
                    Text(item.releaseDate, color = TextMuted, fontSize = 13.sp)
                }
                if (item.totalEpisodes > 0) {
                    Text("•", color = TextMuted)
                    Text("${item.totalEpisodes} EP", color = TextMuted, fontSize = 13.sp)
                }
            }

            // Description
            Text(
                item.description,
                color = TextSoft.copy(.8f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // CTA Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AWRButton(text = "▶  Watch Now", onClick = onClick)
                AWROutlinedButton(text = "+ Favorites", onClick = onClick)
            }
        }
    }
}

// ─── Horizontal Media Row ──────────────────────────────────────────────────────

@Composable
fun MediaRow(
    items: List<MediaItem>,
    isLoading: Boolean,
    onItemClick: (MediaItem) -> Unit,
    cardWidth: androidx.compose.ui.unit.Dp = 140.dp,
    cardHeight: androidx.compose.ui.unit.Dp = 210.dp
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading && items.isEmpty()) {
            items(6) { ShimmerCard(width = cardWidth, height = cardHeight) }
        } else {
            items(items) { item ->
                MediaPosterCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    width = cardWidth,
                    height = cardHeight
                )
            }
        }
    }
}
