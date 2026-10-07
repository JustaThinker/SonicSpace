package com.music.sonic.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.menu.AlbumMenu
import com.music.sonic.ui.menu.ArtistMenu
import com.music.sonic.ui.menu.PlaylistMenu
import com.music.sonic.viewmodels.LibraryMixViewModel

private const val LIBRARY_ROW_MAX_ITEMS = 5
private val SHELF_CARD_WIDTH = 148.dp

@Composable
fun ReplayBanner(onClick: () -> Unit) {
  Box(
    Modifier
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
  ) {
    Box(
      Modifier
        .matchParentSize()
        .background(
          Brush.horizontalGradient(
            listOf(
              Color(0xFF1E3C72),
              Color(0xFF2A5298)
            )
          )
        )
    ) {
      Box(
        Modifier
          .matchParentSize()
          .background(
            Brush.horizontalGradient(
              listOf(
                Color.Black.copy(alpha = 0.34f),
                Color.Black.copy(alpha = 0.12f),
                Color.Transparent
              )
            )
          )
      )
    }
    Row(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = "Your Replay",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Discover your listening stats and top tracks",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.82f),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      Spacer(Modifier.width(12.dp))
      Icon(
        painter = painterResource(R.drawable.navigate_next),
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.85f),
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

@Composable
fun NewShelfCard(
  label: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier.width(SHELF_CARD_WIDTH)
) {
  Column(
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(R.drawable.add),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(34.dp)
      )
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryShelfCard(
  title: String,
  subtitle: String,
  thumbnailUrl: String?,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  isCircle: Boolean = false,
  isPinned: Boolean = false,
  modifier: Modifier = Modifier.width(SHELF_CARD_WIDTH)
) {
  val shape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)
  Column(
    modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
      contentAlignment = Alignment.Center
    ) {
      if (thumbnailUrl != null) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(thumbnailUrl)
            .crossfade(true)
            .build(),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          painter = painterResource(R.drawable.library_music),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(40.dp)
        )
      }
    }
    Spacer(Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isPinned) {
        Icon(
          painter = painterResource(R.drawable.ic_push_pin),
          contentDescription = "Pinned",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false)
      )
    }
    if (subtitle.isNotBlank()) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun OnDeviceCard(
  title: String,
  subtitle: String,
  colors: List<Color>,
  iconRes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier.width(SHELF_CARD_WIDTH)
) {
  Column(
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.horizontalGradient(colors)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(40.dp)
      )
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun LibrarySectionHeader(
  title: String,
  subtitle: String = "",
  onShowAll: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (subtitle.isNotBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
    if (onShowAll != null) {
      Text(
        text = "Show all",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .clickable(onClick = onShowAll)
          .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMixScreen(
  navController: NavController,
  filterContent: @Composable () -> Unit,
  onNewPlaylist: () -> Unit,
  onShowAllPlaylists: () -> Unit,
  onShowAllAlbums: () -> Unit,
  onShowAllArtists: () -> Unit,
  viewModel: LibraryMixViewModel = hiltViewModel()
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val pullRefreshState = rememberPullToRefreshState()

  val playlists by viewModel.playlists.collectAsState()
  val albums by viewModel.albums.collectAsState()
  val artists by viewModel.artists.collectAsState()
  val likedSongCount by viewModel.likedSongCount.collectAsState()

  val lazyListState = rememberLazyListState()

  PullToRefreshBox(
    state = pullRefreshState,
    isRefreshing = isRefreshing,
    onRefresh = viewModel::refresh,
    indicator = {
      PullToRefreshDefaults.LoadingIndicator(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        modifier = Modifier.align(Alignment.TopCenter)
          .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
      )
    }
  ) {
    LazyColumn(
      state = lazyListState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
      item(key = "chips_row") {
        filterContent()
      }

      item(key = "replay_banner") {
        ReplayBanner(onClick = { navController.navigate("stats") })
      }

      item(key = "shelf_on_device") {
        Column(Modifier.padding(bottom = 20.dp)) {
          LibrarySectionHeader(title = "On device")
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item(key = "liked_songs_device") {
              OnDeviceCard(
                title = stringResource(R.string.liked),
                subtitle = "$likedSongCount ${stringResource(R.string.songs)}",
                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
                iconRes = R.drawable.favorite,
                onClick = { navController.navigate("auto_playlist/liked") }
              )
            }
            item(key = "downloads") {
              OnDeviceCard(
                title = stringResource(R.string.downloaded_songs),
                subtitle = "Downloaded songs",
                colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298)),
                iconRes = R.drawable.download,
                onClick = { navController.navigate("auto_playlist/downloaded") }
              )
            }
            item(key = "local_music") {
              OnDeviceCard(
                title = stringResource(R.string.local_music),
                subtitle = stringResource(R.string.audio_files_on_device),
                colors = listOf(Color(0xFF134E5E), Color(0xFF71B280)),
                iconRes = R.drawable.library_music,
                onClick = { navController.navigate("auto_playlist/local") }
              )
            }
          }
        }
      }

      item(key = "shelf_playlists") {
        val visiblePlaylists = playlists.take(LIBRARY_ROW_MAX_ITEMS - 2)
        Column(Modifier.padding(bottom = 20.dp)) {
          LibrarySectionHeader(
            title = stringResource(R.string.filter_playlists),
            onShowAll = if (playlists.size + 2 > LIBRARY_ROW_MAX_ITEMS) onShowAllPlaylists else null
          )
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item(key = "new_playlist_tile") {
              NewShelfCard(
                label = stringResource(R.string.create_playlist),
                subtitle = "Saved to your library",
                onClick = onNewPlaylist
              )
            }
            item(key = "liked_songs_playlist") {
              OnDeviceCard(
                title = stringResource(R.string.liked),
                subtitle = "$likedSongCount ${stringResource(R.string.songs)}",
                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
                iconRes = R.drawable.favorite,
                onClick = { navController.navigate("auto_playlist/liked") }
              )
            }
            items(visiblePlaylists, key = { it.id }) { playlist ->
              LibraryShelfCard(
                title = playlist.playlist.name,
                subtitle = "${playlist.songCount} ${stringResource(R.string.songs)}",
                thumbnailUrl = playlist.thumbnails.firstOrNull(),
                isPinned = playlist.playlist.isPinned,
                onClick = {
                  val playlistId = playlist.id.removePrefix("VL")
                  when (playlistId) {
                    "LM" -> navController.navigate("auto_playlist/liked")
                    "SE" -> navController.navigate("auto_playlist/downloaded")
                    else -> navController.navigate("local_playlist/${playlist.id}")
                  }
                },
                onLongClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  menuState.show {
                    PlaylistMenu(
                      playlist = playlist,
                      coroutineScope = scope,
                      onDismiss = menuState::dismiss
                    )
                  }
                }
              )
            }
          }
        }
      }

      item(key = "shelf_albums") {
        val visibleAlbums = albums.take(LIBRARY_ROW_MAX_ITEMS)
        if (visibleAlbums.isNotEmpty()) {
          Column(Modifier.padding(bottom = 20.dp)) {
            LibrarySectionHeader(
              title = stringResource(R.string.filter_albums),
              onShowAll = if (albums.size > LIBRARY_ROW_MAX_ITEMS) onShowAllAlbums else null
            )
            LazyRow(
              contentPadding = PaddingValues(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              items(visibleAlbums, key = { it.id }) { album ->
                LibraryShelfCard(
                  title = album.album.title,
                  subtitle = album.artists.joinToString { it.name },
                  thumbnailUrl = album.album.thumbnailUrl,
                  onClick = { navController.navigate("album/${album.id}") },
                  onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                      AlbumMenu(
                        originalAlbum = album,
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

      item(key = "shelf_artists") {
        val visibleArtists = artists.take(LIBRARY_ROW_MAX_ITEMS)
        if (visibleArtists.isNotEmpty()) {
          Column(Modifier.padding(bottom = 20.dp)) {
            LibrarySectionHeader(
              title = stringResource(R.string.filter_artists),
              onShowAll = if (artists.size > LIBRARY_ROW_MAX_ITEMS) onShowAllArtists else null
            )
            LazyRow(
              contentPadding = PaddingValues(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              items(visibleArtists, key = { it.id }) { artist ->
                LibraryShelfCard(
                  title = artist.artist.name,
                  subtitle = "${artist.songCount} ${stringResource(R.string.songs)}",
                  thumbnailUrl = artist.artist.thumbnailUrl,
                  isCircle = true,
                  onClick = { navController.navigate("artist/${artist.id}") },
                  onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                      ArtistMenu(
                        originalArtist = artist,
                        coroutineScope = scope,
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
    }
  }
}
