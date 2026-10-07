package com.music.sonic.ui.screens.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.constants.AudioLoudnessPreset
import com.music.sonic.constants.AudioLoudnessPresetKey
import com.music.sonic.constants.AudioNormalizationKey
import com.music.sonic.constants.BassBoostKey
import com.music.sonic.constants.CrossfadeDurationKey
import com.music.sonic.constants.CrossfadeEnabledKey
import com.music.sonic.constants.CrossfeedEnabledKey
import com.music.sonic.constants.PlaybackPitchLockKey
import com.music.sonic.constants.PlaybackSpeedKey
import com.music.sonic.constants.SkipSilenceInstantKey
import com.music.sonic.constants.SkipSilenceKey
import com.music.sonic.constants.SpatialAudioKey
import com.music.sonic.constants.DolbyAtmosEnabledKey
import com.music.sonic.constants.SpatialAudioStrengthKey
import com.music.sonic.constants.StereoBalanceKey
import com.music.sonic.constants.TrebleBoostKey
import com.music.sonic.ui.component.DefaultDialog
import com.music.sonic.ui.component.IconButton
import com.music.sonic.ui.screens.equalizer.axion.AxionEqViewModel
import com.music.sonic.utils.rememberEnumPreference
import com.music.sonic.utils.rememberPreference
import kotlin.math.abs
import kotlin.math.roundToInt

private val BandFrequencies = listOf(
  "31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz"
)

