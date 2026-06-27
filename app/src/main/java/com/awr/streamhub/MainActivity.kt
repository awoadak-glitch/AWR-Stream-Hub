package com.awr.streamhub

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

private val Bg = Color(0xFF05050A)
private val Panel = Color(0xFF10111A)
private val Panel2 = Color(0xFF171826)
private val Gold = Color(0xFFFFD36A)
private val Orange = Color(0xFFFF8C42)
private val Red = Color(0xFFFF3F6E)
private val Cyan = Color(0xFF4DE8FF)
private val Violet = Color(0xFF9E7BFF)
private val Muted = Color(0xFF8B8EA7)

private const val DEFAULT_OWNER = "awoadak-glitch"
private const val DEFAULT_REPO = "AWR-Stream-Hub"
private const val DEFAULT_BRANCH = "main"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StreamHubApp() }
    }
}

enum class HubTab(val label: String, val icon: String) {
    Home("Home", "✦"), Movies("Movies", "▣"), Drama("K-Drama", "◆"), Anime("Anime", "◈"), Search("Search", "⌕"), Requests("Requests", "⚡"), Settings("Settings", "⚙")
}

data class ServerLink(
    val name: String,
    val url: String
)

data class EpisodeItem(
    val name: String,
    val season: Int,
    val episode: Int,
    val servers: List<ServerLink>
)

data class SeasonItem(
    val season: Int,
    val episodes: List<EpisodeItem>
)

data class MediaItem(
    val id: String,
    val title: String,
    val original: String,
    val type: String,
    val year: String,
    val rating: String,
    val language: String,
    val genres: List<String>,
    val overview: String,
    val poster: String,
    val backdrop: String,
    val subtitleStatus: String,
    val episodes: Int = 1,
    val servers: List<ServerLink> = emptyList(),
    val seasons: List<SeasonItem> = emptyList()
)

data class JobItem(val title: String, val status: String, val tag: String)

data class GitHubSettings(
    val owner: String = DEFAULT_OWNER,
    val repo: String = DEFAULT_REPO,
    val branch: String = DEFAULT_BRANCH,
    val token: String = ""
) {
    fun raw(path: String): String = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
}

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
        LaunchedEffect(Unit) { delay(900); splash = false }
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
        Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0x33FFD36A), Bg, Color.Black), radius = 1200f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(118.dp).scale(pulse).clip(CircleShape).background(Brush.linearGradient(listOf(Gold, Orange, Red))),
                contentAlignment = Alignment.Center
            ) { Text("AWR", color = Color.Black, fontSize = 31.sp, fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(22.dp))
            Text("STREAM HUB", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("Real GitHub catalog • direct workflow dispatch", color = Muted, fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(HubTab.Home) }
    var selected by remember { mutableStateOf<MediaItem?>(null) }
    var settings by remember { mutableStateOf(loadSettings(context)) }
    var movies by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var anime by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var kdrama by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Ready") }
    val jobs = remember { mutableStateListOf<JobItem>() }

    fun refreshCatalog() {
        scope.launch {
            loading = true
            message = "Loading GitHub Raw catalog..."
            val result = withContext(Dispatchers.IO) { loadCatalog(settings) }
            movies = result.movies
            anime = result.anime
            kdrama = result.kdrama
            loading = false
            message = result.message
        }
    }

    fun sendRequest(title: String, kind: String) {
        if (title.isBlank()) {
            message = "اكتب اسم العمل أولاً"
            return
        }
        scope.launch {
            message = "Sending request to GitHub Actions..."
            jobs.add(0, JobItem(title, "Sending workflow_dispatch", kind.uppercase(Locale.US)))
            val result = withContext(Dispatchers.IO) { dispatchPriority(settings, title, kind) }
            jobs.add(0, JobItem(title, result, kind.uppercase(Locale.US)))
            message = result
        }
    }

    fun sendSubtitleRequest(item: MediaItem, sourceLanguage: String) {
        scope.launch {
            jobs.add(0, JobItem(item.title, "Sending subtitle workflow", "SRT"))
            val result = withContext(Dispatchers.IO) { dispatchSubtitle(settings, item.id, sourceLanguage) }
            jobs.add(0, JobItem(item.title, result, "SRT"))
            message = result
        }
    }

    LaunchedEffect(settings.owner, settings.repo, settings.branch) { refreshCatalog() }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { PremiumBottomBar(tab) { tab = it } }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF070711), Color(0xFF05050A), Color.Black))).padding(padding)
        ) {
            AmbientGlow()
            Crossfade(tab, label = "screen") { current ->
                when (current) {
                    HubTab.Home -> HomeScreen(movies + anime + kdrama, loading, message, onRefresh = { refreshCatalog() }, onSelect = { selected = it }, jobs = jobs)
                    HubTab.Movies -> CatalogScreen("Movies", "data/movies.json من GitHub Raw", movies, loading, onRefresh = { refreshCatalog() }, onSelect = { selected = it })
                    HubTab.Drama -> CatalogScreen("K‑Drama", "data/kdrama.json من GitHub Raw", kdrama, loading, onRefresh = { refreshCatalog() }, onSelect = { selected = it })
                    HubTab.Anime -> CatalogScreen("Anime", "data/anime.json من GitHub Raw", anime, loading, onRefresh = { refreshCatalog() }, onSelect = { selected = it })
                    HubTab.Search -> SearchScreen(movies + anime + kdrama, onSelect = { selected = it }, onRequest = { title, kind -> sendRequest(title, kind) })
                    HubTab.Requests -> RequestCenter(jobs, onRefresh = { refreshCatalog() })
                    HubTab.Settings -> SettingsScreen(settings = settings, message = message, onSave = {
                        settings = it
                        saveSettings(context, it)
                        message = "Settings saved"
                        refreshCatalog()
                    }, onTest = {
                        scope.launch {
                            message = "Testing GitHub token..."
                            message = withContext(Dispatchers.IO) { testGitHub(settings) }
                        }
                    })
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
            DetailSheet(item, onRequestCatalog = { sendRequest(item.title, item.type.lowercase(Locale.US)) }, onRequestSubtitle = { sendSubtitleRequest(item, item.language) })
        }
    }
}

