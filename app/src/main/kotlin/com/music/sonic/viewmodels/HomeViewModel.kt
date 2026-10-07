package com.music.sonic.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.models.filterYoutubeShorts
import com.music.innertube.pages.ExplorePage
import com.music.innertube.pages.HomePage
import com.music.innertube.utils.completed
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.music.sonic.constants.HideExplicitKey
import com.music.sonic.constants.HideVideoSongsKey
import com.music.sonic.constants.HideYoutubeShortsKey
import com.music.sonic.constants.InnerTubeCookieKey
import com.music.sonic.constants.QuickPicks
import com.music.sonic.constants.QuickPicksKey
import com.music.sonic.constants.SongSortType
import com.music.sonic.db.MusicDatabase
import com.music.sonic.db.entities.Album
import com.music.sonic.db.entities.LocalItem
import com.music.sonic.db.entities.Song
import com.music.sonic.db.entities.SpeedDialItem
import com.music.sonic.extensions.filterVideoSongs
import com.music.sonic.extensions.toEnum
import com.music.sonic.models.SimilarRecommendation
import com.music.sonic.models.toMediaMetadata
import com.music.sonic.utils.SyncUtils
import com.music.sonic.utils.dataStore
import com.music.sonic.utils.get
import com.music.sonic.utils.reportException
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyDiscoverItem(
  val seed: Song,
  val recommendation: YTItem,
  val relatedEndpoint: BrowseEndpoint?
)

data class CommunityPlaylistItem(val playlist: PlaylistItem, val songs: List<SongItem>)

