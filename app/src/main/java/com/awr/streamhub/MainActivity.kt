package com.awr.streamhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Bg = Color(0xFF05050A)
private val Panel = Color(0xFF10111A)
private val Panel2 = Color(0xFF171826)
private val Gold = Color(0xFFFFD36A)
private val Orange = Color(0xFFFF8C42)
private val Red = Color(0xFFFF3F6E)
private val Cyan = Color(0xFF4DE8FF)
private val Violet = Color(0xFF9E7BFF)
private val SoftText = Color(0xFFD7D7E7)
private val Muted = Color(0xFF8B8EA7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StreamHubApp() }
    }
}

enum class HubTab(val label: String, val icon: String) {
    Home("Home", "✦"), Movies("Movies", "▣"), Drama("K-Drama", "◆"), Anime("Anime", "◈"), Search("Search", "⌕"), Requests("Requests", "⚡")
}

data class MediaItem(
    val id: String,
    val title: String,
    val original: String,
    val type: String,
    val year: String,
    val rating: String,
    val badge: String,
    val language: String,
    val genres: List<String>,
    val overview: String,
    val accentA: Color,
    val accentB: Color,
    val subtitle: String,
    val episodes: Int = 1,
    val progress: Float = 0f
)

data class JobItem(val title: String, val status: String, val progress: Float, val tag: String)

private val sampleItems = listOf(
    MediaItem("m1", "Crimson Horizon", "Crimson Horizon", "Movie", "2026", "9.1", "Trending #1", "EN", listOf("Action", "Sci‑Fi", "IMAX"), "A betrayed pilot crosses a burning skyline to stop a global satellite war. Built as a premium hero card for your future metadata backend.", Red, Orange, "Ready AR/EN", 1, .82f),
    MediaItem("m2", "Silent Crown", "Silent Crown", "Movie", "2025", "8.8", "Fresh", "EN", listOf("Drama", "Mystery"), "A quiet royal secret turns into a worldwide chase. Includes subtitle fetch states and request hooks.", Violet, Cyan, "Need SRT", 1, .27f),
    MediaItem("k1", "Neon Seoul", "네온 서울", "K-Drama", "2026", "9.4", "K‑Drama Hot", "KO", listOf("Romance", "Thriller", "16 EP"), "A hacker and a prosecutor uncover a city built on erased memories. Designed for episode-based subtitle requests.", Cyan, Violet, "Queue", 16, .44f),
    MediaItem("k2", "Moon Contract", "달의 계약", "K-Drama", "2025", "8.9", "Top Weekly", "KO", listOf("Fantasy", "Romance", "12 EP"), "A mysterious contract links two souls across time. Each episode can request Arabic and English SRT directly.", Orange, Gold, "Ready AR", 12, .62f),
    MediaItem("a1", "Blade Aurora", "ブレードオーロラ", "Anime", "2026", "9.7", "Anime Peak", "JA", listOf("Shounen", "Action", "24 EP"), "A young guardian unlocks a forbidden aurora blade. Optimized for anime-style cards, glow, and fast request UI.", Red, Violet, "Ready AR/EN", 24, .73f),
    MediaItem("a2", "Starlit Classroom", "星明かりの教室", "Anime", "2025", "9.0", "New Episode", "JA", listOf("School", "Fantasy", "12 EP"), "A classroom appears only at midnight, where students learn spells from future versions of themselves.", Gold, Cyan, "Need SRT", 12, .19f),
    MediaItem("m3", "North Signal", "North Signal", "Movie", "2024", "8.6", "4K", "EN", listOf("Survival", "Adventure"), "A frozen signal brings a rescue team to a place that should not exist.", Cyan, Red, "Ready EN", 1, .53f),
    MediaItem("a3", "Zero Kingdom", "ゼロ王国", "Anime", "2026", "9.2", "Fan Request", "JA", listOf("Isekai", "Magic", "25 EP"), "A strategist wakes inside a kingdom that resets after every defeat.", Violet, Orange, "Queue", 25, .36f)
)

