package com.music.sonic.data.sources

import com.music.sonic.constants.AudioQuality

/**
 * Contract that every external music source implements.
 *
 * Sources are tried in priority order by [SourceResolver]. Returning `null` signals
 * "I don't have this track" and the resolver moves to the next source. Only when all
 * external sources return null does the player fall through to YouTube Music.
 */
interface MusicSource {
    /** Short display name shown in Settings, e.g. "JioSaavn", "Addon Server". */
    val name: String

    /** Whether this source is currently enabled and configured. */
    val isEnabled: Boolean

    /**
     * Attempts to resolve a playable stream for the given track.
     *
     * @param title       Track title (from Room DB / YouTube metadata).
     * @param artist      Primary artist name.
     * @param durationSec Track duration in seconds. 0 = unknown.
     * @param quality     The user's preferred [AudioQuality] tier.
     * @return A [ResolvedStream] on success, or `null` if this source cannot serve the track.
     */
    suspend fun resolve(
        title: String,
        artist: String,
        durationSec: Int,
        quality: AudioQuality,
    ): ResolvedStream?
}