@Composable
private fun AmbientGlow() {
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(.12f, .30f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glowAlpha")
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Gold.copy(glow), Color.Transparent), radius = 560f)))
}

@Composable
private fun PremiumBottomBar(active: HubTab, onTab: (HubTab) -> Unit) {
    Surface(color = Color.Transparent, modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
        NavigationBar(
            containerColor = Color(0xCC090A12),
            tonalElevation = 0.dp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).clip(RoundedCornerShape(28.dp)).border(1.dp, Color.White.copy(.08f), RoundedCornerShape(28.dp))
        ) {
            HubTab.entries.forEach { t ->
                NavigationBarItem(
                    selected = active == t,
                    onClick = { onTab(t) },
                    icon = { Text(t.icon, fontSize = 18.sp) },
                    label = { Text(t.label, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = Gold, indicatorColor = Gold, unselectedIconColor = Muted, unselectedTextColor = Muted)
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(items: List<MediaItem>, loading: Boolean, message: String, onRefresh: () -> Unit, onSelect: (MediaItem) -> Unit, jobs: List<JobItem>) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Column {
                Text("AWR Stream Hub", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("متصل مباشرة ببيانات GitHub Raw بدون أعمال وهمية داخل التطبيق", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                StatusPanel(message, loading, onRefresh)
            }
        }
        item { HeroCard(items.firstOrNull(), onSelect) }
        item { SectionTitle("Latest from GitHub", "${items.size} items loaded") }
        item {
            if (items.isEmpty() && !loading) EmptyState("لا توجد بيانات في ملفات GitHub بعد. شغل Workflow الجلب ثم اضغط Refresh.")
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(items.take(12)) { MediaCard(it, compact = true, onSelect = onSelect) } }
        }
        item { SectionTitle("Workflow jobs", "طلباتك من التطبيق") }
        items(jobs.take(8)) { JobRow(it) }
    }
}

@Composable
private fun StatusPanel(message: String, loading: Boolean, onRefresh: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC11131E)), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("GitHub connection", color = Gold, fontWeight = FontWeight.Bold)
                    Text(message, color = Color.White, fontSize = 13.sp)
                }
                Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("Refresh") }
            }
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Gold, trackColor = Color.White.copy(.08f))
        }
    }
}

