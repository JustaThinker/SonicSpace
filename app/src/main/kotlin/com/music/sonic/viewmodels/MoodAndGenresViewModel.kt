package com.music.sonic.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.pages.MoodAndGenres
import com.music.sonic.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MoodAndGenresViewModel @Inject constructor() : ViewModel() {
  val moodAndGenres = MutableStateFlow<List<MoodAndGenres>?>(null)

  private val _moodGenreArtworks = MutableStateFlow<Map<String, String>>(emptyMap())
  val moodGenreArtworks = _moodGenreArtworks.asStateFlow()

  init {
    viewModelScope.launch {
      YouTube.moodAndGenres()
        .onSuccess { list ->
          moodAndGenres.value = list
          loadCategoryArtworks(list)
        }
        .onFailure { reportException(it) }
    }
  }

  private fun loadCategoryArtworks(list: List<MoodAndGenres>) {
    viewModelScope.launch(Dispatchers.IO) {
      list.flatMap { it.items }.forEach { item ->
        launch {
          val key = "${item.endpoint.browseId}|${item.endpoint.params}"
          if (!_moodGenreArtworks.value.containsKey(key)) {
            val artwork =
              YouTube.browse(item.endpoint.browseId, item.endpoint.params)
                .getOrNull()
                ?.items
                ?.flatMap { it.items }
                ?.firstNotNullOfOrNull { it.thumbnail }

            if (!artwork.isNullOrBlank()) {
              _moodGenreArtworks.value = _moodGenreArtworks.value + (key to artwork)
            }
          }
        }
      }
    }
  }
}
