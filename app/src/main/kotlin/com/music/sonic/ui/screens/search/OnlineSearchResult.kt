package com.music.sonic.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.innertube.YouTube
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.sonic.LocalDatabase
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.constants.MiniPlayerBottomSpacing
import com.music.sonic.constants.MiniPlayerHeight
import com.music.sonic.constants.NavigationBarHeight
import com.music.sonic.constants.PauseSearchHistoryKey
import com.music.sonic.db.entities.SearchHistory
import com.music.sonic.models.toMediaMetadata
import com.music.sonic.playback.queues.YouTubeQueue
import com.music.sonic.ui.component.EmptyPlaceholder
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.PlayingIndicator
import com.music.sonic.ui.component.shimmer.ListItemPlaceHolder
import com.music.sonic.ui.component.shimmer.ShimmerHost
import com.music.sonic.ui.menu.YouTubeAlbumMenu
import com.music.sonic.ui.menu.YouTubeArtistMenu
import com.music.sonic.ui.menu.YouTubePlaylistMenu
import com.music.sonic.ui.menu.YouTubeSongMenu
import com.music.sonic.ui.utils.resize
import com.music.sonic.utils.joinByBullet
import com.music.sonic.utils.makeTimeString
import com.music.sonic.utils.rememberPreference
import com.music.sonic.viewmodels.OnlineSearchViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchResult(
  navController: NavController,
  viewModel: OnlineSearchViewModel = hiltViewModel(),
  pureBlack: Boolean = false,
) {
  val database = LocalDatabase.current
  val menuState = LocalMenuState.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val coroutineScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }

  var isSearchFocused by remember { mutableStateOf(false) }

  val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)

  BackHandler(enabled = isSearchFocused) {
    isSearchFocused = false
    focusManager.clearFocus()
  }

  val encodedQuery = navController.currentBackStackEntry?.arguments?.getString("query") ?: ""
  val decodedQuery =
    remember(encodedQuery) {
      try {
        URLDecoder.decode(encodedQuery, "UTF-8")
      } catch (e: Exception) {
        encodedQuery
      }
    }

  var query by
    rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(TextFieldValue(decodedQuery, TextRange(decodedQuery.length)))
    }

  val onSearch: (String) -> Unit = remember {
    { searchQuery ->
      if (searchQuery.isNotEmpty()) {
        isSearchFocused = false
        focusManager.clearFocus()

        navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}") {
          popUpTo("search/${URLEncoder.encode(decodedQuery, "UTF-8")}") { inclusive = true }

          if (!pauseSearchHistory) {
            coroutineScope.launch(Dispatchers.IO) {
              database.query { insert(SearchHistory(query = searchQuery)) }
            }
          }
        }
      }
    }
  }

  LaunchedEffect(decodedQuery) {
    query = TextFieldValue(decodedQuery, TextRange(decodedQuery.length))
  }

  val searchFilter by viewModel.filter.collectAsState()
  val searchSummary = viewModel.summaryPage
  val itemsPage by
    remember(searchFilter) {
      derivedStateOf { searchFilter?.value?.let { viewModel.viewStateMap[it] } }
    }

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" } }
      .collect { shouldLoadMore ->
        if (!shouldLoadMore) return@collect
        viewModel.loadMore()
      }
  }

  val onItemClick: (YTItem) -> Unit = { item ->
    when (item) {
      is SongItem -> {
        if (item.id == mediaMetadata?.id) {
          playerConnection.togglePlayPause()
        } else {
          playerConnection.playQueue(
            YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata())
          )
        }
      }
      is AlbumItem -> navController.navigate("album/${item.id}")
      is ArtistItem -> navController.navigate("artist/${item.id}")
      is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
    }
  }

  val onItemMoreClick: (YTItem) -> Unit = { item ->
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    menuState.show {
      when (item) {
        is SongItem ->
          YouTubeSongMenu(
            song = item,
            navController = navController,
            onDismiss = menuState::dismiss,
          )
        is AlbumItem ->
          YouTubeAlbumMenu(
            albumItem = item,
            navController = navController,
            onDismiss = menuState::dismiss,
          )
        is ArtistItem ->
          YouTubeArtistMenu(
            artist = item,
            onDismiss = menuState::dismiss,
          )
        is PlaylistItem ->
          YouTubePlaylistMenu(
            playlist = item,
            coroutineScope = coroutineScope,
            onDismiss = menuState::dismiss,
          )
      }
    }
  }

  val onItemPlayClick: (YTItem) -> Unit = { item ->
    when (item) {
      is SongItem -> {
        if (item.id == mediaMetadata?.id) {
          playerConnection.togglePlayPause()
        } else {
          playerConnection.playQueue(
            YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata())
          )
        }
      }
      is AlbumItem -> {
        navController.navigate("album/${item.id}")
      }
      is ArtistItem -> {
        if (item.radioEndpoint != null) {
          playerConnection.playQueue(YouTubeQueue(item.radioEndpoint!!))
        } else {
          navController.navigate("artist/${item.id}")
        }
      }
      is PlaylistItem -> {
        navController.navigate("online_playlist/${item.id}")
      }
    }
  }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
  ) {
    // Top Search Bar
    OutlinedTextField(
      value = query,
      onValueChange = { newQuery -> query = newQuery },
      placeholder = {
        Text(
          text = stringResource(R.string.search_yt_music),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      leadingIcon = {
        IconButton(onClick = { navController.navigateUp() }) {
          Icon(
            painter = painterResource(R.drawable.arrow_back),
            contentDescription = stringResource(R.string.dismiss),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      trailingIcon = {
        if (query.text.isNotEmpty()) {
          IconButton(onClick = { query = TextFieldValue("") }) {
            Icon(
              painter = painterResource(R.drawable.close),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = { onSearch(query.text) }),
      singleLine = true,
      shape = RoundedCornerShape(28.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor =
            if (pureBlack) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerHigh,
          unfocusedContainerColor =
            if (pureBlack) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerHigh,
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent
        ),
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .focusRequester(focusRequester)
          .onFocusChanged { focusState ->
            if (focusState.isFocused) {
              isSearchFocused = true
            }
          }
    )

    Box(modifier = Modifier.weight(1f)) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Filter Chips Row
        SearchFilterChipsBar(
          currentFilter = searchFilter,
          onFilterSelected = { selectedFilter ->
            if (viewModel.filter.value != selectedFilter) {
              viewModel.filter.value = selectedFilter
            }
            coroutineScope.launch { lazyListState.animateScrollToItem(0) }
          },
          pureBlack = pureBlack
        )

        LazyColumn(
          state = lazyListState,
          contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
          modifier = Modifier.fillMaxWidth()
        ) {
          if (searchFilter == null) {
            // "All" Tab
            val summaries = searchSummary?.summaries.orEmpty()
            val topResultSummary =
              summaries.firstOrNull { it.title.equals("Top result", ignoreCase = true) }
            val topResultItem = topResultSummary?.items?.firstOrNull()

            // Featured Top Result Hero Card
            if (topResultItem != null) {
              item(key = "hero_top_result") {
                TopResultCard(
                  item = topResultItem,
                  isActive =
                    when (topResultItem) {
                      is SongItem -> mediaMetadata?.id == topResultItem.id
                      is AlbumItem -> mediaMetadata?.album?.id == topResultItem.id
                      else -> false
                    },
                  isPlaying = isPlaying,
                  onClick = { onItemClick(topResultItem) },
                  onPlayClick = { onItemPlayClick(topResultItem) },
                  pureBlack = pureBlack,
                  modifier = Modifier.animateItem()
                )
              }
            }

            // Remaining Section Summaries
            summaries.forEach { summary ->
              val itemsToDisplay =
                if (summary == topResultSummary && summary.items.size > 1) {
                  summary.items.drop(1)
                } else if (summary == topResultSummary) {
                  emptyList()
                } else {
                  summary.items
                }

              if (itemsToDisplay.isNotEmpty()) {
                item(key = "header_${summary.title}") {
                  Text(
                    text = summary.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                      Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
                        .animateItem()
                  )
                }

                itemsIndexed(
                  items = itemsToDisplay,
                  key = { index, item -> "${summary.title}/${item.id}/$index" },
                ) { _, item ->
                  SearchResultRow(
                    item = item,
                    isActive =
                      when (item) {
                        is SongItem -> mediaMetadata?.id == item.id
                        is AlbumItem -> mediaMetadata?.album?.id == item.id
                        else -> false
                      },
                    isPlaying = isPlaying,
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemMoreClick(item) },
                    onMoreClick = { onItemMoreClick(item) },
                    modifier = Modifier.animateItem()
                  )
                }
              }
            }

            if (searchSummary != null && summaries.isEmpty()) {
              item {
                EmptyPlaceholder(
                  icon = R.drawable.search,
                  text = stringResource(R.string.no_results_found),
                )
              }
            }
          } else {
            // Filtered Tab
            val filteredItems = itemsPage?.items.orEmpty().distinctBy { it.id }

            itemsIndexed(
              items = filteredItems,
              key = { _, it -> "filtered_${it.id}" },
            ) { _, item ->
              SearchResultRow(
                item = item,
                isActive =
                  when (item) {
                    is SongItem -> mediaMetadata?.id == item.id
                    is AlbumItem -> mediaMetadata?.album?.id == item.id
                    else -> false
                  },
                isPlaying = isPlaying,
                onClick = { onItemClick(item) },
                onLongClick = { onItemMoreClick(item) },
                onMoreClick = { onItemMoreClick(item) },
                modifier = Modifier.animateItem()
              )
            }

            if (itemsPage?.continuation != null) {
              item(key = "loading") { ShimmerHost { repeat(3) { ListItemPlaceHolder() } } }
            }

            if (itemsPage != null && filteredItems.isEmpty()) {
              item {
                EmptyPlaceholder(
                  icon = R.drawable.search,
                  text = stringResource(R.string.no_results_found),
                )
              }
            }
          }

          if (
            (searchFilter == null && searchSummary == null) ||
              (searchFilter != null && itemsPage == null)
          ) {
            item {
              ShimmerHost {
                TopResultCardPlaceholder()
                repeat(6) { ListItemPlaceHolder() }
              }
            }
          }

          item(key = "bottom_spacer") {
            Spacer(
              modifier =
                Modifier.height(MiniPlayerHeight + MiniPlayerBottomSpacing + NavigationBarHeight)
            )
          }
        }
      }

      // Suggestions overlay when search bar is focused
      if (isSearchFocused) {
        OnlineSearchScreen(
          query = query.text,
          onQueryChange = { query = it },
          navController = navController,
          onSearch = onSearch,
          onDismiss = {
            isSearchFocused = false
            focusManager.clearFocus()
          },
          pureBlack = pureBlack
        )
      }
    }
  }
}

@Composable
fun TopResultCard(
  item: YTItem,
  isActive: Boolean,
  isPlaying: Boolean,
  onClick: () -> Unit,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
  pureBlack: Boolean = false,
) {
  val isArtist = item is ArtistItem
  val artworkShape = if (isArtist) CircleShape else RoundedCornerShape(12.dp)

  val typeBadge =
    when (item) {
      is SongItem -> stringResource(R.string.filter_songs).uppercase()
      is ArtistItem -> stringResource(R.string.filter_artists).uppercase()
      is AlbumItem -> stringResource(R.string.filter_albums).uppercase()
      is PlaylistItem -> stringResource(R.string.filter_playlists).uppercase()
    }

  val subtitle =
    when (item) {
      is SongItem ->
        joinByBullet(
          item.artists.joinToString { it.name },
          makeTimeString(item.duration?.times(1000L))
        )
      is AlbumItem ->
        joinByBullet(
          stringResource(R.string.filter_albums),
          item.artists?.joinToString { it.name },
          item.year?.toString()
        )
      is ArtistItem -> stringResource(R.string.filter_artists)
      is PlaylistItem ->
        joinByBullet(
          stringResource(R.string.filter_playlists),
          item.author?.name,
          item.songCountText
        )
    }

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    color =
      if (pureBlack) Color(0xFF141414)
      else MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 2.dp,
    border =
      BorderStroke(
        1.dp,
        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
      )
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier.size(56.dp).clip(artworkShape),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model =
            ImageRequest.Builder(LocalContext.current)
              .data(item.thumbnail?.resize(544, 544))
              .crossfade(true)
              .build(),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        if (isActive && isPlaying) {
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
          ) {
            PlayingIndicator(
              color = Color.White,
              modifier = Modifier.height(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Surface(
          shape = RoundedCornerShape(50),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(bottom = 3.dp)
        ) {
          Text(
            text = "TOP $typeBadge",
            style =
              MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
          )
        }

        Text(
          text = item.title,
          style =
            MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            ),
          color =
            if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (!subtitle.isNullOrEmpty()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      FilledIconButton(
        onClick = onPlayClick,
        shape = CircleShape,
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          painter =
            painterResource(
              if (isActive && isPlaying) R.drawable.pause else R.drawable.play
            ),
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultRow(
  item: YTItem,
  isActive: Boolean,
  isPlaying: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isArtist = item is ArtistItem
  val artworkShape = if (isArtist) CircleShape else RoundedCornerShape(10.dp)

  val subtitle =
    when (item) {
      is SongItem ->
        joinByBullet(
          item.artists.joinToString { it.name },
          makeTimeString(item.duration?.times(1000L))
        )
      is AlbumItem ->
        joinByBullet(
          stringResource(R.string.filter_albums),
          item.artists?.joinToString { it.name },
          item.year?.toString()
        )
      is ArtistItem -> stringResource(R.string.filter_artists)
      is PlaylistItem ->
        joinByBullet(
          stringResource(R.string.filter_playlists),
          item.author?.name,
          item.songCountText
        )
    }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 2.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(
          if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
          else Color.Transparent
        )
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick
        )
        .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Box(
      modifier = Modifier.size(50.dp).clip(artworkShape),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model =
          ImageRequest.Builder(LocalContext.current)
            .data(item.thumbnail?.resize(256, 256))
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      if (isActive) {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.5f)),
          contentAlignment = Alignment.Center
        ) {
          if (isPlaying) {
            PlayingIndicator(
              color = Color.White,
              modifier = Modifier.height(20.dp)
            )
          } else {
            Icon(
              painter = painterResource(R.drawable.play),
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.title,
        style =
          MaterialTheme.typography.bodyLarge.copy(
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
          ),
        color =
          if (isActive) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      if (!subtitle.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    IconButton(
      onClick = onMoreClick,
      modifier = Modifier.size(36.dp)
    ) {
      Icon(
        painter = painterResource(R.drawable.more_vert),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
fun SearchFilterChipsBar(
  currentFilter: YouTube.SearchFilter?,
  onFilterSelected: (YouTube.SearchFilter?) -> Unit,
  pureBlack: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val filters =
    listOf(
      null to stringResource(R.string.filter_all),
      FILTER_SONG to stringResource(R.string.filter_songs),
      FILTER_VIDEO to stringResource(R.string.filter_videos),
      FILTER_ALBUM to stringResource(R.string.filter_albums),
      FILTER_ARTIST to stringResource(R.string.filter_artists),
      FILTER_COMMUNITY_PLAYLIST to stringResource(R.string.filter_community_playlists),
      FILTER_FEATURED_PLAYLIST to stringResource(R.string.filter_featured_playlists),
    )

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .horizontalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Spacer(Modifier.width(8.dp))

    filters.forEach { (filter, label) ->
      val isSelected = currentFilter == filter

      FilterChip(
        selected = isSelected,
        onClick = { onFilterSelected(filter) },
        label = {
          Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          )
        },
        shape = RoundedCornerShape(50),
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor =
              if (pureBlack) Color(0xFF141414)
              else MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
          ),
        border =
          BorderStroke(
            width = 1.dp,
            color =
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
          )
      )
    }

    Spacer(Modifier.width(8.dp))
  }
}

@Composable
fun TopResultCardPlaceholder(modifier: Modifier = Modifier) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .height(130.dp),
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
  ) {}
}
