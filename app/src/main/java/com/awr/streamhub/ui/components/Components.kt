package com.awr.streamhub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.ui.theme.*

// ─── AWR Logo Badge ──────────────────────────────────────────────────────────

@Composable
fun AWRBadge(size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Gold, Orange, Red))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "AWR",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.27f).sp
        )
    }
}

// ─── Pill tag ────────────────────────────────────────────────────────────────

@Composable
fun Pill(text: String, bg: Color = Color.White.copy(.08f), fg: Color = TextSoft) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, fg.copy(.18f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

// ─── Section Title ───────────────────────────────────────────────────────────

@Composable
fun SectionTitle(title: String, emoji: String = "", modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (emoji.isNotEmpty()) {
            Text(emoji, fontSize = 18.sp)
        }
        Text(
            title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.3).sp
        )
    }
}

// ─── Media Poster Card (Vertical) ────────────────────────────────────────────

@Composable
fun MediaPosterCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 140.dp,
    height: Dp = 210.dp
) {
    val accent = accentForType(item.type.name)

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel2),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Poster image
            if (item.image.isNotEmpty()) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder gradient
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(accent.copy(.6f), Bg)))
                )
            }

            // Gradient overlay bottom
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(.85f)),
                            startY = height.value * 0.4f
                        )
                    )
            )

            // Type badge top-right
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(accent.copy(.85f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(item.type.emoji, fontSize = 10.sp)
            }

            // Title + rating bottom
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                if (item.rating.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("★", color = Gold, fontSize = 11.sp)
                        Text(item.rating, color = TextSoft, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ─── Wide Result Card (Horizontal) ───────────────────────────────────────────

@Composable
fun MediaWideCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = accentForType(item.type.name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(.06f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            Box(
                Modifier
                    .size(width = 72.dp, height = 100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(.4f))
            ) {
                if (item.image.isNotEmpty()) {
                    AsyncImage(
                        model = item.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill(item.type.label + " " + item.type.emoji, accent.copy(.2f), accent)
                Text(
                    item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        item.description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.rating.isNotEmpty()) Pill("★ ${item.rating}", Gold.copy(.12f), Gold)
                    if (item.releaseDate.isNotEmpty()) Pill(item.releaseDate, Color.White.copy(.05f), TextMuted)
                    if (item.totalEpisodes > 0) Pill("${item.totalEpisodes} EP", Color.White.copy(.05f), TextMuted)
                }
            }
        }
    }
}

// ─── Continue Watching Card ───────────────────────────────────────────────────

@Composable
fun ContinueWatchingCard(
    title: String,
    image: String,
    episodeNumber: Int,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(190.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel2)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (image.isNotEmpty()) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.9f))))
            )

            // Play icon
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = Color.White, fontSize = 16.sp)
            }

            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                Text("EP $episodeNumber", color = TextMuted, fontSize = 10.sp)
                Text(
                    title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = Gold,
                    trackColor = Color.White.copy(.2f)
                )
            }
        }
    }
}

// ─── Shimmer Loading ─────────────────────────────────────────────────────────

@Composable
fun ShimmerCard(modifier: Modifier = Modifier, width: Dp = 140.dp, height: Dp = 210.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    Box(
        modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Panel2.copy(alpha))
    )
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
fun EmptyState(message: String, emoji: String = "🎬", modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(message, color = TextMuted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

// ─── Premium Button ───────────────────────────────────────────────────────────

@Composable
fun AWRButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

@Composable
fun AWROutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(.25f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

// ─── Error Banner ─────────────────────────────────────────────────────────────

@Composable
fun ErrorBanner(message: String, onRetry: (() -> Unit)? = null) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Red.copy(.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Red.copy(.3f))
    ) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠ $message", color = Red, fontSize = 13.sp, modifier = Modifier.weight(1f))
            if (onRetry != null) {
                TextButton(onClick = onRetry) { Text("Retry", color = Gold) }
            }
        }
    }
}
