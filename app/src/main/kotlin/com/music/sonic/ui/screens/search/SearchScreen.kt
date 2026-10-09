package com.music.sonic.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.utils.YouTubeUrlParser
import com.music.sonic.LocalDatabase
import com.music.sonic.LocalIsPlayerExpanded
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.constants.GridItemSize
import com.music.sonic.constants.GridItemsSizeKey
import com.music.sonic.constants.GridThumbnailHeight
import com.music.sonic.constants.PauseSearchHistoryKey
import com.music.sonic.constants.SearchSource
import com.music.sonic.constants.SearchSourceKey
import com.music.sonic.db.entities.SearchHistory
import com.music.sonic.playback.queues.YouTubeQueue
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.MOOD_CARD_ASPECT
import com.music.sonic.ui.component.MOOD_CARD_SHAPE
import com.music.sonic.ui.component.MOOD_SPACING
import com.music.sonic.ui.component.MoodAndGenreCard
import com.music.sonic.ui.component.NavigationTitle
import com.music.sonic.ui.component.PAGE_GUTTER
import com.music.sonic.ui.component.YouTubeGridItem
import com.music.sonic.ui.component.moodColumns
import com.music.sonic.ui.menu.YouTubeAlbumMenu
import com.music.sonic.ui.screens.search.suggestions.SuggestionsTabContent
import com.music.sonic.utils.rememberEnumPreference
import com.music.sonic.utils.rememberPreference
import com.music.sonic.viewmodels.ExploreViewModel
import com.music.sonic.viewmodels.MoodAndGenresViewModel
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, pureBlack: Boolean) {
  val database = LocalDatabase.current
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val isPlayerExpanded = LocalIsPlayerExpanded.current
  val playerConnection = LocalPlayerConnection.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

  var searchSource by rememberEnumPreference(SearchSourceKey, SearchSource.ONLINE)
  var query by
    rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
  val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)

  var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
  var searchActive by rememberSaveable { mutableStateOf(false) }
  var showSearchContent by remember { mutableStateOf(false) }

  LaunchedEffect(searchActive) {
    if (searchActive) {

      kotlinx.coroutines.delay(100)
      showSearchContent = true
    } else {
      showSearchContent = false
    }
  }

  val searchBarHorizontalPadding by
    animateDpAsState(
      targetValue = if (searchActive) 0.dp else 16.dp,
      animationSpec = tween(durationMillis = 245, easing = FastOutSlowInEasing),
      label = "SearchBarHorizontalPadding"
    )
  val searchBarTopPadding by
    animateDpAsState(
      targetValue = if (searchActive) 0.dp else 8.dp,
      animationSpec = tween(durationMillis = 245, easing = FastOutSlowInEasing),
      label = "SearchBarTopPadding"
    )

  val onSearch: (String) -> Unit = remember {
    { searchQuery ->
      if (searchQuery.isNotEmpty()) {
        focusManager.clearFocus()
        when (val parsedUrl = YouTubeUrlParser.parse(searchQuery)) {
          is YouTubeUrlParser.ParsedUrl.Video -> {
            playerConnection?.playQueue(
              YouTubeQueue(
                WatchEndpoint(videoId = parsedUrl.id),
              ),
            )
          }
          is YouTubeUrlParser.ParsedUrl.Artist -> {
            navController.navigate("artist/${parsedUrl.id}")
          }
          null -> {
            navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
          }
        }

        if (!pauseSearchHistory) {
          coroutineScope.launch(Dispatchers.IO) {
            database.query { insert(SearchHistory(query = searchQuery)) }
          }
        }
      }
    }
  }

  val onSearchFromSuggestion: (String) -> Unit = remember {
    { searchQuery ->
      if (searchQuery.isNotEmpty()) {
        focusManager.clearFocus()
        when (val parsedUrl = YouTubeUrlParser.parse(searchQuery)) {
          is YouTubeUrlParser.ParsedUrl.Video -> {
            playerConnection?.playQueue(
              YouTubeQueue(
                WatchEndpoint(videoId = parsedUrl.id),
              ),
            )
          }
          is YouTubeUrlParser.ParsedUrl.Artist -> {
            navController.navigate("artist/${parsedUrl.id}")
          }
          null -> {
            navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
          }
        }

        if (!pauseSearchHistory) {
          coroutineScope.launch(Dispatchers.IO) {
            database.query { insert(SearchHistory(query = searchQuery)) }
          }
        }
      }
    }
  }

  Scaffold(
    topBar = {
      Column(
        modifier =
          Modifier.background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
      ) {
        SearchBar(
          inputField = {
            BasicTextField(
              value = query,
              onValueChange = { query = it },
              modifier =
                Modifier.fillMaxWidth()
                  .height(56.dp)
                  .focusRequester(focusRequester)
                  .onFocusChanged { if (it.isFocused) searchActive = true },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions =
                KeyboardActions(
                  onSearch = {
                    onSearch(query.text)
                    searchActive = false
                  }
                ),
              textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
              cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
              decorationBox = { innerTextField ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                  IconButton(
                    onClick = {
                      if (searchActive) {
                        searchActive = false
                        query = TextFieldValue("")
                      } else {
                        searchActive = true
                      }
                    }
                  ) {
                    Icon(
                      painter =
                        painterResource(
                          if (searchActive) R.drawable.arrow_back else R.drawable.search
                        ),
                      contentDescription =
                        if (searchActive) stringResource(R.string.dismiss) else null,
                      tint = MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    if (query.text.isEmpty()) {
                      Text(
                        text =
                          stringResource(
                            when (searchSource) {
                              SearchSource.LOCAL -> R.string.search_library
                              SearchSource.ONLINE -> R.string.search_yt_music
                            }
                          ),
                        style =
                          TextStyle(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 16.sp
                          )
                      )
                    }
                    innerTextField()
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.text.isNotEmpty()) {
                      IconButton(onClick = { query = TextFieldValue("") }) {
                        Icon(
                          painter = painterResource(R.drawable.close),
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.onSurface
                        )
                      }
                    }
                    IconButton(
                      onClick = {
                        searchSource =
                          if (searchSource == SearchSource.ONLINE) SearchSource.LOCAL
                          else SearchSource.ONLINE
                      }
                    ) {
                      Icon(
                        painter =
                          painterResource(
                            when (searchSource) {
                              SearchSource.LOCAL -> R.drawable.library_music
                              SearchSource.ONLINE -> R.drawable.globe_search
                            }
                          ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            )
          },
          expanded = searchActive,
          onExpandedChange = { searchActive = it },
          colors =
            SearchBarDefaults.colors(
              containerColor =
                if (pureBlack) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = searchBarHorizontalPadding)
              .padding(top = searchBarTopPadding)
        ) {
          if (showSearchContent) {
            when (searchSource) {
              SearchSource.LOCAL ->
                LocalSearchScreen(
                  query = query.text,
                  navController = navController,
                  onDismiss = { searchActive = false },
                  pureBlack = pureBlack
                )
              SearchSource.ONLINE ->
                OnlineSearchScreen(
                  query = query.text,
                  onQueryChange = { query = it },
                  navController = navController,
                  onSearch = {
                    onSearchFromSuggestion(it)
                    searchActive = false
                  },
                  onDismiss = { searchActive = false },
                  pureBlack = pureBlack
                )
            }
          }
        }

        AnimatedVisibility(
          visible = !searchActive,
          enter =
            expandVertically(
              animationSpec = tween(durationMillis = 245, easing = FastOutSlowInEasing)
            ) + fadeIn(),
          exit =
            shrinkVertically(
              animationSpec = tween(durationMillis = 245, easing = FastOutSlowInEasing)
            ) + fadeOut()
        ) {
          Column {
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryTabRow(
              selectedTabIndex = selectedTabIndex,
              containerColor = Color.Transparent,
              divider = {
                androidx.compose.material3.HorizontalDivider(
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                  thickness = 1.5.dp
                )
              },
              indicator = {
                Box(
                  modifier = Modifier.tabIndicatorOffset(selectedTabIndex).fillMaxWidth(),
                  contentAlignment = Alignment.BottomCenter
                ) {
                  Box(
                    modifier =
                      Modifier.width(32.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(MaterialTheme.colorScheme.onSurface)
                  )
                }
              }
            ) {
              Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = { Text(stringResource(R.string.tab_explore)) }
              )
              Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = { Text("Apple Music") }
              )
            }
          }
        }
      }
    },
    containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background
  ) { paddingValues ->
    val bottomPadding =
      LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).fillMaxSize()) {
      if (!searchActive) {
        val tabPadding = PaddingValues(bottom = bottomPadding)
        when (selectedTabIndex) {
          0 -> ExploreTabContent(navController = navController, contentPadding = tabPadding)
          1 -> SuggestionsTabContent(navController = navController, contentPadding = tabPadding)
        }
      }
    }
  }

  DisposableEffect(lifecycleOwner, isPlayerExpanded) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {

          if (isPlayerExpanded) {
            keyboardController?.hide()
            focusManager.clearFocus()
          }
        }
        Lifecycle.Event.ON_PAUSE -> {

          focusManager.clearFocus()
          keyboardController?.hide()
        }
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)

    if (isPlayerExpanded) {
      keyboardController?.hide()
      focusManager.clearFocus()
    }

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}

