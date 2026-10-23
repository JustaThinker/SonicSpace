package com.music.sonic.personalization

import android.content.Context
import com.music.sonic.BuildConfig
import com.music.sonic.constants.LastFMSessionKey
import com.music.sonic.constants.LastFMUsernameKey
import com.music.sonic.constants.SongSortType
import com.music.sonic.db.MusicDatabase
import com.music.sonic.utils.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private const val TASTE_PROFILE_TTL_MILLIS = 60L * 60 * 1000  // 1 hour

/**
 * Builds and caches the user's [TasteProfile] for up to [TASTE_PROFILE_TTL_MILLIS].
 * Data is fetched in parallel from Last.fm (if logged in) and the local MusicDatabase.
 */
@Singleton
class TasteProfileProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lastFmDataSource: LastFmDataSource,
    private val database: MusicDatabase,
) {
    private val mutex = Mutex()
    private var cached: TasteProfile? = null
    private var cachedForUsername: String? = null
    private var cachedAt: Long = 0L

    /**
     * Returns the current [TasteProfile], using the 1-hour in-memory cache unless
     * [forceRefresh] is true or the username has changed.
     */
    suspend fun get(forceRefresh: Boolean = false): TasteProfile = mutex.withLock {
        val prefs = context.dataStore.data.first()
        val username = try { prefs[LastFMUsernameKey] } catch (_: Exception) { null }.orEmpty()
        val hasSession = try { prefs[LastFMSessionKey] } catch (_: Exception) { null }.orEmpty().isNotBlank()

        // API key comes from BuildConfig, initialized at app startup via LastFM.initialize()
        val apiKey = BuildConfig.LASTFM_API_KEY.takeIf { it.isNotEmpty() }.orEmpty()

        val now = System.currentTimeMillis()
        cached?.let {
            if (!forceRefresh &&
                cachedForUsername == username &&
                (now - cachedAt) < TASTE_PROFILE_TTL_MILLIS
            ) return@withLock it
        }

        val profile = build(username = username, apiKey = apiKey, hasSession = hasSession)
        cached = profile
        cachedForUsername = username
        cachedAt = now
        profile
    }

    private suspend fun build(
        username: String,
        apiKey: String,
        hasSession: Boolean,
    ): TasteProfile = coroutineScope {
        val hasLastFm = username.isNotBlank() && apiKey.isNotBlank() && hasSession

        // Parallel: Last.fm calls (if authenticated) + local DB
        val topArtistsDeferred   = if (hasLastFm) async(Dispatchers.IO) { lastFmDataSource.topArtists(username, apiKey)   } else null
        val recentTracksDeferred = if (hasLastFm) async(Dispatchers.IO) { lastFmDataSource.recentTracks(username, apiKey)  } else null
        val topTracksDeferred    = if (hasLastFm) async(Dispatchers.IO) { lastFmDataSource.topTracks(username, apiKey)    } else null
        val topTagsDeferred      = if (hasLastFm) async(Dispatchers.IO) { lastFmDataSource.topTags(username, apiKey)      } else null

        val sixtyDaysAgo = System.currentTimeMillis() - 86400000L * 60

        val localTopArtistsDeferred = async(Dispatchers.IO) {
            database.mostPlayedArtists(fromTimeStamp = sixtyDaysAgo, limit = 30).first()
        }
        val localLikedDeferred = async(Dispatchers.IO) {
            database.likedSongs(SongSortType.CREATE_DATE, descending = true).first()
        }
        val localTopSongsDeferred = async(Dispatchers.IO) {
            database.mostPlayedSongs(fromTimeStamp = 0L, limit = 50).first()
        }

        val lfmTopArtists   = topArtistsDeferred?.await().orEmpty()
        val lfmRecent       = recentTracksDeferred?.await().orEmpty()
        val lfmTopTracks    = topTracksDeferred?.await().orEmpty()
        val lfmTopTags      = topTagsDeferred?.await().orEmpty()
        val localTopArtists = localTopArtistsDeferred.await()
        val localLiked      = localLikedDeferred.await()
        val localTopSongs   = localTopSongsDeferred.await()

        // Build affinity map: normalized total play time per artist from local DB
        val maxPlay = localTopSongs.maxOfOrNull { it.song.totalPlayTime }?.toDouble().takeIf { it != 0.0 } ?: 1.0
        val affinityMap = localTopSongs
            .groupBy { it.artists.firstOrNull()?.name?.trim()?.lowercase() ?: "" }
            .filterKeys { it.isNotEmpty() }
            .mapValues { (_, songs) ->
                songs.sumOf { it.song.totalPlayTime.toDouble() } / maxPlay
            }

        TasteProfile(
            topArtistNames  = lfmTopArtists.map { it.name.lowercase() }.toSet(),
            recentArtists   = lfmRecent.map { it.artist.name.lowercase() }.toSet(),
            topTags         = lfmTopTags.map { it.name.lowercase() }.toSet(),
            topTrackKeys    = lfmTopTracks.map { "${it.artist.name.lowercase()}|${it.name.lowercase()}" }.toSet(),
            recentTrackKeys = lfmRecent.map { "${it.artist.name.lowercase()}|${it.name.lowercase()}" }.toSet(),
            artistAffinity  = affinityMap,
            localTopArtists = localTopArtists.map { it.artist.name.lowercase() }.toSet(),
            localLikedIds   = localLiked.map { it.id }.toSet(),
        )
    }
}
