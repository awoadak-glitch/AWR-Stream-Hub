package com.awr.streamhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.awr.streamhub.data.local.WatchHistoryEntity
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    history: List<WatchHistoryEntity>,
    onItemClick: (WatchHistoryEntity) -> Unit,
    onClearAll: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Watch History",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${history.size} entries",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            if (history.isNotEmpty()) {
                TextButton(onClick = { showClearDialog = true }) {
                    Text("Clear all", color = Red.copy(.8f), fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (history.isEmpty()) {
            EmptyState(
                message = "No watch history yet\nStart watching to track progress",
                emoji = "📺"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { entry ->
                    HistoryCard(entry = entry, onClick = { onItemClick(entry) })
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = Panel,
            title = { Text("Clear Watch History", color = TextPrimary) },
            text = { Text("This will delete all your watch history. This cannot be undone.", color = TextSoft) },
            confirmButton = {
                Button(
                    onClick = { onClearAll(); showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun HistoryCard(entry: WatchHistoryEntity, onClick: () -> Unit) {
    val accent = accentForType(entry.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(.06f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            Box(
                Modifier
                    .size(width = 80.dp, height = 60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(.3f))
            ) {
                if (entry.image.isNotEmpty()) {
                    AsyncImage(
                        model = entry.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Play overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color.Black, fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    entry.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Episode ${entry.episodeNumber}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                // Progress bar
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    LinearProgressIndicator(
                        progress = { entry.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = accent,
                        trackColor = Color.White.copy(.1f)
                    )
                    Text(
                        "${(entry.progressPercent * 100).toInt()}% watched • ${formatDate(entry.watchedAt)}",
                        color = TextHint,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
