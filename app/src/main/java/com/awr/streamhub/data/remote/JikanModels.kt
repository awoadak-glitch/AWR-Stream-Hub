package com.awr.streamhub.data.remote

import com.google.gson.annotations.SerializedName

data class JikanResponse<T>(
    val data: T? = null,
    val pagination: JikanPagination? = null
)

data class JikanPagination(
    val last_visible_page: Int = 1,
    val has_next_page: Boolean = false,
    val current_page: Int = 1
)

data class JikanAnime(
    @SerializedName("mal_id") val malId: Int = 0,
    val title: String = "",
    @SerializedName("title_english") val titleEnglish: String? = null,
    @SerializedName("title_japanese") val titleJapanese: String? = null,
    val images: JikanImages? = null,
    val synopsis: String? = null,
    val score: Double? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    val status: String? = null,
    val type: String? = null,
    val episodes: Int? = null,
    @SerializedName("aired") val aired: JikanAired? = null,
    val genres: List<JikanGenre> = emptyList(),
    val year: Int? = null,
    val trailer: JikanTrailer? = null,
    val rating: String? = null
)

data class JikanImages(
    val jpg: JikanImageSet? = null,
    val webp: JikanImageSet? = null
)

data class JikanImageSet(
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("small_image_url") val smallImageUrl: String? = null,
    @SerializedName("large_image_url") val largeImageUrl: String? = null
)

data class JikanAired(val string: String? = null)

data class JikanGenre(@SerializedName("mal_id") val malId: Int = 0, val name: String = "")

data class JikanTrailer(val url: String? = null, @SerializedName("embed_url") val embedUrl: String? = null)

data class JikanCharacter(
    val character: JikanCharacterInfo? = null,
    val role: String? = null,
    @SerializedName("voice_actors") val voiceActors: List<JikanVoiceActor> = emptyList()
)

data class JikanCharacterInfo(val name: String = "", val images: JikanImages? = null)
data class JikanVoiceActor(val person: JikanVAInfo? = null, val language: String? = null)
data class JikanVAInfo(val name: String = "")