@HiltViewModel
class HomeViewModel
@Inject
constructor(
  @ApplicationContext val context: Context,
  val database: MusicDatabase,
  val syncUtils: SyncUtils,
) : ViewModel() {
  val isRefreshing = MutableStateFlow(false)
  val isLoading = MutableStateFlow(false)
  val isRandomizing = MutableStateFlow(false)

  private val quickPicksEnum =
    context.dataStore.data
      .map {
        (try {
            it[QuickPicksKey]
          } catch (e: Exception) {
            null
          })
          .toEnum(QuickPicks.QUICK_PICKS)
      }
      .distinctUntilChanged()

  val quickPicks = MutableStateFlow<List<Song>?>(null)
  val networkQuickPicks = MutableStateFlow<List<SongItem>?>(null)
  private var previousQuickPickIds = emptySet<String>()
  private var previousNetworkQuickPickIds = emptySet<String>()
  val recentSongs: StateFlow<List<Song>?> =
    database
      .events()
      .map { events ->
        events
          .filter { it.event.playTime > 0L }
          .map { it.song }
          .distinctBy { it.id }
          .take(20)
      }
      .stateIn(viewModelScope, SharingStarted.Lazily, null)

  val likedSongs: StateFlow<List<Song>?> =
    database
      .likedSongs(SongSortType.CREATE_DATE, descending = true)
      .map { songs -> songs.take(20) }
      .stateIn(viewModelScope, SharingStarted.Lazily, null)

  val dailyDiscover = MutableStateFlow<List<DailyDiscoverItem>?>(null)
  val forgottenFavorites = MutableStateFlow<List<Song>?>(null)
  val keepListening = MutableStateFlow<List<LocalItem>?>(null)
  val similarRecommendations = MutableStateFlow<List<SimilarRecommendation>?>(null)
  val accountPlaylists = MutableStateFlow<List<PlaylistItem>?>(null)
  val homePage = MutableStateFlow<HomePage?>(null)
  val explorePage = MutableStateFlow<ExplorePage?>(null)
  val communityPlaylists = MutableStateFlow<List<CommunityPlaylistItem>?>(null)
  val selectedChip = MutableStateFlow<HomePage.Chip?>(null)
  private val previousHomePage = MutableStateFlow<HomePage?>(null)

  val aiRecommendedPlaylist =
    database
      .playlistsByNameAsc()
      .map { playlists -> playlists.find { it.playlist.name == "Recommended by AI" } }
      .flatMapLatest { playlist ->
        if (playlist != null && playlist.songCount > 0) {
          database.playlistSongs(playlist.playlist.id).map { playlistSongs ->
            playlist to playlistSongs.map { it.song }
          }
        } else {
          flowOf(null)
        }
      }
      .stateIn(viewModelScope, SharingStarted.Lazily, null)

  val allLocalItems = MutableStateFlow<List<LocalItem>>(emptyList())
  val allYtItems = MutableStateFlow<List<YTItem>>(emptyList())

  val speedDialItems: StateFlow<List<YTItem>> =
    combine(database.speedDialDao.getAll(), keepListening, quickPicks) {
        pinned,
        keepListening,
        quick ->
        val pinnedItems = pinned.map { it.toYTItem() }
        val filled = pinnedItems.toMutableList()
        val targetSize = 27

        if (filled.size < targetSize) {

          keepListening?.let { k ->
            val needed = targetSize - filled.size
            val available =
              k.filter { item -> filled.none { p -> p.id == item.id } }
                .mapNotNull { item ->
                  when (item) {
                    is Song ->
                      SongItem(
                        id = item.id,
                        title = item.title,
                        artists = item.artists.map { Artist(name = it.name, id = it.id) },
                        thumbnail = item.thumbnailUrl ?: "",
                        explicit = false
                      )
                    is Album ->
                      AlbumItem(
                        browseId = item.id,
                        playlistId = item.album.playlistId ?: "",
                        title = item.title,
                        artists = item.artists.map { Artist(name = it.name, id = it.id) },
                        year = item.album.year,
                        thumbnail = item.thumbnailUrl ?: ""
                      )
                    else -> null
                  }
                }
            filled.addAll(available.take(needed))
          }
        }

        if (filled.size < targetSize) {

          quick?.let { q ->
            val needed = targetSize - filled.size
            val available =
              q.filter { song -> filled.none { p -> p.id == song.id } }
                .map { song ->
                  SongItem(
                    id = song.id,
                    title = song.title,
                    artists = song.artists.map { Artist(name = it.name, id = it.id) },
                    thumbnail = song.thumbnailUrl ?: "",
                    explicit = false
                  )
                }
            filled.addAll(available.take(needed))
          }
        }

        filled.take(targetSize)
      }
      .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  suspend fun getRandomItem(): YTItem? {
    try {
      isRandomizing.value = true

      kotlinx.coroutines.delay(1000)

      val userSongs = mutableListOf<YTItem>()
      val otherSources = mutableListOf<YTItem>()

      quickPicks.value?.let { songs ->
        userSongs.addAll(
          songs.map { song ->
            SongItem(
              id = song.id,
              title = song.title,
              artists = song.artists.map { Artist(name = it.name, id = it.id) },
              thumbnail = song.thumbnailUrl ?: "",
              explicit = false
            )
          }
        )
      }

      keepListening.value?.let { items ->
        items.forEach { item ->
          when (item) {
            is Song ->
              userSongs.add(
                SongItem(
                  id = item.id,
                  title = item.title,
                  artists = item.artists.map { Artist(name = it.name, id = it.id) },
                  thumbnail = item.thumbnailUrl ?: "",
                  explicit = false
                )
              )
            is Album ->
              otherSources.add(
                AlbumItem(
                  browseId = item.id,
                  playlistId = item.album.playlistId ?: "",
                  title = item.title,
                  artists = item.artists.map { Artist(name = it.name, id = it.id) },
                  year = item.album.year,
                  thumbnail = item.thumbnailUrl ?: ""
                )
              )
            else -> {}
          }
        }
      }

      otherSources.addAll(allYtItems.value)

      val item =
        if (userSongs.isNotEmpty() && (otherSources.isEmpty() || Random.nextFloat() < 0.8f)) {
          userSongs.distinctBy { it.id }.shuffled().firstOrNull()
        } else {
          otherSources.distinctBy { it.id }.shuffled().firstOrNull()
        } ?: userSongs.firstOrNull() ?: otherSources.firstOrNull()

      return item
    } finally {
      isRandomizing.value = false
    }
  }

  val accountName = MutableStateFlow("Guest")
  val accountImageUrl = MutableStateFlow<String?>(null)

  fun togglePin(item: YTItem) {
    viewModelScope.launch(Dispatchers.IO) {
      val speedDialItem = SpeedDialItem.fromYTItem(item)
      val isPinned = database.speedDialDao.isPinned(speedDialItem.id).first()
      if (isPinned) {
        database.speedDialDao.delete(speedDialItem.id)
      } else {
        database.speedDialDao.insert(speedDialItem)
      }
    }
  }

  private var lastProcessedCookie: String? = null

  private var isProcessingAccountData = false

  private suspend fun getDailyDiscover() {
    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
    val hideExplicit = context.dataStore.get(HideExplicitKey, false)

    val likedSongs = database.likedSongsByCreateDateAsc().first()
    val fromTimeStamp = System.currentTimeMillis() - 86400000L * 30
    val topPlayedSongs = database.mostPlayedSongs(fromTimeStamp, limit = 15).first()
    val recentPlayedSongs = database.events().first().map { it.song }.distinctBy { it.id }.take(10)

    var allCandidateSeeds = (likedSongs + topPlayedSongs + recentPlayedSongs).distinctBy { it.id }
    if (allCandidateSeeds.isEmpty()) {
      allCandidateSeeds = database.songs(SongSortType.CREATE_DATE, descending = true).first().take(5)
    }
    if (allCandidateSeeds.isEmpty()) return

    // Ensure artist diversity in seeds
    val seedsByArtist = allCandidateSeeds.groupBy { it.artists.firstOrNull()?.id ?: it.id }
    val diverseSeeds = seedsByArtist.values.mapNotNull { it.shuffled().firstOrNull() }.shuffled().take(6)
    val seeds = if (diverseSeeds.size >= 3) diverseSeeds else allCandidateSeeds.shuffled().take(5)

    val knownSongIds = (likedSongs.map { it.id } + recentPlayedSongs.map { it.id }).toSet()
    val items = java.util.Collections.synchronizedList(mutableListOf<DailyDiscoverItem>())

    kotlinx.coroutines.coroutineScope {
      seeds
        .map { seed ->
          launch(Dispatchers.IO) {
            val endpoint =
              YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
            if (endpoint != null) {
              YouTube.related(endpoint).onSuccess { page ->
                val candidateSongs =
                  page.songs
                    .filter { item ->
                      if (hideVideoSongs && item.isVideoSong) return@filter false
                      if (hideExplicit && item.explicit) return@filter false
                      item.id != seed.id
                    }

                // Prioritize true discovery: tracks the user hasn't already liked or heavily played
                val undiscovered = candidateSongs.filter { it.id !in knownSongIds }
                val selectedRecs = (undiscovered.take(3) + candidateSongs.take(3)).distinctBy { it.id }.take(3)

                selectedRecs.forEach { rec ->
                  items.add(
                    DailyDiscoverItem(
                      seed = seed,
                      recommendation = rec,
                      relatedEndpoint = endpoint
                    )
                  )
                }
              }
            }
          }
        }
        .forEach { it.join() }
    }

    dailyDiscover.value = items.toList().distinctBy { it.recommendation.id }.shuffled()
  }

  private suspend fun getQuickPicks() {
    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
    val likedIds = database.likedSongs(SongSortType.CREATE_DATE, descending = true)
      .first()
      .mapTo(mutableSetOf()) { it.id }
    when (quickPicksEnum.first()) {
      QuickPicks.QUICK_PICKS -> {
        val relatedSongs = database.quickPicks().first()
          .filterVideoSongs(hideVideoSongs)
          .filterNot { it.id in likedIds }
        val forgotten =
          database.forgottenFavorites().first()
            .filterVideoSongs(hideVideoSongs)
            .filterNot { it.id in likedIds }
            .take(8)

        val recentSongsList = database.events().first().map { it.song }.distinctBy { it.id }.take(5)
        val seeds = recentSongsList.take(5)

        val ytSimilarSongs = java.util.Collections.synchronizedList(mutableListOf<Song>())

        coroutineScope {
          seeds.map { seedSong ->
            launch(Dispatchers.IO) {
              val endpoint =
                YouTube.next(WatchEndpoint(videoId = seedSong.id)).getOrNull()?.relatedEndpoint
              if (endpoint != null) {
                YouTube.related(endpoint).onSuccess { page ->
                  page.songs.take(6).forEach { ytSong ->
                    if (hideVideoSongs && ytSong.isVideoSong) return@forEach
                    var local = database.song(ytSong.id).first()
                    if (local == null) {
                      database.query {
                        try {
                          insert(ytSong.toMediaMetadata())
                        } catch (_: Exception) {}
                      }
                      local = database.song(ytSong.id).first()
                    }
                    local?.let {
                      if (!hideVideoSongs || !it.song.isVideo) {
                        ytSimilarSongs.add(it)
                      }
                    }
                  }
                }
              }
            }
          }.forEach { it.join() }
        }

        val combined =
          (relatedSongs + forgotten + ytSimilarSongs)
            .filterNot { it.id in likedIds }
            .distinctBy { it.id }

        var finalPicks = combined.ifEmpty { relatedSongs.distinctBy { it.id } }

        if (finalPicks.isEmpty()) {
          finalPicks = database.songs(SongSortType.CREATE_DATE, descending = true)
            .first()
            .filterVideoSongs(hideVideoSongs)
            .filterNot { it.id in likedIds }
        }

        quickPicks.value = selectFreshBatch(finalPicks, previousQuickPickIds, 20) { it.id }
        previousQuickPickIds = quickPicks.value.orEmpty().mapTo(mutableSetOf()) { it.id }
      }
      QuickPicks.LAST_LISTEN -> {
        val song = database.events().first().firstOrNull()?.song
        if (song != null && database.hasRelatedSongs(song.id)) {
          val candidates = database.getRelatedSongs(song.id)
            .first()
            .filterVideoSongs(hideVideoSongs)
            .filterNot { it.id in likedIds }
          quickPicks.value = selectFreshBatch(candidates, previousQuickPickIds, 20) { it.id }
        } else {
          val fallbackSongs = database.quickPicks().first()
            .filterVideoSongs(hideVideoSongs)
            .filterNot { it.id in likedIds }
          val dbSongs = database.songs(SongSortType.CREATE_DATE, descending = true)
            .first()
            .filterVideoSongs(hideVideoSongs)
            .filterNot { it.id in likedIds }
          quickPicks.value = selectFreshBatch(fallbackSongs + dbSongs, previousQuickPickIds, 20) { it.id }
        }
        previousQuickPickIds = quickPicks.value.orEmpty().mapTo(mutableSetOf()) { it.id }
      }
    }
  }

  private fun <T> selectFreshBatch(
    candidates: List<T>,
    previousIds: Set<String>,
    limit: Int,
    idOf: (T) -> String,
  ): List<T> {
    val unique = candidates.distinctBy(idOf)
    val fresh = unique.filterNot { idOf(it) in previousIds }.shuffled()
    val repeats = unique.filter { idOf(it) in previousIds }.shuffled()
    return (fresh + repeats).take(limit)
  }

  private suspend fun getCommunityPlaylists() {
    val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 4
    val artistSeeds =
      database
        .mostPlayedArtists(fromTimeStamp, limit = 10)
        .first()
        .filter { it.artist.isYouTubeArtist }
        .shuffled()
        .take(3)
    val songSeeds = database.mostPlayedSongs(fromTimeStamp, limit = 5).first().shuffled().take(2)

    val candidatePlaylists = java.util.Collections.synchronizedList(mutableListOf<PlaylistItem>())

    kotlinx.coroutines.coroutineScope {
      artistSeeds.map { seed ->
        launch(Dispatchers.IO) {
          YouTube.artist(seed.id).onSuccess { page ->
            page.sections.forEach { section ->
              section.items.filterIsInstance<PlaylistItem>().forEach { playlist ->
                if (
                  playlist.author?.name != "YouTube Music" &&
                    playlist.author?.name != "YouTube" &&
                    playlist.author?.name != "Playlist" &&
                    playlist.author?.name != seed.artist.name &&
                    !playlist.id.startsWith("RD") &&
                    !playlist.id.startsWith("OLAK")
                ) {
                  candidatePlaylists.add(playlist)
                }
              }
            }
          }
        }
      }

      songSeeds.map { seed ->
        launch(Dispatchers.IO) {
          val endpoint = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
          if (endpoint != null) {
            YouTube.related(endpoint).onSuccess { page ->
              page.playlists.forEach { playlist ->
                if (
                  playlist.author?.name != "YouTube Music" &&
                    playlist.author?.name != "YouTube" &&
                    playlist.author?.name != "Playlist" &&
                    !playlist.id.startsWith("RD") &&
                    !playlist.id.startsWith("OLAK")
                ) {
                  candidatePlaylists.add(playlist)
                }
              }
            }
          }
        }
      }
    }

    val uniqueCandidates = candidatePlaylists.distinctBy { it.id }.shuffled().take(5)

    val playlists = java.util.Collections.synchronizedList(mutableListOf<CommunityPlaylistItem>())

    kotlinx.coroutines.coroutineScope {
      uniqueCandidates
        .map { playlist ->
          launch(Dispatchers.IO) {
            YouTube.playlist(playlist.id).onSuccess { page ->
              val songs = page.songs.take(10)
              if (songs.isNotEmpty()) {

                val songCountText = page.playlist.songCountText ?: playlist.songCountText
                val updatedPlaylist = playlist.copy(songCountText = songCountText)
                playlists.add(CommunityPlaylistItem(updatedPlaylist, songs))
              }
            }
          }
        }
        .forEach { it.join() }
    }

    communityPlaylists.value = playlists.shuffled()
  }

  private suspend fun loadLocalDataPhase() {
    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)

    getQuickPicks()

    forgottenFavorites.value =
      database.forgottenFavorites().first().filterVideoSongs(hideVideoSongs).shuffled().take(20)

    val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2
    var keepListeningSongs =
      database
        .mostPlayedSongs(fromTimeStamp, limit = 15, offset = 5)
        .first()
        .filterVideoSongs(hideVideoSongs)
    if (keepListeningSongs.isEmpty()) {
      keepListeningSongs =
        database
          .mostPlayedSongs(fromTimeStamp, limit = 15, offset = 0)
          .first()
          .filterVideoSongs(hideVideoSongs)
    }
    if (keepListeningSongs.isEmpty()) {
      keepListeningSongs =
        database
          .mostPlayedSongs(0L, limit = 15, offset = 0)
          .first()
          .filterVideoSongs(hideVideoSongs)
    }
    if (keepListeningSongs.isEmpty()) {
      keepListeningSongs =
        database
          .events()
          .first()
          .map { it.song }
          .distinctBy { it.id }
          .filterVideoSongs(hideVideoSongs)
          .take(15)
    }
    if (keepListeningSongs.isEmpty()) {
      keepListeningSongs =
        database
          .songs(SongSortType.CREATE_DATE, descending = true)
          .first()
          .filterVideoSongs(hideVideoSongs)
          .take(15)
    }

    var keepListeningAlbums =
      database
        .mostPlayedAlbums(fromTimeStamp, limit = 8, offset = 2)
        .first()
        .filter { it.album.thumbnailUrl != null }
    if (keepListeningAlbums.isEmpty()) {
      keepListeningAlbums =
        database
          .mostPlayedAlbums(fromTimeStamp, limit = 8, offset = 0)
          .first()
          .filter { it.album.thumbnailUrl != null }
    }
    if (keepListeningAlbums.isEmpty()) {
      keepListeningAlbums =
        database
          .mostPlayedAlbums(0L, limit = 8, offset = 0)
          .first()
          .filter { it.album.thumbnailUrl != null }
    }

    var keepListeningArtists =
      database
        .mostPlayedArtists(fromTimeStamp)
        .first()
        .filter { it.artist.isYouTubeArtist && it.artist.thumbnailUrl != null }
    if (keepListeningArtists.isEmpty()) {
      keepListeningArtists =
        database
          .mostPlayedArtists(0L)
          .first()
          .filter { it.artist.isYouTubeArtist && it.artist.thumbnailUrl != null }
    }

    val combinedKeepListening =
      (keepListeningSongs.shuffled().take(10) +
          keepListeningAlbums.shuffled().take(5) +
          keepListeningArtists.shuffled().take(5))
        .shuffled()

    keepListening.value = combinedKeepListening.ifEmpty {
      (quickPicks.value.orEmpty().take(10) + forgottenFavorites.value.orEmpty().take(5)).shuffled()
    }

    allLocalItems.value =
      (quickPicks.value.orEmpty() +
          likedSongs.value.orEmpty() +
          forgottenFavorites.value.orEmpty() +
          keepListening.value.orEmpty())
        .filter { it is Song || it is Album }
  }

  private suspend fun loadSimilarRecommendations() {
    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
    val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2

    coroutineScope {
      var candidateArtists = database
        .mostPlayedArtists(fromTimeStamp, limit = 15)
        .first()
        .filter { it.artist.isYouTubeArtist }
      if (candidateArtists.isEmpty()) {
        candidateArtists = database
          .mostPlayedArtists(0L, limit = 15)
          .first()
          .filter { it.artist.isYouTubeArtist }
      }

      val artistDeferreds =
        candidateArtists
          .shuffled()
          .take(4)
          .map { artist ->
            async(Dispatchers.IO) {
              val items = mutableListOf<YTItem>()
              YouTube.artist(artist.id).onSuccess { page ->
                page.sections.takeLast(3).forEach { section -> items += section.items }
              }
              SimilarRecommendation(
                title = artist,
                items =
                  items
                    .distinctBy { item -> item.id }
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)
                    .shuffled()
                    .take(12)
                    .ifEmpty {
                      return@async null
                    }
              )
            }
          }

      var candidateSongs = database
        .mostPlayedSongs(fromTimeStamp, limit = 15)
        .first()
        .filter { it.album != null }
      if (candidateSongs.isEmpty()) {
        candidateSongs = database
          .mostPlayedSongs(0L, limit = 15)
          .first()
          .filter { it.album != null }
      }
      if (candidateSongs.isEmpty()) {
        candidateSongs = database.likedSongsByCreateDateAsc().first().filter { it.album != null }
      }

      val songDeferreds =
        candidateSongs
          .shuffled()
          .take(3)
          .map { song ->
            async(Dispatchers.IO) {
              val endpoint =
                YouTube.next(WatchEndpoint(videoId = song.id)).getOrNull()?.relatedEndpoint
                  ?: return@async null
              val page = YouTube.related(endpoint).getOrNull() ?: return@async null
              SimilarRecommendation(
                title = song,
                items =
                  (page.songs.shuffled().take(10) +
                      page.albums.shuffled().take(5) +
                      page.artists.shuffled().take(3) +
                      page.playlists.shuffled().take(3))
                    .distinctBy { it.id }
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)
                    .shuffled()
                    .ifEmpty {
                      return@async null
                    }
              )
            }
          }

      var candidateAlbums = database
        .mostPlayedAlbums(fromTimeStamp, limit = 10)
        .first()
        .filter { it.album.thumbnailUrl != null }
      if (candidateAlbums.isEmpty()) {
        candidateAlbums = database
          .mostPlayedAlbums(0L, limit = 10)
          .first()
          .filter { it.album.thumbnailUrl != null }
      }

      val albumDeferreds =
        candidateAlbums
          .shuffled()
          .take(2)
          .map { album ->
            async(Dispatchers.IO) {
              val items = mutableListOf<YTItem>()
              YouTube.album(album.id).onSuccess { page -> page.otherVersions.let { items += it } }
              album.artists.firstOrNull()?.id?.let { artistId ->
                YouTube.artist(artistId).onSuccess { page ->
                  page.sections.lastOrNull()?.items?.let { items += it }
                }
              }
              SimilarRecommendation(
                title = album,
                items =
                  items
                    .distinctBy { it.id }
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)
                    .shuffled()
                    .take(10)
                    .ifEmpty {
                      return@async null
                    }
              )
            }
          }

      val results = (artistDeferreds + songDeferreds + albumDeferreds).awaitAll()
      similarRecommendations.value = results.filterNotNull().shuffled()
    }
  }

  private suspend fun loadNetworkDataPhase() {
    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
    networkQuickPicks.value = null

    coroutineScope {
      launch(Dispatchers.IO) { getDailyDiscover() }
      launch(Dispatchers.IO) { getCommunityPlaylists() }
      launch(Dispatchers.IO) { loadSimilarRecommendations() }
      launch(Dispatchers.IO) {
        YouTube.home()
          .onSuccess { page ->
            val filteredPage =
              page.copy(
                sections =
                  page.sections.mapNotNull { section ->
                    val filteredItems =
                      section.items
                        .filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs)
                        .filterYoutubeShorts(hideYoutubeShorts)
                    if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
                  }
              )
            homePage.value = filteredPage

            val likedIds = database.likedSongs(SongSortType.CREATE_DATE, descending = true)
              .first()
              .mapTo(mutableSetOf()) { it.id }
            val candidates = filteredPage.sections
              .filterNot { section ->
                listOf("keep listening", "listen again", "recent", "history")
                  .any { section.title.contains(it, ignoreCase = true) }
              }
              .flatMap { section -> section.items.filterIsInstance<SongItem>() }
              .filterNot { it.id in likedIds }
              .distinctBy { it.id }
            networkQuickPicks.value = selectFreshBatch(candidates, previousNetworkQuickPickIds, 20) { it.id }
            previousNetworkQuickPickIds = networkQuickPicks.value.orEmpty().mapTo(mutableSetOf()) { it.id }
          }
          .onFailure {
            networkQuickPicks.value = emptyList()
            reportException(it)
          }
      }
      launch(Dispatchers.IO) {
        YouTube.explore()
          .onSuccess { page ->
            explorePage.value =
              page.copy(newReleaseAlbums = page.newReleaseAlbums.filterExplicit(hideExplicit))
          }
          .onFailure { reportException(it) }
      }
      if (YouTube.cookie != null) {
        launch(Dispatchers.IO) { loadAccountPlaylists() }
      }
    }

    allYtItems.value =
      similarRecommendations.value?.flatMap { it.items }.orEmpty() +
        homePage.value?.sections?.flatMap { it.items }.orEmpty()
  }

  private suspend fun load() {
    isLoading.value = true

    loadLocalDataPhase()

    loadNetworkDataPhase()

    isLoading.value = false
  }

  private val _isLoadingMore = MutableStateFlow(false)
  val isLoadingMore = _isLoadingMore.asStateFlow()

  fun loadMoreYouTubeItems(continuation: String?) {
    if (continuation == null || _isLoadingMore.value) return
    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)

    viewModelScope.launch(Dispatchers.IO) {
      _isLoadingMore.value = true
      var currentContinuation = continuation
      var hasNewItems = false

      while (currentContinuation != null && !hasNewItems) {
        val nextSections = YouTube.home(currentContinuation).getOrNull() ?: break
        currentContinuation = nextSections.continuation

        val newSections =
          nextSections.sections.mapNotNull { section ->
            val filteredItems =
              section.items
                .filterExplicit(hideExplicit)
                .filterVideoSongs(hideVideoSongs)
                .filterYoutubeShorts(hideYoutubeShorts)
            if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
          }

        if (newSections.isNotEmpty()) {
          hasNewItems = true
        }

        homePage.value =
          nextSections.copy(
            chips = homePage.value?.chips,
            continuation = currentContinuation,
            sections = homePage.value?.sections.orEmpty() + newSections
          )
      }
      _isLoadingMore.value = false
    }
  }

  fun toggleChip(chip: HomePage.Chip?) {
    if (chip == null || chip == selectedChip.value && previousHomePage.value != null) {
      homePage.value = previousHomePage.value
      previousHomePage.value = null
      selectedChip.value = null
      return
    }

    if (selectedChip.value == null) {
      previousHomePage.value = homePage.value
    }

    viewModelScope.launch(Dispatchers.IO) {
      val hideExplicit = context.dataStore.get(HideExplicitKey, false)
      val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
      val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
      val nextSections = YouTube.home(params = chip.endpoint?.params).getOrNull() ?: return@launch

      homePage.value =
        nextSections.copy(
          chips = homePage.value?.chips,
          sections =
            nextSections.sections.map { section ->
              section.copy(
                items =
                  section.items
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)
                    .filterYoutubeShorts(hideYoutubeShorts)
              )
            }
        )
      selectedChip.value = chip
    }
  }

  private suspend fun loadAccountPlaylists() {
    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
    YouTube.library("FEmusic_liked_playlists")
      .completed()
      .onSuccess {
        accountPlaylists.value =
          it.items
            .filterIsInstance<PlaylistItem>()
            .filterNot { it.id == "SE" }
            .filterYoutubeShorts(hideYoutubeShorts)
      }
      .onFailure { reportException(it) }
  }

  fun refresh() {
    if (isRefreshing.value) return
    viewModelScope.launch(Dispatchers.IO) {
      try {
        isRefreshing.value = true
        load()
      } finally {
        isRefreshing.value = false
      }
    }

    viewModelScope.launch(Dispatchers.IO) { syncUtils.tryAutoSync() }
  }

  init {
    // Load immediately — BitChord approach: don't block on cookie, fetch home feed for all users.
    // Cookie-gated features (account playlists, history) fall back gracefully when not signed in.
    viewModelScope.launch(Dispatchers.IO) {
      // Seed the cookie from DataStore before first load so authenticated calls work on startup
      val initialCookie = try {
        context.dataStore.data.map { it[InnerTubeCookieKey] }.first()
      } catch (e: Exception) {
        null
      }
      if (!initialCookie.isNullOrEmpty()) {
        YouTube.cookie = initialCookie
      }
      load()
    }

    viewModelScope.launch(Dispatchers.IO) { syncUtils.tryAutoSync() }

    viewModelScope.launch(Dispatchers.IO) {
      context.dataStore.data
        .map {
          (try {
            it[InnerTubeCookieKey]
          } catch (e: Exception) {
            null
          })
        }
        .collect { cookie ->
          if (isProcessingAccountData) return@collect

          lastProcessedCookie = cookie
          isProcessingAccountData = true

          try {
            if (cookie != null && cookie.isNotEmpty()) {

              YouTube.cookie = cookie

              YouTube.accountInfo()
                .onSuccess { info ->
                  accountName.value = info.name
                  accountImageUrl.value = info.thumbnailUrl
                }
                .onFailure { reportException(it) }
            } else {
              accountName.value = "Guest"
              accountImageUrl.value = null
              accountPlaylists.value = null
            }
          } finally {
            isProcessingAccountData = false
          }
        }
    }

    viewModelScope.launch(Dispatchers.IO) {
      context.dataStore.data
        .map {
          (try {
            it[HideYoutubeShortsKey]
          } catch (e: Exception) {
            null
          }) ?: false
        }
        .distinctUntilChanged()
        .collect {
          if (YouTube.cookie != null && accountPlaylists.value != null) {
            loadAccountPlaylists()
          }
        }
    }
  }
}