@Composable
fun StreamHubApp() {
    val scheme = darkColorScheme(
        primary = Gold,
        secondary = Orange,
        tertiary = Cyan,
        background = Bg,
        surface = Panel,
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )
    MaterialTheme(colorScheme = scheme) {
        var splash by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) { delay(1100); splash = false }
        if (splash) SplashScreen() else MainShell()
    }
}

@Composable
private fun SplashScreen() {
    val pulse by rememberInfiniteTransition(label = "splash").animateFloat(
        initialValue = .92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0x33FFD36A), Bg, Color.Black), radius = 1200f)
        ), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(120.dp).scale(pulse).clip(CircleShape).background(Brush.linearGradient(listOf(Gold, Orange, Red))),
                contentAlignment = Alignment.Center
            ) { Text("AWR", color = Color.Black, fontSize = 31.sp, fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(22.dp))
            Text("STREAM HUB", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("Global cinema • instant requests • subtitle intelligence", color = Muted, fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell() {
    var tab by remember { mutableStateOf(HubTab.Home) }
    var selected by remember { mutableStateOf<MediaItem?>(null) }
    val jobs = remember { mutableStateListOf(
        JobItem("Blade Aurora S01E08", "Arabic SRT rendering", .71f, "SRT"),
        JobItem("Neon Seoul", "Instant catalog request", .38f, "REQ"),
        JobItem("Crimson Horizon", "English timing sync", .93f, "SYNC")
    ) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { PremiumBottomBar(tab) { tab = it } }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF070711), Color(0xFF05050A), Color.Black))
            ).padding(padding)
        ) {
            AmbientGlow()
            Crossfade(tab, label = "screen") { current ->
                when (current) {
                    HubTab.Home -> HomeScreen(onSelect = { selected = it }, jobs = jobs)
                    HubTab.Movies -> CatalogScreen("Movies", "Foreign movies pipeline", sampleItems.filter { it.type == "Movie" }, onSelect = { selected = it })
                    HubTab.Drama -> CatalogScreen("K‑Drama", "Korean drama episodes", sampleItems.filter { it.type == "K-Drama" }, onSelect = { selected = it })
                    HubTab.Anime -> CatalogScreen("Anime", "Anime seasons and episode requests", sampleItems.filter { it.type == "Anime" }, onSelect = { selected = it })
                    HubTab.Search -> SearchScreen(onSelect = { selected = it }, onRequest = { title -> jobs.add(0, JobItem(title, "Instant request sent", .12f, "NOW")) })
                    HubTab.Requests -> RequestCenter(jobs)
                }
            }
        }
    }

    selected?.let { item ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = sheetState,
            containerColor = Panel,
            contentColor = Color.White,
            dragHandle = { Box(Modifier.padding(top = 10.dp).size(52.dp, 5.dp).clip(CircleShape).background(Color.White.copy(.18f))) }
        ) {
            DetailSheet(item) {
                jobs.add(0, JobItem(item.title, "Subtitle generation requested", .08f, "SRT"))
            }
        }
    }
}

@Composable
private fun AmbientGlow() {
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        .18f, .42f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Gold.copy(glow), Color.Transparent), radius = 520f)))
}

