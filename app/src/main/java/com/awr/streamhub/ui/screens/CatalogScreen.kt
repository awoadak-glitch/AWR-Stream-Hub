package com.awr.streamhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*
import com.awr.streamhub.viewmodel.HomeState

@Composable
fun CatalogScreen(
    type: MediaType,
    state: HomeState,
    onItemClick: (MediaItem) -> Unit
) {
    val items = when (type) {
        MediaType.ANIME -> state.popularAnime + state.trendingAnime + state.recentAnime
        MediaType.MOVIE -> state.trendingMovies
        MediaType.KDRAMA -> state.hotDrama
        else -> emptyList()
    }.distinctBy { it.id }

    var selectedGenre by remember { mutableStateOf("All") }
    val genres = remember(items) {
        listOf("All") + items.flatMap { it.genres }.distinct().take(8)
    }

    val filtered = remember(items, selectedGenre) {
        if (selectedGenre == "All") items
        else items.filter { it.genres.contains(selectedGenre) }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    type.label,
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${filtered.size} titles",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            val accent = accentForType(type.name)
            Box(
                Modifier
                    .size(46.dp)
                    .background(accent.copy(.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(type.emoji, fontSize = 22.sp)
            }
        }

        // Genre filter chips
        if (genres.size > 1) {
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres.size) { idx ->
                    val g = genres[idx]
                    val isSelected = g == selectedGenre
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGenre = g },
                        label = { Text(g, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold,
                            selectedLabelColor = Color.Black,
                            containerColor = Panel2,
                            labelColor = TextSoft
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Gold.copy(.5f),
                            borderColor = Color.White.copy(.1f)
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Grid
        if (state.isLoading && filtered.isEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(12) { ShimmerCard(modifier = Modifier.fillMaxWidth(), height = 200.dp) }
            }
        } else if (filtered.isEmpty()) {
            EmptyState("No titles found", type.emoji)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 14.dp, top = 4.dp, end = 14.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { item ->
                    MediaPosterCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        width = 120.dp,
                        height = 190.dp
                    )
                }
            }
        }
    }
}
