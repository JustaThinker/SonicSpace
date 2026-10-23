package com.music.sonic.data.sources

import android.util.Log
import com.music.sonic.constants.AudioQuality
import com.music.sonic.constants.apiName
import com.music.sonic.constants.displayName
import com.music.sonic.di.SourcesHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AddonSource"

/**
 * Connects to a user-configured HTTP addon server to resolve lossless or high-quality streams.
 *
 * The addon server is a community-hosted or self-hosted service that can return FLAC/ALAC/AAC
 * streams from services like Tidal, Qobuz, Deezer, or Apple Music. The server must implement
 * the simple REST protocol described below.
 *
 * ## Addon Server Protocol
 *
 * **Search endpoint** (GET):
 * ```
 * {baseUrl}/search?q={title}+{artist}&limit=3
 * ```
 * Returns JSON:
 * ```json
 * {
 *   "tracks": [
 *     {
 *       "id": "12345",
 *       "title": "Song Title",
 *       "artist": "Artist Name",
 *       "duration": 213,
 *       "url": "https://...",           // optional: direct stream URL
 *       "mimeType": "audio/flac",       // optional
 *       "bitrateKbps": 1411,            // optional
 *       "bitDepth": 24,                 // optional
 *       "sampleRateHz": 96000           // optional
 *     }
 *   ]
 * }
 * ```
 *
 * If `url` is absent from a track, a second request fetches it:
 *
 * **Stream endpoint** (GET):
 * ```
 * {baseUrl}/stream/{trackId}?quality={quality}
 * ```
 * Returns JSON:
 * ```json
 * {
 *   "url": "https://...",
 *   "mimeType": "audio/flac",
 *   "bitrateKbps": 1411,
 *   "bitDepth": 24,
 *   "sampleRateHz": 96000
 * }
 * ```
 *
 * This protocol is compatible with the BitChord/LastWave addon server format.
 *
 * Inspired by the AddonSource pattern from BitChord (kushagrasinghx/BitChord) and
 * LastWave (Clash-Projects/LastWave-native) — both open-source.
 */
