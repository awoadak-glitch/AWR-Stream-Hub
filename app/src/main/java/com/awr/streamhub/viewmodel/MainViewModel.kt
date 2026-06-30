package com.awr.streamhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.awr.streamhub.data.local.AppDatabase
import com.awr.streamhub.data.local.FavoriteEntity
import com.awr.streamhub.data.local.WatchHistoryEntity
import com.awr.streamhub.data.models.MediaItem
import com.awr.streamhub.data.models.MediaType
import com.awr.streamhub.data.models.VideoInfo
import com.awr.streamhub.data.repository.MediaRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── UI State ────────────────────────────────────────────────────────────────

data class HomeState(
    val banner: List<MediaItem> = emptyList(),
    val trendingAnime: List<MediaItem> = emptyList(),
    val popularAnime: List<MediaItem> = emptyList(),
    val recentAnime: List<MediaItem> = emptyList(),
    val trendingMovies: List<MediaItem> = emptyList(),
    val hotDrama: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class SearchState(
    val query: String = "",
    val results: List<MediaItem> = emptyList(),
    val isSearching: Boolean = false,
    val activeFilter: MediaType? = null
)

data class DetailState(
    val item: MediaItem? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val error: String? = null
)

data class PlayerState(
    val videoInfo: VideoInfo? = null,
    val isLoading: Boolean = false,
    val currentEpisodeId: String = "",
    val currentMediaId: String = "",
    val selectedSubtitleLang: String = "off",
    val error: String? = null
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = MediaRepository(db)

    // Home
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    // Search
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Detail
    private val _detailState = MutableStateFlow(DetailState())
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    // Player
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Local data
    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repo.getRecentHistory(15)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repo.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadHome()
    }

    // ─── Home ─────────────────────────────────────────────────────────────

    fun loadHome() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, error = null) }

            // Load in parallel (using separate coroutines)
            var trendingAnime = emptyList<MediaItem>()
            var popularAnime = emptyList<MediaItem>()
            var recentAnime = emptyList<MediaItem>()
            var trendingMovies = emptyList<MediaItem>()
            var hotDrama = emptyList<MediaItem>()

            launch {
                repo.getTrendingAnime().onSuccess { trendingAnime = it }
                    .also { updateHome { h -> h.copy(trendingAnime = trendingAnime, banner = trendingAnime.take(5)) } }
            }
            launch {
                repo.getTopAnime().onSuccess { popularAnime = it }
                    .also { updateHome { h -> h.copy(popularAnime = popularAnime) } }
            }
            launch {
                repo.getCurrentSeasonAnime().onSuccess { recentAnime = it }
                    .also { updateHome { h -> h.copy(recentAnime = recentAnime) } }
            }
            launch {
                repo.getTrendingMovies().onSuccess { trendingMovies = it }
                    .also { updateHome { h -> h.copy(trendingMovies = trendingMovies) } }
            }
            launch {
                repo.searchKDrama("popular korean").onSuccess { hotDrama = it }
                    .also { updateHome { h -> h.copy(hotDrama = hotDrama) } }
            }

            _homeState.update { it.copy(isLoading = false) }
        }
    }

    private fun updateHome(transform: (HomeState) -> HomeState) {
        _homeState.update(transform)
    }

    // ─── Search ───────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _searchState.update { it.copy(query = query) }
        if (query.length >= 2) performSearch(query)
    }

    fun setSearchFilter(type: MediaType?) {
        _searchState.update { it.copy(activeFilter = type) }
        val q = _searchState.value.query
        if (q.length >= 2) performSearch(q, type)
    }

    private fun performSearch(query: String, filter: MediaType? = _searchState.value.activeFilter) {
        viewModelScope.launch {
            _searchState.update { it.copy(isSearching = true) }
            val results = mutableListOf<MediaItem>()

            when (filter) {
                MediaType.ANIME -> {
                    repo.searchAnimeJikan(query).onSuccess { results.addAll(it) }
                }
                MediaType.MOVIE -> {
                    repo.searchMovies(query).onSuccess { results.addAll(it) }
                }
                MediaType.KDRAMA -> {
                    repo.searchKDrama(query).onSuccess { results.addAll(it) }
                }
                null -> {
                    // Search all
                    repo.searchAnimeJikan(query).onSuccess { results.addAll(it.take(5)) }
                    repo.searchMovies(query).onSuccess { results.addAll(it.take(5)) }
                    repo.searchKDrama(query).onSuccess { results.addAll(it.take(5)) }
                }
                else -> {}
            }

            _searchState.update { it.copy(results = results, isSearching = false) }
        }
    }

    // ─── Detail ───────────────────────────────────────────────────────────

    fun loadDetail(item: MediaItem) {
        viewModelScope.launch {
            _detailState.update { it.copy(item = item, isLoading = true, error = null) }

            // Watch for favorite status
            repo.isFavorite(item.id).collect { isFav ->
                _detailState.update { it.copy(isFavorite = isFav) }
            }
        }

        viewModelScope.launch {
            // Load full details based on type
            val result = when {
                item.id.startsWith("jikan_") -> {
                    val jikanId = item.id.removePrefix("jikan_").toIntOrNull()
                    if (jikanId != null) repo.getAnimeById(jikanId) else Result.success(item)
                }
                item.type == MediaType.MOVIE -> repo.getMovieInfo(item.id)
                item.type == MediaType.KDRAMA -> repo.getKDramaInfo(item.id)
                item.type == MediaType.ANIME -> repo.getAnimeInfo(item.id)
                else -> Result.success(item)
            }

            result.onSuccess { fullItem ->
                _detailState.update { it.copy(item = fullItem, isLoading = false) }
            }.onFailure { e ->
                // Keep showing the item we have, just mark as done loading
                _detailState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleFavorite() {
        val item = _detailState.value.item ?: return
        viewModelScope.launch {
            if (_detailState.value.isFavorite) {
                repo.removeFavorite(item.id)
            } else {
                repo.addFavorite(item)
            }
        }
    }

    // ─── Player ───────────────────────────────────────────────────────────

    fun loadEpisodeSources(episodeId: String, mediaId: String, type: MediaType) {
        viewModelScope.launch {
            _playerState.update { it.copy(isLoading = true, error = null, currentEpisodeId = episodeId, currentMediaId = mediaId) }

            val result = when (type) {
                MediaType.ANIME -> repo.getAnimeEpisodeSources(episodeId)
                MediaType.MOVIE -> repo.getMovieSources(episodeId, mediaId)
                MediaType.KDRAMA -> repo.getKDramaSources(episodeId, mediaId)
                else -> repo.getMovieSources(episodeId, mediaId)
            }

            result.onSuccess { info ->
                _playerState.update { it.copy(videoInfo = info, isLoading = false) }
            }.onFailure { e ->
                _playerState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectSubtitle(lang: String) {
        _playerState.update { it.copy(selectedSubtitleLang = lang) }
    }

    fun saveProgress(mediaId: String, title: String, image: String, type: String,
                     episodeId: String, episodeNumber: Int, progressMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repo.saveWatchProgress(mediaId, title, image, type, episodeId, episodeNumber, progressMs, durationMs)
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repo.clearHistory() }
    }
}