private val PresetGainMap = mapOf(
  "Flat" to floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
  "Bass Boost" to floatArrayOf(350f, 300f, 250f, 150f, 50f, 0f, 0f, 0f, 0f, 0f),
  "Bass & Treble" to floatArrayOf(350f, 250f, 150f, 0f, -50f, -50f, 0f, 150f, 250f, 350f),
  "Vocal" to floatArrayOf(-100f, -50f, 0f, 150f, 300f, 350f, 250f, 100f, 0f, -50f),
  "Rock" to floatArrayOf(250f, 200f, 100f, -50f, -100f, 0f, 150f, 200f, 250f, 300f),
  "Pop" to floatArrayOf(-50f, 100f, 200f, 250f, 200f, 0f, -50f, -50f, 100f, 150f),
  "Electronic" to floatArrayOf(350f, 300f, 150f, 0f, -100f, 100f, 150f, 200f, 250f, 300f),
  "Acoustic" to floatArrayOf(150f, 150f, 100f, 50f, 100f, 100f, 150f, 150f, 100f, 50f),
  "Classical" to floatArrayOf(250f, 200f, 150f, 100f, -50f, -50f, 0f, 100f, 150f, 200f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsScreenEntryPoint(
  navController: NavController? = null,
  eqViewModel: AxionEqViewModel = hiltViewModel()
) {
  val haptic = LocalHapticFeedback.current

  // Persisted Preferences (Fully connected to playback engine)
  val (spatialAudio, onSpatialAudioChange) = rememberPreference(SpatialAudioKey, defaultValue = false)
  val (dolbyAtmosEnabled, onDolbyAtmosChange) = rememberPreference(DolbyAtmosEnabledKey, defaultValue = true)
  val (spatialStrength, onSpatialStrengthChange) = rememberPreference(SpatialAudioStrengthKey, defaultValue = 1.4f)
  val (crossfeed, onCrossfeedChange) = rememberPreference(CrossfeedEnabledKey, defaultValue = false)

  val (bassBoost, onBassBoostChange) = rememberPreference(BassBoostKey, defaultValue = 0f)
  val (trebleBoost, onTrebleBoostChange) = rememberPreference(TrebleBoostKey, defaultValue = 0f)
  val (stereoBalance, onStereoBalanceChange) = rememberPreference(StereoBalanceKey, defaultValue = 0f)

  val (playbackSpeed, onPlaybackSpeedChange) = rememberPreference(PlaybackSpeedKey, defaultValue = 1.0f)
  val (pitchLock, onPitchLockChange) = rememberPreference(PlaybackPitchLockKey, defaultValue = true)

  val (audioNormalization, onAudioNormalizationChange) = rememberPreference(AudioNormalizationKey, defaultValue = true)
  val (loudnessPreset, onLoudnessPresetChange) = rememberEnumPreference(
    AudioLoudnessPresetKey,
    defaultValue = AudioLoudnessPreset.NORMAL
  )

  val (skipSilence, onSkipSilenceChange) = rememberPreference(SkipSilenceKey, defaultValue = false)
  val (skipSilenceInstant, onSkipSilenceInstantChange) = rememberPreference(SkipSilenceInstantKey, defaultValue = false)
  val (crossfadeEnabled, onCrossfadeEnabledChange) = rememberPreference(CrossfadeEnabledKey, defaultValue = false)
  val (crossfadeDuration, onCrossfadeDurationChange) = rememberPreference(CrossfadeDurationKey, defaultValue = 5f)

  // Axion EQ State
  val eqEnabled by eqViewModel.enabled.collectAsState()
  val bandGains by eqViewModel.bandGains.collectAsState()
  val customProfiles by eqViewModel.customProfiles.collectAsState()

  var activePresetName by remember { mutableStateOf("Flat") }
  var showSavePresetDialog by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf(false) }

  // Detect which preset matches current gains
  LaunchedEffect(bandGains) {
    val matching = PresetGainMap.entries.find { (_, gains) ->
      gains.indices.all { i -> abs(gains[i] - bandGains[i]) < 1f }
    }
    activePresetName = matching?.key ?: "Custom"
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground,
          navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
          actionIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
          Text(
            text = stringResource(R.string.effects),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp
            )
          )
        },
        navigationIcon = {
          if (navController != null && navController.previousBackStackEntry != null) {
            IconButton(
              onClick = { navController.popBackStack() },
              onLongClick = {}
            ) {
              Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier.size(22.dp)
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { showInfoDialog = true },
            onLongClick = {}
          ) {
            Icon(
              painter = painterResource(R.drawable.info),
              contentDescription = "Audio Info",
              modifier = Modifier.size(20.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = {
              haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
              onSpatialAudioChange(false)
              onSpatialStrengthChange(1.4f)
              onCrossfeedChange(false)
              onBassBoostChange(0f)
              onTrebleBoostChange(0f)
              onStereoBalanceChange(0f)
              onPlaybackSpeedChange(1.0f)
              onPitchLockChange(true)
              onAudioNormalizationChange(true)
              onSkipSilenceChange(false)
              onSkipSilenceInstantChange(false)
              onCrossfadeEnabledChange(false)
              eqViewModel.setEnabled(false)
              eqViewModel.reset()
            },
            onLongClick = {}
          ) {
            Icon(
              painter = painterResource(R.drawable.refresh),
              contentDescription = "Reset All",
              modifier = Modifier.size(20.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(innerPadding)
        .windowInsetsPadding(
          LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
        )
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // ==========================================
      // SECTION 1: SPATIAL & SURROUND SOUND
      // ==========================================
      EffectSectionGroup(title = "Spatial & Surround Sound") {
        FeatureSwitchRow(
          title = "Spatial Audio (360° Sound)",
          subtitle = "Expands stereo soundstage for spatial depth",
          checked = spatialAudio,
          onCheckedChange = onSpatialAudioChange,
          badge = if (spatialAudio) "${String.format("%.1f", spatialStrength)}x" else null
        )

        AnimatedVisibility(
          visible = spatialAudio,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Soundstage Width",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format("%.2f", spatialStrength)}x",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }

            ResponsiveSlider(
              value = spatialStrength,
              onValueChange = onSpatialStrengthChange,
              valueRange = 1.0f..2.0f,
              steps = 19
            )
          }
        }

        FeatureDivider()

        FeatureSwitchRow(
          title = "Headphone Crossfeed",
          subtitle = "Blends L/R channels to reduce listening fatigue",
          checked = crossfeed,
          onCheckedChange = onCrossfeedChange
        )
      }

      // ==========================================
      // SECTION 2: GRAPHIC EQUALIZER & TONE
      // ==========================================
      EffectSectionGroup(title = "Equalizer & Tone") {
        FeatureSwitchRow(
          title = "Dolby Atmos",
          subtitle = "Native playback of immersive spatial audio",
          checked = dolbyAtmosEnabled,
          onCheckedChange = onDolbyAtmosChange
        )

        FeatureSwitchRow(
          title = "10-Band Graphic Equalizer",
          subtitle = "Biquad peaking filter engine with custom presets",
          checked = eqEnabled,
          onCheckedChange = { eqViewModel.setEnabled(it) },
          badge = if (eqEnabled) activePresetName else "OFF"
        )

        AnimatedVisibility(
          visible = eqEnabled,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Preset Chips Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              PresetGainMap.forEach { (name, presetGains) ->
                val isSelected = activePresetName == name
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    eqViewModel.setBandsGains(presetGains.copyOf(), fromUser = true)
                    activePresetName = name
                  },
                  label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                )
              }

              customProfiles.forEach { profile ->
                val isSelected = activePresetName == profile.name
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    val newGains = FloatArray(10) { i ->
                      val b = profile.bands.getOrNull(i)
                      ((b?.gain ?: 0.0) * 50.0).toFloat()
                    }
                    eqViewModel.setBandsGains(newGains, fromUser = true)
                    activePresetName = profile.name
                  },
                  label = { Text(profile.name, style = MaterialTheme.typography.labelSmall) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 10-Band Interactive EQ Board
            EqualizerInteractiveBoard(
              bandGains = bandGains,
              onGainChanged = { bandIndex, gain ->
                eqViewModel.setBandGain(bandIndex, gain)
              }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Reset & Save
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = {
                  eqViewModel.reset()
                  activePresetName = "Flat"
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.refresh),
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flat Curve", style = MaterialTheme.typography.labelSmall)
              }

              OutlinedButton(
                onClick = { showSavePresetDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.add),
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Preset", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }

        FeatureDivider()

        // Bass Boost
        val bassActive = bassBoost > 0.05f
        FeatureSwitchRow(
          title = "Bass Boost",
          subtitle = "Enhance low-end frequencies",
          checked = bassActive,
          onCheckedChange = { active ->
            val newBass = if (active) 6.0f else 0f
            onBassBoostChange(newBass)
            applyBassBoostToEq(newBass, eqEnabled, bandGains, eqViewModel)
          },
          badge = if (bassActive) "+${String.format("%.1f", bassBoost)} dB" else null
        )

        AnimatedVisibility(
          visible = bassActive,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            ResponsiveSlider(
              value = bassBoost,
              onValueChange = { newVal ->
                onBassBoostChange(newVal)
                applyBassBoostToEq(newVal, eqEnabled, bandGains, eqViewModel)
              },
              valueRange = 0f..15f,
              steps = 14
            )
          }
        }

        FeatureDivider()

        // Vocal & Treble Clarity
        val trebleActive = trebleBoost > 0.05f
        FeatureSwitchRow(
          title = "Vocal & Treble Clarity",
          subtitle = "Boost high-range presence and vocal crispness",
          checked = trebleActive,
          onCheckedChange = { active ->
            val newTreb = if (active) 4.0f else 0f
            onTrebleBoostChange(newTreb)
            applyTrebleBoostToEq(newTreb, eqEnabled, bandGains, eqViewModel)
          },
          badge = if (trebleActive) "+${String.format("%.1f", trebleBoost)} dB" else null
        )

        AnimatedVisibility(
          visible = trebleActive,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            ResponsiveSlider(
              value = trebleBoost,
              onValueChange = { newVal ->
                onTrebleBoostChange(newVal)
                applyTrebleBoostToEq(newVal, eqEnabled, bandGains, eqViewModel)
              },
              valueRange = 0f..12f,
              steps = 11
            )
          }
        }
      }

      // ==========================================
      // SECTION 3: PLAYBACK & STEREO BALANCE
      // ==========================================
      EffectSectionGroup(title = "Playback & Stereo Balance") {
        // Stereo Channel Balance
        val balanceActive = abs(stereoBalance) > 1f
        FeatureSwitchRow(
          title = "Stereo Channel Balance",
          subtitle = "Adjust audio volume between Left & Right channels",
          checked = balanceActive,
          onCheckedChange = { active ->
            onStereoBalanceChange(if (active) -15f else 0f)
          },
          badge = if (balanceActive) {
            when {
              stereoBalance < -1f -> "L ${(-stereoBalance).roundToInt()}%"
              stereoBalance > 1f -> "R ${stereoBalance.roundToInt()}%"
              else -> "CENTER"
            }
          } else null
        )

        AnimatedVisibility(
          visible = balanceActive,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Left (L)", style = MaterialTheme.typography.labelSmall)
              Text(
                text = if (abs(stereoBalance) < 1f) "Centered" else if (stereoBalance < 0) "L ${(-stereoBalance).roundToInt()}%" else "R ${stereoBalance.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text("Right (R)", style = MaterialTheme.typography.labelSmall)
            }

            ResponsiveSlider(
              value = stereoBalance,
              onValueChange = onStereoBalanceChange,
              valueRange = -50f..50f,
              steps = 19
            )
          }
        }

        FeatureDivider()

        // Playback Speed & Pitch
        val speedActive = abs(playbackSpeed - 1.0f) > 0.02f
        FeatureSwitchRow(
          title = "Playback Speed & Pitch",
          subtitle = "Adjust tempo and optionally maintain pitch",
          checked = speedActive,
          onCheckedChange = { active ->
            onPlaybackSpeedChange(if (active) 1.25f else 1.0f)
          },
          badge = if (speedActive) "${String.format("%.2f", playbackSpeed)}x" else null
        )

        AnimatedVisibility(
          visible = speedActive,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Speed: ${String.format("%.2f", playbackSpeed)}x",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              FilterChip(
                selected = pitchLock,
                onClick = { onPitchLockChange(!pitchLock) },
                label = {
                  Text(
                    text = if (pitchLock) "Pitch Locked" else "Shift Pitch",
                    style = MaterialTheme.typography.labelSmall
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }

            ResponsiveSlider(
              value = playbackSpeed,
              onValueChange = onPlaybackSpeedChange,
              valueRange = 0.5f..2.0f,
              steps = 29
            )

            // Speed Preset Pills
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              listOf(0.75f, 0.90f, 1.0f, 1.10f, 1.25f, 1.5f).forEach { rate ->
                val isSelected = abs(playbackSpeed - rate) < 0.03f
                TextButton(
                  onClick = { onPlaybackSpeedChange(rate) },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${rate}x",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }
              }
            }
          }
        }
      }

      // ==========================================
      // SECTION 4: VOLUME & DYNAMICS
      // ==========================================
      EffectSectionGroup(title = "Volume & Dynamics") {
        FeatureSwitchRow(
          title = "Loudness Normalization",
          subtitle = "Prevent volume jumps between songs",
          checked = audioNormalization,
          onCheckedChange = onAudioNormalizationChange,
          badge = if (audioNormalization) loudnessPreset.name else null
        )

        AnimatedVisibility(
          visible = audioNormalization,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            AudioLoudnessPreset.entries.forEach { preset ->
              val selected = loudnessPreset == preset
              FilterChip(
                selected = selected,
                onClick = { onLoudnessPresetChange(preset) },
                label = {
                  Text(
                    text = preset.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }
          }
        }
      }

      // ==========================================
      // SECTION 5: TRANSITIONS & FLOW
      // ==========================================
      EffectSectionGroup(title = "Transitions & Flow") {
        FeatureSwitchRow(
          title = "Skip Silence",
          subtitle = "Automatically trim silent gaps during playback",
          checked = skipSilence,
          onCheckedChange = onSkipSilenceChange
        )

        AnimatedVisibility(
          visible = skipSilence,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column {
            FeatureDivider()
            FeatureSwitchRow(
              title = "Instant Skip",
              subtitle = "Jump directly to audio onset without fade",
              checked = skipSilenceInstant,
              onCheckedChange = onSkipSilenceInstantChange
            )
          }
        }

        FeatureDivider()

        FeatureSwitchRow(
          title = "Crossfade",
          subtitle = "Smoothly blend playback transitions between tracks",
          checked = crossfadeEnabled,
          onCheckedChange = onCrossfadeEnabledChange,
          badge = if (crossfadeEnabled) "${crossfadeDuration.toInt()}s" else null
        )

        AnimatedVisibility(
          visible = crossfadeEnabled,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Crossfade Duration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${crossfadeDuration.toInt()} sec",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }

            ResponsiveSlider(
              value = crossfadeDuration,
              onValueChange = onCrossfadeDurationChange,
              valueRange = 1f..15f,
              steps = 13
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(140.dp))
    }
  }

  // Dialog for saving custom EQ preset
  if (showSavePresetDialog) {
    SaveCustomPresetDialog(
      onDismiss = { showSavePresetDialog = false },
      onSave = { name ->
        eqViewModel.saveCustomProfile(name)
        activePresetName = name
        showSavePresetDialog = false
      }
    )
  }

  // Dialog for Audio Info
  if (showInfoDialog) {
    DefaultDialog(
      onDismiss = { showInfoDialog = false },
      title = { Text("Audio Processing Engine") },
      buttons = {
        TextButton(onClick = { showInfoDialog = false }) {
          Text("Got it")
        }
      }
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          "• Spatial Audio: Expands stereo mid-side soundstage for 3D immersion without vocal distortion.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          "• Headphone Crossfeed: Blends L/R channels to simulate speaker room acoustics.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          "• Graphic EQ: 10-band biquad peaking filters with precision decibel scaling.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          "• Stereo Balance: Precision channel level control for left/right adjustment.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          "• Playback Speed: Native ExoPlayer audio rate and pitch scaling.",
          style = MaterialTheme.typography.bodySmall
        )
      }
    }
  }
}

private fun applyBassBoostToEq(
  boostDb: Float,
  eqEnabled: Boolean,
  bandGains: FloatArray,
  eqViewModel: AxionEqViewModel
) {
  if (eqEnabled) {
    val updated = bandGains.copyOf()
    updated[0] = boostDb * 25f
    updated[1] = boostDb * 20f
    updated[2] = boostDb * 12f
    eqViewModel.setBandsGains(updated, fromUser = true)
  }
}

private fun applyTrebleBoostToEq(
  boostDb: Float,
  eqEnabled: Boolean,
  bandGains: FloatArray,
  eqViewModel: AxionEqViewModel
) {
  if (eqEnabled) {
    val updated = bandGains.copyOf()
    updated[8] = boostDb * 25f
    updated[9] = boostDb * 25f
    eqViewModel.setBandsGains(updated, fromUser = true)
  }
}

/**
 * Standard Echo Music Group Container Card
 */
@Composable
private fun EffectSectionGroup(
  title: String,
  content: @Composable () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = title.uppercase(),
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold
      ),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        content()
      }
    }
  }
}

