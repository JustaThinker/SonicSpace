package com.music.sonic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.utils.completed
import com.music.innertube.utils.parseCookieString
import com.music.sonic.LocalDatabase
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.constants.GridItemSize
import com.music.sonic.constants.GridItemsSizeKey
import com.music.sonic.constants.GridThumbnailHeight
import com.music.sonic.constants.InnerTubeCookieKey
import com.music.sonic.constants.ListItemHeight
import com.music.sonic.ui.component.MoodAndGenreCard
import com.music.sonic.constants.ListThumbnailSize
import com.music.sonic.constants.ShowSpeedDialKey
import com.music.sonic.constants.SmallGridThumbnailHeight
import com.music.sonic.constants.ThumbnailCornerRadius
import com.music.sonic.db.entities.Album
import com.music.sonic.db.entities.Artist
import com.music.sonic.db.entities.LocalItem
import com.music.sonic.db.entities.Playlist
import com.music.sonic.db.entities.PlaylistEntity
import com.music.sonic.db.entities.PlaylistSongMap
import com.music.sonic.db.entities.Song
import com.music.sonic.extensions.toMediaItem
import com.music.sonic.models.toMediaMetadata
import com.music.sonic.playback.queues.ListQueue
import com.music.sonic.playback.queues.YouTubeQueue
import com.music.sonic.ui.component.AlbumGridItem
import com.music.sonic.ui.component.ArtistGridItem
import com.music.sonic.ui.component.ChipsRow
import com.music.sonic.ui.component.LocalBottomSheetPageState
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.NavigationTitle
import com.music.sonic.ui.component.PlayingIndicator
import com.music.sonic.ui.component.RecentTrackRow
import com.music.sonic.ui.component.RandomizeGridItem
import com.music.sonic.ui.component.SongGridItem
import com.music.sonic.ui.component.SongListItem
import com.music.sonic.ui.component.SpeedDialGridItem
import com.music.sonic.ui.component.YouTubeGridItem
import com.music.sonic.ui.component.YouTubeListItem
import com.music.sonic.ui.component.shimmer.GridItemPlaceHolder
import com.music.sonic.ui.component.shimmer.ShimmerHost
import com.music.sonic.ui.component.shimmer.TextPlaceholder
import com.music.sonic.ui.menu.AlbumMenu
import com.music.sonic.ui.menu.ArtistMenu
import com.music.sonic.ui.menu.SongMenu
import com.music.sonic.ui.menu.YouTubeAlbumMenu
import com.music.sonic.ui.menu.YouTubeArtistMenu
import com.music.sonic.ui.menu.YouTubePlaylistMenu
import com.music.sonic.ui.menu.YouTubeSongMenu
import com.music.sonic.ui.utils.SnapLayoutInfoProvider
import com.music.sonic.ui.utils.resize
import com.music.sonic.utils.listItemShape
import com.music.sonic.utils.rememberEnumPreference
import com.music.sonic.utils.rememberPreference
import com.music.sonic.viewmodels.CommunityPlaylistItem
import com.music.sonic.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.min

private val PAGE_GUTTER = 16.dp
private val SHELF_CARD_WIDTH = 148.dp
private const val TRACKS_PER_COLUMN = 4

