package com.awr.streamhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awr.streamhub.data.models.Episode
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.ui.screens.*
import com.awr.streamhub.ui.theme.*
import com.awr.streamhub.viewmodel.MainViewModel
import kotlinx.coroutines.delay

// ─── Navigation Destinations ──────────────────────────────────────────────────

sealed class Screen {
    object Home : Screen()
    object Anime : Screen()
    object Movies : Screen()
    object KDrama : Screen()
    object Search : Screen()
    object Favorites : Screen()
    object History : Screen()
    data class Detail(val item: MediaItem) : Screen()
    data class Player(val item: MediaItem, val episode: Episode) : Screen()
}

enum class BottomTab(val label: String, val emoji: String, val screen: Screen) {
    HOME("Home", "✦", Screen.Home),
    ANIME("Anime", "◈", Screen.Anime),
    MOVIES("Movies", "▣", Screen.Movies),
    KDRAMA("K-Drama", "◆", Screen.KDrama),
    SEARCH("Search", "⌕", Screen.Search),
    FAVORITES("Faves", "♥", Screen.Favorites),
    HISTORY("History", "⏱", Screen.History)
}

// ─── Activity ─────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            AWRStreamHubApp(viewModel)
        }
    }
}

@Composable
fun AWRStreamHubApp(viewModel: MainViewModel) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val history by viewModel.watchHistory.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    MaterialTheme(colorScheme = AWRColorScheme) {
        var splash by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) { delay(1200); splash = false }

        AnimatedVisibility(visible = splash, exit = fadeOut(tween(400))) {
            SplashScreen()
        }

        AnimatedVisibility(visible = !splash, enter = fadeIn(tween(400))) {
            var activeTab by remember { mutableStateOf(BottomTab.HOME) }
            var screenStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Home)) }

            val currentScreen = screenStack.last()

            fun navigateTo(screen: Screen) {
                screenStack = screenStack + screen
            }

            fun navigateBack() {
                if (screenStack.size > 1) {
                    screenStack = screenStack.dropLast(1)
                }
            }

            val isPlayerScreen = currentScreen is Screen.Player
            val isDetailScreen = currentScreen is Screen.Detail

            Scaffold(
                containerColor = Bg,
                bottomBar = {
                    AnimatedVisibility(
                        visible = !isPlayerScreen && !isDetailScreen,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        AWRBottomBar(
                            activeTab = activeTab,
                            onTabSelected = { tab ->
                                activeTab = tab
                                // Reset to main screens on tab click
                                screenStack = listOf(tab.screen)
                            }
                        )
                    }
                }
            ) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF070711), Bg, Color.Black)))
                        .then(if (isPlayerScreen) Modifier else Modifier.padding(padding))
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Home -> HomeScreen(
                            state = homeState,
                            history = history,
                            onItemClick = { item ->
                                viewModel.loadDetail(item)
                                navigateTo(Screen.Detail(item))
                            },
                            onContinueWatching = { entry ->
                                // Navigate to player with saved progress
                                val resumeItem = MediaItem(
                                    id = entry.mediaId,
                                    title = entry.title,
                                    image = entry.image,
                                    type = MediaType.entries.find { it.name == entry.type } ?: MediaType.ANIME
                                )
                                val episode = Episode(id = entry.episodeId, number = entry.episodeNumber)
                                viewModel.loadEpisodeSources(entry.episodeId, entry.mediaId, resumeItem.type)
                                navigateTo(Screen.Player(resumeItem, episode))
                            },
                            onRefresh = { viewModel.loadHome() }
                        )

                        is Screen.Anime -> CatalogScreen(
                            type = MediaType.ANIME,
                            state = homeState,
                            onItemClick = { item ->
                                viewModel.loadDetail(item)
                                navigateTo(Screen.Detail(item))
                            }
                        )

                        is Screen.Movies -> CatalogScreen(
                            type = MediaType.MOVIE,
                            state = homeState,
                            onItemClick = { item ->
                                viewModel.loadDetail(item)
                                navigateTo(Screen.Detail(item))
                            }
                        )

                        is Screen.KDrama -> CatalogScreen(
                            type = MediaType.KDRAMA,
                            state = homeState,
                            onItemClick = { item ->
                                viewModel.loadDetail(item)
                                navigateTo(Screen.Detail(item))
                            }
                        )

                        is Screen.Search -> SearchScreen(
                            state = searchState,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onFilterChange = { viewModel.setSearchFilter(it) },
                            onItemClick = { item ->
                                viewModel.loadDetail(item)
                                navigateTo(Screen.Detail(item))
                            }
                        )

                        is Screen.Favorites -> FavoritesScreen(
                            favorites = favorites,
                            onItemClick = { item ->
                                viewModel.loadDetail(item)
                                navigateTo(Screen.Detail(item))
                            }
                        )

                        is Screen.History -> HistoryScreen(
                            history = history,
                            onItemClick = { entry ->
                                val resumeItem = MediaItem(
                                    id = entry.mediaId,
                                    title = entry.title,
                                    image = entry.image,
                                    type = MediaType.entries.find { it.name == entry.type } ?: MediaType.ANIME
                                )
                                val episode = Episode(id = entry.episodeId, number = entry.episodeNumber)
                                viewModel.loadEpisodeSources(entry.episodeId, entry.mediaId, resumeItem.type)
                                navigateTo(Screen.Player(resumeItem, episode))
                            },
                            onClearAll = { viewModel.clearHistory() }
                        )

                        is Screen.Detail -> DetailScreen(
                            state = detailState,
                            onBack = { navigateBack() },
                            onPlay = { episode ->
                                val item = screen.item
                                viewModel.loadEpisodeSources(episode.id, item.id, item.type)
                                navigateTo(Screen.Player(item, episode))
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite() }
                        )

                        is Screen.Player -> PlayerScreen(
                            mediaItem = screen.item,
                            episodeNumber = screen.episode.number,
                            state = playerState,
                            onBack = { navigateBack() },
                            onProgress = { pos, dur ->
                                viewModel.saveProgress(
                                    mediaId = screen.item.id,
                                    title = screen.item.title,
                                    image = screen.item.image,
                                    type = screen.item.type.name,
                                    episodeId = screen.episode.id,
                                    episodeNumber = screen.episode.number,
                                    progressMs = pos,
                                    durationMs = dur
                                )
                            },
                            onNextEpisode = {
                                // Load next episode
                                val item = detailState.item ?: screen.item
                                val currentEpNum = screen.episode.number
                                val nextEp = item.episodes.find { it.number == currentEpNum + 1 }
                                if (nextEp != null) {
                                    viewModel.loadEpisodeSources(nextEp.id, item.id, item.type)
                                    screenStack = screenStack.dropLast(1) + Screen.Player(item, nextEp)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
private fun AWRBottomBar(activeTab: BottomTab, onTabSelected: (BottomTab) -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = Color(0xDD08090F),
            tonalElevation = 0.dp,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(.07f), RoundedCornerShape(26.dp))
        ) {
            BottomTab.entries.forEach { tab ->
                val isActive = activeTab == tab
                NavigationBarItem(
                    selected = isActive,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Text(
                            tab.emoji,
                            fontSize = if (isActive) 20.sp else 17.sp
                        )
                    },
                    label = {
                        Text(
                            tab.label,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Gold,
                        indicatorColor = Gold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}

// ─── Splash Screen ────────────────────────────────────────────────────────────

@Composable
private fun SplashScreen() {
    val pulse by rememberInfiniteTransition(label = "splash").animateFloat(
        initialValue = .88f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0x44FFD36A), Bg, Color.Black),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(120.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Gold, Orange, Red))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AWR",
                    color = Color.Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "STREAM HUB",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Anime • Movies • K-Drama",
                color = TextMuted,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(
                color = Gold.copy(.6f),
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
        }
    }
}
