package com.awr.streamhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.RequestPage
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

private val Gold = Color(0xFFFFD54A)
private val Gold2 = Color(0xFFFFA927)
private val Night = Color(0xFF06070B)
private val CardBlack = Color(0xFF11131C)
private val Soft = Color(0xFFB7B3A5)
private val NeonBlue = Color(0xFF55D7FF)
private val Pink = Color(0xFFFF5C8A)
private val Green = Color(0xFF65F2A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AwrStreamHubApp() }
    }
}

data class MediaItem(
    val id: String,
    val title: String,
    val type: String,
    val year: String,
    val score: String,
    val tag: String,
    val accent: Color,
    val posterCode: String,
    val overview: String,
    val episodes: String = "Movie",
    val subtitleStatus: String = "Ready"
)

data class RequestItem(val query: String, val category: String, val status: String)

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Movies("Movies", Icons.Default.LocalMovies),
    KDrama("K-Drama", Icons.Default.MovieFilter),
    Anime("Anime", Icons.Outlined.Animation),
    Search("Search", Icons.Default.Search),
    Requests("Requests", Icons.Outlined.RequestPage),
    Settings("Settings", Icons.Default.Settings)
}

private val demoItems = listOf(
    MediaItem("m1", "Shadow Protocol", "Movie", "2025", "9.1", "Action", Gold, "SP", "A dark agent story with fast chases, secret files, and a missing subtitle pack."),
    MediaItem("m2", "Midnight Seoul", "K-Drama", "2024", "8.8", "Romance", Pink, "MS", "A stylish Korean drama about fame, silence, and choices under neon rain.", "16 Episodes"),
    MediaItem("m3", "Blade Sakura", "Anime", "2025", "9.4", "Shonen", NeonBlue, "BS", "A cinematic anime journey with sword fights, friendship, and emotional arcs.", "24 Episodes"),
    MediaItem("m4", "The Last Signal", "Movie", "2023", "8.5", "Sci-Fi", Green, "LS", "A signal from deep space changes one translator app forever."),
    MediaItem("m5", "Love Contract", "K-Drama", "2025", "8.7", "Drama", Color(0xFFB38CFF), "LC", "A contract marriage turns into a slow-burn mystery with premium visuals.", "12 Episodes"),
    MediaItem("m6", "Demon Archive", "Anime", "2024", "9.0", "Dark", Color(0xFFFF7755), "DA", "A hidden archive unlocks ancient voices and subtitles from another world.", "13 Episodes")
)

@Composable
fun AwrStreamHubApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Night,
            surface = CardBlack,
            primary = Gold,
            secondary = Gold2,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        var selectedTab by remember { mutableStateOf(Tab.Home) }
        var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
        val requests = remember { mutableStateListOf<RequestItem>() }
        Box(Modifier.fillMaxSize().background(Night)) {
            CinematicBackground()
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { ProBottomBar(selectedTab) { selectedTab = it } }
            ) { padding ->
                AnimatedContent(
                    targetState = selectedTab,
                    modifier = Modifier.padding(padding),
                    transitionSpec = {
                        (fadeIn(tween(260)) + slideInHorizontally { it / 5 }) togetherWith
                            (fadeOut(tween(180)) + slideOutHorizontally { -it / 8 })
                    },
                    label = "tabSwitch"
                ) { tab ->
                    when (tab) {
                        Tab.Home -> HomeScreen(onOpen = { selectedItem = it }, onSearch = { selectedTab = Tab.Search })
                        Tab.Movies -> CategoryScreen("Foreign Movies", "100 new movies every 10 minutes", demoItems.filter { it.type == "Movie" }, onOpen = { selectedItem = it })
                        Tab.KDrama -> CategoryScreen("Korean Drama", "Romance, thriller, historical, modern", demoItems.filter { it.type == "K-Drama" }, onOpen = { selectedItem = it })
                        Tab.Anime -> CategoryScreen("Anime Zone", "Shonen, seinen, fantasy, seasonal", demoItems.filter { it.type == "Anime" }, onOpen = { selectedItem = it })
                        Tab.Search -> SearchScreen(onOpen = { selectedItem = it }, onRequest = { q, c -> requests.add(0, RequestItem(q, c, "Queued instantly")); selectedTab = Tab.Requests })
                        Tab.Requests -> RequestsScreen(requests)
                        Tab.Settings -> SettingsScreen()
                    }
                }
            }
            selectedItem?.let { item ->
                DetailSheet(item = item, onDismiss = { selectedItem = null })
            }
        }
    }
}

