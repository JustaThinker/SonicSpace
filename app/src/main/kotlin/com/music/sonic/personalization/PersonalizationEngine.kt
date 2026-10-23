package com.music.sonic.personalization

import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.sonic.constants.SongSortType
import com.music.sonic.db.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_SUGGESTIONS = 40
private const val SEED_COUNT = 6

// Genre neighbor expansion — adapted from LastWave's RECO_GENRE_NEIGHBORS
private val GENRE_NEIGHBORS = mapOf(
    "indie rock" to "alternative rock", "alternative rock" to "indie rock",
    "house" to "deep house", "deep house" to "tech house",
    "hip hop" to "rap", "rap" to "hip hop",
    "metal" to "heavy metal", "heavy metal" to "metal",
    "pop" to "indie pop", "indie pop" to "dream pop",
    "jazz" to "soul", "soul" to "r&b", "r&b" to "neo-soul",
    "electronic" to "experimental", "ambient" to "chillwave",
    "punk" to "post-punk", "post-punk" to "new wave",
    "folk" to "indie folk", "indie folk" to "folk",
    "trap" to "drill", "drill" to "trap",
)

private data class ScoredSong(val song: SongItem, val score: Double)

/**
 * Personalization engine for SonicSpace.
 *
 * Strategy:
 * 1. Pull top-played + liked seeds from local DB
 * 2. Fetch YouTube related songs for each seed in parallel
 * 3. Score each candidate using [TasteProfile] signals
 * 4. Return top-scored, deduplicated, blacklisted results
 */
@Singleton
class PersonalizationEngine @Inject constructor(
    private val database: MusicDatabase,
    private val tasteProfileProvider: TasteProfileProvider,
) {

    /**
     * Returns up to [limit] personalized song recommendations.
     * [blacklistIds] — IDs to exclude (e.g. already queued or recently shown).
     */
    suspend fun recommend(
        limit: Int = MAX_SUGGESTIONS,
        forceRefresh: Boolean = false,
        blacklistIds: Set<String> = emptySet(),
    ): List<SongItem> = coroutineScope {
        val profile = tasteProfileProvider.get(forceRefresh)

        val sixtyDaysAgo = System.currentTimeMillis() - 86400000L * 60

        // Gather local candidates: top-played + liked + recent events
        val topSongs  = database.mostPlayedSongs(fromTimeStamp = sixtyDaysAgo, limit = 30).first()
        val liked     = database.likedSongs(SongSortType.CREATE_DATE, descending = true).first().take(20)
        val recentEvents = database.events().first().map { it.song }.distinctBy { it.id }.take(10)

        val allCandidates = (topSongs + liked + recentEvents)
            .distinctBy { it.id }
            .filterNot { it.id in blacklistIds }

        // Artist-diverse seed selection: one track per artist
        val seeds = allCandidates
            .groupBy { it.artists.firstOrNull()?.id ?: it.id }
            .values
            .mapNotNull { group -> group.shuffled().firstOrNull() }
            .shuffled()
            .take(SEED_COUNT)

        if (seeds.isEmpty()) return@coroutineScope emptyList()

        // Fetch YouTube related songs for each seed in parallel
        val rawCandidates = Collections.synchronizedList(mutableListOf<SongItem>())
        seeds.map { seed ->
            async(Dispatchers.IO) {
                runCatching {
                    val nextPage = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()
                    val relatedEndpoint = nextPage?.relatedEndpoint ?: return@runCatching
                    YouTube.related(relatedEndpoint).onSuccess { page ->
                        rawCandidates.addAll(page.songs)
                    }
                }
            }
        }.awaitAll()

        // Score, deduplicate, filter, and rank
        rawCandidates
            .distinctBy { it.id }
            .filterNot { it.id in blacklistIds }
            .filterNot { it.id in profile.localLikedIds }  // don't resurface already-liked songs
            .map { song -> ScoredSong(song, score(song, profile)) }
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.song }
    }

    // ── Scoring — adapted from LastWave's RecommendationEngine ──────────────

    private fun score(song: SongItem, profile: TasteProfile): Double {
        var s = 0.0
        val artistKey = song.artists.firstOrNull()?.name?.trim()?.lowercase() ?: return 0.0

        // Artist affinity from local DB play time (0–30 pts)
        s += (profile.artistAffinity[artistKey] ?: 0.0) * 30.0

        // Known artist boost (Last.fm top / recent, or local top)
        val isKnownArtist = artistKey in profile.topArtistNames ||
                            artistKey in profile.recentArtists ||
                            artistKey in profile.localTopArtists
        if (isKnownArtist) s += 20.0

        // Gentle discovery bonus for artists outside the user's known set
        if (!isKnownArtist) s += 5.0

        // Already-heard track downgrade (avoid re-surfacing known tracks)
        val trackKey = "$artistKey|${song.title.trim().lowercase()}"
        if (trackKey in profile.topTrackKeys)    s -= 10.0  // top track — skip
        if (trackKey in profile.recentTrackKeys) s -= 5.0   // recently played — mild skip

        return s
    }
}
