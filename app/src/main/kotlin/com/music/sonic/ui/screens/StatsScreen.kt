package com.music.sonic.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.models.WatchEndpoint
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.constants.StatPeriod
import com.music.sonic.models.toMediaMetadata
import com.music.sonic.playback.queues.YouTubeQueue
import com.music.sonic.ui.component.ChoiceChipsRow
import com.music.sonic.ui.component.LocalAlbumsGrid
import com.music.sonic.ui.component.LocalArtistsGrid
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.LocalSongsGrid
import com.music.sonic.ui.component.NavigationTitle
import com.music.sonic.ui.menu.AlbumMenu
import com.music.sonic.ui.menu.ArtistMenu
import com.music.sonic.ui.menu.SongMenu
import com.music.sonic.utils.joinByBullet
import com.music.sonic.utils.makeTimeString
import com.music.sonic.viewmodels.StatsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
  navController: NavController,
  viewModel: StatsViewModel = hiltViewModel(),
) {
  val menuState = LocalMenuState.current
  val haptic = LocalHapticFeedback.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

  val indexChips by viewModel.indexChips.collectAsState()
  val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsState()
  val mostPlayedSongsStats by viewModel.mostPlayedSongsStats.collectAsState()
  val mostPlayedArtists by viewModel.mostPlayedArtists.collectAsState()
  val mostPlayedAlbums by viewModel.mostPlayedAlbums.collectAsState()
  val firstEvent by viewModel.firstEvent.collectAsState()
  val currentDate = LocalDateTime.now()

  val totalPlayTime by viewModel.totalPlayTime.collectAsState()
  val allTimePlayTime by viewModel.allTimePlayTime.collectAsState()
  val uniqueSongsCount by viewModel.uniqueSongsCount.collectAsState()
  val uniqueArtistsCount by viewModel.uniqueArtistsCount.collectAsState()
  val uniqueAlbumsCount by viewModel.uniqueAlbumsCount.collectAsState()

  val coroutineScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val selectedOption by viewModel.selectedOption.collectAsState()

  val weeklyDates =
    if (currentDate != null && firstEvent != null) {
      generateSequence(currentDate) { it.minusWeeks(1) }
        .takeWhile { it.isAfter(firstEvent?.event?.timestamp?.minusWeeks(1)) }
        .mapIndexed { index, date ->
          val endDate = date.plusWeeks(1).minusDays(1).coerceAtMost(currentDate)
          val formatter = DateTimeFormatter.ofPattern("dd MMM")

          val startDateFormatted = formatter.format(date)
          val endDateFormatted = formatter.format(endDate)

          val startMonth = date.month
          val endMonth = endDate.month
          val startYear = date.year
          val endYear = endDate.year

          val text =
            when {
              startYear != currentDate.year ->
                "$startDateFormatted, $startYear - $endDateFormatted, $endYear"
              startMonth != endMonth -> "$startDateFormatted - $endDateFormatted"
              else -> "${date.dayOfMonth} - $endDateFormatted"
            }
          Pair(index, text)
        }
        .toList()
    } else {
      emptyList()
    }

  val monthlyDates =
    if (currentDate != null && firstEvent != null) {
      generateSequence(currentDate.plusMonths(1).withDayOfMonth(1).minusDays(1)) {
          it.minusMonths(1)
        }
        .takeWhile {
          it.isAfter(
            firstEvent?.event?.timestamp?.withDayOfMonth(1),
          )
        }
        .mapIndexed { index, date ->
          val formatter = DateTimeFormatter.ofPattern("MMM")
          val formattedDate = formatter.format(date)
          val text =
            if (date.year != currentDate.year) {
              "$formattedDate ${date.year}"
            } else {
              formattedDate
            }
          Pair(index, text)
        }
        .toList()
    } else {
      emptyList()
    }

  val yearlyDates =
    if (currentDate != null && firstEvent != null) {
      generateSequence(
          currentDate.plusYears(1).withDayOfYear(1).minusDays(1),
        ) {
          it.minusYears(1)
        }
        .takeWhile {
          it.isAfter(
            firstEvent?.event?.timestamp,
          )
        }
        .mapIndexed { index, date -> Pair(index, "${date.year}") }
        .toList()
    } else {
      emptyList()
    }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      state = lazyListState,
      contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
      item(key = "choice_chips") {
        ChoiceChipsRow(
          chips =
            when (selectedOption) {
              OptionStats.WEEKS -> weeklyDates
              OptionStats.MONTHS -> monthlyDates
              OptionStats.YEARS -> yearlyDates
              OptionStats.CONTINUOUS -> {
                listOf(
                  StatPeriod.WEEK_1.ordinal to pluralStringResource(R.plurals.n_week, 1, 1),
                  StatPeriod.MONTH_1.ordinal to pluralStringResource(R.plurals.n_month, 1, 1),
                  StatPeriod.MONTH_3.ordinal to pluralStringResource(R.plurals.n_month, 3, 3),
                  StatPeriod.MONTH_6.ordinal to pluralStringResource(R.plurals.n_month, 6, 6),
                  StatPeriod.YEAR_1.ordinal to pluralStringResource(R.plurals.n_year, 1, 1),
                  StatPeriod.ALL.ordinal to stringResource(R.string.filter_all),
                )
              }
            },
          options =
            listOf(
              OptionStats.CONTINUOUS to stringResource(id = R.string.continuous),
              OptionStats.WEEKS to stringResource(R.string.weeks),
              OptionStats.MONTHS to stringResource(R.string.months),
              OptionStats.YEARS to stringResource(R.string.years),
            ),
          selectedOption = selectedOption,
          onSelectionChange = {
            viewModel.selectedOption.value = it
            viewModel.indexChips.value = 0
          },
          currentValue = indexChips,
          onValueUpdate = { viewModel.indexChips.value = it },
        )
      }

      item(key = "listening_summary") {
        val currentPeriodLabel =
          when (selectedOption) {
            OptionStats.WEEKS -> weeklyDates.getOrNull(indexChips)?.second.orEmpty()
            OptionStats.MONTHS -> monthlyDates.getOrNull(indexChips)?.second.orEmpty()
            OptionStats.YEARS -> yearlyDates.getOrNull(indexChips)?.second.orEmpty()
            OptionStats.CONTINUOUS -> {
              when (indexChips) {
                StatPeriod.WEEK_1.ordinal -> pluralStringResource(R.plurals.n_week, 1, 1)
                StatPeriod.MONTH_1.ordinal -> pluralStringResource(R.plurals.n_month, 1, 1)
                StatPeriod.MONTH_3.ordinal -> pluralStringResource(R.plurals.n_month, 3, 3)
                StatPeriod.MONTH_6.ordinal -> pluralStringResource(R.plurals.n_month, 6, 6)
                StatPeriod.YEAR_1.ordinal -> pluralStringResource(R.plurals.n_year, 1, 1)
                StatPeriod.ALL.ordinal -> stringResource(R.string.filter_all)
                else -> ""
              }
            }
          }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          if (currentPeriodLabel.isNotBlank()) {
            Text(
              text = currentPeriodLabel,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(bottom = 12.dp)
            )
          }

          // Total Listening Time Hero Card
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              val durationText = formatDuration(totalPlayTime)
              val durationFontSize =
                when {
                  durationText.length <= 7 -> 32.sp
                  durationText.length == 8 -> 28.sp
                  durationText.length == 9 -> 24.sp
                  else -> 20.sp
                }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = stringResource(R.string.total_listening_time),
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = durationText,
                  style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = durationFontSize,
                    letterSpacing = (-1).sp
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              Spacer(modifier = Modifier.width(16.dp))
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter = painterResource(R.drawable.timer),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Segmented Unique Counts Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            val segmentBgColor = MaterialTheme.colorScheme.surfaceContainerHigh

            // Songs Segment
            Box(
              modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .background(
                  color = segmentBgColor,
                  shape = RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp,
                    topEnd = 6.dp,
                    bottomEnd = 6.dp
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.music_note),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = uniqueSongsCount.toString(),
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = stringResource(R.string.songs),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // Artists Segment
            Box(
              modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .background(color = segmentBgColor, shape = RoundedCornerShape(6.dp)),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.artist),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = uniqueArtistsCount.toString(),
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = stringResource(R.string.artists),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // Albums Segment
            Box(
              modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .background(
                  color = segmentBgColor,
                  shape = RoundedCornerShape(
                    topEnd = 20.dp,
                    bottomEnd = 20.dp,
                    topStart = 6.dp,
                    bottomStart = 6.dp
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.album),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = uniqueAlbumsCount.toString(),
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = stringResource(R.string.albums),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // All-Time Total Play Time Card
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    painter = painterResource(R.drawable.timer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Column {
                  Text(
                    text = stringResource(R.string.all_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = stringResource(R.string.total_listening_time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = formatDuration(allTimePlayTime),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      item(key = "mostPlayedSongs") {
        NavigationTitle(
          title = "${mostPlayedSongsStats.size} ${stringResource(id = R.string.songs)}",
          modifier = Modifier.animateItem(),
        )

        LazyRow(
          modifier = Modifier.animateItem(),
        ) {
          itemsIndexed(
            items = mostPlayedSongsStats,
            key = { _, song -> song.id },
          ) { index, song ->
            LocalSongsGrid(
              title = "${index + 1}. ${song.title}",
              subtitle =
                joinByBullet(
                  pluralStringResource(
                    R.plurals.n_time,
                    song.songCountListened,
                    song.songCountListened,
                  ),
                  makeTimeString(song.timeListened),
                ),
              thumbnailUrl = song.thumbnailUrl,
              isActive = song.id == mediaMetadata?.id,
              isPlaying = isPlaying,
              forceCrop = true,
              modifier =
                Modifier.fillMaxWidth()
                  .combinedClickable(
                    onClick = {
                      if (song.id == mediaMetadata?.id) {
                        playerConnection.togglePlayPause()
                      } else {
                        playerConnection.playQueue(
                          YouTubeQueue(
                            endpoint = WatchEndpoint(song.id),
                            preloadItem = mostPlayedSongs[index].toMediaMetadata(),
                          ),
                        )
                      }
                    },
                    onLongClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      menuState.show {
                        SongMenu(
                          originalSong = mostPlayedSongs[index],
                          navController = navController,
                          onDismiss = menuState::dismiss,
                        )
                      }
                    },
                  )
                  .animateItem(),
            )
          }
        }
      }

      item(key = "mostPlayedArtists") {
        NavigationTitle(
          title = "${mostPlayedArtists.size} ${stringResource(id = R.string.artists)}",
          modifier = Modifier.animateItem(),
        )

        LazyRow(
          modifier = Modifier.animateItem(),
        ) {
          itemsIndexed(
            items = mostPlayedArtists,
            key = { _, artist -> artist.id },
          ) { index, artist ->
            LocalArtistsGrid(
              title = "${index + 1}. ${artist.artist.name}",
              subtitle =
                joinByBullet(
                  pluralStringResource(R.plurals.n_time, artist.songCount, artist.songCount),
                  makeTimeString(artist.timeListened?.toLong()),
                ),
              thumbnailUrl = artist.artist.thumbnailUrl,
              forceCrop = true,
              modifier =
                Modifier.combinedClickable(
                    onClick = { navController.navigate("artist/${artist.id}") },
                    onLongClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      menuState.show {
                        ArtistMenu(
                          originalArtist = artist,
                          coroutineScope = coroutineScope,
                          onDismiss = menuState::dismiss,
                        )
                      }
                    },
                  )
                  .animateItem(),
            )
          }
        }
      }

      item(key = "mostPlayedAlbums") {
        NavigationTitle(
          title = "${mostPlayedAlbums.size} ${stringResource(id = R.string.albums)}",
          modifier = Modifier.animateItem(),
        )

        if (mostPlayedAlbums.isNotEmpty()) {
          LazyRow(
            modifier = Modifier.animateItem(),
          ) {
            itemsIndexed(
              items = mostPlayedAlbums,
              key = { _, album -> album.id },
            ) { index, album ->
              LocalAlbumsGrid(
                title = "${index + 1}. ${album.album.title}",
                subtitle =
                  joinByBullet(
                    pluralStringResource(
                      R.plurals.n_time,
                      album.songCountListened!!,
                      album.songCountListened!!
                    ),
                    makeTimeString(album.timeListened),
                  ),
                thumbnailUrl = album.album.thumbnailUrl,
                isActive = album.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                forceCrop = true,
                modifier =
                  Modifier.fillMaxWidth()
                    .combinedClickable(
                      onClick = { navController.navigate("album/${album.id}") },
                      onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                          AlbumMenu(
                            originalAlbum = album,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                          )
                        }
                      },
                    )
                    .animateItem(),
              )
            }
          }
        }
      }
    }

  }
}

/** Converts milliseconds to a human-readable "Xh Ym" string */
private fun formatDuration(ms: Long): String {
  if (ms <= 0L) return "0m"
  val totalMinutes = ms / 60_000
  val hours = totalMinutes / 60
  val minutes = totalMinutes % 60
  return when {
    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
    hours > 0 -> "${hours}h"
    else -> "${minutes}m"
  }
}
