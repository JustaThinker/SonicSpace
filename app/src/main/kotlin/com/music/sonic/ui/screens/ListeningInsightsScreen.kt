package com.music.sonic.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.ui.component.IconButton
import com.music.sonic.ui.utils.backToMain
import com.music.sonic.viewmodels.DailyStat
import com.music.sonic.viewmodels.ListeningInsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningInsightsScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  viewModel: ListeningInsightsViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val windowInsets = LocalPlayerAwareWindowInsets.current

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surface,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Listening Insights",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        navigationIcon = {
          IconButton(
            onClick = { navController.navigateUp() },
            onLongClick = navController::backToMain
          ) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    if (uiState.isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
          start = 16.dp,
          end = 16.dp,
          top = innerPadding.calculateTopPadding() + 8.dp,
          bottom = windowInsets.asPaddingValues().calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Summary Cards
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              )
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "Total Hours",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = "${(uiState.totalHours * 10).toInt() / 10.0}h",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
              )
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "Tracks Listened",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                  text = "${uiState.totalTracksCount}",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.secondary
                )
              }
            }
          }
        }

        // Weekly Bar Chart
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "Weekly Activity (Minutes)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              WeeklyBarChart(
                stats = uiState.weeklyStats,
                primaryColor = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // 24-Hour Time Heatmap
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "24-Hour Listening Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              HourlyHeatmapGrid(
                heatmap = uiState.hourlyHeatmap,
                accentColor = MaterialTheme.colorScheme.tertiary
              )
            }
          }
        }

        // Top Songs Section
        if (uiState.topSongs.isNotEmpty()) {
          item {
            Text(
              text = "Top Played Songs",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(top = 8.dp)
            )
          }

          itemsIndexed(uiState.topSongs) { index, eventWithSong ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
              )
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                      text = "#${index + 1}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                }

                AsyncImage(
                  model = eventWithSong.song.thumbnailUrl,
                  contentDescription = null,
                  modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = eventWithSong.song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun WeeklyBarChart(
  stats: List<DailyStat>,
  primaryColor: Color
) {
  val maxMins = stats.maxOfOrNull { it.minutes }?.coerceAtLeast(1) ?: 1

  Row(
    modifier = Modifier.fillMaxWidth().height(160.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    stats.forEach { stat ->
      val fraction = (stat.minutes.toFloat() / maxMins).coerceIn(0.05f, 1f)
      val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 800),
        label = "barFraction"
      )

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "${stat.minutes}m",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .width(20.dp)
            .fillMaxHeight(animatedFraction * 0.75f)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(primaryColor)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = stat.dayLabel,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun HourlyHeatmapGrid(
  heatmap: List<Int>,
  accentColor: Color
) {
  val maxVal = heatmap.maxOrNull()?.coerceAtLeast(1) ?: 1

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      heatmap.take(12).forEachIndexed { hour, count ->
        val alpha = (count.toFloat() / maxVal).coerceIn(0.12f, 1f)
        Box(
          modifier = Modifier
            .weight(1f)
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = alpha)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${hour}h",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onTertiary
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      heatmap.drop(12).take(12).forEachIndexed { index, count ->
        val hour = index + 12
        val alpha = (count.toFloat() / maxVal).coerceIn(0.12f, 1f)
        Box(
          modifier = Modifier
            .weight(1f)
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = alpha)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${hour}h",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onTertiary
          )
        }
      }
    }
  }
}