@Composable
fun CinematicBackground() {
    val t = rememberInfiniteTransition(label = "bg")
    val shift by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "shift"
    )
    Canvas(Modifier.fillMaxSize().blur(28.dp)) {
        drawCircle(Gold.copy(alpha = 0.18f), radius = size.minDimension * 0.45f, center = Offset(size.width * (0.1f + shift * 0.35f), size.height * 0.08f))
        drawCircle(Pink.copy(alpha = 0.10f), radius = size.minDimension * 0.36f, center = Offset(size.width * 0.92f, size.height * (0.2f + shift * 0.2f)))
        drawCircle(NeonBlue.copy(alpha = 0.12f), radius = size.minDimension * 0.38f, center = Offset(size.width * (0.25f + shift * 0.35f), size.height * 0.92f))
    }
    Canvas(Modifier.fillMaxSize().alpha(0.12f)) {
        for (i in 0..18) {
            val y = size.height * i / 18f
            drawLine(Color.White, Offset(0f, y), Offset(size.width, y + sin(i.toFloat()) * 22f), strokeWidth = 1f)
        }
    }
}

@Composable
fun HomeScreen(onOpen: (MediaItem) -> Unit, onSearch: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 18.dp,
            start = 18.dp,
            end = 18.dp,
            bottom = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { TopHeader(onSearch) }
        item { HeroCarousel(demoItems.take(3), onOpen) }
        item { StatsRow() }
        item { SectionHeader("Trending now", "Auto updated catalog") }
        item { PosterRail(demoItems, onOpen) }
        item { SectionHeader("Subtitle mission", "One tap per movie or episode") }
        items(demoItems.take(4)) { SubtitleMissionCard(it, onOpen) }
    }
}

@Composable
fun TopHeader(onSearch: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("AWR Stream Hub", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Movies • K-Drama • Anime • Smart SRT", color = Soft, fontSize = 13.sp)
        }
        IconButton(onClick = onSearch, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.09f))) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
        }
    }
}

@Composable
fun HeroCarousel(items: List<MediaItem>, onOpen: (MediaItem) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), flingBehavior = rememberSnapFlingBehavior(lazyListState = androidx.compose.foundation.lazy.rememberLazyListState())) {
        items(items) { item ->
            HeroCard(item, onOpen)
        }
    }
}

@Composable
fun HeroCard(item: MediaItem, onOpen: (MediaItem) -> Unit) {
    val transition = rememberInfiniteTransition(label = "hero")
    val glow by transition.animateFloat(0.85f, 1.05f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "glow")
    Card(
        modifier = Modifier
            .width(310.dp)
            .height(200.dp)
            .clickable { onOpen(item) }
            .graphicsLayer { scaleX = glow; scaleY = glow },
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(item.accent.copy(alpha = 0.96f), CardBlack, Night)))
                .padding(20.dp)
        ) {
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.66f)) {
                AssistPill(item.type)
                Spacer(Modifier.height(10.dp))
                Text(item.title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black, maxLines = 2)
                Text("${item.year} • ★ ${item.score} • ${item.tag}", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Night, modifier = Modifier.clip(CircleShape).background(Gold).padding(6.dp).size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open details", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            PosterArt(item, Modifier.align(Alignment.CenterEnd).width(98.dp).height(148.dp))
        }
    }
}

@Composable
fun StatsRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MiniStat("300", "items / 10 min", Gold, Modifier.weight(1f))
        MiniStat("0", "duplicates", Green, Modifier.weight(1f))
        MiniStat("2", "SRT langs", NeonBlue, Modifier.weight(1f))
    }
}

@Composable
fun MiniStat(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(82.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
            Text(value, color = accent, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text(label, color = Soft, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Soft, fontSize = 12.sp)
        }
        Text("View all", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PosterRail(items: List<MediaItem>, onOpen: (MediaItem) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(items) { PosterTile(it, onOpen) }
    }
}

@Composable
fun PosterTile(item: MediaItem, onOpen: (MediaItem) -> Unit) {
    Column(Modifier.width(138.dp).clickable { onOpen(item) }) {
        PosterArt(item, Modifier.fillMaxWidth().aspectRatio(0.68f))
        Spacer(Modifier.height(9.dp))
        Text(item.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        Text("★ ${item.score} • ${item.year}", color = Soft, fontSize = 12.sp)
    }
}

@Composable
fun PosterArt(item: MediaItem, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(item.accent, CardBlack, Night)))
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, size.height * .72f)
                    cubicTo(size.width * .3f, size.height * .55f, size.width * .62f, size.height * .9f, size.width, size.height * .64f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, Color.White.copy(alpha = .10f))
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(item.posterCode, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(item.type.uppercase(), color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun SubtitleMissionCard(item: MediaItem, onOpen: (MediaItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(item) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.075f))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterArt(item, Modifier.size(74.dp))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text("${item.type} • ${item.episodes}", color = Soft, fontSize = 12.sp)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistPill("AR SRT")
                    AssistPill("EN SRT")
                }
            }
            Icon(Icons.Default.Subtitles, contentDescription = null, tint = Gold)
        }
    }
}

