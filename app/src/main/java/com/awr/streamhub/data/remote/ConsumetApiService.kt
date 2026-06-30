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

    // ─── Movies (تحويل من TMDB إلى FlixHQ لتخطي حظر Cloudflare) ──────────────

    @GET("movies/flixhq/trending")
    suspend fun getTrendingMovies(
        @Query("type") type: String = "movie",
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("movies/flixhq/popular")
    suspend fun getPopularMovies(
        @Query("type") type: String = "movie",
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("movies/flixhq/{query}")
    suspend fun searchMovies(
        @Path("query") query: String,
        @Query("page") page: Int = 1,
        @Query("type") type: String? = null
    ): ConsumetSearchResult

    @GET("movies/flixhq/info")
    suspend fun getMovieInfo(
        @Query("id") id: String,
        @Query("type") type: String = "movie"
    ): ConsumetMedia

    @GET("movies/flixhq/watch")
    suspend fun getMovieSources(
        @Query("episodeId") episodeId: String,
        @Query("mediaId") mediaId: String,
        @Query("type") type: String = "movie"
    ): ConsumetSources

    // ─── K-Drama (تصحيح المسار من anime إلى movies) ─────────────────────────

    @GET("movies/dramacool/{query}")
    suspend fun searchDrama(
        @Path("query") query: String,
        @Query("page") page: Int = 1
    ): ConsumetSearchResult

    @GET("movies/dramacool/info")
    suspend fun getDramaInfo(
        @Query("id") id: String
    ): ConsumetMedia

    @GET("movies/dramacool/watch")
    suspend fun getDramaEpisodeSources(
        @Query("episodeId") episodeId: String,
        @Query("mediaId") mediaId: String
    ): ConsumetSources
}
