package com.awr.streamhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.ui.components.*
import com.awr.streamhub.ui.theme.*
import com.awr.streamhub.viewmodel.SearchState

@Composable
fun SearchScreen(
    state: SearchState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (MediaType?) -> Unit,
    onItemClick: (MediaItem) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(Modifier.fillMaxSize()) {
        // Header
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Search",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(14.dp))

        // Search input
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "Anime, movies, K-Drama...",
                    color = TextHint
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
            },
            trailingIcon = {
                if (state.isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Gold
                    )
                } else if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Text("✕", color = TextMuted, fontSize = 16.sp)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = Color.White.copy(.12f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Gold,
                focusedContainerColor = Panel,
                unfocusedContainerColor = Panel
            )
        )

        Spacer(Modifier.height(12.dp))

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(null to "All") +
                    MediaType.entries.map { it to it.label }

            items(filters.size) { idx ->
                val (type, label) = filters[idx]
                val isSelected = state.activeFilter == type
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(type) },
                    label = {
                        Text(
                            "${type?.emoji ?: "✦"} $label",
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Color.Black,
                        containerColor = Panel,
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

        Spacer(Modifier.height(14.dp))

        // Results
        when {
            state.query.length < 2 -> {
                // Empty / hint state
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🔍", fontSize = 52.sp)
                    Text(
                        "Type at least 2 characters to search",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Search across Anime, Movies, and K-Drama",
                        color = TextHint,
                        fontSize = 12.sp
                    )
                }
            }

            state.isSearching -> {
                repeat(5) {
                    Spacer(Modifier.height(8.dp))
                    ShimmerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        width = 999.dp,
                        height = 100.dp
                    )
                }
            }

            state.results.isEmpty() -> {
                EmptyState(
                    message = "No results for \"${state.query}\"",
                    emoji = "🎭"
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 18.dp,
                        vertical = 4.dp,
                        bottom = 90.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${state.results.size} results",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    items(state.results) { item ->
                        MediaWideCard(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}