@Composable
private fun HeroCard(item: MediaItem?, onSelect: (MediaItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Panel2),
        border = BorderStroke(1.dp, Color.White.copy(.08f))
    ) {
        Box(Modifier.fillMaxSize()) {
            if (item?.backdrop?.isNotBlank() == true) {
                AsyncImage(model = item.backdrop, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color.Black.copy(.85f), Red.copy(.35f), Gold.copy(.18f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item?.type ?: "GitHub Raw", color = Gold, fontWeight = FontWeight.Bold)
                Text(item?.title ?: "شغّل Workflow الجلب لملء الكتالوج", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(item?.overview ?: "التطبيق لا يحتوي أعمال وهمية، يعرض فقط ما يأتي من data/*.json في الريبو.", color = Color.White.copy(.82f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item != null) Button(onClick = { onSelect(item) }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("Open") }
            }
        }
    }
}

@Composable
private fun CatalogScreen(title: String, subtitle: String, items: List<MediaItem>, loading: Boolean, onRefresh: () -> Unit, onSelect: (MediaItem) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text(subtitle, color = Muted, fontSize = 13.sp)
                }
                OutlinedButton(onClick = onRefresh, border = BorderStroke(1.dp, Gold)) { Text("Refresh", color = Gold) }
            }
            if (loading) Spacer(Modifier.height(10.dp)); if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold)
        }
        if (items.isEmpty() && !loading) item { EmptyState("لا يوجد محتوى في هذا الملف على GitHub حالياً.") }
        item {
            LazyVerticalGrid(columns = GridCells.Adaptive(160.dp), modifier = Modifier.height(if (items.isEmpty()) 1.dp else 980.dp), contentPadding = PaddingValues(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { MediaCard(it, compact = false, onSelect = onSelect) }
            }
        }
    }
}

@Composable
private fun MediaCard(item: MediaItem, compact: Boolean, onSelect: (MediaItem) -> Unit) {
    val width = if (compact) Modifier.width(172.dp) else Modifier.fillMaxWidth()
    Card(
        modifier = width.height(if (compact) 268.dp else 300.dp).clickable { onSelect(item) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD121420)),
        border = BorderStroke(1.dp, Color.White.copy(.08f))
    ) {
        Box(Modifier.fillMaxSize()) {
            if (item.poster.isNotBlank()) AsyncImage(model = item.poster, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.35f), Color.Black.copy(.92f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.type, color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(item.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${item.year} • ${item.rating} • ${item.language}", color = Color.White.copy(.75f), fontSize = 12.sp)
                Text(item.contentHint(), color = Cyan, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SearchScreen(items: List<MediaItem>, onSelect: (MediaItem) -> Unit, onRequest: (String, String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("anime") }
    val filtered = remember(query, items) { if (query.isBlank()) items.take(20) else items.filter { it.title.contains(query, true) || it.original.contains(query, true) } }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Search & Instant Request", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("البحث داخل بيانات GitHub، وإذا لم تجد العمل أرسل workflow_dispatch", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Anime / Movie / K-Drama title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("anime", "movie", "kdrama").forEach { k ->
                    OutlinedButton(onClick = { kind = k }, border = BorderStroke(1.dp, if (kind == k) Gold else Color.White.copy(.18f))) { Text(k.uppercase(Locale.US), color = if (kind == k) Gold else Color.White) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onRequest(query, kind) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("Request now via GitHub Workflow") }
        }
        items(filtered) { MediaListRow(it, onSelect) }
    }
}

@Composable
private fun MediaListRow(item: MediaItem, onSelect: (MediaItem) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onSelect(item) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xCC11131E))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Violet, Red)))) {
                if (item.poster.isNotBlank()) AsyncImage(model = item.poster, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.type} • ${item.year} • ${item.language}", color = Muted, fontSize = 12.sp)
            }
            Text(item.subtitleStatus, color = Cyan, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RequestCenter(jobs: List<JobItem>, onRefresh: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Request Center", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("طلبات workflow_dispatch من هذا الجهاز", color = Muted) }
                OutlinedButton(onClick = onRefresh, border = BorderStroke(1.dp, Gold)) { Text("Refresh", color = Gold) }
            }
        }
        if (jobs.isEmpty()) item { EmptyState("لا توجد طلبات مرسلة من التطبيق بعد.") }
        items(jobs) { JobRow(it) }
    }
}