private fun trackColumnWidth(available: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp =
  minOf(available * 0.88f, 400.dp)

private fun NavController.navigateToPlaylistItem(playlist: PlaylistItem) {
  when (val playlistId = playlist.id.removePrefix("VL")) {
    "LM" -> navigate("auto_playlist/liked")
    "SE" -> navigate("auto_playlist/downloaded")
    else -> navigate("online_playlist/$playlistId")
  }
}

sealed class HomeSection(val id: String) {
  data object SpeedDial : HomeSection("speed_dial")

  data object QuickPicks : HomeSection("quick_picks")

  data object NetworkQuickPicks : HomeSection("network_quick_picks")

  data object KeepListening : HomeSection("keep_listening")

  data object DailyDiscover : HomeSection("daily_discover")

  data object ForgottenFavorites : HomeSection("forgotten_favorites")

  data object FromTheCommunity : HomeSection("from_the_community")

  data object AccountPlaylists : HomeSection("account_playlists")

  data object AiRecommendations : HomeSection("ai_recommendations")

  data class HomePageSection(val index: Int) : HomeSection("home_page_section_$index")

  data class SimilarRecommendation(val index: Int) :
    HomeSection("similar_recommendation_$index")

  data object MoodAndGenres : HomeSection("mood_and_genres")
}

@Composable
private fun HomeSectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  thumbnail: (@Composable () -> Unit)? = null,
  onClick: (() -> Unit)? = null,
  onPlayAllClick: (() -> Unit)? = null,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = PAGE_GUTTER, vertical = 8.dp)
  ) {
    thumbnail?.invoke()

    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    if (onPlayAllClick != null) {
      OutlinedButton(
        onClick = onPlayAllClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
        colors =
          ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
          ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
        modifier = Modifier.height(28.dp)
      ) {
        Text(
          text = stringResource(R.string.play_all),
          style = MaterialTheme.typography.labelSmall
        )
      }
    } else if (onClick != null) {
      Icon(
        painter = painterResource(R.drawable.arrow_forward),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeShelfCard(
  title: String,
  subtitle: String,
  thumbnailUrl: String?,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier.width(SHELF_CARD_WIDTH),
  isActive: Boolean = false,
  isPlaying: Boolean = false,
  isArtist: Boolean = false,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .clip(if (isArtist) CircleShape else RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      if (!thumbnailUrl.isNullOrEmpty()) {
        AsyncImage(
          model = thumbnailUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
      if (isActive && isPlaying) {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.45f)),
          contentAlignment = Alignment.Center
        ) {
          PlayingIndicator(
            color = Color.White,
            modifier = Modifier.height(24.dp)
          )
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = if (isArtist) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start,
      modifier = Modifier.fillMaxWidth()
    )
    if (subtitle.isNotBlank()) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (isArtist) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun HomeSectionHeaderPlaceholder(
  titleWidth: androidx.compose.ui.unit.Dp = 160.dp,
  subtitleWidth: androidx.compose.ui.unit.Dp? = null,
  showAction: Boolean = false,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = PAGE_GUTTER, vertical = 8.dp)
  ) {
    Column(verticalArrangement = Arrangement.Center) {
      Box(
        modifier =
          Modifier.width(titleWidth)
            .height(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
      )
      if (subtitleWidth != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier =
            Modifier.width(subtitleWidth)
              .height(13.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
        )
      }
    }
    if (showAction) {
      Box(
        modifier =
          Modifier.size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
      )
    }
  }
}

@Composable
private fun HomeTrackRowPlaceholder() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 52.dp)
        .padding(vertical = 4.dp)
  ) {
    Box(
      modifier =
        Modifier.size(48.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    Spacer(Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth(0.72f)
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
      )
      Spacer(Modifier.height(6.dp))
      Box(
        modifier =
          Modifier.fillMaxWidth(0.48f)
            .height(12.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
      )
    }
  }
}

@Composable
private fun HomeShelfCardPlaceholder() {
  Column(
    modifier = Modifier.width(SHELF_CARD_WIDTH)
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .aspectRatio(1f)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box(
      modifier =
        Modifier.fillMaxWidth(0.85f)
          .height(14.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Box(
      modifier =
        Modifier.fillMaxWidth(0.55f)
          .height(12.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
    )
  }
}

@Composable
private fun HomeShelfSectionPlaceholder(
  titleWidth: androidx.compose.ui.unit.Dp = 180.dp,
  subtitleWidth: androidx.compose.ui.unit.Dp? = 110.dp,
) {
  HomeSectionHeaderPlaceholder(titleWidth = titleWidth, subtitleWidth = subtitleWidth)
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(bottom = 16.dp)
        .horizontalScroll(rememberScrollState())
  ) {
    Spacer(Modifier.width(PAGE_GUTTER))
    repeat(4) {
      HomeShelfCardPlaceholder()
      Spacer(Modifier.width(14.dp))
    }
  }
}

@Composable
private fun HomeQuickPicksSectionPlaceholder(maxWidth: androidx.compose.ui.unit.Dp) {
  HomeSectionHeaderPlaceholder(titleWidth = 150.dp, subtitleWidth = 90.dp, showAction = true)
  val colWidth = trackColumnWidth(maxWidth)
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(bottom = 16.dp)
        .horizontalScroll(rememberScrollState())
  ) {
    Spacer(Modifier.width(PAGE_GUTTER))
    repeat(2) {
      Column(
        modifier = Modifier.width(colWidth),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        repeat(TRACKS_PER_COLUMN) {
          HomeTrackRowPlaceholder()
        }
      }
      Spacer(Modifier.width(12.dp))
    }
  }
}

@Composable
fun CommunityPlaylistCard(
  item: CommunityPlaylistItem,
  onClick: () -> Unit,
  onSongClick: (SongItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val database = LocalDatabase.current
  val playerConnection = LocalPlayerConnection.current
  val scope = rememberCoroutineScope()
  val isDark = isSystemInDarkTheme()

  val containerColor =
    if (isDark) {
      MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

  val dbPlaylist by database.playlistByBrowseId(item.playlist.id).collectAsState(initial = null)
  val isBookmarked = dbPlaylist?.playlist?.bookmarkedAt != null

  Card(
    modifier = modifier.width(320.dp).height(420.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    shape = RoundedCornerShape(28.dp),
    onClick = onClick
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp))) {
          Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
              AsyncImage(
                model = item.songs.getOrNull(0)?.thumbnail?.resize(544, 544),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
              )
              AsyncImage(
                model = item.songs.getOrNull(1)?.thumbnail?.resize(544, 544),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
              )
            }
            Row(modifier = Modifier.weight(1f)) {
              AsyncImage(
                model = item.songs.getOrNull(2)?.thumbnail?.resize(544, 544),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
              )
              AsyncImage(
                model = item.songs.getOrNull(3)?.thumbnail?.resize(544, 544),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
              )
            }
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
          Text(
            text = item.playlist.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = item.playlist.author?.name ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1
          )
        }
      }

      Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
        item.songs.take(3).forEach { song ->
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(onClick = { onSongClick(song) }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            AsyncImage(
              model = song.thumbnail.resize(544, 544),
              contentDescription = null,
              modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
              contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = song.artists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
      ) {
        IconButton(
          onClick = {
            item.playlist.playEndpoint?.let { playerConnection?.playQueue(YouTubeQueue(it)) }
          },
          modifier =
            Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface, CircleShape)
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_widget_play),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(24.dp)
          )
        }

        IconButton(
          onClick = {
            item.playlist.radioEndpoint?.let { playerConnection?.playQueue(YouTubeQueue(it)) }
          },
          modifier =
            Modifier.size(48.dp)
              .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                CircleShape
              )
        ) {
          Icon(
            painter = painterResource(R.drawable.radio),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
          )
        }

        IconButton(
          onClick = {
            scope.launch(Dispatchers.IO) {
              if (dbPlaylist?.playlist == null) {
                database.transaction {
                  val playlistEntity =
                    PlaylistEntity(
                        name = item.playlist.title,
                        browseId = item.playlist.id,
                        thumbnailUrl = item.playlist.thumbnail,
                        remoteSongCount =
                          item.playlist.songCountText?.split(" ")?.firstOrNull()?.toIntOrNull(),
                        playEndpointParams = item.playlist.playEndpoint?.params,
                        shuffleEndpointParams = item.playlist.shuffleEndpoint?.params,
                        radioEndpointParams = item.playlist.radioEndpoint?.params
                      )
                      .toggleLike()
                  insert(playlistEntity)
                  scope.launch(Dispatchers.IO) {
                    item.songs
                      .ifEmpty {
                        YouTube.playlist(item.playlist.id).completed().getOrNull()?.songs.orEmpty()
                      }
                      .map { it.toMediaMetadata() }
                      .onEach(::insert)
                      .mapIndexed { index, song ->
                        PlaylistSongMap(
                          songId = song.id,
                          playlistId = playlistEntity.id,
                          position = index,
                          setVideoId = song.setVideoId
                        )
                      }
                      .forEach(::insert)
                  }
                }
              } else {
                database.transaction {
                  val currentPlaylist = dbPlaylist!!.playlist
                  update(currentPlaylist.toggleLike())
                }
              }
            }
          },
          modifier =
            Modifier.size(48.dp)
              .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                CircleShape
              )
        ) {
          Icon(
            painter =
              painterResource(
                if (isBookmarked) R.drawable.library_add_check else R.drawable.library_add
              ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyDiscoverCard(
  dailyDiscover: com.music.sonic.viewmodels.DailyDiscoverItem,
  onClick: () -> Unit,
  navController: NavController,
  modifier: Modifier = Modifier
) {
  val database = LocalDatabase.current
  val playCount by
    database.getLifetimePlayCount(dailyDiscover.recommendation.id).collectAsState(initial = 0)
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current

  val song = dailyDiscover.recommendation as? SongItem
  val playsString = stringResource(R.string.plays)

  Card(
    modifier =
      modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(28.dp))
        .combinedClickable(
          onClick = onClick,
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (song != null) {
              menuState.show {
                YouTubeSongMenu(
                  song = song,
                  navController = navController,
                  onDismiss = { menuState.dismiss() }
                )
              }
            }
          }
        ),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
      ),
    shape = RoundedCornerShape(28.dp)
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      AsyncImage(
        model =
          ImageRequest.Builder(LocalContext.current)
            .data(dailyDiscover.recommendation.thumbnail?.resize(1200, 1200))
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      if (maxWidth > 200.dp) {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(
                brush =
                  Brush.verticalGradient(
                    colors =
                      listOf(
                        Color.Black.copy(alpha = 0.3f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f),
                        Color.Black.copy(alpha = 0.9f)
                      )
                  )
              )
        )

        Column(
          modifier = Modifier.fillMaxSize().padding(24.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = dailyDiscover.recommendation.title,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White
            )
            Text(
              text =
                buildString {
                  append(
                    (dailyDiscover.recommendation as? SongItem)?.artists?.joinToString(", ") {
                      it.name
                    } ?: ""
                  )
                  if (playCount > 0) {
                    append(" • $playCount $playsString")
                  }
                },
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.7f)
            )
          }

          val messages =
            listOf(
              R.string.daily_discover_sounds_like,
              R.string.daily_discover_because_you_listen_to,
              R.string.daily_discover_similar_to,
              R.string.daily_discover_based_on,
              R.string.daily_discover_for_fans_of
            )
          val messageRes =
            remember(dailyDiscover.seed.id) {
              messages[abs(dailyDiscover.seed.id.hashCode()) % messages.size]
            }

          Text(
            text =
              stringResource(
                messageRes,
                "${dailyDiscover.seed.title} • ${dailyDiscover.seed.artists.joinToString(", ") { it.name }}"
              ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
  navController: NavController,
  snackbarHostState: SnackbarHostState,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val bottomSheetPageState = LocalBottomSheetPageState.current
  val database = LocalDatabase.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current

  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val quickPicks by viewModel.quickPicks.collectAsState()
  val networkQuickPicks by viewModel.networkQuickPicks.collectAsState()
  val aiRecommendedPlaylist by viewModel.aiRecommendedPlaylist.collectAsState()
  val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
  val keepListening by viewModel.keepListening.collectAsState()
  val similarRecommendations by viewModel.similarRecommendations.collectAsState()
  val accountPlaylists by viewModel.accountPlaylists.collectAsState()
  val homePage by viewModel.homePage.collectAsState()
  val explorePage by viewModel.explorePage.collectAsState()
  val dailyDiscover by viewModel.dailyDiscover.collectAsState()
  val communityPlaylists by viewModel.communityPlaylists.collectAsState()

  val speedDialItems by viewModel.speedDialItems.collectAsState()
  val selectedChip by viewModel.selectedChip.collectAsState()

  val isLoading: Boolean by viewModel.isLoading.collectAsState()
  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val isRandomizing by viewModel.isRandomizing.collectAsState()
  val pullRefreshState = rememberPullToRefreshState()

  val accountName by viewModel.accountName.collectAsState()
  val accountImageUrl by viewModel.accountImageUrl.collectAsState()
  val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
  val (showSpeedDial) = rememberPreference(ShowSpeedDialKey, false)

  val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
  val url = if (isLoggedIn) accountImageUrl else null

  val scope = rememberCoroutineScope()

  var randomizeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

  val lazylistState = rememberLazyListState()
  val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)
  val currentGridHeight =
    if (gridItemSize == GridItemSize.BIG) GridThumbnailHeight else SmallGridThumbnailHeight
  val backStackEntry by navController.currentBackStackEntryAsState()
  val scrollToTop =
    backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

  LaunchedEffect(scrollToTop?.value) {
    if (scrollToTop?.value == true) {
      lazylistState.animateScrollToItem(0)
      backStackEntry?.savedStateHandle?.set("scrollToTop", false)
    }
  }

  LaunchedEffect(Unit) {
    snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
      .collect { lastVisibleIndex ->
        val len = lazylistState.layoutInfo.totalItemsCount
        if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
          viewModel.loadMoreYouTubeItems(homePage?.continuation)
        }
      }
  }

  NetworkReload(onReload = viewModel::refresh)

  if (selectedChip != null) {
    BackHandler { viewModel.toggleChip(selectedChip) }
  }

  val localGridItem: @Composable (LocalItem) -> Unit = { item ->
    when (item) {
      is Song -> {
        val isActive = item.id == mediaMetadata?.id
        HomeShelfCard(
          title = item.title,
          subtitle = item.artists.joinToString(", ") { it.name },
          thumbnailUrl = item.thumbnailUrl,
          isActive = isActive,
          isPlaying = isPlaying,
          isArtist = false,
          onClick = {
            if (isActive) {
              playerConnection.togglePlayPause()
            } else {
              playerConnection.playQueue(
                YouTubeQueue.radio(item.toMediaMetadata()),
              )
            }
          },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              SongMenu(
                originalSong = item,
                navController = navController,
                onDismiss = menuState::dismiss,
              )
            }
          }
        )
      }
      is Album -> {
        val isActive = item.id == mediaMetadata?.album?.id
        HomeShelfCard(
          title = item.album.title,
          subtitle = item.artists.joinToString(", ") { it.name },
          thumbnailUrl = item.album.thumbnailUrl,
          isActive = isActive,
          isPlaying = isPlaying,
          isArtist = false,
          onClick = { navController.navigate("album/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              AlbumMenu(
                originalAlbum = item,
                navController = navController,
                onDismiss = menuState::dismiss
              )
            }
          }
        )
      }
      is Artist -> {
        HomeShelfCard(
          title = item.artist.name,
          subtitle = stringResource(R.string.artist),
          thumbnailUrl = item.artist.thumbnailUrl,
          isActive = false,
          isPlaying = false,
          isArtist = true,
          onClick = { navController.navigate("artist/${item.id}") },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
              ArtistMenu(
                originalArtist = item,
                coroutineScope = scope,
                onDismiss = menuState::dismiss,
              )
            }
          }
        )
      }
      is Playlist -> {}
    }
  }

  val ytGridItem: @Composable (YTItem) -> Unit = { item ->
    val isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id)
    val subtitle =
      when (item) {
        is SongItem -> item.artists.joinToString(", ") { it.name }
        is AlbumItem -> listOfNotNull(item.artists?.joinToString(", ") { it.name }, item.year?.toString()).joinToString(" • ")
        is ArtistItem -> stringResource(R.string.artist)
        is PlaylistItem -> listOfNotNull(item.author?.name, item.songCountText).joinToString(" • ")
      }

    HomeShelfCard(
      title = item.title,
      subtitle = subtitle,
      thumbnailUrl = item.thumbnail,
      isActive = isActive,
      isPlaying = isPlaying,
      isArtist = item is ArtistItem,
      onClick = {
        when (item) {
          is SongItem ->
            playerConnection.playQueue(
              YouTubeQueue(
                item.endpoint ?: WatchEndpoint(videoId = item.id),
                item.toMediaMetadata()
              )
            )
          is AlbumItem -> navController.navigate("album/${item.id}")
          is ArtistItem -> navController.navigate("artist/${item.id}")
          is PlaylistItem -> navController.navigateToPlaylistItem(item)
        }
      },
      onLongClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        menuState.show {
          when (item) {
            is SongItem ->
              YouTubeSongMenu(
                song = item,
                navController = navController,
                onDismiss = menuState::dismiss
              )
            is AlbumItem ->
              YouTubeAlbumMenu(
                albumItem = item,
                navController = navController,
                onDismiss = menuState::dismiss
              )
            is ArtistItem -> YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
            is PlaylistItem ->
              YouTubePlaylistMenu(
                playlist = item,
                coroutineScope = scope,
                onDismiss = menuState::dismiss
              )
          }
        }
      }
    )
  }

  val homeSections =
    remember(
      networkQuickPicks,
      dailyDiscover,
      forgottenFavorites,
      communityPlaylists,
      aiRecommendedPlaylist,
      homePage?.sections,
      similarRecommendations,
    ) {
      val list = mutableListOf<HomeSection>()

      // 0. Network Quick Picks (YouTube home feed songs) — shown first so there's always content
      //    even for new users with empty local databases. BitChord-style: pull from home shelves.
      if (!networkQuickPicks.isNullOrEmpty()) {
        list.add(HomeSection.NetworkQuickPicks)
      }

      // 1. Daily Discover
      if (!dailyDiscover.isNullOrEmpty()) {
        list.add(HomeSection.DailyDiscover)
      }

      // 2. Forgotten Favorites
      if (!forgottenFavorites.isNullOrEmpty()) {
        list.add(HomeSection.ForgottenFavorites)
      }

      // 3. From The Community
      if (!communityPlaylists.isNullOrEmpty()) {
        list.add(HomeSection.FromTheCommunity)
      }

      // 4. AI Recommendations
      if (aiRecommendedPlaylist != null && aiRecommendedPlaylist!!.second.isNotEmpty()) {
        list.add(HomeSection.AiRecommendations)
      }

      // 5. YouTube Home Feed Shelves in natural fixed order
      //    Filter out "Listen together" shelves (collaborative/social shelves)
      homePage?.sections?.indices?.forEach { i ->
        val section = homePage?.sections?.getOrNull(i)
        val title = section?.title?.lowercase() ?: ""
        if (!title.contains("listen together") && !title.contains("listen_together")) {
          list.add(HomeSection.HomePageSection(i))
        }
      }

      // 6. Similar Recommendations in natural fixed order
      similarRecommendations?.indices?.forEach { i ->
        list.add(HomeSection.SimilarRecommendation(i))
      }

      list
    }

  PullToRefreshBox(
    state = pullRefreshState,
    isRefreshing = isRefreshing,
    onRefresh = viewModel::refresh,
    indicator = {
      PullToRefreshDefaults.LoadingIndicator(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        modifier =
          Modifier.align(Alignment.TopCenter)
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
      )
    }
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
      val screenMaxWidth = maxWidth
      val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
      val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
      val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
      val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF121212) else Color(0xFFFAFAFA)

      val gradientAlpha by animateFloatAsState(
        targetValue = if (isLoading && homePage == null) 0f else 1f,
        animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
        label = "GradientFadeIn"
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
          .graphicsLayer {
            val scrollY = if (lazylistState.firstVisibleItemIndex == 0) lazylistState.firstVisibleItemScrollOffset else 2000
            val scrollAlpha = (1f - (scrollY / 600f)).coerceIn(0f, 1f)
            alpha = scrollAlpha * gradientAlpha
            translationY = -scrollY * 0.1f
          }
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(color1, color2, color3, backgroundColor)
            )
          )
      )

      LazyColumn(
        state = lazylistState,
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
      ) {
        item(key = "home_display_title") {
          Text(
            text = "Listen Now",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = PAGE_GUTTER, end = PAGE_GUTTER, top = 6.dp, bottom = 2.dp)
          )
        }

        item(key = "category_chips") {
          ChipsRow(
            chips =
              homePage
                ?.chips
                ?.filter {
                  !it.title.equals("Podcasts", ignoreCase = true) &&
                    !it.title.equals("Uploaded", ignoreCase = true)
                }
                ?.map { it to it.title } ?: emptyList(),
            currentValue = selectedChip,
            onValueUpdate = { viewModel.toggleChip(it) }
          )
        }

        if (isLoading && homePage?.chips.isNullOrEmpty()) {
          item(key = "chips_shimmer") {
            ShimmerHost {
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .padding(top = 2.dp, bottom = 6.dp)
                    .horizontalScroll(rememberScrollState()),
              ) {
                Spacer(Modifier.width(PAGE_GUTTER))
                val chipWidths = listOf(68.dp, 84.dp, 76.dp, 92.dp, 72.dp)
                chipWidths.forEach { width ->
                  Box(
                    modifier =
                      Modifier.width(width)
                        .height(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                  )
                  Spacer(Modifier.width(8.dp))
                }
              }
            }
          }
        }

        homeSections.forEach { section ->
          when (section) {
            HomeSection.SpeedDial -> {
              speedDialItems
                .takeIf { it.isNotEmpty() }
                ?.let { items ->
                  item(key = "speed_dial_title") {
                    HomeSectionHeader(
                      title = stringResource(R.string.speed_dial),
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "speed_dial_list") {
                    val targetItemSize = 160.dp
                    val availableWidth = maxWidth - (PAGE_GUTTER * 2)
                    val columns = (availableWidth / targetItemSize).toInt().coerceAtLeast(3)
                    val rows = if (columns >= 6) 1 else if (columns >= 4) 2 else 3
                    val itemsPerPage = columns * rows
                    val itemWidth = availableWidth / columns

                    val pagerState =
                      rememberPagerState(
                        pageCount = { (items.size + itemsPerPage - 1) / itemsPerPage }
                      )

                    Column(
                      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).animateItem(),
                    ) {
                      HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth().height(itemWidth * rows),
                      ) { page ->
                        val pageStartIndex = page * itemsPerPage
                        val pageItems = items.drop(pageStartIndex).take(itemsPerPage)

                        Column(modifier = Modifier.fillMaxSize()) {
                          for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                              for (col in 0 until columns) {
                                val itemIndex = row * columns + col

                                val isRandomizeSlot = (page == 0 && itemIndex == itemsPerPage - 1)

                                if (isRandomizeSlot) {
                                  Box(
                                    modifier =
                                      Modifier.width(itemWidth).height(itemWidth).padding(4.dp)
                                  ) {
                                    RandomizeGridItem(
                                      isLoading = isRandomizing,
                                      onClick = {
                                        if (isRandomizing) {
                                          randomizeJob?.cancel()
                                        } else {
                                          randomizeJob =
                                            scope.launch {
                                              val randomItem = viewModel.getRandomItem()
                                              if (randomItem != null) {
                                                when (randomItem) {
                                                  is SongItem ->
                                                    playerConnection.playQueue(
                                                      YouTubeQueue(
                                                        randomItem.endpoint
                                                          ?: WatchEndpoint(videoId = randomItem.id),
                                                        randomItem.toMediaMetadata()
                                                      )
                                                    )
                                                  is AlbumItem ->
                                                    navController.navigate("album/${randomItem.id}")
                                                  is ArtistItem ->
                                                    navController.navigate(
                                                      "artist/${randomItem.id}"
                                                    )
                                                  is PlaylistItem ->
                                                    navController.navigateToPlaylistItem(randomItem)
                                                }
                                              }
                                            }
                                        }
                                      }
                                    )
                                  }
                                } else if (itemIndex < pageItems.size) {
                                  val item = pageItems[itemIndex]
                                  val isPinned by
                                    database.speedDialDao
                                      .isPinned(item.id)
                                      .collectAsState(initial = false)

                                  Box(
                                    modifier =
                                      Modifier.width(itemWidth).height(itemWidth).padding(4.dp)
                                  ) {
                                    SpeedDialGridItem(
                                      item = item,
                                      isPinned = isPinned,
                                      isActive =
                                        item.id in
                                          listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                      isPlaying = isPlaying,
                                      modifier =
                                        Modifier.fillMaxSize()
                                          .combinedClickable(
                                            onClick = {
                                              when (item) {
                                                is SongItem ->
                                                  playerConnection.playQueue(
                                                    YouTubeQueue(
                                                      item.endpoint
                                                        ?: WatchEndpoint(videoId = item.id),
                                                      item.toMediaMetadata()
                                                    )
                                                  )
                                                is AlbumItem ->
                                                  navController.navigate("album/${item.id}")
                                                is ArtistItem ->
                                                  navController.navigate("artist/${item.id}")
                                                is PlaylistItem ->
                                                  navController.navigateToPlaylistItem(item)
                                              }
                                            },
                                            onLongClick = {
                                              haptic.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                              )
                                              menuState.show {
                                                when (item) {
                                                  is SongItem ->
                                                    YouTubeSongMenu(
                                                      song = item,
                                                      navController = navController,
                                                      onDismiss = menuState::dismiss
                                                    )
                                                  is AlbumItem ->
                                                    YouTubeAlbumMenu(
                                                      albumItem = item,
                                                      navController = navController,
                                                      onDismiss = menuState::dismiss
                                                    )
                                                  is ArtistItem ->
                                                    YouTubeArtistMenu(
                                                      artist = item,
                                                      onDismiss = menuState::dismiss
                                                    )
                                                  is PlaylistItem ->
                                                    YouTubePlaylistMenu(
                                                      playlist = item,
                                                      coroutineScope = scope,
                                                      onDismiss = menuState::dismiss
                                                    )
                                                }
                                              }
                                            }
                                          )
                                    )
                                  }
                                } else {
                                  Spacer(modifier = Modifier.width(itemWidth))
                                }
                              }
                            }
                          }
                        }
                      }

                      if (pagerState.pageCount > 1) {
                        Row(
                          modifier = Modifier.height(24.dp).fillMaxWidth(),
                          horizontalArrangement =
                            Arrangement.Center,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          repeat(pagerState.pageCount) { iteration ->
                            val color =
                              if (pagerState.currentPage == iteration)
                                MaterialTheme.colorScheme.onSurface
                              else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            Box(
                              modifier =
                                Modifier.padding(4.dp)
                                  .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                  .background(color)
                                  .size(8.dp)
                            )
                          }
                        }
                      }
                    }
                  }
                }
            }
            HomeSection.QuickPicks -> {
              quickPicks
                ?.takeIf { it.isNotEmpty() }
                ?.let { quickPicksList ->
                  val distinctQuickPicks = quickPicksList.distinctBy { it.id }
                  val previewQuickPicks = distinctQuickPicks.take(TRACKS_PER_COLUMN * 4)

                  item(key = "quick_picks_title") {
                    val title = stringResource(R.string.picked_for_you)
                    HomeSectionHeader(
                      title = title,
                      onPlayAllClick = {
                        playerConnection.playQueue(
                          ListQueue(
                            title = title,
                            items = distinctQuickPicks.map { it.toMediaItem() }
                          )
                        )
                      },
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "quick_picks_list") {
                    BoxWithConstraints(modifier = Modifier.padding(bottom = 16.dp)) {
                      val columnWidth = trackColumnWidth(maxWidth)
                      val columns = previewQuickPicks.chunked(TRACKS_PER_COLUMN)

                      LazyRow(
                        contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.animateItem()
                      ) {
                        items(columns) { column ->
                          Column(
                            modifier = Modifier.width(columnWidth),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                          ) {
                            column.forEach { originalSong ->
                              val observedSong by
                                database.song(originalSong.id)
                                  .collectAsState(initial = originalSong)
                              val song = observedSong ?: originalSong
                              val isActive = song.id == mediaMetadata?.id

                              RecentTrackRow(
                                title = song.title,
                                artist = song.artists.joinToString { it.name },
                                thumbnailUrl = song.thumbnailUrl,
                                isActive = isActive,
                                isPlaying = isPlaying,
                                forceCrop = true,
                                onClick = {
                                  if (isActive) {
                                    playerConnection.togglePlayPause()
                                  } else {
                                    playerConnection.playQueue(
                                      YouTubeQueue.radio(song.toMediaMetadata())
                                    )
                                  }
                                },
                                onLongClick = {
                                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                  menuState.show {
                                    SongMenu(
                                      originalSong = song,
                                      navController = navController,
                                      onDismiss = menuState::dismiss
                                    )
                                  }
                                },
                                trailingContent = {
                                  IconButton(
                                    onClick = {
                                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                      menuState.show {
                                        SongMenu(
                                          originalSong = song,
                                          navController = navController,
                                          onDismiss = menuState::dismiss
                                        )
                                      }
                                    }
                                  ) {
                                    Icon(
                                      painter = painterResource(R.drawable.more_vert),
                                      contentDescription = null,
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                  }
                                }
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }
            }
            HomeSection.NetworkQuickPicks -> {
              networkQuickPicks
                ?.takeIf { it.isNotEmpty() }
                ?.let { picks ->
                  val distinctPicks = picks.distinctBy { it.id }

                  item(key = "network_quick_picks_title") {
                    val title = stringResource(R.string.picked_for_you)
                    HomeSectionHeader(
                      title = title,
                      onPlayAllClick = {
                        playerConnection.playQueue(
                          ListQueue(
                            title = title,
                            items = distinctPicks.map { it.toMediaMetadata().toMediaItem() }
                          )
                        )
                      },
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "network_quick_picks_list") {
                    LazyRow(
                      contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                      horizontalArrangement = Arrangement.spacedBy(14.dp),
                      modifier = Modifier.padding(bottom = 16.dp).animateItem()
                    ) {
                      items(distinctPicks, key = { it.id }) { song ->
                        val isActive = song.id == mediaMetadata?.id
                        HomeShelfCard(
                          title = song.title,
                          subtitle = song.artists.joinToString(", ") { it.name },
                          thumbnailUrl = song.thumbnail,
                          isActive = isActive,
                          isPlaying = isPlaying,
                          isArtist = false,
                          onClick = {
                            if (isActive) {
                              playerConnection.togglePlayPause()
                            } else {
                              playerConnection.playQueue(
                                YouTubeQueue(
                                  song.endpoint ?: WatchEndpoint(videoId = song.id),
                                  song.toMediaMetadata()
                                )
                              )
                            }
                          },
                          onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                              YouTubeSongMenu(
                                song = song,
                                navController = navController,
                                onDismiss = menuState::dismiss
                              )
                            }
                          }
                        )
                      }
                    }
                  }
                }
            }
            HomeSection.KeepListening -> {
              keepListening
                ?.takeIf { it.isNotEmpty() }
                ?.let { keepListeningList ->
                  item(key = "keep_listening_title") {
                    HomeSectionHeader(
                      title = stringResource(R.string.keep_listening),
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "keep_listening_list") {
                    LazyRow(
                      contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                      horizontalArrangement = Arrangement.spacedBy(14.dp),
                      modifier = Modifier.padding(bottom = 16.dp).animateItem()
                    ) {
                      items(keepListeningList.distinctBy { it.id }, key = { it.id }) { item ->
                        localGridItem(item)
                      }
                    }
                  }
                }
            }
            HomeSection.DailyDiscover -> {
              dailyDiscover
                ?.takeIf { it.isNotEmpty() }
                ?.let { discoverList ->
                  item(key = "daily_discover_title") {
                    val title = stringResource(R.string.your_daily_discover)
                    HomeSectionHeader(
                      title = title,
                      onPlayAllClick = {
                        val queueItems =
                          discoverList.mapNotNull {
                            (it.recommendation as? SongItem)?.toMediaMetadata()
                          }

                        if (queueItems.isNotEmpty()) {
                          playerConnection.playQueue(
                            ListQueue(title = title, items = queueItems.map { it.toMediaItem() })
                          )
                        }
                      },
                      modifier = Modifier.animateItem()
                    )
                  }
                  item(key = "daily_discover_content") {
                    Box(
                      modifier = Modifier.fillMaxWidth().height(340.dp).padding(horizontal = PAGE_GUTTER, vertical = 8.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      val carouselState = rememberCarouselState { discoverList.size }
                      HorizontalMultiBrowseCarousel(
                        state = carouselState,
                        preferredItemWidth = 320.dp,
                        itemSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth().height(320.dp)
                      ) { i ->
                        val item = discoverList[i]
                        DailyDiscoverCard(
                          dailyDiscover = item,
                          onClick = {
                            val song = item.recommendation as? SongItem
                            val metadata = song?.toMediaMetadata()
                            if (metadata != null) {
                              playerConnection.playQueue(
                                YouTubeQueue(
                                  song.endpoint ?: WatchEndpoint(videoId = song.id),
                                  metadata
                                )
                              )
                            }
                          },
                          navController = navController,
                          modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                        )
                      }
                    }
                  }
                }
            }
            HomeSection.ForgottenFavorites -> {
              forgottenFavorites
                ?.takeIf { it.isNotEmpty() }
                ?.let { forgottenFavoritesList ->
                  item(key = "forgotten_favorites_title") {
                    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
                    HomeSectionHeader(
                      title = forgottenFavoritesTitle,
                      onPlayAllClick = {
                        playerConnection.playQueue(
                          ListQueue(
                            title = forgottenFavoritesTitle,
                            items = forgottenFavoritesList.distinctBy { it.id }.map { it.toMediaItem() }
                          )
                        )
                      },
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "forgotten_favorites_list") {
                    BoxWithConstraints(modifier = Modifier.padding(bottom = 16.dp)) {
                      val columnWidth = trackColumnWidth(maxWidth)
                      val columns = forgottenFavoritesList.distinctBy { it.id }.chunked(TRACKS_PER_COLUMN)

                      LazyRow(
                        contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.animateItem()
                      ) {
                        items(columns) { column ->
                          Column(
                            modifier = Modifier.width(columnWidth),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                          ) {
                            column.forEach { originalSong ->
                              val song by
                                database.song(originalSong.id).collectAsState(initial = originalSong)
                              val currentSong = song ?: originalSong
                              val isActive = currentSong.id == mediaMetadata?.id

                              RecentTrackRow(
                                title = currentSong.title,
                                artist = currentSong.artists.joinToString { it.name },
                                thumbnailUrl = currentSong.thumbnailUrl,
                                isActive = isActive,
                                isPlaying = isPlaying,
                                forceCrop = true,
                                onClick = {
                                  if (isActive) {
                                    playerConnection.togglePlayPause()
                                  } else {
                                    playerConnection.playQueue(
                                      YouTubeQueue.radio(currentSong.toMediaMetadata())
                                    )
                                  }
                                },
                                onLongClick = {
                                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                  menuState.show {
                                    SongMenu(
                                      originalSong = currentSong,
                                      navController = navController,
                                      onDismiss = menuState::dismiss
                                    )
                                  }
                                },
                                trailingContent = {
                                  IconButton(
                                    onClick = {
                                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                      menuState.show {
                                        SongMenu(
                                          originalSong = currentSong,
                                          navController = navController,
                                          onDismiss = menuState::dismiss
                                        )
                                      }
                                    }
                                  ) {
                                    Icon(
                                      painter = painterResource(R.drawable.more_vert),
                                      contentDescription = null,
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                  }
                                }
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }
            }
            HomeSection.FromTheCommunity -> {
              communityPlaylists
                ?.takeIf { it.isNotEmpty() }
                ?.let { playlists ->
                  item(key = "community_playlists_title") {
                    HomeSectionHeader(
                      title = stringResource(R.string.from_the_community),
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "community_playlists_content") {
                    LazyRow(
                      contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                      horizontalArrangement = Arrangement.spacedBy(16.dp),
                      modifier = Modifier.padding(bottom = 16.dp).animateItem()
                    ) {
                      items(playlists.distinctBy { it.playlist.id }, key = { it.playlist.id }) { item ->
                        CommunityPlaylistCard(
                          item = item,
                          onClick = { navController.navigateToPlaylistItem(item.playlist) },
                          onSongClick = { song ->
                            playerConnection.playQueue(
                              YouTubeQueue(
                                song.endpoint ?: WatchEndpoint(videoId = song.id),
                                song.toMediaMetadata()
                              )
                            )
                          }
                        )
                      }
                    }
                  }
                }
            }
            HomeSection.AccountPlaylists -> {
              accountPlaylists
                ?.takeIf { it.isNotEmpty() }
                ?.let { playlists ->
                  item(key = "account_playlists_title") {
                    HomeSectionHeader(
                      title = accountName ?: stringResource(R.string.your_youtube_playlists),
                      subtitle = if (accountName != null) stringResource(R.string.your_youtube_playlists) else null,
                      thumbnail = {
                        if (url != null) {
                          AsyncImage(
                            model =
                              ImageRequest.Builder(LocalContext.current)
                                .data(url)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .diskCacheKey(url)
                                .crossfade(false)
                                .build(),
                            placeholder = painterResource(id = R.drawable.person),
                            error = painterResource(id = R.drawable.person),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier =
                              Modifier.size(36.dp)
                                .clip(CircleShape)
                          )
                        } else {
                          Icon(
                            painter = painterResource(id = R.drawable.person),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                          )
                        }
                      },
                      onClick = { navController.navigate("account") },
                      modifier = Modifier.animateItem()
                    )
                  }

                  item(key = "account_playlists_list") {
                    LazyRow(
                      contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                      horizontalArrangement = Arrangement.spacedBy(14.dp),
                      modifier = Modifier.padding(bottom = 16.dp).animateItem()
                    ) {
                      items(
                        items = playlists.distinctBy { it.id },
                        key = { it.id },
                      ) { item ->
                        ytGridItem(item)
                      }
                    }
                  }
                }
            }
            HomeSection.AiRecommendations -> {
              aiRecommendedPlaylist?.let { pair ->
                val (playlist, songs) = pair
                item(key = "ai_recommendation_title") {
                  val lastUpdatedStr =
                    playlist.playlist.lastUpdateTime?.let {
                      "Last updated: " +
                        it.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a"))
                    }
                  HomeSectionHeader(
                    title = playlist.title,
                    subtitle = lastUpdatedStr,
                    onClick = { navController.navigate("local_playlist/${playlist.id}") },
                    modifier = Modifier.animateItem()
                  )
                }
                item(key = "ai_recommendation_list") {
                  LazyRow(
                    contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 16.dp).animateItem()
                  ) {
                    items(items = songs.distinctBy { it.id }, key = { it.id }) { songObj ->
                      localGridItem(songObj)
                    }
                  }
                }
              }
            }
            is HomeSection.HomePageSection -> {
              val sectionData = homePage?.sections?.getOrNull(section.index)
              sectionData?.let {
                val sectionSongs = sectionData.items.filterIsInstance<SongItem>()
                val hasPlayableSongs = sectionSongs.isNotEmpty()
                val isSongsOnlySection =
                  sectionData.items.isNotEmpty() && sectionData.items.all { it is SongItem }

                item(key = "home_section_title_${section.index}") {
                  HomeSectionHeader(
                    title = sectionData.title,
                    subtitle = sectionData.label,
                    thumbnail =
                      sectionData.thumbnail?.let { thumbnailUrl ->
                        {
                          AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                          )
                        }
                      },
                    onClick =
                      sectionData.endpoint?.let { endpoint ->
                        {
                          when {
                            endpoint.browseId == "FEmusic_moods_and_genres" ->
                              navController.navigate("mood_and_genres")
                            endpoint.params != null ->
                              navController.navigate(
                                "youtube_browse/${endpoint.browseId}?params=${endpoint.params}"
                              )
                            else -> navController.navigate("browse/${endpoint.browseId}")
                          }
                        }
                      },
                    onPlayAllClick =
                      if (hasPlayableSongs) {
                        {
                          playerConnection.playQueue(
                            ListQueue(
                              title = sectionData.title,
                              items = sectionSongs.map { it.toMediaMetadata().toMediaItem() }
                            )
                          )
                        }
                      } else null,
                    modifier = Modifier.animateItem()
                  )
                }

                if (isSongsOnlySection) {
                  item(key = "home_section_list_${section.index}") {
                    BoxWithConstraints(modifier = Modifier.padding(bottom = 16.dp)) {
                      val columnWidth = trackColumnWidth(maxWidth)
                      val columns = sectionSongs.distinctBy { it.id }.chunked(TRACKS_PER_COLUMN)

                      LazyRow(
                        contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.animateItem()
                      ) {
                        items(columns) { column ->
                          Column(
                            modifier = Modifier.width(columnWidth),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                          ) {
                            column.forEach { song ->
                              val isActive = song.id == mediaMetadata?.id

                              RecentTrackRow(
                                title = song.title,
                                artist = song.artists.joinToString { it.name },
                                thumbnailUrl = song.thumbnail,
                                isActive = isActive,
                                isPlaying = isPlaying,
                                forceCrop = true,
                                onClick = {
                                  if (isActive) {
                                    playerConnection.togglePlayPause()
                                  } else {
                                    playerConnection.playQueue(
                                      YouTubeQueue(
                                        song.endpoint ?: WatchEndpoint(videoId = song.id),
                                        song.toMediaMetadata()
                                      )
                                    )
                                  }
                                },
                                onLongClick = {
                                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                  menuState.show {
                                    YouTubeSongMenu(
                                      song = song,
                                      navController = navController,
                                      onDismiss = menuState::dismiss
                                    )
                                  }
                                },
                                trailingContent = {
                                  IconButton(
                                    onClick = {
                                      menuState.show {
                                        YouTubeSongMenu(
                                          song = song,
                                          navController = navController,
                                          onDismiss = menuState::dismiss
                                        )
                                      }
                                    }
                                  ) {
                                    Icon(
                                      painter = painterResource(R.drawable.more_vert),
                                      contentDescription = null,
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                  }
                                }
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                } else {
                  item(key = "home_section_list_${section.index}") {
                    LazyRow(
                      contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                      horizontalArrangement = Arrangement.spacedBy(14.dp),
                      modifier = Modifier.padding(bottom = 16.dp).animateItem()
                    ) {
                      items(sectionData.items.distinctBy { it.id }, key = { it.id }) { item ->
                        ytGridItem(item)
                      }
                    }
                  }
                }
              }
            }
            is HomeSection.SimilarRecommendation -> {
              val recommendation = similarRecommendations?.getOrNull(section.index)
              recommendation?.let {
                item(key = "similar_to_title_${section.index}") {
                  HomeSectionHeader(
                    title = recommendation.title.title,
                    subtitle = stringResource(R.string.similar_to),
                    thumbnail =
                      recommendation.title.thumbnailUrl?.let { thumbnailUrl ->
                        {
                          AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                          )
                        }
                      },
                    onClick = {
                      val recTitle = recommendation.title
                      when (recTitle) {
                        is Song -> navController.navigate("album/${recTitle.album!!.id}")
                        is Album -> navController.navigate("album/${recTitle.id}")
                        is Artist -> navController.navigate("artist/${recTitle.id}")
                        is Playlist -> {}
                      }
                    },
                    modifier = Modifier.animateItem()
                  )
                }

                item(key = "similar_to_list_${section.index}") {
                  LazyRow(
                    contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 16.dp).animateItem()
                  ) {
                    items(recommendation.items.distinctBy { it.id }, key = { it.id }) { item ->
                      ytGridItem(item)
                    }
                  }
                }
              }
            }
            HomeSection.MoodAndGenres -> {
              explorePage?.moodAndGenres?.let { moodAndGenres ->
                item(key = "mood_and_genres_title") {
                  HomeSectionHeader(
                    title = stringResource(R.string.mood_and_genres),
                    onClick = { navController.navigate("mood_and_genres") },
                    modifier = Modifier.animateItem()
                  )
                }
                item(key = "mood_and_genres_list") {
                  LazyHorizontalGrid(
                    rows = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = PAGE_GUTTER, vertical = 6.dp),
                    modifier =
                      Modifier.height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp).padding(bottom = 16.dp).animateItem()
                  ) {
                    items(moodAndGenres.distinctBy { it.title }, key = { it.title }) { item ->
                      MoodAndGenreCard(
                        item = item,
                        onClick = {
                          navController.navigate(
                            "youtube_browse/${item.endpoint.browseId}?params=${item.endpoint.params}"
                          )
                        },
                        modifier = Modifier.padding(6.dp).width(180.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        if (
          isLoading || homePage?.continuation != null && homePage?.sections?.isNotEmpty() == true
        ) {
          item(key = "loading_shimmer") {
            ShimmerHost(modifier = Modifier.animateItem()) {
              val isInitialLoad =
                homePage == null || (homePage?.sections.isNullOrEmpty() && isLoading)
              if (isInitialLoad) {
                // 1. Quick Picks / Picked for you skeleton (multi-column track rows matching RecentTrackRow layout)
                HomeQuickPicksSectionPlaceholder(maxWidth = screenMaxWidth)

                // 2. Shelf section skeleton (e.g. Daily Discover / Forgotten Favorites)
                HomeShelfSectionPlaceholder(titleWidth = 190.dp, subtitleWidth = 110.dp)

                // 3. Another shelf section skeleton (e.g. Community Playlists / Albums)
                HomeShelfSectionPlaceholder(titleWidth = 150.dp, subtitleWidth = null)
              } else {
                // Continuation / pagination loading indicator at bottom
                HomeShelfSectionPlaceholder(titleWidth = 170.dp, subtitleWidth = 100.dp)
              }
            }
          }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(30.dp)) }
      }
    }
  }
}
