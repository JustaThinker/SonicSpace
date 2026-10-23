package com.music.sonic.data.sources

/**
 * A stream URL resolved from an external high-quality source (JioSaavn, Addon server, etc.).
 * When this is non-null, it is used directly by [SourceResolver] instead of falling through
 * to YouTube Music.
 */
data class ResolvedStream(
    /** Direct playable HTTP(S) URL. */
    val url: String,
    /** MIME type, e.g. "audio/mpeg", "audio/flac", "audio/mp4". */
    val mimeType: String,
    /** Bitrate in kbps. 0 = unknown. */
    val bitrateKbps: Int = 0,
    /** Bit depth. 0 = unknown (e.g. 16 or 24). */
    val bitDepth: Int = 0,
    /** Sample rate in Hz. 0 = unknown. */
    val sampleRateHz: Int = 0,
    /** Human-readable source label shown in signal-path UI, e.g. "JioSaavn 320kbps". */
    val sourceLabel: String = "",
)
