package com.awr.streamhub.data.repository

import com.awr.streamhub.data.local.AppDatabase
import com.awr.streamhub.data.local.FavoriteEntity
import com.awr.streamhub.data.local.WatchHistoryEntity
import com.awr.streamhub.data.models.*
import com.awr.streamhub.data.remote.ConsumetMedia
import com.awr.streamhub.data.remote.JikanAnime
import com.awr.streamhub.data.remote.NetworkModule
import kotlinx.coroutines.flow.Flow

class MediaRepository(private val db: AppDatabase) {

    private val jikan = NetworkModule.jikanService
    private val consumet = NetworkModule.consumetService

    // ─── Anime via Jikan ─────────────────────────────────────────────────

    suspend fun getTopAnime(page: Int = 1): Result<List<MediaItem>> = runCatching {
        val response = jikan.getTopAnime(page = page, filter = "bypopularity")
        response.data?.map { it.toMediaItem() } ?: emptyList()
    }

    suspend fun getTrendingAnime(): Result<List<MediaItem>> = runCatching {
        val response = jikan.getTopAnime(filter = "airing", limit = 10)
        response.data?.map { it.toMediaItem() } ?: emptyList()
    }

    suspend fun getCurrentSeasonAnime(): Result<List<MediaItem>> = runCatching {
        val response = jikan.getCurrentSeason(limit = 20)
        response.data?.map { it.toMediaItem() } ?: emptyList()
    }

    suspend fun searchAnimeJikan(query: String): Result<List<MediaItem>> = runCatching {
        val response = jikan.searchAnime(query = query, sfw = true)
        response.data?.map { it.toMediaItem() } ?: emptyList()
    }

    suspend fun getAnimeById(id: Int): Result<MediaItem> = runCatching {
        val response = jikan.getAnimeById(id)
        response.data?.toMediaItem() ?: throw Exception("Anime not found")
    }

    // ─── Anime via Consumet (for streaming) ──────────────────────────────

    suspend fun searchAnimeConsume(query: String): Result<List<MediaItem>> = runCatching {
        val response = consumet.searchAnime(query)
        response.results.map { it.toMediaItem(MediaType.ANIME) }
    }

    suspend fun getAnimeInfo(id: String): Result<MediaItem> = runCatching {
        val response = consumet.getAnimeInfo(id)
        response.toMediaItem(MediaType.ANIME)
    }

    suspend fun getAnimeEpisodeSources(episodeId: String): Result<VideoInfo> = runCatching {
        val response = consumet.getAnimeEpisodeSources(episodeId)
        VideoInfo(
            sources = response.sources.map {
                StreamSource(url = it.url, quality = it.quality ?: "auto", isM3U8 = it.isM3U8)
            },
            subtitles = response.subtitles.map {
                SubtitleTrack(url = it.url, lang = it.lang, label = it.lang)
            },
            intro = response.intro?.let { IntroInfo(it.start, it.end) }
        )
    }

    // ─── Movies via Consumet/TMDB ─────────────────────────────────────────

    suspend fun getTrendingMovies(): Result<List<MediaItem>> = runCatching {
        val response = consumet.getTrendingMovies(type = "movie")
        response.results.map { it.toMediaItem(MediaType.MOVIE) }
    }

    suspend fun getPopularMovies(): Result<List<MediaItem>> = runCatching {
        val response = consumet.getPopularMovies(type = "movie")
        response.results.map { it.toMediaItem(MediaType.MOVIE) }
    }

    suspend fun searchMovies(query: String): Result<List<MediaItem>> = runCatching {
        val response = consumet.searchMovies(query = query, type = "movie")
        response.results.map { it.toMediaItem(MediaType.MOVIE) }
    }

    suspend fun getMovieInfo(id: String): Result<MediaItem> = runCatching {
        val response = consumet.getMovieInfo(id = id, type = "movie")
        response.toMediaItem(MediaType.MOVIE)
    }

    suspend fun getMovieSources(episodeId: String, mediaId: String): Result<VideoInfo> = runCatching {
        val response = consumet.getMovieSources(episodeId = episodeId, mediaId = mediaId, type = "movie")
        VideoInfo(
            sources = response.sources.map {
                StreamSource(url = it.url, quality = it.quality ?: "auto", isM3U8 = it.isM3U8)
            },
            subtitles = response.subtitles.map {
                SubtitleTrack(url = it.url, lang = it.lang, label = it.lang)
            }
        )
    }