@Composable
private fun PremiumBottomBar(active: HubTab, onTab: (HubTab) -> Unit) {
    Surface(color = Color.Transparent, modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
        NavigationBar(
            containerColor = Color(0xCC090A12),
            tonalElevation = 0.dp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp).clip(RoundedCornerShape(28.dp)).border(1.dp, Color.White.copy(.08f), RoundedCornerShape(28.dp))
        ) {
            HubTab.entries.forEach { t ->
                NavigationBarItem(
                    selected = active == t,
                    onClick = { onTab(t) },
                    icon = { Text(t.icon, fontSize = 19.sp) },
                    label = { Text(t.label, fontSize = 10.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Gold,
                        indicatorColor = Gold,
                        unselectedIconColor = Muted,
                        unselectedTextColor = Muted
                    )
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(onSelect: (MediaItem) -> Unit, jobs: List<JobItem>) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Header("AWR Stream Hub", "Full stack cinema catalog — frontend now, backend-ready architecture") }
        item { MetricStrip() }
        item { HeroSpotlight(sampleItems.first(), onSelect) }
        item { SectionTitle("Live operations", "Requests, SRT rendering, catalog jobs") }
        item { JobsRail(jobs) }
        item { SectionTitle("Trending now", "Movies, K‑Drama, Anime") }
        item { MediaRail(sampleItems, onSelect) }
        item { SectionTitle("Backend pipelines", "What will connect next") }
        item { BackendBlueprint() }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Gold, Orange))), contentAlignment = Alignment.Center) {
            Text("AI", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun MetricStrip() {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("Auto pull", "100×3", "every 10 min")
        MetricCard("Request mode", "Instant", "priority queue")
        MetricCard("SRT", "AR + EN", "on demand")
        MetricCard("Storage", "GitHub", "JSON pages")
    }
}

@Composable
private fun MetricCard(label: String, value: String, sub: String) {
    Card(colors = CardDefaults.cardColors(Panel2), shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Column(Modifier.width(136.dp).padding(14.dp)) {
            Text(label, color = Muted, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(sub, color = Gold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun HeroSpotlight(item: MediaItem, onSelect: (MediaItem) -> Unit) {
    val scale by animateFloatAsState(1f, tween(600), label = "heroScale")
    Card(
        modifier = Modifier.fillMaxWidth().height(305.dp).scale(scale).clickable { onSelect(item) },
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, Color.White.copy(.10f)),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(item.accentA, item.accentB, Color.Black)))) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.88f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Pill(item.badge, Color.Black.copy(.35f), Gold)
                Spacer(Modifier.height(10.dp))
                Text(item.title, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, maxLines = 2)
                Text(item.overview, color = SoftText, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PremiumButton("Open details") { onSelect(item) }
                    OutlinedButton(onClick = { onSelect(item) }, border = BorderStroke(1.dp, Color.White.copy(.22f)), shape = RoundedCornerShape(18.dp)) {
                        Text("Fetch SRT", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, sub: String) {
    Column {
        Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun JobsRail(jobs: List<JobItem>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(jobs) { job -> JobCard(job) }
    }
}

@Composable
private fun JobCard(job: JobItem) {
    Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Gold.copy(.22f))) {
        Column(Modifier.width(230.dp).padding(15.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Pill(job.tag, Gold.copy(.16f), Gold)
                Text("${(job.progress * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(job.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(job.status, color = Muted, fontSize = 12.sp, maxLines = 1)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { job.progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Gold, trackColor = Color.White.copy(.08f))
        }
    }
}

@Composable
private fun MediaRail(items: List<MediaItem>, onSelect: (MediaItem) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(items) { item -> MediaPoster(item, onSelect, Modifier.width(176.dp)) }
    }
}

@Composable
private fun MediaPoster(item: MediaItem, onSelect: (MediaItem) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(286.dp).clickable { onSelect(item) },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(.08f))
    ) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(item.accentA.copy(.92f), item.accentB.copy(.86f), Color.Black)))) {
            Text(item.type.uppercase(), color = Color.White.copy(.34f), fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd).padding(13.dp))
            Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Pill(item.subtitle, Color.Black.copy(.38f), Gold)
                Spacer(Modifier.height(8.dp))
                Text(item.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${item.year} • ★ ${item.rating} • ${item.language}", color = SoftText, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatalogScreen(title: String, subtitle: String, items: List<MediaItem>, onSelect: (MediaItem) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Header(title, subtitle)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Ready SRT", "Need SRT", "Top rated", "New", "Episodes").forEachIndexed { index, chip ->
                FilterChip(selected = index == 0, onClick = {}, label = { Text(chip) })
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(items) { item -> MediaPoster(item, onSelect, Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun SearchScreen(onSelect: (MediaItem) -> Unit, onRequest: (String) -> Unit) {
    var q by remember { mutableStateOf("") }
    val results = remember(q) { if (q.isBlank()) sampleItems else sampleItems.filter { it.title.contains(q, true) || it.type.contains(q, true) || it.genres.any { g -> g.contains(q, true) } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Header("Ultra Search", "Search the catalog or request anything instantly") }
        item {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Movie, series, anime, episode…") },
                singleLine = true,
                shape = RoundedCornerShape(22.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumButton("Instant request") { if (q.isNotBlank()) onRequest(q) }
                OutlinedButton(onClick = { if (q.isNotBlank()) onRequest("Translate SRT for $q") }, shape = RoundedCornerShape(18.dp)) { Text("Request SRT", color = Color.White) }
            }
        }
        item { SectionTitle("Results", "Tap any card for details and SRT actions") }
        items(results) { item -> WideResult(item, onSelect) }
    }
}

@Composable
private fun WideResult(item: MediaItem, onSelect: (MediaItem) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onSelect(item) }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Panel), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(82.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(item.accentA, item.accentB))))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(item.overview, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { item.genres.take(3).forEach { Pill(it, Color.White.copy(.06f), SoftText) } }
            }
        }
    }
}