@Composable
fun CategoryScreen(title: String, subtitle: String, items: List<MediaItem>, onOpen: (MediaItem) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 18.dp, 18.dp, 18.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Soft)
        }
        items(items + demoItems.shuffled()) { item ->
            BigListCard(item, onOpen)
        }
    }
}

@Composable
fun BigListCard(item: MediaItem, onOpen: (MediaItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(156.dp).clickable { onOpen(item) },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))
    ) {
        Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterArt(item, Modifier.width(88.dp).fillMaxHeight())
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                AssistPill(item.tag)
                Spacer(Modifier.height(8.dp))
                Text(item.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, maxLines = 1)
                Text(item.overview, color = Soft, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistPill("★ ${item.score}")
                    AssistPill(item.subtitleStatus)
                }
            }
        }
    }
}

@Composable
fun SearchScreen(onOpen: (MediaItem) -> Unit, onRequest: (String, String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Any") }
    val result = remember(query) { demoItems.filter { it.title.contains(query, ignoreCase = true) || it.type.contains(query, ignoreCase = true) } }
    LazyColumn(
        contentPadding = PaddingValues(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 18.dp, 18.dp, 18.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Ultra Search", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Find titles or request anything instantly", color = Soft)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Movie, drama, anime name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Any", "Movie", "K-Drama", "Anime").forEach { c ->
                    ChipButton(c, selected = category == c) { category = c }
                }
            }
            AnimatedVisibility(query.isNotBlank(), enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                Button(
                    onClick = { onRequest(query, category) },
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Night)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Request instantly if not found", fontWeight = FontWeight.Black)
                }
            }
        }
        items(if (query.isBlank()) demoItems else result) { item -> BigListCard(item, onOpen) }
    }
}

@Composable
fun RequestsScreen(requests: List<RequestItem>) {
    val sample = if (requests.isEmpty()) listOf(
        RequestItem("Solo Leveling S02", "Anime", "Ready for GitHub request pipeline"),
        RequestItem("Queen of Tears", "K-Drama", "Waiting for metadata bot")
    ) else requests
    LazyColumn(
        contentPadding = PaddingValues(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 18.dp, 18.dp, 18.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Requests", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("User asks, bot fetches metadata, subtitles follow per title", color = Soft)
        }
        items(sample) { req ->
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.075f)), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(req.query, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("${req.category} • ${req.status}", color = Soft, fontSize = 12.sp)
                    }
                    AssistPill("Priority")
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Frontend placeholders for the next backend phase", color = Soft)
        SettingsCard("GitHub Data Source", "Raw JSON catalog URL will be connected here.", Icons.Default.Download)
        SettingsCard("Auto Fetch", "100 movies + 100 K-drama + 100 anime every 10 minutes.", Icons.Default.Bolt)
        SettingsCard("Subtitle Engine", "One tap AR/EN SRT generation per movie or episode.", Icons.Default.Translate)
        SettingsCard("Design Mode", "Cinematic glass UI, smooth tabs, premium poster cards.", Icons.Default.Category)
    }
}

@Composable
fun SettingsCard(title: String, subtitle: String, icon: ImageVector) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.075f)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.clip(CircleShape).background(Gold.copy(alpha = .14f)).padding(10.dp).size(26.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Black)
                Text(subtitle, color = Soft, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(item: MediaItem, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color(0xFF0B0D13)) {
        Column(Modifier.padding(18.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PosterArt(item, Modifier.width(112.dp).height(160.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                    Text("${item.type} • ${item.year} • ★ ${item.score}", color = Soft)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistPill("AR")
                        AssistPill("EN")
                        AssistPill(item.episodes)
                    }
                }
            }
            Text(item.overview, color = Color.White.copy(alpha = .86f), fontSize = 14.sp)
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Night)
            ) {
                Icon(Icons.Default.Subtitles, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Fetch Arabic + English SRT", fontWeight = FontWeight.Black)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("Open player", color = Gold) }
                TextButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("Request dub", color = Gold) }
            }
        }
    }
}

@Composable
fun ProBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Surface(color = Color(0xF20A0B10), tonalElevation = 10.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                val isSelected = tab == selected
                val scale by animateFloatAsState(if (isSelected) 1.13f else 1f, spring(), label = "navScale")
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelect(tab) }
                        .background(if (isSelected) Gold.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 7.dp)
                        .scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(tab.icon, contentDescription = tab.label, tint = if (isSelected) Gold else Soft, modifier = Modifier.size(22.dp))
                    Text(tab.label, color = if (isSelected) Color.White else Soft, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun AssistPill(text: String) {
    Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.11f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChipButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(if (selected) Gold else Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(text, color = if (selected) Night else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

private infix fun androidx.compose.animation.EnterTransition.togetherWith(exit: androidx.compose.animation.ExitTransition) =
    androidx.compose.animation.togetherWith(exit)