    // ─── K-Drama via Consumet ─────────────────────────────────────────────

    suspend fun searchKDrama(query: String): Result<List<MediaItem>> = runCatching {
        val response = consumet.searchDrama(query)
        response.results.map { it.toMediaItem(MediaType.KDRAMA) }
    }

    suspend fun getKDramaInfo(id: String): Result<MediaItem> = runCatching {
        val response = consumet.getDramaInfo(id)
        response.toMediaItem(MediaType.KDRAMA)
    }

    suspend fun getKDramaSources(episodeId: String, mediaId: String): Result<VideoInfo> = runCatching {
        val response = consumet.getDramaEpisodeSources(episodeId = episodeId, mediaId = mediaId)
        VideoInfo(
            sources = response.sources.map {
                StreamSource(url = it.url, quality = it.quality ?: "auto", isM3U8 = it.isM3U8)
            },
            subtitles = response.subtitles.map {
                SubtitleTrack(url = it.url, lang = it.lang, label = it.lang)
            }
        )
    }

    // ─── Local: Watch History ─────────────────────────────────────────────

    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = db.watchHistoryDao().getAllHistory()
    fun getRecentHistory(limit: Int = 10): Flow<List<WatchHistoryEntity>> = db.watchHistoryDao().getRecentHistory(limit)
    suspend fun getHistoryForMedia(mediaId: String) = db.watchHistoryDao().getHistoryForMedia(mediaId)

    suspend fun saveWatchProgress(
        mediaId: String, title: String, image: String, type: String,
        episodeId: String, episodeNumber: Int, progressMs: Long, durationMs: Long
    ) {
        db.watchHistoryDao().insertOrUpdate(
            WatchHistoryEntity(
                mediaId = mediaId, title = title, image = image, type = type,
                episodeId = episodeId, episodeNumber = episodeNumber,
                progressMs = progressMs, durationMs = durationMs
            )
        )
    }

    suspend fun clearHistory() = db.watchHistoryDao().clearAll()

    // ─── Local: Favorites ─────────────────────────────────────────────────

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = db.favoriteDao().getAllFavorites()
    fun isFavorite(mediaId: String): Flow<Boolean> = db.favoriteDao().isFavorite(mediaId)

    suspend fun toggleFavorite(item: MediaItem) {
        val isFav = db.favoriteDao().isFavorite(item.id)
        // We'll handle this in ViewModel with a collect
        db.favoriteDao().addFavorite(
            FavoriteEntity(
                mediaId = item.id,
                title = item.title,
                image = item.image,
                type = item.type.name,
                rating = item.rating
            )
        )
    }

    suspend fun addFavorite(item: MediaItem) {
        db.favoriteDao().addFavorite(
            FavoriteEntity(
                mediaId = item.id, title = item.title,
                image = item.image, type = item.type.name, rating = item.rating
            )
        )
    }

    suspend fun removeFavorite(mediaId: String) = db.favoriteDao().removeFavorite(mediaId)

    // ─── Mappers ─────────────────────────────────────────────────────────

    private fun JikanAnime.toMediaItem(): MediaItem = MediaItem(
        id = "jikan_$malId",
        title = titleEnglish?.takeIf { it.isNotBlank() } ?: title,
        titleEn = titleEnglish ?: title,
        image = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl ?: "",
        cover = images?.jpg?.largeImageUrl ?: "",
        description = synopsis ?: "",
        rating = score?.let { "%.1f".format(it) } ?: "",
        releaseDate = year?.toString() ?: aired?.string ?: "",
        genres = genres.map { it.name },
        status = status ?: "",
        type = MediaType.ANIME,
        totalEpisodes = episodes ?: 0,
        trailer = trailer?.url
    )

    private fun ConsumetMedia.toMediaItem(type: MediaType): MediaItem = MediaItem(
        id = id,
        title = getTitleString(),
        titleEn = getTitleString(),
        image = image ?: "",
        cover = cover ?: image ?: "",
        description = description ?: "",
        rating = rating?.let { "%.1f".format(it) } ?: "",
        releaseDate = releaseDate ?: "",
        genres = genres ?: emptyList(),
        status = status ?: "",
        type = type,
        totalEpisodes = totalEpisodes ?: 0,
        episodes = episodes?.map {
            Episode(
                id = it.id, number = it.number,
                title = it.title, image = it.image,
                description = it.description, airDate = it.airDate
            )
        } ?: emptyList()
    )
}