@Composable
private fun FeatureDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 2.dp)
      .height(0.5.dp)
      .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
  )
}

/**
 * Clean Feature Switch Row
 */
@Composable
private fun FeatureSwitchRow(
  title: String,
  subtitle: String? = null,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  badge: String? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onCheckedChange(!checked) }
      .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
      )

      if (badge != null) {
        Spacer(modifier = Modifier.height(3.dp))
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      thumbContent = {
        Icon(
          painter = painterResource(if (checked) R.drawable.check else R.drawable.close),
          contentDescription = null,
          modifier = Modifier.size(SwitchDefaults.IconSize)
        )
      }
    )
  }
}

/**
 * Native Vertical EQ Band Board
 */
@Composable
private fun EqualizerInteractiveBoard(
  bandGains: FloatArray,
  onGainChanged: (Int, Float) -> Unit
) {
  val haptic = LocalHapticFeedback.current

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BandFrequencies.forEachIndexed { index, freqLabel ->
          val rawGain = bandGains.getOrElse(index) { 0f }
          val dbGain = rawGain / 50f

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(42.dp)
          ) {
            Text(
              text = if (dbGain > 0) "+${dbGain.roundToInt()}" else "${dbGain.roundToInt()}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = if (abs(dbGain) > 0.5f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            )

            Spacer(modifier = Modifier.height(4.dp))

            InteractiveVerticalEqBar(
              dbGain = dbGain,
              onDbChanged = { newDb ->
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onGainChanged(index, newDb * 50f)
              }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = freqLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              ),
              textAlign = TextAlign.Center,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

/**
 * Interactive Vertical EQ Track with Thumb & Fill
 */
@Composable
private fun InteractiveVerticalEqBar(
  dbGain: Float,
  onDbChanged: (Float) -> Unit
) {
  val totalHeightDp = 100.dp
  val minDb = -12f
  val maxDb = 12f

  val normalized = ((dbGain - minDb) / (maxDb - minDb)).coerceIn(0f, 1f)

  Box(
    modifier = Modifier
      .height(totalHeightDp)
      .width(26.dp)
      .pointerInput(Unit) {
        detectVerticalDragGestures { change, _ ->
          change.consume()
          val currentY = change.position.y
          val heightPx = size.height.toFloat()
          if (heightPx > 0) {
            val fraction = 1f - (currentY / heightPx).coerceIn(0f, 1f)
            val newDb = minDb + fraction * (maxDb - minDb)
            val snappedDb = if (abs(newDb) < 0.5f) 0f else newDb.roundToInt().toFloat()
            onDbChanged(snappedDb)
          }
        }
      },
    contentAlignment = Alignment.BottomCenter
  ) {
    Box(
      modifier = Modifier
        .width(6.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(3.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    )

    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .width(14.dp)
        .height(2.dp)
        .clip(RoundedCornerShape(1.dp))
        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    )

    val isPositive = dbGain >= 0f
    Box(
      modifier = Modifier
        .align(if (isPositive) Alignment.Center else Alignment.BottomCenter)
        .width(6.dp)
        .fillMaxHeight(fraction = if (isPositive) (normalized - 0.5f).coerceAtLeast(0f) else (0.5f - normalized).coerceAtLeast(0f))
        .clip(RoundedCornerShape(3.dp))
        .background(MaterialTheme.colorScheme.primary)
    )

    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = (normalized * 84).dp)
        .size(16.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary)
    )
  }
}

@Composable
private fun ResponsiveSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  valueRange: ClosedFloatingPointRange<Float>,
  steps: Int = 0
) {
  Slider(
    value = value,
    onValueChange = onValueChange,
    valueRange = valueRange,
    steps = steps,
    modifier = Modifier.fillMaxWidth(),
    colors = SliderDefaults.colors(
      thumbColor = MaterialTheme.colorScheme.primary,
      activeTrackColor = MaterialTheme.colorScheme.primary,
      inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )
  )
}

@Composable
private fun SaveCustomPresetDialog(
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var presetName by remember { mutableStateOf("") }

  DefaultDialog(
    onDismiss = onDismiss,
    title = { Text("Save Equalizer Preset") },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
      TextButton(
        onClick = {
          if (presetName.isNotBlank()) {
            onSave(presetName.trim())
          }
        },
        enabled = presetName.isNotBlank()
      ) {
        Text("Save")
      }
    }
  ) {
    Column {
      Text(
        text = "Enter a name for your custom EQ curve:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = presetName,
        onValueChange = { presetName = it },
        label = { Text("Preset Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
