package com.music.sonic.data.sources

import android.util.Log
import com.music.sonic.constants.AudioQuality
import com.music.sonic.di.SourcesHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JioSaavnSource"

/**
 * Serves 320 kbps MP3 streams from JioSaavn via the public saavn.dev API.
 *
 * This source is always enabled by default and requires no user configuration or
 * authentication. It is tried as the first built-in fallback when no Addon server is
 * configured or when the Addon server cannot find the track.
 *
 * API: https://saavn.dev/api/search/songs?query={q}&limit=5
 * Returns pre-decrypted download URLs across quality tiers (12k/48k/96k/160k/320kbps).
 *
 * Inspired by the JioSaavn source in BitChord (kushagrasinghx/BitChord) — open-source,
 * MIT licence. The API endpoint and quality-URL structure are adapted from that project.
 */
@Singleton
class JioSaavnSource @Inject constructor(
    @SourcesHttpClient private val httpClient: OkHttpClient,
    private val settingsStore: com.music.sonic.utils.SettingsStore,
) : MusicSource {

    override val name: String get() = "JioSaavn"

    /** Enabled whenever the user hasn't disabled it AND Data Saver is off. */
    override val isEnabled: Boolean
        get() = settingsStore.jiosaavnEnabled && !settingsStore.dataSaverEnabled

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override suspend fun resolve(
        title: String,
        artist: String,
        durationSec: Int,
        quality: AudioQuality,
    ): ResolvedStream? = withContext(Dispatchers.IO) {
        // JioSaavn only serves up to 320 kbps MP3 — skip if user strictly wants lossless
        // and hasn't fallen back yet. SourceResolver handles ordering, so we return the
        // 320 kbps stream unconditionally and let the resolver decide whether to use it.
        if (!isEnabled) return@withContext null

        val query = buildQuery(title, artist)
        if (query.isBlank()) return@withContext null

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://saavn.dev/api/search/songs?query=$encodedQuery&limit=5"

        val response = runCatching {
            val req = Request.Builder().url(url)
                .header("User-Agent", "SonicSpace/1.0 (Android)")
                .build()
            httpClient.newCall(req).execute()
        }.getOrElse { e ->
            Log.w(TAG, "Network error searching JioSaavn: ${e.message}")
            return@withContext null
        }

        if (!response.isSuccessful) {
            Log.w(TAG, "JioSaavn search failed: HTTP ${response.code}")
            return@withContext null
        }

        val body = response.body?.string() ?: return@withContext null
        val searchResult = runCatching { json.decodeFromString<JioSaavnSearchResponse>(body) }
            .getOrElse { e ->
                Log.w(TAG, "JioSaavn JSON parse error: ${e.message}")
                return@withContext null
            }

        val songs = searchResult.data?.results ?: return@withContext null
        val best = findBestMatch(songs, title, artist, durationSec) ?: return@withContext null

        // Pick highest available quality URL (320kbps preferred, fall back gracefully)
        val (streamUrl, kbps) = best.downloadUrl
            ?.sortedByDescending { it.quality.toIntOrNull() ?: 0 }
            ?.firstOrNull { it.url.isNotBlank() }
            ?.let { it.url to (it.quality.toIntOrNull() ?: 320) }
            ?: return@withContext null

        Log.i(TAG, "Resolved '${best.name}' by '${best.primaryArtists}' at ${kbps}kbps")

        ResolvedStream(
            url = streamUrl,
            mimeType = "audio/mpeg",
            bitrateKbps = kbps,
            bitDepth = 16,
            sampleRateHz = 44100,
            sourceLabel = "JioSaavn ${kbps}kbps",
        )
    }

    // ─── Matching ────────────────────────────────────────────────────────────

    private fun findBestMatch(
        songs: List<JioSaavnSong>,
        title: String,
        artist: String,
        durationSec: Int,
    ): JioSaavnSong? {
        val normTitle = normalize(title)
        val normArtist = normalize(artist)

        return songs
            .mapNotNull { song ->
                val songTitle = normalize(song.name ?: return@mapNotNull null)
                val songArtist = normalize(song.primaryArtists ?: "")
                val songDuration = song.duration ?: 0

                val titleScore = titleSimilarity(normTitle, songTitle)
                if (titleScore < 0.55f) return@mapNotNull null

                val artistScore = if (normArtist.isBlank()) 0.5f
                else if (songArtist.contains(normArtist) || normArtist.contains(songArtist)) 1f
                else 0.3f

                val durationScore = if (durationSec <= 0 || songDuration <= 0) 0.5f
                else {
                    val diff = Math.abs(songDuration - durationSec)
                    if (diff <= 3) 1f else if (diff <= 8) 0.6f else 0f
                }

                val total = titleScore * 0.5f + artistScore * 0.3f + durationScore * 0.2f
                song to total
            }
            .maxByOrNull { it.second }
            ?.takeIf { it.second >= 0.45f }
            ?.first
    }

    private fun buildQuery(title: String, artist: String): String {
        val cleanTitle = title.replace(Regex("\\(.*?\\)|\\[.*?]"), "").trim()
        return if (artist.isNotBlank()) "$cleanTitle $artist" else cleanTitle
    }

    private fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("[^\\p{ASCII}]"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun titleSimilarity(a: String, b: String): Float {
        if (a == b) return 1f
        if (a.isEmpty() || b.isEmpty()) return 0f
        if (a.contains(b) || b.contains(a)) return 0.9f
        val aWords = a.split(" ").toSet()
        val bWords = b.split(" ").toSet()
        val intersection = aWords.intersect(bWords).size
        val union = aWords.union(bWords).size
        return if (union == 0) 0f else intersection.toFloat() / union
    }

    // ─── JSON models ─────────────────────────────────────────────────────────

    @Serializable
    private data class JioSaavnSearchResponse(
        val data: JioSaavnSearchData? = null,
    )

    @Serializable
    private data class JioSaavnSearchData(
        val results: List<JioSaavnSong>? = null,
    )

    @Serializable
    private data class JioSaavnSong(
        val id: String? = null,
        val name: String? = null,
        val primaryArtists: String? = null,
        val duration: Int? = null,
        val downloadUrl: List<JioSaavnQualityUrl>? = null,
    )

    @Serializable
    private data class JioSaavnQualityUrl(
        val quality: String = "",
        val url: String = "",
    )
}
