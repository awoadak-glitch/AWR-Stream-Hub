package com.awr.streamhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.awr.streamhub.data.local.FavoriteEntity
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteEntity>,
    onItemClick: (MediaItem) -> Unit
) {
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
                    "Favorites",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${favorites.size} saved",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Text("♥", color = Red, fontSize = 28.sp)
        }

        Spacer(Modifier.height(14.dp))

        if (favorites.isEmpty()) {
            EmptyState(
                message = "No favorites yet\nTap ♥ on any title to save it",
                emoji = "💫"
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 14.dp, top = 4.dp, end = 14.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites) { fav ->
                    FavoriteCard(fav = fav, onClick = {
                        onItemClick(
                            MediaItem(
                                id = fav.mediaId,
                                title = fav.title,
                                image = fav.image,
                                rating = fav.rating,
                                type = MediaType.entries.find { it.name == fav.type } ?: MediaType.ANIME
                            )
                        )
                    })
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(fav: FavoriteEntity, onClick: () -> Unit) {
    val type = MediaType.entries.find { it.name == fav.type } ?: MediaType.ANIME
    val accent = accentForType(fav.type)

    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        if (fav.image.isNotEmpty()) {
            AsyncImage(
                model = fav.image,
                contentDescription = fav.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(accent.copy(.5f), Bg)))
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.88f))))
        )

        // Heart badge
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(26.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Red.copy(.8f)),
            contentAlignment = Alignment.Center
        ) {
            Text("♥", color = Color.White, fontSize = 12.sp)
        }

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                fav.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            if (fav.rating.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("★", color = Gold, fontSize = 10.sp)
                    Text(fav.rating, color = TextSoft, fontSize = 10.sp)
                }
            }
        }
    }
}
