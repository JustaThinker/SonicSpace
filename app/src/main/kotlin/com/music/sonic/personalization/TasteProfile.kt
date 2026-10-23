package com.music.sonic.personalization

/**
 * Snapshot of the user's listening taste, built from Last.fm + local DB signals.
 * Cached for 1 hour by [TasteProfileProvider].
 */
data class TasteProfile(
    val topArtistNames: Set<String>,        // Last.fm top artists (lowercase)
    val recentArtists: Set<String>,          // Last.fm recent track artists (lowercase)
    val topTags: Set<String>,                // Last.fm top tags (lowercase)
    val topTrackKeys: Set<String>,           // "artist|title" dedup keys (lowercase)
    val recentTrackKeys: Set<String>,        // recent "artist|title" keys
    val artistAffinity: Map<String, Double>, // normalized play-weight per artist
    val localTopArtists: Set<String>,        // from MusicDatabase.mostPlayedArtists
    val localLikedIds: Set<String>,          // song IDs the user liked locally
) {
    companion object {
        val EMPTY = TasteProfile(
            topArtistNames = emptySet(),
            recentArtists = emptySet(),
            topTags = emptySet(),
            topTrackKeys = emptySet(),
            recentTrackKeys = emptySet(),
            artistAffinity = emptyMap(),
            localTopArtists = emptySet(),
            localLikedIds = emptySet(),
        )
    }
}
