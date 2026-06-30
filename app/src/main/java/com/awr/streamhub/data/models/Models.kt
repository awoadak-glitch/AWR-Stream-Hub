package com.awr.streamhub.data.models

// ─── Domain Models ────────────────────────────────────────────────────────────

data class MediaItem(
    val id: String,
    val title: String,
    val titleEn: String = "",
    val image: String = "",
    val cover: String = "",
    val description: String = "",
    val rating: String = "",
    val releaseDate: String = "",
    val genres: List<String> = emptyList(),
    val status: String = "",
    val type: MediaType = MediaType.ANIME,
    val totalEpisodes: Int = 0,
    val episodes: List<Episode> = emptyList(),
    val trailer: String? = null,
    val isAdult: Boolean = false
)

data class Episode(
    val id: String,
    val number: Int,
    val title: String? = null,
    val image: String? = null,
    val description: String? = null,
    val airDate: String? = null
)

data class StreamSource(
    val url: String,
    val quality: String = "auto",
    val isM3U8: Boolean = false
)

data class SubtitleTrack(
    val url: String,
    val lang: String,
    val label: String
)

data class VideoInfo(
    val sources: List<StreamSource>,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val intro: IntroInfo? = null
)

data class IntroInfo(val start: Int, val end: Int)

enum class MediaType(val label: String, val emoji: String) {
    ANIME("Anime", "◈"),
    MOVIE("Movie", "▣"),
    KDRAMA("K-Drama", "◆"),
    TV("TV Show", "◉")
}

enum class HomeSection(val title: String, val emoji: String) {
    BANNER("", ""),
    CONTINUE_WATCHING("Continue Watching", "▶"),
    TRENDING("Trending Now", "🔥"),
    POPULAR_ANIME("Popular Anime", "◈"),
    RECENT_ANIME("Recently Added Anime", "✨"),
    MOVIES("Top Movies", "▣"),
    KDRAMA("K-Drama Hot", "◆"),
    RECENTLY_ADDED("Recently Added", "🆕")
}
