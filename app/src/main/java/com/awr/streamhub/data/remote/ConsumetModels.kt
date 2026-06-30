package com.awr.streamhub.data.remote

import com.google.gson.annotations.SerializedName

data class ConsumetSearchResult(
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val results: List<ConsumetMedia> = emptyList(),
    val totalPages: Int = 1
)

data class ConsumetMedia(
    val id: String = "",
    val title: Any? = null, // can be String or object
    val url: String? = null,
    val image: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val rating: Float? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    val genres: List<String>? = null,
    val status: String? = null,
    val type: String? = null,
    val totalEpisodes: Int? = null,
    val episodes: List<ConsumetEpisode>? = null,
    val recommendations: List<ConsumetMedia>? = null,
    val trailer: ConsumetTrailer? = null
) {
    fun getTitleString(): String {
        return when (val t = title) {
            is String -> t
            is Map<*, *> -> (t["english"] ?: t["romaji"] ?: t["native"] ?: "").toString()
            else -> ""
        }
    }
}

data class ConsumetEpisode(
    val id: String = "",
    val number: Int = 0,
    val title: String? = null,
    val image: String? = null,
    val description: String? = null,
    val airDate: String? = null
)

data class ConsumetTrailer(val id: String? = null, val site: String? = null, val url: String? = null)

data class ConsumetSources(
    val sources: List<ConsumetSource> = emptyList(),
    val subtitles: List<ConsumetSubtitle> = emptyList(),
    val intro: ConsumetIntro? = null,
    val headers: Map<String, String>? = null
)

data class ConsumetSource(
    val url: String = "",
    val quality: String? = null,
    @SerializedName("isM3U8") val isM3U8: Boolean = false
)

data class ConsumetSubtitle(
    val url: String = "",
    val lang: String = ""
)

data class ConsumetIntro(val start: Int = 0, val end: Int = 0)