@Composable
private fun RequestCenter(jobs: MutableList<JobItem>) {
    var title by remember { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Header("Request Center", "Priority request pipeline for missing movies, episodes and subtitles") }
        item {
            Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, Gold.copy(.22f))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Request anything instantly", color = Color.White, fontWeight = FontWeight.Black, fontSize = 21.sp)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Example: One Piece S02E04 Arabic SRT") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumButton("Send request") { if (title.isNotBlank()) { jobs.add(0, JobItem(title, "Waiting for backend worker", .05f, "REQ")); title = "" } }
                        OutlinedButton(onClick = { if (title.isNotBlank()) { jobs.add(0, JobItem(title, "SRT priority worker", .08f, "SRT")); title = "" } }, shape = RoundedCornerShape(18.dp)) { Text("Fetch SRT", color = Color.White) }
                    }
                }
            }
        }
        item { SectionTitle("Queue", "Frontend states ready for backend connection") }
        items(jobs) { JobCard(it) }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun BackendBlueprint() {
    Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                "GitHub Action: pulls 100 movies + 100 K‑Drama + 100 anime every 10 minutes",
                "Request API: accepts instant user requests and writes them to a priority queue",
                "Subtitle Worker: creates or fetches AR/EN SRT for a selected item",
                "Dedup Engine: prevents repeated titles and stores seen IDs",
                "Android App: reads paged JSON and shows live job states"
            ).forEachIndexed { index, text ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) { Text("${index + 1}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp) }
                    Spacer(Modifier.width(10.dp))
                    Text(text, color = SoftText, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailSheet(item: MediaItem, onSubtitle: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(listOf(item.accentA, item.accentB, Color.Black)))) {
                Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                    Pill(item.badge, Color.Black.copy(.35f), Gold)
                    Text(item.title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text(item.original, color = SoftText, fontSize = 13.sp)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Pill(item.type, Gold.copy(.16f), Gold)
                Pill(item.year, Color.White.copy(.06f), SoftText)
                Pill("★ ${item.rating}", Color.White.copy(.06f), SoftText)
                Pill("${item.episodes} EP", Color.White.copy(.06f), SoftText)
                Pill(item.subtitle, Color.White.copy(.06f), SoftText)
            }
        }
        item { Text(item.overview, color = SoftText, fontSize = 15.sp, lineHeight = 22.sp) }
        item {
            Card(colors = CardDefaults.cardColors(Panel2), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Subtitle Intelligence", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("This button will connect to the backend worker later: search existing subtitles, generate missing AR/EN SRT, sync timing, and update the catalog JSON status.", color = Muted, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumButton("Fetch AR + EN SRT") { onSubtitle() }
                        OutlinedButton(onClick = onSubtitle, shape = RoundedCornerShape(18.dp)) { Text("Request item", color = Color.White) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(30.dp)) }
    }
}

@Composable
private fun PremiumButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
        shape = RoundedCornerShape(18.dp)
    ) { Text(text, fontWeight = FontWeight.Black) }
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color) {
    Box(Modifier.clip(CircleShape).background(bg).border(1.dp, fg.copy(.16f), CircleShape).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
