package com.awr.streamhub.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ConsumetApiService {

    // ─── Anime (using Gogoanime provider) ──────────────────────────────────

    @GET("anime/gogoanime/{query}")
    suspend fun searchAnime(
        @Path("query") query: String,
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("anime/gogoanime/info/{id}")
    suspend fun getAnimeInfo(
        @Path("id") id: String
    ): ConsumetMedia

    @GET("anime/gogoanime/watch/{episodeId}")
    suspend fun getAnimeEpisodeSources(
        @Path("episodeId") episodeId: String,
        @Query("server") server: String = "gogocdn"
    ): ConsumetSources

    // ─── Movies (TMDB provider) ────────────────────────────────────────────

    @GET("meta/tmdb/trending")
    suspend fun getTrendingMovies(
        @Query("type") type: String = "movie",
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("meta/tmdb/popular")
    suspend fun getPopularMovies(
        @Query("type") type: String = "movie",
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("meta/tmdb/{query}")
    suspend fun searchMovies(
        @Path("query") query: String,
        @Query("page") page: Int = 1,
        @Query("type") type: String? = null
    ): ConsumetSearchResult

    @GET("meta/tmdb/info/{id}")
    suspend fun getMovieInfo(
        @Path("id") id: String,
        @Query("type") type: String = "movie"
    ): ConsumetMedia

    @GET("meta/tmdb/watch/{episodeId}")
    suspend fun getMovieSources(
        @Path("episodeId") episodeId: String,
        @Query("id") mediaId: String,
        @Query("type") type: String = "movie"
    ): ConsumetSources

    // ─── K-Drama (using DramaQ provider) ──────────────────────────────────

    @GET("anime/dramacool/{query}")
    suspend fun searchDrama(
        @Path("query") query: String,
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("anime/dramacool/info")
    suspend fun getDramaInfo(
        @Query("id") id: String
    ): ConsumetMedia

    @GET("anime/dramacool/watch")
    suspend fun getDramaEpisodeSources(
        @Query("episodeId") episodeId: String,
        @Query("mediaId") mediaId: String
    ): ConsumetSources
}
