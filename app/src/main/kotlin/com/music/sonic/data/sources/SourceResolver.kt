package com.music.sonic.data.sources

import android.util.Log
import com.music.sonic.constants.AudioQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SourceResolver"

/**
 * Orchestrates the multi-source resolution chain for high-quality audio.
 *
 * Priority order (lowest index = tried first):
 * 1. **Addon Server** — user-configured HTTP endpoint; can return lossless FLAC/ALAC from
 *    Tidal, Qobuz, Deezer, Apple Music, etc.
 * 2. **JioSaavn** — built-in, always-on 320 kbps MP3 source; no configuration required.
 * 3. **YouTube Music** — the existing InnerTube stream, used as the last resort.
 *    YouTube is NOT managed here; returning `null` from [resolve] signals MusicService to
 *    fall through to its existing `YTPlayerUtils.playerResponseForPlayback()` path.
 *
 * [resolve] is called from `MusicService.createDataSourceFactory()` on every cache miss,
 * **before** the YouTube call, when [AudioQuality] ≠ [AudioQuality.YOUTUBE_ONLY].
 *
 * Design inspired by BitChord (kushagrasinghx/BitChord) SourceRegistry + LastWave
 * (Clash-Projects/LastWave-native) LosslessMusicApi — both open-source.
 */
@Singleton
class SourceResolver @Inject constructor(
    private val addonSource: AddonSource,
    private val jiosaavnSource: JioSaavnSource,
) {
    /**
     * Tries each source in priority order and returns the first successful [ResolvedStream],
     * or `null` if no external source can serve this track (caller falls back to YouTube).
     *
     * For all quality tiers the cascade is the same:
     *   **Addon server → JioSaavn 320 kbps → null (YouTube)**
     *
     * When [AudioQuality.LOSSLESS] is requested and only JioSaavn is available, JioSaavn is
     * used as "best available" (320 kbps is still better than YouTube Opus in most cases).
     * This avoids a harsh drop from lossless intent straight to a lower-quality YouTube stream.
     *
     * The only tier that skips external sources entirely is [AudioQuality.YOUTUBE_ONLY].
     */
    suspend fun resolve(
        title: String,
        artist: String,
        durationSec: Int,
        quality: AudioQuality,
    ): ResolvedStream? = withContext(Dispatchers.IO) {
        if (quality == AudioQuality.YOUTUBE_ONLY) {
            Log.d(TAG, "Quality = YOUTUBE_ONLY — skipping all external sources")
            return@withContext null
        }

        // 1. Addon server (lossless / hi-res capable)
        if (addonSource.isEnabled) {
            val stream = runCatching {
                addonSource.resolve(title, artist, durationSec, quality)
            }.getOrElse { e ->
                Log.w(TAG, "Addon source error: ${e.message}")
                null
            }
            if (stream != null) {
                Log.i(TAG, "[$quality] Resolved via Addon: ${stream.sourceLabel}")
                return@withContext stream
            }
        }

        // 2. JioSaavn (320 kbps MP3) — best-available fallback for ALL quality tiers,
        //    including LOSSLESS (graceful degradation: 320 kbps > YouTube Opus).
        if (jiosaavnSource.isEnabled) {
            val stream = runCatching {
                jiosaavnSource.resolve(title, artist, durationSec, quality)
            }.getOrElse { e ->
                Log.w(TAG, "JioSaavn source error: ${e.message}")
                null
            }
            if (stream != null) {
                val label = if (quality == AudioQuality.LOSSLESS)
                    "JioSaavn 320kbps (best available — no lossless match)"
                else
                    "JioSaavn 320kbps"
                Log.i(TAG, "[$quality] Resolved via $label")
                return@withContext stream
            }
        }

        // 3. All external sources exhausted → return null → caller uses YouTube
        Log.d(TAG, "[$quality] No external source matched '$title' by '$artist' — using YouTube")
        null
    }
}