@Composable
private fun JobRow(job: JobItem) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC11131E)), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) { Text(job.tag.take(3), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(job.title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(job.status, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen(settings: GitHubSettings, message: String, onSave: (GitHubSettings) -> Unit, onTest: () -> Unit) {
    var owner by remember(settings) { mutableStateOf(settings.owner) }
    var repo by remember(settings) { mutableStateOf(settings.repo) }
    var branch by remember(settings) { mutableStateOf(settings.branch) }
    var token by remember(settings) { mutableStateOf(settings.token) }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("GitHub Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("ضع التوكن هنا من داخل التطبيق. التطبيق يقرأ البيانات من Raw ويشغل workflows عبر GitHub API.", color = Muted, fontSize = 13.sp)
        }
        item { OutlinedTextField(owner, { owner = it.trim() }, label = { Text("Owner") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { OutlinedTextField(repo, { repo = it.trim() }, label = { Text("Repo") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { OutlinedTextField(branch, { branch = it.trim() }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { OutlinedTextField(token, { token = it.trim() }, label = { Text("GitHub Token") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation()) }
        item {
            Button(onClick = { onSave(GitHubSettings(owner.ifBlank { DEFAULT_OWNER }, repo.ifBlank { DEFAULT_REPO }, branch.ifBlank { DEFAULT_BRANCH }, token)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("Save settings") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onTest, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Gold)) { Text("Test token", color = Gold) }
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color.White)
        }
    }
}

@Composable
private fun DetailSheet(item: MediaItem, onRequestCatalog: () -> Unit, onRequestSubtitle: () -> Unit) {
    val context = LocalContext.current
    var expandedSeason by remember(item.id) { mutableStateOf(item.seasons.firstOrNull()?.season ?: 1) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(96.dp, 136.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(Red, Violet)))) {
                    if (item.poster.isNotBlank()) AsyncImage(model = item.poster, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text(item.original, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${item.type} • ${item.year} • ${item.rating} • ${item.language}", color = Gold, fontSize = 13.sp)
                    Text(item.contentHint(), color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(item.overview, color = Color.White.copy(.82f), lineHeight = 20.sp)
            if (item.genres.isNotEmpty()) Text("Genres: ${item.genres.joinToString()}", color = Muted, fontSize = 13.sp)
            Text("Subtitle status: ${item.subtitleStatus}", color = Cyan, fontWeight = FontWeight.Bold)
        }

        if (item.servers.isNotEmpty()) {
            item { SectionTitle("Watch servers", "${item.servers.size} available") }
            items(item.servers) { server ->
                ServerButton(server = server, onOpen = { openUrl(context, server.url) })
            }
        }

        if (item.seasons.isNotEmpty()) {
            item { SectionTitle("Episodes", "${item.seasons.sumOf { it.episodes.size }} episodes") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(item.seasons) { season ->
                        OutlinedButton(
                            onClick = { expandedSeason = season.season },
                            border = BorderStroke(1.dp, if (expandedSeason == season.season) Gold else Color.White.copy(.18f))
                        ) {
                            Text("Season ${season.season}", color = if (expandedSeason == season.season) Gold else Color.White)
                        }
                    }
                }
            }

            val selectedSeason = item.seasons.firstOrNull { it.season == expandedSeason } ?: item.seasons.first()
            items(selectedSeason.episodes) { episode ->
                EpisodeCard(episode = episode, onOpen = { server -> openUrl(context, server.url) })
            }
        }

        item {
            Button(onClick = onRequestSubtitle, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("Fetch / Generate AR + EN SRT") }
            OutlinedButton(onClick = onRequestCatalog, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Gold)) { Text("Request catalog refresh for this title", color = Gold) }
            Spacer(Modifier.height(25.dp))
        }
    }
}

@Composable
private fun ServerButton(server: ServerLink, onOpen: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC11131E)), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(server.name.ifBlank { "Server" }, color = Color.White, fontWeight = FontWeight.Bold)
                Text(server.url, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(10.dp))
            Button(onClick = onOpen, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("MX") }
        }
    }
}

@Composable
private fun EpisodeCard(episode: EpisodeItem, onOpen: (ServerLink) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC11131E)), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(episode.name, color = Color.White, fontWeight = FontWeight.Bold)
            if (episode.servers.isEmpty()) {
                Text("لا توجد سيرفرات لهذه الحلقة", color = Muted, fontSize = 12.sp)
            } else {
                episode.servers.forEach { server ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(server.name.ifBlank { "Server" }, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(server.url, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { onOpen(server) }, border = BorderStroke(1.dp, Gold)) { Text("MX", color = Gold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun EmptyState(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC11131E)), shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Color.White.copy(.08f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("لا توجد بيانات", color = Gold, fontWeight = FontWeight.Bold)
            Text(text, color = Color.White.copy(.78f), fontSize = 13.sp)
        }
    }
}

private data class CatalogResult(val movies: List<MediaItem>, val anime: List<MediaItem>, val kdrama: List<MediaItem>, val message: String)

private fun loadCatalog(settings: GitHubSettings): CatalogResult {
    val errors = mutableListOf<String>()
    val movies = runCatching { fetchMediaArray(settings.raw("data/movies.json"), "Movie") }
        .getOrElse { errors.add("movies: ${it.message}"); emptyList() }
    val anime = runCatching { fetchMediaArray(settings.raw("data/anime.json"), "Anime") }
        .getOrElse { errors.add("anime: ${it.message}"); emptyList() }
    val kdrama = runCatching { fetchMediaArray(settings.raw("data/kdrama.json"), "K-Drama") }
        .getOrElse { errors.add("kdrama: ${it.message}"); emptyList() }
    val total = movies.size + anime.size + kdrama.size
    val ok = "Loaded $total items: Movies ${movies.size}, Anime ${anime.size}, K-Drama ${kdrama.size}"
    val msg = if (errors.isEmpty()) ok else "$ok | ${errors.joinToString(" | ")}"
    return CatalogResult(movies, anime, kdrama, msg)
}

private fun fetchMediaArray(url: String, fallbackType: String): List<MediaItem> {
    val freshUrl = url + if (url.contains("?")) "&awr_cache=${System.currentTimeMillis()}" else "?awr_cache=${System.currentTimeMillis()}"
    val text = httpGet(freshUrl, null).trim()
    if (text.isBlank()) return emptyList()
    val arr = when {
        text.startsWith("[") -> JSONArray(text)
        text.startsWith("{") -> {
            val obj = JSONObject(text)
            obj.optJSONArray("items") ?: obj.optJSONArray("results") ?: obj.optJSONArray("data") ?: JSONArray()
        }
        else -> throw IllegalStateException("Invalid JSON from $url: ${text.take(40)}")
    }
    return List(arr.length()) { idx -> parseMedia(arr.getJSONObject(idx), fallbackType) }
}

private fun parseMedia(o: JSONObject, fallbackType: String): MediaItem {
    val kind = o.optString("kind", o.optString("type", fallbackType)).lowercase(Locale.US)
    val type = when {
        kind.contains("anime") -> "Anime"
        kind.contains("kdrama") || kind.contains("drama") -> "K-Drama"
        else -> "Movie"
    }
    return MediaItem(
        id = o.optString("id", slug(o.optString("title", "item"))),
        title = o.optString("title", o.optString("name", "Untitled")),
        original = o.optString("original", o.optString("original_title", o.optString("original_name", ""))),
        type = type,
        year = o.optString("year", extractYear(o.optString("release_date", o.optString("first_air_date", "")))),
        rating = o.optString("rating", o.optString("vote_average", "0")),
        language = o.optString("language", o.optString("original_language", "auto")).uppercase(Locale.US),
        genres = jsonList(o.optJSONArray("genres")),
        overview = o.optString("overview", "No overview from GitHub catalog."),
        poster = normalizeImage(o.optString("poster", o.optString("poster_path", ""))),
        backdrop = normalizeImage(o.optString("backdrop", o.optString("backdrop_path", ""))),
        subtitleStatus = o.optString("subtitle_status", o.optString("subtitle", "unknown")),
        episodes = o.optInt("episodes", o.optInt("number_of_episodes", countEpisodes(o.optJSONArray("seasons")))),
        servers = parseAllServers(o),
        seasons = parseSeasons(o.optJSONArray("seasons"))
    )
}

private fun parseAllServers(o: JSONObject): List<ServerLink> {
    val servers = mutableListOf<ServerLink>()

    servers.addAll(parseServers(o.optJSONArray("servers")))
    servers.addAll(parseServers(o.optJSONArray("watch_urls")))

    val watchUrl = o.optString("watch_url", "")
    if (watchUrl.isNotBlank()) {
        servers.add(ServerLink(name = o.optString("watch_name", "Watch"), url = watchUrl))
    }

    val videoUrl = o.optString("video_url", "")
    if (videoUrl.isNotBlank()) {
        servers.add(ServerLink(name = o.optString("video_name", "Video"), url = videoUrl))
    }

    val streamUrl = o.optString("stream_url", "")
    if (streamUrl.isNotBlank()) {
        servers.add(ServerLink(name = o.optString("stream_name", "Stream"), url = streamUrl))
    }

    return servers.distinctBy { it.url.trim() }.filter { it.url.isNotBlank() }
}

private fun parseServers(arr: JSONArray?): List<ServerLink> {
    if (arr == null) return emptyList()
    return List(arr.length()) { idx ->
        val o = arr.optJSONObject(idx) ?: JSONObject()
        ServerLink(
            name = o.optString("name", "Server ${idx + 1}"),
            url = o.optString("url", o.optString("watch_url", o.optString("video_url", "")))
        )
    }.filter { it.url.isNotBlank() }
}

private fun parseSeasons(arr: JSONArray?): List<SeasonItem> {
    if (arr == null) return emptyList()
    return List(arr.length()) { sIdx ->
        val seasonObj = arr.optJSONObject(sIdx) ?: JSONObject()
        val seasonNumber = seasonObj.optInt("season", seasonObj.optInt("season_number", sIdx + 1))
        val episodesArr = seasonObj.optJSONArray("episodes")
        val episodes = if (episodesArr == null) emptyList() else List(episodesArr.length()) { eIdx ->
            val epObj = episodesArr.optJSONObject(eIdx) ?: JSONObject()
            val epNumber = epObj.optInt("episode", epObj.optInt("episode_number", eIdx + 1))
            val directServers = parseServers(epObj.optJSONArray("servers") ?: epObj.optJSONArray("watch_urls"))
            val singleUrl = epObj.optString("url", epObj.optString("watch_url", epObj.optString("video_url", "")))
            val servers = if (directServers.isNotEmpty()) {
                directServers
            } else if (singleUrl.isNotBlank()) {
                listOf(ServerLink(epObj.optString("server", "Default"), singleUrl))
            } else {
                emptyList()
            }
            EpisodeItem(
                name = epObj.optString("name", "حلقة $epNumber"),
                season = seasonNumber,
                episode = epNumber,
                servers = servers
            )
        }
        SeasonItem(season = seasonNumber, episodes = episodes)
    }.filter { it.episodes.isNotEmpty() }
}

private fun countEpisodes(arr: JSONArray?): Int {
    if (arr == null) return 1
    var total = 0
    for (i in 0 until arr.length()) {
        total += arr.optJSONObject(i)?.optJSONArray("episodes")?.length() ?: 0
    }
    return if (total > 0) total else 1
}

private fun MediaItem.contentHint(): String {
    val seasonCount = seasons.size
    val episodeCount = seasons.sumOf { it.episodes.size }
    return when {
        seasonCount > 0 -> "$seasonCount seasons • $episodeCount episodes"
        servers.isNotEmpty() -> "${servers.size} servers"
        else -> subtitleStatus
    }
}

private fun openUrl(context: Context, url: String) {
    if (url.isBlank()) return

    val uri = Uri.parse(url.trim())
    val mimeType = guessMimeType(url)

    val mxPackages = listOf(
        "com.mxtech.videoplayer.ad",   // MX Player
        "com.mxtech.videoplayer.pro"   // MX Player Pro
    )

    for (pkg in mxPackages) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            setPackage(pkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("title", "AWR Stream Hub")
        }

        try {
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            // Try the next MX Player package.
        }
    }

    Toast.makeText(
        context,
        "لم يتم العثور على MX Player أو الرابط ليس فيديو مباشر. استخدم رابط mp4 أو m3u8 أو mpd.",
        Toast.LENGTH_LONG
    ).show()
}

private fun guessMimeType(url: String): String {
    val clean = url.substringBefore("?").lowercase(Locale.US)
    return when {
        clean.endsWith(".m3u8") -> "application/x-mpegURL"
        clean.endsWith(".mpd") -> "application/dash+xml"
        clean.endsWith(".mp4") -> "video/mp4"
        clean.endsWith(".mkv") -> "video/x-matroska"
        clean.endsWith(".webm") -> "video/webm"
        clean.endsWith(".avi") -> "video/x-msvideo"
        else -> "video/*"
    }
}

private fun dispatchPriority(settings: GitHubSettings, title: String, kind: String): String {
    if (settings.token.isBlank()) return "ضع GitHub Token في Settings أولاً"
    val body = JSONObject()
        .put("ref", settings.branch)
        .put("inputs", JSONObject().put("title", title).put("kind", kind))
        .toString()
    return workflowDispatch(settings, "priority-requests.yml", body)
}

private fun dispatchSubtitle(settings: GitHubSettings, mediaId: String, sourceLanguage: String): String {
    if (settings.token.isBlank()) return "ضع GitHub Token في Settings أولاً"
    val body = JSONObject()
        .put("ref", settings.branch)
        .put("inputs", JSONObject().put("media_id", mediaId).put("source_language", sourceLanguage.ifBlank { "auto" }))
        .toString()
    return workflowDispatch(settings, "subtitle-demand.yml", body)
}

private fun workflowDispatch(settings: GitHubSettings, workflow: String, body: String): String {
    val url = "https://api.github.com/repos/${settings.owner}/${settings.repo}/actions/workflows/$workflow/dispatches"
    val code = httpPost(url, settings.token, body)
    return when (code) {
        204 -> "تم تشغيل GitHub Workflow: $workflow"
        401, 403 -> "رفض GitHub التوكن أو الصلاحيات: HTTP $code"
        404 -> "لم يجد GitHub ملف workflow: $workflow أو الريبو"
        else -> "GitHub API response: HTTP $code"
    }
}

private fun testGitHub(settings: GitHubSettings): String {
    if (settings.token.isBlank()) return "ضع GitHub Token أولاً"
    return try {
        val result = httpGet("https://api.github.com/repos/${settings.owner}/${settings.repo}", settings.token)
        val json = JSONObject(result)
        "Connected: ${json.optString("full_name", settings.owner + "/" + settings.repo)}"
    } catch (e: Exception) {
        "GitHub test failed: ${e.message}"
    }
}

private fun httpGet(url: String, token: String?): String {
    val conn = (URL(url).openConnection() as HttpURLConnection)
    conn.requestMethod = "GET"
    conn.connectTimeout = 15000
    conn.readTimeout = 20000
    conn.setRequestProperty("Accept", "application/vnd.github+json")
    if (!token.isNullOrBlank()) conn.setRequestProperty("Authorization", "Bearer $token")
    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    val text = BufferedReader(InputStreamReader(stream)).use { it.readText() }
    if (code !in 200..299) throw IllegalStateException("HTTP $code: ${text.take(120)}")
    return text
}

private fun httpPost(url: String, token: String, body: String): Int {
    val conn = (URL(url).openConnection() as HttpURLConnection)
    conn.requestMethod = "POST"
    conn.connectTimeout = 15000
    conn.readTimeout = 20000
    conn.doOutput = true
    conn.setRequestProperty("Accept", "application/vnd.github+json")
    conn.setRequestProperty("Authorization", "Bearer $token")
    conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
    conn.setRequestProperty("Content-Type", "application/json")
    OutputStreamWriter(conn.outputStream).use { it.write(body) }
    return conn.responseCode
}

private fun loadSettings(context: Context): GitHubSettings {
    val p = context.getSharedPreferences("github", Context.MODE_PRIVATE)
    return GitHubSettings(
        owner = p.getString("owner", DEFAULT_OWNER) ?: DEFAULT_OWNER,
        repo = p.getString("repo", DEFAULT_REPO) ?: DEFAULT_REPO,
        branch = p.getString("branch", DEFAULT_BRANCH) ?: DEFAULT_BRANCH,
        token = p.getString("token", "") ?: ""
    )
}

private fun saveSettings(context: Context, settings: GitHubSettings) {
    context.getSharedPreferences("github", Context.MODE_PRIVATE).edit()
        .putString("owner", settings.owner)
        .putString("repo", settings.repo)
        .putString("branch", settings.branch)
        .putString("token", settings.token)
        .apply()
}

private fun jsonList(arr: JSONArray?): List<String> {
    if (arr == null) return emptyList()
    return List(arr.length()) { idx -> arr.optString(idx) }.filter { it.isNotBlank() }
}

private fun normalizeImage(value: String): String {
    if (value.isBlank()) return ""
    if (value.startsWith("http")) return value
    return "https://image.tmdb.org/t/p/w780$value"
}

private fun extractYear(date: String): String = if (date.length >= 4) date.take(4) else "—"
private fun slug(s: String): String = URLEncoder.encode(s.lowercase(Locale.US).replace(" ", "-"), "UTF-8")