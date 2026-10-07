package com.music.sonic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.sonic.BuildConfig
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.constants.AudioOffload
import com.music.sonic.constants.AudioQuality
import com.music.sonic.constants.AudioQualityKey
import com.music.sonic.constants.AutoDownloadOnLikeKey
import com.music.sonic.constants.AutoLoadMoreKey
import com.music.sonic.constants.AutoSkipNextOnErrorKey
import com.music.sonic.constants.DisableLoadMoreWhenRepeatAllKey
import com.music.sonic.constants.DownloadOnWifiOnlyKey
import com.music.sonic.constants.EnableExportAsMp3Key
import com.music.sonic.constants.EnableGoogleCastKey
import com.music.sonic.constants.HistoryDuration
import com.music.sonic.constants.KeepScreenOn
import com.music.sonic.constants.PauseOnMute
import com.music.sonic.constants.PersistentQueueKey
import com.music.sonic.constants.PersistentShuffleAcrossQueuesKey
import com.music.sonic.constants.PreloadLyricsEnabledKey
import com.music.sonic.constants.PreloadNextSongEnabledKey
import com.music.sonic.constants.PreloadNextSongLimitKey
import com.music.sonic.constants.PreventDuplicateTracksInQueueKey
import com.music.sonic.constants.RememberShuffleAndRepeatKey
import com.music.sonic.constants.ResumeOnBluetoothConnectKey
import com.music.sonic.constants.SeekExtraSeconds
import com.music.sonic.constants.ShufflePlaylistFirstKey
import com.music.sonic.constants.SimilarContent
import com.music.sonic.constants.StopMusicOnTaskClearKey
import com.music.sonic.ui.component.EnumDialog
import com.music.sonic.ui.component.IconButton
import com.music.sonic.ui.component.Material3SettingsGroup
import com.music.sonic.ui.component.Material3SettingsItem
import com.music.sonic.ui.utils.backToMain
import com.music.sonic.utils.rememberEnumPreference
import com.music.sonic.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  highlightKey: String? = null
) {
  val scrollState = rememberScrollState()

  val (audioQuality, onAudioQualityChange) =
    rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.OPUS)

  val (persistentQueue, onPersistentQueueChange) =
    rememberPreference(PersistentQueueKey, defaultValue = true)

  val (audioOffload, onAudioOffloadChange) =
    rememberPreference(key = AudioOffload, defaultValue = false)

  val (preloadNextSongEnabled, onPreloadNextSongEnabledChange) =
    rememberPreference(key = PreloadNextSongEnabledKey, defaultValue = true)

  val (preloadNextSongLimit, onPreloadNextSongLimitChange) =
    rememberPreference(key = PreloadNextSongLimitKey, defaultValue = 10)

  val (preloadLyricsEnabled, onPreloadLyricsEnabledChange) =
    rememberPreference(key = PreloadLyricsEnabledKey, defaultValue = true)

  val (dataSaverEnabled, onDataSaverEnabledChange) =
    rememberPreference(key = com.music.sonic.constants.DataSaverEnabledKey, defaultValue = false)

  val (enableExportAsMp3, onEnableExportAsMp3Change) =
    rememberPreference(key = EnableExportAsMp3Key, defaultValue = false)

  val (enableGoogleCast, onEnableGoogleCastChange) =
    rememberPreference(key = EnableGoogleCastKey, defaultValue = true)

  val (seekExtraSeconds, onSeekExtraSeconds) =
    rememberPreference(SeekExtraSeconds, defaultValue = false)

  val (autoLoadMore, onAutoLoadMoreChange) =
    rememberPreference(AutoLoadMoreKey, defaultValue = true)
  val (disableLoadMoreWhenRepeatAll, onDisableLoadMoreWhenRepeatAllChange) =
    rememberPreference(DisableLoadMoreWhenRepeatAllKey, defaultValue = false)
  val (autoDownloadOnLike, onAutoDownloadOnLikeChange) =
    rememberPreference(AutoDownloadOnLikeKey, defaultValue = false)
  val (downloadOnWifiOnly, onDownloadOnWifiOnlyChange) =
    rememberPreference(DownloadOnWifiOnlyKey, defaultValue = false)
  val (similarContentEnabled, similarContentEnabledChange) =
    rememberPreference(key = SimilarContent, defaultValue = true)
  val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) =
    rememberPreference(AutoSkipNextOnErrorKey, defaultValue = false)
  val (persistentShuffleAcrossQueues, onPersistentShuffleAcrossQueuesChange) =
    rememberPreference(PersistentShuffleAcrossQueuesKey, defaultValue = false)
  val (rememberShuffleAndRepeat, onRememberShuffleAndRepeatChange) =
    rememberPreference(RememberShuffleAndRepeatKey, defaultValue = true)
  val (shufflePlaylistFirst, onShufflePlaylistFirstChange) =
    rememberPreference(ShufflePlaylistFirstKey, defaultValue = false)
  val (preventDuplicateTracksInQueue, onPreventDuplicateTracksInQueueChange) =
    rememberPreference(PreventDuplicateTracksInQueueKey, defaultValue = false)
  val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) =
    rememberPreference(StopMusicOnTaskClearKey, defaultValue = true)
  val (pauseOnMute, onPauseOnMuteChange) = rememberPreference(PauseOnMute, defaultValue = false)
  val (resumeOnBluetoothConnect, onResumeOnBluetoothConnectChange) =
    rememberPreference(ResumeOnBluetoothConnectKey, defaultValue = false)
  val (keepScreenOn, onKeepScreenOnChange) = rememberPreference(KeepScreenOn, defaultValue = false)
  val (historyDuration, onHistoryDurationChange) =
    rememberPreference(HistoryDuration, defaultValue = 1f)

  var showAudioQualityDialog by remember { mutableStateOf(false) }
  var showDownloadQualityDialog by remember { mutableStateOf(false) }
  var showPlaybackEngineDialog by remember { mutableStateOf(false) }

  val (playbackEngine, onPlaybackEngineChange) =
    rememberEnumPreference(
      com.music.sonic.constants.PlaybackEngineKey,
      defaultValue = com.music.sonic.constants.PlaybackEngine.AUTO
    )

  val (downloadQuality, onDownloadQualityChange) =
    rememberEnumPreference(
      com.music.sonic.constants.DownloadQualityKey,
      defaultValue = com.music.sonic.constants.DownloadQuality.YOUTUBE
    )

  if (showAudioQualityDialog) {
    EnumDialog(
      onDismiss = { showAudioQualityDialog = false },
      onSelect = {
        onAudioQualityChange(it)
        showAudioQualityDialog = false
      },
      title = stringResource(R.string.audio_quality),
      current = audioQuality,
      values = listOf(AudioQuality.OPUS),
      valueText = {
        when (it) {
          AudioQuality.OPUS -> "Opus"
          else -> ""
        }
      },
      valueDescription = { "" }
    )
  }

  if (showDownloadQualityDialog) {
    EnumDialog(
      onDismiss = { showDownloadQualityDialog = false },
      onSelect = {
        onDownloadQualityChange(it)
        showDownloadQualityDialog = false
      },
      title = stringResource(R.string.download_quality_title),
      current = downloadQuality,
      values = listOf(com.music.sonic.constants.DownloadQuality.YOUTUBE),
      valueText = {
        when (it) {
          com.music.sonic.constants.DownloadQuality.YOUTUBE -> "YouTube Music (AAC/Default)"
          else -> ""
        }
      }
    )
  }

  if (showPlaybackEngineDialog) {
    EnumDialog(
      onDismiss = { showPlaybackEngineDialog = false },
      onSelect = {
        onPlaybackEngineChange(it)
        com.music.sonic.utils.YTPlayerUtils.playbackEngine = it
        showPlaybackEngineDialog = false
      },
      title = "Playback Engine",
      current = playbackEngine,
      values =
        listOf(
          com.music.sonic.constants.PlaybackEngine.POTOKEN,
          com.music.sonic.constants.PlaybackEngine.BRAVEPIPE,
          com.music.sonic.constants.PlaybackEngine.AUTO
        ),
      valueText = {
        when (it) {
          com.music.sonic.constants.PlaybackEngine.POTOKEN -> "PoToken (Recommended)"
          com.music.sonic.constants.PlaybackEngine.BRAVEPIPE -> "BravePipe (NewPipe)"
          com.music.sonic.constants.PlaybackEngine.AUTO -> "Auto (Try Both)"
        }
      },
      valueDescription = {
        when (it) {
          com.music.sonic.constants.PlaybackEngine.POTOKEN ->
            "Uses WebView PoToken + CipherDeobfuscator. Most reliable and future-proof."
          com.music.sonic.constants.PlaybackEngine.BRAVEPIPE ->
            "Uses NewPipe extractor for stream resolution. Lightweight but may break with YouTube updates."
          com.music.sonic.constants.PlaybackEngine.AUTO ->
            "Tries PoToken first, falls back to BravePipe if it fails."
        }
      }
    )
  }

  Column(
    Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
      )
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
  ) {
    Spacer(
      Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
    )

    Spacer(modifier = Modifier.height(16.dp))

    Material3SettingsGroup(
      scrollState = scrollState,
      title = "Data Saver",
      items =
        buildList {
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Data Saver Mode (Beta)"),
              icon = painterResource(R.drawable.offline),
              title = { Text("Data Saver Mode (Beta)") },
              description = {
                Text(
                  "Disable lyrics, videos, and preloading to save data"
                )
              },
              trailingContent = {
                Switch(
                  checked = dataSaverEnabled,
                  onCheckedChange = onDataSaverEnabledChange,
                  thumbContent = {
                    Icon(
                      painter =
                        painterResource(
                          id = if (dataSaverEnabled) R.drawable.check else R.drawable.close
                        ),
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                  }
                )
              },
              onClick = { onDataSaverEnabledChange(!dataSaverEnabled) }
            )
          )
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.player),
      items =
        buildList {
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_quality)),
              icon = painterResource(R.drawable.graphic_eq),
              title = { Text(stringResource(R.string.audio_quality)) },
              description = {
                Text(
                  when (audioQuality) {
                    AudioQuality.OPUS -> "Opus"
                    else -> "Opus"
                  }
                )
              },
              onClick = null
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.download_quality_title)),
              icon = painterResource(R.drawable.download),
              title = { Text(stringResource(R.string.download_quality_title)) },
              description = {
                Text(
                  when (downloadQuality) {
                    com.music.sonic.constants.DownloadQuality.YOUTUBE ->
                      "YouTube Music (AAC/Default)"
                    else -> "YouTube Music (AAC/Default)"
                  }
                )
              },
              onClick = { showDownloadQualityDialog = true }
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.download_on_wifi_only)),
              icon = painterResource(R.drawable.download),
              title = { Text(stringResource(R.string.download_on_wifi_only)) },
              description = { Text(stringResource(R.string.download_on_wifi_only_desc)) },
              trailingContent = {
                Switch(
                  checked = downloadOnWifiOnly,
                  onCheckedChange = onDownloadOnWifiOnlyChange,
                  thumbContent = {
                    Icon(
                      painter =
                        painterResource(
                          id = if (downloadOnWifiOnly) R.drawable.check else R.drawable.close
                        ),
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                  }
                )
              },
              onClick = { onDownloadOnWifiOnlyChange(!downloadOnWifiOnly) }
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = false,
              icon = painterResource(R.drawable.tune),
              title = { Text("Playback Engine") },
              description = {
                Text(
                  when (playbackEngine) {
                    com.music.sonic.constants.PlaybackEngine.POTOKEN -> "PoToken (Recommended)"
                    com.music.sonic.constants.PlaybackEngine.BRAVEPIPE -> "BravePipe (NewPipe)"
                    com.music.sonic.constants.PlaybackEngine.AUTO -> "Auto (Try Both)"
                  }
                )
              },
              onClick = { showPlaybackEngineDialog = true }
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.history_duration)),
              icon = painterResource(R.drawable.history),
              title = { Text(stringResource(R.string.history_duration)) },
              description = {
                Slider(
                  value = historyDuration,
                  onValueChange = { onHistoryDurationChange(it.roundToInt().toFloat()) },
                  valueRange = 1f..100f,
                  steps = 9
                )
              },
              trailingContent = { Text(text = historyDuration.roundToInt().toString()) }
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.audio_offload)),
              icon = painterResource(R.drawable.graphic_eq),
              title = { Text(stringResource(R.string.audio_offload)) },
              description = { Text(stringResource(R.string.audio_offload_description)) },
              trailingContent = {
                Switch(
                  checked = audioOffload,
                  onCheckedChange = onAudioOffloadChange,
                  thumbContent = {
                    Icon(
                      painter =
                        painterResource(
                          id = if (audioOffload) R.drawable.check else R.drawable.close
                        ),
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                  }
                )
              },
              onClick = { onAudioOffloadChange(!audioOffload) }
            )
          )

          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == "Preload Next Song"),
              icon = painterResource(R.drawable.skip_next),
              title = { Text("Preload Next Song") },
              description = { Text("Cache the next song for gapless playback") },
              trailingContent = {
                Switch(
                  checked = preloadNextSongEnabled,
                  onCheckedChange = onPreloadNextSongEnabledChange,
                  thumbContent = {
                    Icon(
                      painter =
                        painterResource(
                          id = if (preloadNextSongEnabled) R.drawable.check else R.drawable.close
                        ),
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                  }
                )
              },
              onClick = { onPreloadNextSongEnabledChange(!preloadNextSongEnabled) }
            )
          )

          if (preloadNextSongEnabled) {
            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == "Preload Limit"),
                icon = painterResource(R.drawable.library_music),
                title = { Text("Preload Limit") },
                description = {
                  Slider(
                    value = preloadNextSongLimit.toFloat(),
                    onValueChange = { onPreloadNextSongLimitChange(it.roundToInt()) },
                    valueRange = 1f..10f,
                    steps = 9
                  )
                },
                trailingContent = { Text(text = preloadNextSongLimit.toString()) }
              )
            )

            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == "Preload Lyrics"),
                icon = painterResource(R.drawable.queue_music),
                title = { Text("Preload Lyrics") },
                description = { Text("Also cache lyrics for the preloaded songs") },
                trailingContent = {
                  Switch(
                    checked = preloadLyricsEnabled,
                    onCheckedChange = onPreloadLyricsEnabledChange,
                    thumbContent = {
                      Icon(
                        painter =
                          painterResource(
                            id = if (preloadLyricsEnabled) R.drawable.check else R.drawable.close
                          ),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                      )
                    }
                  )
                },
                onClick = { onPreloadLyricsEnabledChange(!preloadLyricsEnabled) }
              )
            )
          }

          if (BuildConfig.CAST_AVAILABLE) {
            add(
              Material3SettingsItem(
                isHighlighted = (highlightKey == stringResource(R.string.google_cast)),
                icon = painterResource(R.drawable.cast),
                title = { Text(stringResource(R.string.google_cast)) },
                description = { Text(stringResource(R.string.google_cast_description)) },
                trailingContent = {
                  Switch(
                    checked = enableGoogleCast,
                    onCheckedChange = onEnableGoogleCastChange,
                    thumbContent = {
                      Icon(
                        painter =
                          painterResource(
                            id = if (enableGoogleCast) R.drawable.check else R.drawable.close
                          ),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                      )
                    }
                  )
                },
                onClick = { onEnableGoogleCastChange(!enableGoogleCast) }
              )
            )
          }
          add(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.seek_seconds_addup)),
              icon = painterResource(R.drawable.arrow_forward),
              title = { Text(stringResource(R.string.seek_seconds_addup)) },
              description = { Text(stringResource(R.string.seek_seconds_addup_description)) },
              trailingContent = {
                Switch(
                  checked = seekExtraSeconds,
                  onCheckedChange = onSeekExtraSeconds,
                  thumbContent = {
                    Icon(
                      painter =
                        painterResource(
                          id = if (seekExtraSeconds) R.drawable.check else R.drawable.close
                        ),
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                  }
                )
              },
              onClick = { onSeekExtraSeconds(!seekExtraSeconds) }
            )
          )
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.queue),
      items =
        listOf(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.persistent_queue)),
            icon = painterResource(R.drawable.queue_music),
            title = { Text(stringResource(R.string.persistent_queue)) },
            description = { Text(stringResource(R.string.persistent_queue_desc)) },
            trailingContent = {
              Switch(
                checked = persistentQueue,
                onCheckedChange = onPersistentQueueChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (persistentQueue) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onPersistentQueueChange(!persistentQueue) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_load_more)),
            icon = painterResource(R.drawable.playlist_add),
            title = { Text(stringResource(R.string.auto_load_more)) },
            description = { Text(stringResource(R.string.auto_load_more_desc)) },
            trailingContent = {
              Switch(
                checked = autoLoadMore,
                onCheckedChange = onAutoLoadMoreChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (autoLoadMore) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onAutoLoadMoreChange(!autoLoadMore) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_download_on_like)),
            icon = painterResource(R.drawable.download),
            title = { Text(stringResource(R.string.auto_download_on_like)) },
            description = { Text(stringResource(R.string.auto_download_on_like_desc)) },
            trailingContent = {
              Switch(
                checked = autoDownloadOnLike,
                onCheckedChange = onAutoDownloadOnLikeChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (autoDownloadOnLike) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onAutoDownloadOnLikeChange(!autoDownloadOnLike) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.enable_similar_content)),
            icon = painterResource(R.drawable.similar),
            title = { Text(stringResource(R.string.enable_similar_content)) },
            description = { Text(stringResource(R.string.similar_content_desc)) },
            trailingContent = {
              Switch(
                checked = similarContentEnabled,
                onCheckedChange = similarContentEnabledChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (similarContentEnabled) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { similarContentEnabledChange(!similarContentEnabled) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.persistent_shuffle_title)),
            icon = painterResource(R.drawable.shuffle),
            title = { Text(stringResource(R.string.persistent_shuffle_title)) },
            description = { Text(stringResource(R.string.persistent_shuffle_desc)) },
            trailingContent = {
              Switch(
                checked = persistentShuffleAcrossQueues,
                onCheckedChange = onPersistentShuffleAcrossQueuesChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id =
                          if (persistentShuffleAcrossQueues) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onPersistentShuffleAcrossQueuesChange(!persistentShuffleAcrossQueues) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.remember_shuffle_and_repeat)),
            icon = painterResource(R.drawable.shuffle),
            title = { Text(stringResource(R.string.remember_shuffle_and_repeat)) },
            description = { Text(stringResource(R.string.remember_shuffle_and_repeat_desc)) },
            trailingContent = {
              Switch(
                checked = rememberShuffleAndRepeat,
                onCheckedChange = onRememberShuffleAndRepeatChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (rememberShuffleAndRepeat) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onRememberShuffleAndRepeatChange(!rememberShuffleAndRepeat) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.shuffle_playlist_first)),
            icon = painterResource(R.drawable.shuffle),
            title = { Text(stringResource(R.string.shuffle_playlist_first)) },
            description = { Text(stringResource(R.string.shuffle_playlist_first_desc)) },
            trailingContent = {
              Switch(
                checked = shufflePlaylistFirst,
                onCheckedChange = onShufflePlaylistFirstChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (shufflePlaylistFirst) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onShufflePlaylistFirstChange(!shufflePlaylistFirst) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.prevent_duplicate_tracks_in_queue)),
            icon = painterResource(R.drawable.queue_music),
            title = { Text(stringResource(R.string.prevent_duplicate_tracks_in_queue)) },
            description = { Text(stringResource(R.string.prevent_duplicate_tracks_in_queue_desc)) },
            trailingContent = {
              Switch(
                checked = preventDuplicateTracksInQueue,
                onCheckedChange = onPreventDuplicateTracksInQueueChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id =
                          if (preventDuplicateTracksInQueue) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onPreventDuplicateTracksInQueueChange(!preventDuplicateTracksInQueue) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_skip_next_on_error)),
            icon = painterResource(R.drawable.skip_next),
            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
            description = { Text(stringResource(R.string.auto_skip_next_on_error_desc)) },
            trailingContent = {
              Switch(
                checked = autoSkipNextOnError,
                onCheckedChange = onAutoSkipNextOnErrorChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (autoSkipNextOnError) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onAutoSkipNextOnErrorChange(!autoSkipNextOnError) }
          )
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    Material3SettingsGroup(
      scrollState = scrollState,
      title = stringResource(R.string.misc),
      items =
        listOf(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.stop_music_on_task_clear)),
            icon = painterResource(R.drawable.clear_all),
            title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
            description = { Text(stringResource(R.string.stop_music_on_task_clear_desc)) },
            trailingContent = {
              Switch(
                checked = stopMusicOnTaskClear,
                onCheckedChange = onStopMusicOnTaskClearChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (stopMusicOnTaskClear) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onStopMusicOnTaskClearChange(!stopMusicOnTaskClear) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.pause_music_when_media_is_muted)),
            icon = painterResource(R.drawable.volume_off_pause),
            title = { Text(stringResource(R.string.pause_music_when_media_is_muted)) },
            description = { Text(stringResource(R.string.pause_music_when_media_is_muted_desc)) },
            trailingContent = {
              Switch(
                checked = pauseOnMute,
                onCheckedChange = onPauseOnMuteChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(id = if (pauseOnMute) R.drawable.check else R.drawable.close),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onPauseOnMuteChange(!pauseOnMute) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.resume_on_bluetooth_connect)),
            icon = painterResource(R.drawable.bluetooth),
            title = { Text(stringResource(R.string.resume_on_bluetooth_connect)) },
            description = { Text(stringResource(R.string.resume_on_bluetooth_connect_desc)) },
            trailingContent = {
              Switch(
                checked = resumeOnBluetoothConnect,
                onCheckedChange = onResumeOnBluetoothConnectChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (resumeOnBluetoothConnect) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onResumeOnBluetoothConnectChange(!resumeOnBluetoothConnect) }
          ),
          Material3SettingsItem(
            isHighlighted =
              (highlightKey == stringResource(R.string.keep_screen_on_when_player_is_expanded)),
            icon = painterResource(R.drawable.screenshot),
            title = { Text(stringResource(R.string.keep_screen_on_when_player_is_expanded)) },
            description = {
              Text(stringResource(R.string.keep_screen_on_when_player_is_expanded_desc))
            },
            trailingContent = {
              Switch(
                checked = keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (keepScreenOn) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onKeepScreenOnChange(!keepScreenOn) }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.export_desc)),
            icon = painterResource(R.drawable.file_export),
            title = { Text(stringResource(R.string.export_desc)) },
            description = { Text("Show 'Export as MP3' in menus") },
            trailingContent = {
              Switch(
                checked = enableExportAsMp3,
                onCheckedChange = onEnableExportAsMp3Change,
                thumbContent = {
                  Icon(
                    painter =
                      painterResource(
                        id = if (enableExportAsMp3) R.drawable.check else R.drawable.close
                      ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = { onEnableExportAsMp3Change(!enableExportAsMp3) }
          )
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    Spacer(
      Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
      )
    )
  }

  TopAppBar(
    title = { Text(stringResource(R.string.player_and_audio)) },
    navigationIcon = {
      IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
      }
    }
  )
}
