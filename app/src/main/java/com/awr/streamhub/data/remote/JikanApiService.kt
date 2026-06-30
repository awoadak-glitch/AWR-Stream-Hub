package com.awr.streamhub.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {

    // Top anime
    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("type") type: String? = null, // tv, movie, ova, special, ona, music
        @Query("filter") filter: String? = null // airing, upcoming, bypopularity, favorite
    ): JikanResponse<List<JikanAnime>>

    // Seasonal anime
    @GET("seasons/now")
    suspend fun getCurrentSeason(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25
    ): JikanResponse<List<JikanAnime>>

    // Search anime
    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("sfw") sfw: Boolean = true
    ): JikanResponse<List<JikanAnime>>

    // Anime by ID
    @GET("anime/{id}")
    suspend fun getAnimeById(
        @Path("id") id: Int
    ): JikanResponse<JikanAnime>

    // Anime characters
    @GET("anime/{id}/characters")
    suspend fun getAnimeCharacters(
        @Path("id") id: Int
    ): JikanResponse<List<JikanCharacter>>

    // Recommendations
    @GET("anime/{id}/recommendations")
    suspend fun getAnimeRecommendations(
        @Path("id") id: Int
    ): JikanResponse<List<Any>>

    // Schedule
    @GET("schedules")
    suspend fun getSchedule(
        @Query("filter") day: String? = null // monday, tuesday, etc
    ): JikanResponse<List<JikanAnime>>
}
