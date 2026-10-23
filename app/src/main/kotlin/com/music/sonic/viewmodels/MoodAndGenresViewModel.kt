package com.music.sonic.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.pages.MoodAndGenres
import com.music.sonic.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.json.JSONObject

@HiltViewModel
class MoodAndGenresViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
) : ViewModel() {
  val moodAndGenres = MutableStateFlow<List<MoodAndGenres>?>(cachedMoodAndGenres)

  private val _moodGenreArtworks = MutableStateFlow<Map<String, String>>(cachedArtworks ?: emptyMap())
  val moodGenreArtworks = _moodGenreArtworks.asStateFlow()

  private val cacheFile by lazy { File(context.filesDir, "mood_genre_artworks.json") }

  init {
    if (cachedArtworks == null) {
      viewModelScope.launch(Dispatchers.IO) {
        val loaded = loadArtworksFromDisk()
        if (loaded.isNotEmpty()) {
          cachedArtworks = loaded
          _moodGenreArtworks.value = loaded
        }
      }
    }

    if (cachedMoodAndGenres == null) {
      viewModelScope.launch {
        YouTube.moodAndGenres()
          .onSuccess { list ->
            cachedMoodAndGenres = list
            moodAndGenres.value = list
            fetchMissingArtworksInBackground(list)
          }
          .onFailure { reportException(it) }
      }
    } else {
      cachedMoodAndGenres?.let { fetchMissingArtworksInBackground(it) }
    }
  }

  private fun fetchMissingArtworksInBackground(list: List<MoodAndGenres>) {
    viewModelScope.launch(Dispatchers.IO) {
      val currentMap = _moodGenreArtworks.value
      val missingItems = list.flatMap { it.items }.filter { item ->
        val key = "${item.endpoint.browseId}|${item.endpoint.params}"
        !currentMap.containsKey(key)
      }

      if (missingItems.isEmpty()) return@launch

      val semaphore = Semaphore(2)
      val newArtworks = mutableMapOf<String, String>()

      missingItems.forEach { item ->
        val key = "${item.endpoint.browseId}|${item.endpoint.params}"
        try {
          val artwork = semaphore.withPermit {
            YouTube.browse(item.endpoint.browseId, item.endpoint.params)
              .getOrNull()
              ?.items
              ?.flatMap { it.items }
              ?.firstNotNullOfOrNull { it.thumbnail }
          }

          if (!artwork.isNullOrBlank()) {
            synchronized(newArtworks) {
              newArtworks[key] = artwork
            }
          }
        } catch (_: Exception) {
        }
      }

      if (newArtworks.isNotEmpty()) {
        val merged = _moodGenreArtworks.value + newArtworks
        cachedArtworks = merged
        _moodGenreArtworks.value = merged
        saveArtworksToDisk(merged)
      }
    }
  }

  private fun loadArtworksFromDisk(): Map<String, String> {
    return try {
      if (!cacheFile.exists()) return emptyMap()
      val json = JSONObject(cacheFile.readText())
      val map = mutableMapOf<String, String>()
      val keys = json.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        val value = json.optString(key)
        if (!value.isNullOrBlank()) {
          map[key] = value
        }
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  private fun saveArtworksToDisk(artworks: Map<String, String>) {
    try {
      val json = JSONObject()
      artworks.forEach { (k, v) -> json.put(k, v) }
      cacheFile.writeText(json.toString())
    } catch (_: Exception) {
    }
  }

  companion object {
    @Volatile
    private var cachedMoodAndGenres: List<MoodAndGenres>? = null

    @Volatile
    private var cachedArtworks: Map<String, String>? = null
  }
}