@Singleton
class AddonSource @Inject constructor(
    @SourcesHttpClient private val httpClient: OkHttpClient,
    private val settingsStore: com.music.sonic.utils.SettingsStore,
) : MusicSource {

    override val name: String get() = "Addon Server"

    override val isEnabled: Boolean
        get() = settingsStore.addonEnabled &&
                settingsStore.addonServerUrl.isNotBlank() &&
                !settingsStore.dataSaverEnabled

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val baseUrl: String
        get() = settingsStore.addonServerUrl.trimEnd('/')

    override suspend fun resolve(
        title: String,
        artist: String,
        durationSec: Int,
        quality: AudioQuality,
    ): ResolvedStream? = withContext(Dispatchers.IO) {
        if (!isEnabled) return@withContext null

        val q = URLEncoder.encode("$title $artist".trim(), "UTF-8")
        val searchUrl = "$baseUrl/search?q=$q&limit=3&quality=${quality.apiName}"

        val searchResponse = httpGet(searchUrl) ?: return@withContext null
        val results = runCatching {
            json.decodeFromString<AddonSearchResponse>(searchResponse)
        }.getOrElse { e ->
            Log.w(TAG, "Addon JSON parse error: ${e.message}")
            return@withContext null
        }

        val best = findBestMatch(results.tracks, title, artist, durationSec)
            ?: return@withContext null

        // If search response already has a direct URL, use it immediately
        if (!best.url.isNullOrBlank()) {
            Log.i(TAG, "Addon resolved '${best.title}' directly (${best.mimeType ?: "?"})")
            return@withContext ResolvedStream(
                url = best.url,
                mimeType = best.mimeType ?: guessMimeType(quality),
                bitrateKbps = best.bitrateKbps ?: 0,
                bitDepth = best.bitDepth ?: 0,
                sampleRateHz = best.sampleRateHz ?: 0,
                sourceLabel = buildLabel(best, quality),
            )
        }

        // Otherwise fetch the stream endpoint
        val streamUrl = "$baseUrl/stream/${best.id}?quality=${quality.apiName}"
        val streamBody = httpGet(streamUrl) ?: return@withContext null
        val stream = runCatching {
            json.decodeFromString<AddonStreamResponse>(streamBody)
        }.getOrElse { e ->
            Log.w(TAG, "Addon stream JSON parse error: ${e.message}")
            return@withContext null
        }

        if (stream.url.isNullOrBlank()) {
            Log.w(TAG, "Addon stream response has no URL for track ${best.id}")
            return@withContext null
        }

        Log.i(TAG, "Addon resolved '${best.title}' via /stream (${stream.mimeType ?: "?"})")
        ResolvedStream(
            url = stream.url,
            mimeType = stream.mimeType ?: guessMimeType(quality),
            bitrateKbps = stream.bitrateKbps ?: best.bitrateKbps ?: 0,
            bitDepth = stream.bitDepth ?: best.bitDepth ?: 0,
            sampleRateHz = stream.sampleRateHz ?: best.sampleRateHz ?: 0,
            sourceLabel = buildLabel(best, quality),
        )
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun findBestMatch(
        tracks: List<AddonTrack>,
        title: String,
        artist: String,
        durationSec: Int,
    ): AddonTrack? {
        val normTitle = title.lowercase().trim()
        val normArtist = artist.lowercase().trim()
        return tracks.firstOrNull { track ->
            val t = track.title?.lowercase() ?: return@firstOrNull false
            val a = track.artist?.lowercase() ?: ""
            val dur = track.duration ?: 0
            val titleMatch = t.contains(normTitle) || normTitle.contains(t)
            val durationOk = durationSec <= 0 || dur <= 0 || Math.abs(dur - durationSec) <= 5
            titleMatch && durationOk && (normArtist.isBlank() || a.contains(normArtist) || normArtist.contains(a))
        }
    }

    private fun httpGet(url: String): String? = runCatching {
        val req = Request.Builder().url(url)
            .header("User-Agent", "SonicSpace/1.0 (Android)")
            .build()
        val response = httpClient.newCall(req).execute()
        if (!response.isSuccessful) {
            Log.w(TAG, "Addon HTTP ${response.code} for $url")
            return null
        }
        response.body?.string()
    }.getOrElse { e ->
        Log.w(TAG, "Addon network error: ${e.message}")
        null
    }

    private fun guessMimeType(quality: AudioQuality) = when (quality) {
        AudioQuality.LOSSLESS -> "audio/flac"
        AudioQuality.HIGH     -> "audio/mpeg"
        else                  -> "audio/mpeg"
    }

    private fun buildLabel(track: AddonTrack, quality: AudioQuality): String {
        val kbps = track.bitrateKbps
        val depth = track.bitDepth
        val rate = track.sampleRateHz
        return when {
            depth != null && rate != null -> "Addon ${depth}bit/${rate / 1000}kHz"
            kbps != null -> "Addon ${kbps}kbps"
            else -> "Addon (${quality.displayName})"
        }
    }

    // ─── JSON models ─────────────────────────────────────────────────────────

    @Serializable
    private data class AddonSearchResponse(
        val tracks: List<AddonTrack> = emptyList(),
    )

    @Serializable
    private data class AddonTrack(
        val id: String? = null,
        val title: String? = null,
        val artist: String? = null,
        val duration: Int? = null,
        val url: String? = null,
        val mimeType: String? = null,
        val bitrateKbps: Int? = null,
        val bitDepth: Int? = null,
        val sampleRateHz: Int? = null,
    )

    @Serializable
    private data class AddonStreamResponse(
        val url: String? = null,
        val mimeType: String? = null,
        val bitrateKbps: Int? = null,
        val bitDepth: Int? = null,
        val sampleRateHz: Int? = null,
    )
}
