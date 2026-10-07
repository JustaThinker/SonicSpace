package com.music.sonic.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.constants.CONTENT_TYPE_HEADER
import com.music.sonic.constants.CONTENT_TYPE_SONG
import com.music.sonic.constants.GridItemSize
import com.music.sonic.constants.GridItemsSizeKey
import com.music.sonic.constants.GridThumbnailHeight
import com.music.sonic.constants.HideExplicitKey
import com.music.sonic.constants.LibraryViewType
import com.music.sonic.constants.SongFilter
import com.music.sonic.constants.SongFilterKey
import com.music.sonic.constants.SongSortDescendingKey
import com.music.sonic.constants.SongSortType
import com.music.sonic.constants.SongSortTypeKey
import com.music.sonic.constants.SongViewTypeKey
import com.music.sonic.constants.YtmSyncKey
import com.music.sonic.extensions.toMediaItem
import com.music.sonic.playback.queues.ListQueue
import com.music.sonic.ui.component.ChipsRow
import com.music.sonic.ui.component.HideOnScrollFAB
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.SongGridItem
import com.music.sonic.ui.component.SongListItem
import com.music.sonic.ui.component.SortHeader
import com.music.sonic.ui.menu.SelectionSongMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.music.sonic.ui.menu.SongMenu
import com.music.sonic.utils.listItemShape
import com.music.sonic.utils.rememberEnumPreference
import com.music.sonic.utils.rememberPreference
import com.music.sonic.viewmodels.LibrarySongsViewModel

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun LibrarySongsScreen(
  navController: NavController,
  onDeselect: () -> Unit,
  viewModel: LibrarySongsViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val menuState = LocalMenuState.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val (sortType, onSortTypeChange) =
    rememberEnumPreference(SongSortTypeKey, SongSortType.CREATE_DATE)
  val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)

  val (ytmSync) = rememberPreference(YtmSyncKey, true)
  val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

  val songs by viewModel.allSongs.collectAsState()

  var filter by rememberEnumPreference(SongFilterKey, SongFilter.LIKED)
  var viewType by rememberEnumPreference(SongViewTypeKey, LibraryViewType.LIST)
  val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

  var inSelectMode by rememberSaveable { mutableStateOf(false) }
  val selection = remember { mutableStateListOf<String>() }
  val onExitSelectionMode = {
    inSelectMode = false
    selection.clear()
  }

  LaunchedEffect(Unit) {
    if (ytmSync) {
      when (filter) {
        SongFilter.LIKED -> viewModel.syncLikedSongs()
        SongFilter.LIBRARY -> viewModel.syncLibrarySongs()
        SongFilter.UPLOADED -> viewModel.syncUploadedSongs()
        else -> return@LaunchedEffect
      }
    }
  }

  val lazyListState = rememberLazyListState()
  val lazyGridState = rememberLazyGridState()

  val backStackEntry by navController.currentBackStackEntryAsState()
  val scrollToTop =
    backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

  LaunchedEffect(scrollToTop?.value) {
    if (scrollToTop?.value == true) {
      when (viewType) {
        LibraryViewType.LIST -> lazyListState.animateScrollToItem(0)
        LibraryViewType.GRID -> lazyGridState.animateScrollToItem(0)
      }
      backStackEntry?.savedStateHandle?.set("scrollToTop", false)
    }
  }

  val filteredSongs =
    if (hideExplicit) {
      songs.filter { !it.song.explicit }
    } else {
      songs
    }

  val filterContent = @Composable {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Spacer(Modifier.width(12.dp))
      FilterChip(
        label = { Text(stringResource(R.string.songs)) },
        selected = true,
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
        onClick = onDeselect,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
          Icon(painter = painterResource(R.drawable.close), contentDescription = "")
        },
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
      )
      ChipsRow(
        chips =
          listOf(
            SongFilter.LIKED to stringResource(R.string.filter_liked),
            SongFilter.LIBRARY to stringResource(R.string.filter_library),
            SongFilter.UPLOADED to stringResource(R.string.filter_uploaded),
            SongFilter.DOWNLOADED to stringResource(R.string.filter_downloaded),
            SongFilter.EXPORTED to stringResource(R.string.action_exported),
          ),
        currentValue = filter,
        onValueUpdate = { filter = it },
        modifier = Modifier.weight(1f),
      )
    }
  }

  val headerContent = @Composable {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      SortHeader(
        sortType = sortType,
        sortDescending = sortDescending,
        onSortTypeChange = onSortTypeChange,
        onSortDescendingChange = onSortDescendingChange,
        sortTypeText = { sortType ->
          when (sortType) {
            SongSortType.CREATE_DATE -> R.string.sort_by_create_date
            SongSortType.NAME -> R.string.sort_by_name
            SongSortType.ARTIST -> R.string.sort_by_artist
            SongSortType.PLAY_TIME -> R.string.sort_by_play_time
          }
        },
      )

      Spacer(Modifier.weight(1f))

      Text(
        text = pluralStringResource(R.plurals.n_song, filteredSongs.size, filteredSongs.size),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.secondary,
      )

      Spacer(Modifier.width(8.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        LibraryViewType.entries.forEachIndexed { index, type ->
          ToggleButton(
            checked = viewType == type,
            onCheckedChange = { viewType = type },
            shapes =
              when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                LibraryViewType.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
              },
            modifier = Modifier.semantics { role = Role.RadioButton },
          ) {
            Icon(
              painter =
                painterResource(
                  when (type) {
                    LibraryViewType.LIST -> R.drawable.list
                    LibraryViewType.GRID -> R.drawable.grid_view
                  }
                ),
              contentDescription = null,
            )
          }
        }
      }
    }
  }

  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
    ) {
      when (viewType) {
        LibraryViewType.LIST ->
          LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
          ) {
            item(
              key = "filter",
              contentType = CONTENT_TYPE_HEADER,
            ) {
              filterContent()
            }

            item(
              key = "header",
              contentType = CONTENT_TYPE_HEADER,
            ) {
              headerContent()
            }

            itemsIndexed(
              items = filteredSongs,
              key = { _, item -> item.song.id },
              contentType = { _, _ -> CONTENT_TYPE_SONG },
            ) { index, song ->
              val onCheckedChange: (Boolean) -> Unit = {
                if (it) {
                  selection.add(song.id)
                } else {
                  selection.remove(song.id)
                }
              }
              SongListItem(
                song = song,
                showInLibraryIcon = true,
                isActive = song.id == mediaMetadata?.id,
                isPlaying = isPlaying,
                showLikedIcon = true,
                showDownloadIcon = filter != SongFilter.DOWNLOADED,
                showSize = filter == SongFilter.DOWNLOADED,
                shape = listItemShape(index, filteredSongs.size),
                trailingContent = {
                  if (inSelectMode) {
                    Checkbox(checked = selection.contains(song.id), onCheckedChange = onCheckedChange)
                  } else {
                    IconButton(
                      onClick = {
                        menuState.show {
                          SongMenu(
                            originalSong = song,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                          )
                        }
                      },
                    ) {
                      Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = null,
                      )
                    }
                  }
                },
                modifier =
                  Modifier.fillMaxWidth()
                    .combinedClickable(
                      onClick = {
                        if (inSelectMode) {
                          onCheckedChange(!selection.contains(song.id))
                        } else if (song.id == mediaMetadata?.id) {
                          playerConnection.togglePlayPause()
                        } else {
                          playerConnection.playQueue(
                            ListQueue(
                              title = context.getString(R.string.queue_all_songs),
                              items = filteredSongs.map { it.toMediaItem() },
                              startIndex = index,
                            ),
                          )
                        }
                      },
                      onLongClick = {
                        if (!inSelectMode) {
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          inSelectMode = true
                          onCheckedChange(true)
                        } else {
                          menuState.show {
                            SongMenu(
                              originalSong = song,
                              navController = navController,
                              onDismiss = menuState::dismiss,
                            )
                          }
                        }
                      }
                    )
                    .animateItem(),
              )
            }
          }

        LibraryViewType.GRID ->
          LazyVerticalGrid(
            state = lazyGridState,
            columns =
              GridCells.Adaptive(
                minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp
              ),
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
          ) {
            item(
              key = "filter",
              span = { GridItemSpan(maxLineSpan) },
              contentType = CONTENT_TYPE_HEADER,
            ) {
              filterContent()
            }

            item(
              key = "header",
              span = { GridItemSpan(maxLineSpan) },
              contentType = CONTENT_TYPE_HEADER,
            ) {
              headerContent()
            }

            itemsIndexed(
              items = filteredSongs,
              key = { _, item -> item.song.id },
              contentType = { _, _ -> CONTENT_TYPE_SONG },
            ) { index, song ->
              val onCheckedChange: (Boolean) -> Unit = {
                if (it) {
                  selection.add(song.id)
                } else {
                  selection.remove(song.id)
                }
              }
              SongGridItem(
                song = song,
                showInLibraryIcon = true,
                isActive = song.id == mediaMetadata?.id,
                isPlaying = isPlaying,
                showLikedIcon = true,
                showDownloadIcon = filter != SongFilter.DOWNLOADED,
                fillMaxWidth = true,
                modifier =
                  Modifier.combinedClickable(
                    onClick = {
                      if (inSelectMode) {
                        onCheckedChange(!selection.contains(song.id))
                      } else if (song.id == mediaMetadata?.id) {
                        playerConnection.togglePlayPause()
                      } else {
                        playerConnection.playQueue(
                          ListQueue(
                            title = context.getString(R.string.queue_all_songs),
                            items = filteredSongs.map { it.toMediaItem() },
                            startIndex = index,
                          ),
                        )
                      }
                    },
                    onLongClick = {
                      if (!inSelectMode) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        inSelectMode = true
                        onCheckedChange(true)
                      } else {
                        menuState.show {
                          SongMenu(
                            originalSong = song,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                          )
                        }
                      }
                    }
                  )
                  .animateItem(),
              )
            }
          }
      }

      AnimatedVisibility(
        visible = inSelectMode,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
      ) {
        TopAppBar(
          title = {
            Text(pluralStringResource(R.plurals.n_selected, selection.size, selection.size))
          },
          navigationIcon = {
            IconButton(onClick = onExitSelectionMode) {
              Icon(painter = painterResource(R.drawable.close), contentDescription = null)
            }
          },
          actions = {
            Checkbox(
              checked = selection.size == filteredSongs.size && selection.isNotEmpty(),
              onCheckedChange = {
                if (selection.size == filteredSongs.size) {
                  selection.clear()
                } else {
                  selection.clear()
                  selection.addAll(filteredSongs.map { it.song.id })
                }
              }
            )
            IconButton(
              enabled = selection.isNotEmpty(),
              onClick = {
                menuState.show {
                  SelectionSongMenu(
                    songSelection = selection.mapNotNull { id -> songs.find { it.song.id == id } },
                    onDismiss = menuState::dismiss,
                    clearAction = onExitSelectionMode
                  )
                }
              }
            ) {
              Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            ),
          windowInsets = WindowInsets(0.dp)
        )
      }

      HideOnScrollFAB(
        visible = filteredSongs.isNotEmpty(),
        lazyListState = lazyListState,
        icon = R.drawable.shuffle,
        onClick = {
          playerConnection.playQueue(
            ListQueue(
              title = context.getString(R.string.queue_all_songs),
              items = filteredSongs.shuffled().map { it.toMediaItem() },
            ),
          )
        },
      )
    }
  }
}
