package com.music.sonic.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.sonic.db.DatabaseDao
import com.music.sonic.db.entities.EventWithSong
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DailyStat(
  val dayLabel: String,
  val minutes: Int
)

data class InsightsUiState(
  val isLoading: Boolean = true,
  val totalHours: Double = 0.0,
  val totalTracksCount: Int = 0,
  val weeklyStats: List<DailyStat> = emptyList(),
  val hourlyHeatmap: List<Int> = List(24) { 0 },
  val topSongs: List<EventWithSong> = emptyList()
)

@HiltViewModel
class ListeningInsightsViewModel @Inject constructor(
  private val databaseDao: DatabaseDao
) : ViewModel() {

  private val _uiState = MutableStateFlow(InsightsUiState())
  val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

  init {
    loadInsights()
  }

  fun loadInsights() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)

      databaseDao.events().collect { eventsList ->
        if (eventsList.isEmpty()) {
          _uiState.value = InsightsUiState(isLoading = false)
          return@collect
        }

        val totalPlayTimeMs = eventsList.sumOf { it.event.playTime }
        val totalHours = totalPlayTimeMs / (1000.0 * 60.0 * 60.0)
        val totalTracks = eventsList.size

        // 24-hour heatmap calculation
        val hourlyCounts = IntArray(24)
        val weeklyMinutesMap = mutableMapOf<Int, Int>() // dayOfWeek -> minutes

        val sevenDaysAgoLdt = LocalDateTime.now().minusDays(7)

        eventsList.forEach { eventWithSong ->
          val ldt = eventWithSong.event.timestamp

          val hour = ldt.hour
          if (hour in 0..23) {
            hourlyCounts[hour]++
          }

          if (ldt.isAfter(sevenDaysAgoLdt)) {
            val dayOfWeek = ldt.dayOfWeek.value
            val playMinutes = (eventWithSong.event.playTime / 60000L).toInt()
            weeklyMinutesMap[dayOfWeek] = (weeklyMinutesMap[dayOfWeek] ?: 0) + playMinutes
          }
        }

        // Format weekly stats
        val daysList = mutableListOf<DailyStat>()
        val currentLdt = LocalDateTime.now()
        for (i in 6 downTo 0) {
          val day = currentLdt.minusDays(i.toLong())
          val dayOfWeekVal = day.dayOfWeek.value
          val label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
          val mins = weeklyMinutesMap[dayOfWeekVal] ?: 0
          daysList.add(DailyStat(label, mins))
        }

        // Top songs by play frequency
        val topSongs = eventsList
          .groupBy { it.song.id }
          .values
          .mapNotNull { it.firstOrNull() }
          .take(5)

        _uiState.value = InsightsUiState(
          isLoading = false,
          totalHours = totalHours,
          totalTracksCount = totalTracks,
          weeklyStats = daysList,
          hourlyHeatmap = hourlyCounts.toList(),
          topSongs = topSongs
        )
      }
    }
  }
}