@Composable
fun ExploreTabContent(
  navController: NavController,
  viewModel: MoodAndGenresViewModel = hiltViewModel(),
  contentPadding: PaddingValues = PaddingValues(0.dp)
) {
  val moodAndGenresList by viewModel.moodAndGenres.collectAsState()
  val moodGenreArtworks by viewModel.moodGenreArtworks.collectAsState()

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val columns = moodColumns(maxWidth)

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = contentPadding
    ) {
      if (moodAndGenresList == null) {
        item(key = "skeleton") {
          ExploreSkeletonRows(columns = columns)
        }
      } else {
        val allItems = moodAndGenresList?.flatMap { it.items }
          ?.distinctBy { "${it.endpoint.browseId}|${it.endpoint.params}" }
          .orEmpty()

        val rows = allItems.chunked(columns)
        items(
          items = rows,
          key = { row -> row.firstOrNull()?.let { "${it.endpoint.browseId}|${it.endpoint.params}" } ?: row.hashCode() }
        ) { row ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(MOOD_SPACING),
            modifier =
              Modifier
                .padding(horizontal = PAGE_GUTTER)
                .padding(bottom = MOOD_SPACING)
          ) {
            row.forEach { item ->
              val artworkUrl = item.thumbnailUrl
                ?: moodGenreArtworks["${item.endpoint.browseId}|${item.endpoint.params}"]

              MoodAndGenreCard(
                item = item,
                thumbnailUrl = artworkUrl,
                onClick = {
                  navController.navigate(
                    "youtube_browse/${item.endpoint.browseId}?params=${item.endpoint.params}"
                  )
                },
                modifier = Modifier.weight(1f)
              )
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
          }
        }
      }

      item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }
}

@Composable
private fun ExploreSkeletonRows(columns: Int) {
  Column(
    modifier = Modifier.padding(horizontal = PAGE_GUTTER, vertical = 8.dp)
  ) {
    repeat(6) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(MOOD_SPACING),
        modifier = Modifier.padding(bottom = MOOD_SPACING)
      ) {
        repeat(columns) {
          Box(
            modifier =
              Modifier
                .weight(1f)
                .aspectRatio(MOOD_CARD_ASPECT)
                .clip(MOOD_CARD_SHAPE)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          )
        }
      }
    }
  }
}
