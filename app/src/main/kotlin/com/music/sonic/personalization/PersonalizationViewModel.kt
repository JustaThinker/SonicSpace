package com.music.sonic.personalization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.models.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the personalization recommendation feature.
 * Exposes [recommendations], [tasteProfile], and [isLoading] as observable state.
 */
@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    private val engine: PersonalizationEngine,
    private val tasteProfileProvider: TasteProfileProvider,
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<SongItem>?>(null)
    val recommendations: StateFlow<List<SongItem>?> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _tasteProfile = MutableStateFlow<TasteProfile?>(null)
    val tasteProfile: StateFlow<TasteProfile?> = _tasteProfile.asStateFlow()

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                _tasteProfile.value = tasteProfileProvider.get(forceRefresh)
                _recommendations.value = engine.recommend(
                    forceRefresh = forceRefresh,
                    blacklistIds = emptySet(),
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Force-refreshes both the taste profile and recommendations. */
    fun refresh() = load(forceRefresh = true)
}
