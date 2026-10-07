package com.music.sonic.ui.screens.search

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.utils.YouTubeUrlParser
import com.music.sonic.LocalDatabase
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.models.toMediaMetadata
import com.music.sonic.playback.queues.YouTubeQueue
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.PlayingIndicator
import com.music.sonic.ui.menu.YouTubeAlbumMenu
import com.music.sonic.ui.menu.YouTubeArtistMenu
import com.music.sonic.ui.menu.YouTubePlaylistMenu
import com.music.sonic.ui.menu.YouTubeSongMenu
import com.music.sonic.ui.utils.resize
import com.music.sonic.utils.joinByBullet
import com.music.sonic.utils.makeTimeString
import com.music.sonic.viewmodels.OnlineSearchSuggestionViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalComposeUiApi::class,
  ExperimentalMaterial3Api::class,
  FlowPreview::class
)
@Composable
fun OnlineSearchScreen(
  query: String,
  onQueryChange: (TextFieldValue) -> Unit,
  navController: NavController,
  onSearch: (String) -> Unit,
  onDismiss: () -> Unit,
  pureBlack: Boolean,
  viewModel: OnlineSearchSuggestionViewModel = hiltViewModel(),
) {
  val database = LocalDatabase.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val menuState = LocalMenuState.current
  val playerConnection = LocalPlayerConnection.current ?: return
  val haptic = LocalHapticFeedback.current

  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  val viewState by viewModel.viewState.collectAsState()

  val lazyListState = rememberLazyListState()

  LaunchedEffect(Unit) {
    snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
      .drop(1)
      .collect { keyboardController?.hide() }
  }

  LaunchedEffect(query) {
    if (YouTubeUrlParser.isYouTubeUrl(query)) {
      viewModel.query.value = query
    } else {
      kotlinx.coroutines.delay(250L)
      viewModel.query.value = query
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
          onDismiss()
        }
      }
      is AlbumItem -> {
        navController.navigate("album/${item.id}")
        onDismiss()
      }
      is ArtistItem -> {
        navController.navigate("artist/${item.id}")
        onDismiss()
      }
      is PlaylistItem -> {
        navController.navigate("online_playlist/${item.id}")
        onDismiss()
      }
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
            onDismiss = {
              menuState.dismiss()
              onDismiss()
            }
          )
        is AlbumItem ->
          YouTubeAlbumMenu(
            albumItem = item,
            navController = navController,
            onDismiss = {
              menuState.dismiss()
              onDismiss()
            }
          )
        is ArtistItem ->
          YouTubeArtistMenu(
            artist = item,
            onDismiss = {
              menuState.dismiss()
              onDismiss()
            }
          )
        is PlaylistItem ->
          YouTubePlaylistMenu(
            playlist = item,
            coroutineScope = coroutineScope,
            onDismiss = {
              menuState.dismiss()
              onDismiss()
            }
          )
      }
    }
  }

  LazyColumn(
    state = lazyListState,
    contentPadding =
      LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom).asPaddingValues(),
    modifier =
      Modifier.fillMaxSize()
        .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
  ) {
    // Parsed from Link Result Card (only when a direct URL is pasted)
    if (viewState.isFromLink && viewState.items.isNotEmpty()) {
      item(key = "instant_result_header") {
        Text(
          text = stringResource(R.string.parsed_from_link),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp).animateItem()
        )
      }

      itemsIndexed(viewState.items, key = { _, it -> "item_${it.id}" }) { _, item ->
        TopResultCard(
          item = item,
          isActive =
            when (item) {
              is SongItem -> mediaMetadata?.id == item.id
              is AlbumItem -> mediaMetadata?.album?.id == item.id
              else -> false
            },
          isPlaying = isPlaying,
          onClick = { onItemClick(item) },
          onPlayClick = { onItemClick(item) },
          pureBlack = pureBlack,
          modifier = Modifier.animateItem()
        )
      }

      item(key = "instant_result_spacer") {
        Spacer(modifier = Modifier.height(10.dp))
      }
    }

    // Search History Section
    if (viewState.history.isNotEmpty()) {
      item(key = "history_header") {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
              .animateItem(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              painter = painterResource(R.drawable.history),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = stringResource(R.string.search_history),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          TextButton(
            onClick = {
              coroutineScope.launch {
                database.query { clearSearchHistory() }
              }
            }
          ) {
            Text(
              text = "Clear",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      itemsIndexed(viewState.history, key = { _, it -> "history_${it.query}" }) { _, history ->
        HistoryRowItem(
          query = history.query,
          searchQuery = query,
          onClick = {
            onSearch(history.query)
            onDismiss()
          },
          onDelete = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            database.query { delete(history) }
          },
          onFillTextField = {
            onQueryChange(TextFieldValue(history.query, TextRange(history.query.length)))
          },
          pureBlack = pureBlack,
          modifier = Modifier.animateItem()
        )
      }
    }

    if (viewState.history.isNotEmpty() && viewState.suggestions.isNotEmpty()) {
      item(key = "history_suggestion_spacer") {
        Spacer(modifier = Modifier.height(12.dp).animateItem())
      }
    }

    // Online Suggestions Section
    if (viewState.suggestions.isNotEmpty()) {
      item(key = "suggestions_header") {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
              .animateItem(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            painter = painterResource(R.drawable.search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.suggestions),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      itemsIndexed(viewState.suggestions, key = { _, it -> "suggestion_$it" }) { _, suggestionQuery ->
        OnlineSuggestionRow(
          suggestion = suggestionQuery,
          searchQuery = query,
          onClick = {
            onSearch(suggestionQuery)
            onDismiss()
          },
          onFillTextField = {
            onQueryChange(TextFieldValue(suggestionQuery, TextRange(suggestionQuery.length)))
          },
          pureBlack = pureBlack,
          modifier = Modifier.animateItem()
        )
      }
    }

    item(key = "bottom_spacer") {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
fun HistoryRowItem(
  query: String,
  searchQuery: String,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  onFillTextField: () -> Unit,
  pureBlack: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val highlightedText =
    buildHighlightedText(
      fullText = query,
      query = searchQuery,
      primaryColor = MaterialTheme.colorScheme.primary,
      defaultColor = MaterialTheme.colorScheme.onSurface
    )

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 2.5.dp)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color =
      if (pureBlack) Color(0xFF141414)
      else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
    border =
      BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
      )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
    ) {
      Box(
        modifier =
          Modifier.size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(R.drawable.history),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = highlightedText,
        style =
          MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
          ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.close),
          contentDescription = stringResource(R.string.delete),
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.width(2.dp))

      IconButton(
        onClick = onFillTextField,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.arrow_top_left),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun OnlineSuggestionRow(
  suggestion: String,
  searchQuery: String,
  onClick: () -> Unit,
  onFillTextField: () -> Unit,
  pureBlack: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val highlightedText =
    buildHighlightedText(
      fullText = suggestion,
      query = searchQuery,
      primaryColor = MaterialTheme.colorScheme.primary,
      defaultColor = MaterialTheme.colorScheme.onSurface
    )

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 2.5.dp)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color =
      if (pureBlack) Color(0xFF141414)
      else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
    border =
      BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
      )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
    ) {
      Box(
        modifier =
          Modifier.size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(R.drawable.search),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = highlightedText,
        style =
          MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp
          ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      IconButton(
        onClick = onFillTextField,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.arrow_top_left),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

fun buildHighlightedText(
  fullText: String,
  query: String,
  primaryColor: Color,
  defaultColor: Color,
): AnnotatedString {
  val cleanQuery = query.trim()
  if (cleanQuery.isEmpty()) {
    return AnnotatedString(fullText)
  }

  val matchIndex = fullText.indexOf(cleanQuery, ignoreCase = true)
  if (matchIndex == -1) {
    return AnnotatedString(fullText)
  }

  return buildAnnotatedString {
    if (matchIndex > 0) {
      withStyle(SpanStyle(color = defaultColor)) {
        append(fullText.substring(0, matchIndex))
      }
    }

    withStyle(
      SpanStyle(
        color = primaryColor,
        fontWeight = FontWeight.Bold
      )
    ) {
      append(fullText.substring(matchIndex, matchIndex + cleanQuery.length))
    }

    if (matchIndex + cleanQuery.length < fullText.length) {
      withStyle(SpanStyle(color = defaultColor)) {
        append(fullText.substring(matchIndex + cleanQuery.length))
      }
    }
  }
}
