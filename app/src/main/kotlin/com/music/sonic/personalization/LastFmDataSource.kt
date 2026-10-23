package com.music.sonic.personalization

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LfmArtist(val name: String = "")

@Serializable
data class LfmTopArtistsResponse(
    val topartists: LfmTopArtists? = null
)

@Serializable
data class LfmTopArtists(val artist: List<LfmArtist> = emptyList())

@Serializable
data class LfmTrack(val name: String = "", val artist: LfmArtist = LfmArtist())

@Serializable
data class LfmRecentTracksResponse(val recenttracks: LfmRecentTracks? = null)

@Serializable
data class LfmRecentTracks(val track: List<LfmTrack> = emptyList())

@Serializable
data class LfmTopTracksResponse(val toptracks: LfmTopTracks? = null)

@Serializable
data class LfmTopTracks(val track: List<LfmTrack> = emptyList())

@Serializable
data class LfmTag(val name: String = "")

@Serializable
data class LfmTopTagsResponse(val toptags: LfmTopTags? = null)

@Serializable
data class LfmTopTags(val tag: List<LfmTag> = emptyList())

/**
 * Lightweight Last.fm read-only data source for personalization.
 * Uses GET requests — no signing required for public read endpoints.
 */
@Singleton
class LastFmDataSource @Inject constructor() {

    private val json = Json { isLenient = true; ignoreUnknownKeys = true }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
            defaultRequest { url("https://ws.audioscrobbler.com/2.0/") }
            expectSuccess = false
        }
    }

    suspend fun topArtists(username: String, apiKey: String, limit: Int = 30): List<LfmArtist> =
        runCatching {
            client.get {
                parameter("method", "user.getTopArtists")
                parameter("user", username)
                parameter("api_key", apiKey)
                parameter("format", "json")
                parameter("limit", limit)
                parameter("period", "6month")
            }.body<LfmTopArtistsResponse>().topartists?.artist.orEmpty()
        }.getOrDefault(emptyList())

    suspend fun recentTracks(username: String, apiKey: String, limit: Int = 50): List<LfmTrack> =
        runCatching {
            client.get {
                parameter("method", "user.getRecentTracks")
                parameter("user", username)
                parameter("api_key", apiKey)
                parameter("format", "json")
                parameter("limit", limit)
            }.body<LfmRecentTracksResponse>().recenttracks?.track.orEmpty()
        }.getOrDefault(emptyList())

    suspend fun topTracks(username: String, apiKey: String, limit: Int = 30): List<LfmTrack> =
        runCatching {
            client.get {
                parameter("method", "user.getTopTracks")
                parameter("user", username)
                parameter("api_key", apiKey)
                parameter("format", "json")
                parameter("limit", limit)
                parameter("period", "6month")
            }.body<LfmTopTracksResponse>().toptracks?.track.orEmpty()
        }.getOrDefault(emptyList())

    suspend fun topTags(username: String, apiKey: String, limit: Int = 20): List<LfmTag> =
        runCatching {
            client.get {
                parameter("method", "user.getTopTags")
                parameter("user", username)
                parameter("api_key", apiKey)
                parameter("format", "json")
                parameter("limit", limit)
            }.body<LfmTopTagsResponse>().toptags?.tag.orEmpty()
        }.getOrDefault(emptyList())
}
