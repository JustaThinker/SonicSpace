package com.music.sonic.ui.player

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.sonic.LocalListenTogetherManager
import com.music.sonic.LocalPlayerConnection
import com.music.sonic.R
import com.music.sonic.constants.ListItemHeight
import com.music.sonic.constants.PlayerBackgroundStyle
import com.music.sonic.constants.QueueEditLockKey
import com.music.sonic.constants.ShowCommentButtonKey
import com.music.sonic.constants.UseNewPlayerDesignKey
import com.music.sonic.echomusic.AudioDeviceBottomSheet
import com.music.sonic.echomusic.isBluetoothHeadphoneConnected
import com.music.sonic.extensions.metadata
import com.music.sonic.extensions.move
import com.music.sonic.extensions.toggleRepeatMode
import com.music.sonic.models.MediaMetadata
import com.music.sonic.ui.component.ActionPromptDialog
import com.music.sonic.ui.component.BottomSheet
import com.music.sonic.ui.component.BottomSheetState
import com.music.sonic.ui.component.GlassComponent
import com.music.sonic.ui.component.LocalBottomSheetPageState
import com.music.sonic.ui.component.LocalGlassEffectConfig
import com.music.sonic.ui.component.LocalMenuState
import com.music.sonic.ui.component.isGlassSupported
import com.music.sonic.ui.component.liquidGlass
import com.music.sonic.ui.component.resolveSurfaceTintColor
import com.music.sonic.ui.component.MediaMetadataListItem
import com.music.sonic.ui.menu.PlayerMenu
import com.music.sonic.ui.menu.QueueMenu
import com.music.sonic.ui.menu.SelectionMediaMetadataMenu
import com.music.sonic.ui.screens.CommentSheet
import com.music.sonic.ui.utils.ShowMediaInfo
import com.music.sonic.utils.listItemShape
import com.music.sonic.utils.makeTimeString
import com.music.sonic.utils.rememberPreference
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@SuppressLint("UnrememberedMutableState")
@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun Queue(
  state: BottomSheetState,
  playerBottomSheetState: BottomSheetState,
  navController: NavController,
  modifier: Modifier = Modifier,
  background: Color,
  onBackgroundColor: Color,
  TextBackgroundColor: Color,
  textButtonColor: Color,
  iconButtonColor: Color,
  pureBlack: Boolean,
  showInlineLyrics: Boolean,
  playerBackground: PlayerBackgroundStyle = PlayerBackgroundStyle.DEFAULT,
  onToggleLyrics: () -> Unit = {},
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val clipboardManager = LocalClipboard.current
  val menuState = LocalMenuState.current
  val bottomSheetPageState = LocalBottomSheetPageState.current
  var showAudioDeviceBottomSheet by remember { mutableStateOf(false) }

  val isBluetoothConnected by
    produceState(initialValue = isBluetoothHeadphoneConnected(context)) {
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

      val receiver =
        object : BroadcastReceiver() {
          override fun onReceive(context: Context, intent: Intent) {
            value = isBluetoothHeadphoneConnected(context)
          }
        }

      val callback =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          object : android.media.AudioDeviceCallback() {
            override fun onAudioDevicesAdded(
              addedDevices: Array<out android.media.AudioDeviceInfo>?
            ) {
              value = isBluetoothHeadphoneConnected(context)
            }

            override fun onAudioDevicesRemoved(
              removedDevices: Array<out android.media.AudioDeviceInfo>?
            ) {
              value = isBluetoothHeadphoneConnected(context)
            }
          }
        } else null

      val filter =
        IntentFilter().apply {
          addAction(AudioManager.ACTION_HEADSET_PLUG)
          addAction("android.bluetooth.adapter.action.STATE_CHANGED")
          addAction("android.bluetooth.device.action.ACL_CONNECTED")
          addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
          addAction("android.media.AUDIO_BECOMING_NOISY")
        }

      context.registerReceiver(receiver, filter)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
      }

      awaitDispose {
        context.unregisterReceiver(receiver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
          audioManager.unregisterAudioDeviceCallback(callback)
        }
      }
    }

  val listenTogetherManager = LocalListenTogetherManager.current
  val isListenTogetherGuest by
    listenTogetherManager?.guestPlaybackRestricted?.collectAsState(initial = false)
      ?: remember { mutableStateOf(false) }

  val playerConnection = LocalPlayerConnection.current ?: return
  val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
  val repeatMode by playerConnection.repeatMode.collectAsState()

  val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
  val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
  val currentSong by playerConnection.currentSong.collectAsState(initial = null)
  val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

  val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

  val selectedSongs = remember { mutableStateListOf<MediaMetadata>() }
  val selectedItems = remember { mutableStateListOf<Timeline.Window>() }

  val castHandler =
    remember(playerConnection) {
      try {
        playerConnection.service.castConnectionHandler
      } catch (e: Exception) {
        null
      }
    }
  val isCasting by castHandler?.isCasting?.collectAsState() ?: remember { mutableStateOf(false) }
  val castIsPlaying by
    castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }

  var inSelectMode by rememberSaveable { mutableStateOf(false) }
  val selection =
    rememberSaveable(
      saver =
        listSaver<MutableList<String>, String>(
          save = { it.toList() },
          restore = { it.toMutableStateList() }
        )
    ) {
      mutableStateListOf()
    }
  val onExitSelectionMode = {
    inSelectMode = false
    selection.clear()
  }
  if (inSelectMode) {
    BackHandler(onBack = onExitSelectionMode)
  }

  var locked by rememberPreference(QueueEditLockKey, defaultValue = false)

  val (useNewPlayerDesign, onUseNewPlayerDesignChange) =
    rememberPreference(UseNewPlayerDesignKey, defaultValue = true)
  val (showCommentButton) = rememberPreference(ShowCommentButtonKey, defaultValue = false)

  val snackbarHostState = remember { SnackbarHostState() }
  var dismissJob: Job? by remember { mutableStateOf(null) }

  var showSleepTimerDialog by remember { mutableStateOf(false) }
  var sleepTimerValue by remember { mutableFloatStateOf(30f) }
  val sleepTimerEnabled =
    remember(
      playerConnection.service.sleepTimer.triggerTime,
      playerConnection.service.sleepTimer.pauseWhenSongEnd
    ) {
      playerConnection.service.sleepTimer.isActive
    }
  var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }
  var showCommentSheet by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(sleepTimerEnabled) {
    if (sleepTimerEnabled) {
      while (isActive) {
        sleepTimerTimeLeft =
          if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
            playerConnection.player.duration - playerConnection.player.currentPosition
          } else {
            playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
          }
        delay(1000L)
      }
    }
  }

  val glassConfig = LocalGlassEffectConfig.current
  val isLiquidGlass =
    ((glassConfig.globalEnabled && glassConfig.isEnabledFor(GlassComponent.PLAYER)) ||
      playerBackground == PlayerBackgroundStyle.LIQUID_GLASS) && isGlassSupported()

  BottomSheet(
    state = state,
    modifier = modifier,
    background = { Box(Modifier.fillMaxSize().background(Color.Unspecified)) },
    collapsedContent = {
      if (useNewPlayerDesign) {

        Row(
          horizontalArrangement = Arrangement.spacedBy(if (isLiquidGlass) 8.dp else 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 12.dp)
              .windowInsetsPadding(
                WindowInsets.systemBars.only(
                  WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                ),
              ),
        ) {
          val buttonSize = 42.dp
          val iconSize = 24.dp
          val queueShape =
            if (isLiquidGlass) RoundedCornerShape(21.dp)
            else
              RoundedCornerShape(
                topStart = 50.dp,
                bottomStart = 50.dp,
                topEnd = 3.dp,
                bottomEnd = 3.dp
              )
          val middleShape =
            if (isLiquidGlass) RoundedCornerShape(21.dp)
            else RoundedCornerShape(3.dp)
          val repeatShape =
            if (isLiquidGlass) RoundedCornerShape(21.dp)
            else
              RoundedCornerShape(
                topStart = 3.dp,
                bottomStart = 3.dp,
                topEnd = 50.dp,
                bottomEnd = 50.dp
              )

          PlayerQueueButton(
            icon = R.drawable.queue_music,
            onClick = { state.expandSoft() },
            isActive = false,
            shape = queueShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = TextBackgroundColor,
            playerBackground = playerBackground,
            isLiquidGlass = isLiquidGlass,
          )

          PlayerQueueButton(
            icon = R.drawable.bedtime,
            onClick = {
              if (sleepTimerEnabled) {
                playerConnection.service.sleepTimer.clear()
              } else {
                showSleepTimerDialog = true
              }
            },
            isActive = sleepTimerEnabled,
            enabled = !isListenTogetherGuest,
            shape = middleShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            text = if (sleepTimerEnabled) makeTimeString(sleepTimerTimeLeft) else null,
            iconSize = iconSize,
            textBackgroundColor = TextBackgroundColor,
            playerBackground = playerBackground,
            isLiquidGlass = isLiquidGlass,
          )

          PlayerQueueButton(
            icon = R.drawable.lyrics,
            onClick = { onToggleLyrics() },
            isActive = showInlineLyrics,
            shape = middleShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = TextBackgroundColor,
            playerBackground = playerBackground,
            isLiquidGlass = isLiquidGlass,
          )

          if (showCommentButton) {
            PlayerQueueButton(
              icon = R.drawable.chat_msg,
              onClick = { showCommentSheet = true },
              isActive = showCommentSheet,
              shape = middleShape,
              modifier = Modifier.size(buttonSize),
              textButtonColor = textButtonColor,
              iconButtonColor = iconButtonColor,
              iconSize = iconSize,
              textBackgroundColor = TextBackgroundColor,
              playerBackground = playerBackground,
              isLiquidGlass = isLiquidGlass,
            )
          }

          val shuffleModeEnabledInside by playerConnection.shuffleModeEnabled.collectAsState()
          PlayerQueueButton(
            icon = R.drawable.shuffle,
            onClick = { playerConnection.player.shuffleModeEnabled = !shuffleModeEnabledInside },
            isActive = shuffleModeEnabledInside,
            enabled = !isListenTogetherGuest,
            shape = middleShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = TextBackgroundColor,
            playerBackground = playerBackground,
            isLiquidGlass = isLiquidGlass,
          )

          PlayerQueueButton(
            icon =
              when (repeatMode) {
                Player.REPEAT_MODE_ALL -> R.drawable.repeat
                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                else -> R.drawable.repeat
              },
            onClick = { playerConnection.player.toggleRepeatMode() },
            isActive = repeatMode != Player.REPEAT_MODE_OFF,
            enabled = !isListenTogetherGuest,
            shape = repeatShape,
            modifier = Modifier.size(buttonSize),
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            iconSize = iconSize,
            textBackgroundColor = TextBackgroundColor,
            playerBackground = playerBackground,
            isLiquidGlass = isLiquidGlass,
          )

          Spacer(modifier = Modifier.weight(1f))

          Box(
            modifier =
              Modifier.size(buttonSize)
                .clip(CircleShape)
                .background(if (isLiquidGlass) Color.White.copy(alpha = 0.14f) else textButtonColor)
                .then(
                  if (isLiquidGlass)
                    Modifier.border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                  else Modifier
                )
                .clickable {
                  menuState.show {
                    PlayerMenu(
                      mediaMetadata = mediaMetadata,
                      navController = navController,
                      playerBottomSheetState = playerBottomSheetState,
                      onShowDetailsDialog = {
                        mediaMetadata?.id?.let { bottomSheetPageState.show { ShowMediaInfo(it) } }
                      },
                      onDismiss = menuState::dismiss
                    )
                  }
                },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              painter = painterResource(id = R.drawable.more_vert),
              contentDescription = null,
              modifier = Modifier.size(iconSize),
              tint = if (isLiquidGlass) Color.White.copy(alpha = 0.85f) else iconButtonColor
            )
          }
        }
      } else {

        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 28.dp, vertical = 12.dp)
              .windowInsetsPadding(
                WindowInsets.systemBars.only(
                  WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                ),
              ),
        ) {
          TextButton(onClick = { state.expandSoft() }, modifier = Modifier.wrapContentWidth()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Icon(
                painter = painterResource(id = R.drawable.apple_queue),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isLiquidGlass) Color.White else TextBackgroundColor
              )
            }
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            modifier = Modifier.width(120.dp)
          ) {
            ToggleButton(
              checked = false,
              onCheckedChange = { showAudioDeviceBottomSheet = true },
              shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
              modifier = Modifier.height(52.dp).weight(1f),
              colors =
                ToggleButtonDefaults.toggleButtonColors(
                  containerColor =
                    if (isLiquidGlass) Color.White.copy(alpha = 0.12f)
                    else TextBackgroundColor.copy(alpha = 0.2f),
                  contentColor = if (isLiquidGlass) Color.White else TextBackgroundColor,
                  checkedContainerColor =
                    if (isLiquidGlass) Color.White.copy(alpha = 0.32f)
                    else TextBackgroundColor.copy(alpha = 0.4f),
                  checkedContentColor = if (isLiquidGlass) Color.White else TextBackgroundColor
                )
            ) {
              Icon(
                painter =
                  painterResource(
                    if (isBluetoothConnected) R.drawable.headset_applemusic
                    else R.drawable.speaker_apple
                  ),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
              )
            }

            ToggleButton(
              checked = sleepTimerEnabled,
              onCheckedChange = {
                if (!isListenTogetherGuest) {
                  if (sleepTimerEnabled) {
                    playerConnection.service.sleepTimer.clear()
                  } else {
                    showSleepTimerDialog = true
                  }
                }
              },
              shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
              modifier = Modifier.height(52.dp).weight(1f),
              colors =
                ToggleButtonDefaults.toggleButtonColors(
                  containerColor =
                    if (isLiquidGlass) Color.White.copy(alpha = 0.12f)
                    else TextBackgroundColor.copy(alpha = 0.2f),
                  contentColor = if (isLiquidGlass) Color.White else TextBackgroundColor,
                  checkedContainerColor =
                    if (isLiquidGlass) Color.White.copy(alpha = 0.32f)
                    else TextBackgroundColor.copy(alpha = 0.4f),
                  checkedContentColor = if (isLiquidGlass) Color.White else TextBackgroundColor
                )
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.sleep_timer),
                  contentDescription = null,
                  modifier = Modifier.size(26.dp)
                )
                if (sleepTimerEnabled) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = makeTimeString(sleepTimerTimeLeft),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLiquidGlass) Color.White else TextBackgroundColor,
                    maxLines = 1
                  )
                }
              }
            }
          }

          TextButton(onClick = { onToggleLyrics() }, modifier = Modifier.wrapContentWidth()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Icon(
                painter = painterResource(id = R.drawable.apple_music_me),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isLiquidGlass) Color.White else TextBackgroundColor
              )
            }
          }
        }
      }
      if (showAudioDeviceBottomSheet) {
        AudioDeviceBottomSheet(onDismiss = { showAudioDeviceBottomSheet = false })
      }

      if (showSleepTimerDialog) {
        ActionPromptDialog(
          titleBar = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
              Text(
                text = stringResource(R.string.sleep_timer),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall,
              )
            }
          },
          onDismiss = { showSleepTimerDialog = false },
          onConfirm = {
            showSleepTimerDialog = false
            playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
          },
          onCancel = { showSleepTimerDialog = false },
          onReset = { sleepTimerValue = 30f },
          content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text =
                  pluralStringResource(
                    R.plurals.minute,
                    sleepTimerValue.roundToInt(),
                    sleepTimerValue.roundToInt()
                  ),
                style = MaterialTheme.typography.bodyLarge,
              )

              Spacer(Modifier.height(16.dp))

              Slider(
                value = sleepTimerValue,
                onValueChange = { sleepTimerValue = it },
                valueRange = 5f..120f,
                steps = (120 - 5) / 5 - 1,
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(Modifier.height(8.dp))

              OutlinedButton(
                onClick = {
                  showSleepTimerDialog = false
                  playerConnection.service.sleepTimer.start(-1)
                }
              ) {
                Text(stringResource(R.string.end_of_song))
              }
            }
          }
        )
      }
    },
  ) {
    val queueTitle by playerConnection.queueTitle.collectAsState()
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val automix by playerConnection.service.automixItems.collectAsState()
    val mutableQueueWindows = remember { mutableStateListOf<Timeline.Window>() }
    val queueLength =
      remember(queueWindows) { queueWindows.sumOf { it.mediaItem.metadata!!.duration } }

    val coroutineScope = rememberCoroutineScope()

    val headerItems = 1
    val lazyListState = rememberLazyListState()
    var dragInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val currentPlayingUid =
      remember(currentWindowIndex, queueWindows) {
        if (currentWindowIndex in queueWindows.indices) {
          queueWindows[currentWindowIndex].uid
        } else null
      }

    val reorderableState =
      rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThresholdPadding =
          WindowInsets.systemBars
            .add(WindowInsets(top = ListItemHeight, bottom = ListItemHeight))
            .asPaddingValues()
      ) { from, to ->
        val currentDragInfo = dragInfo
        dragInfo =
          if (currentDragInfo == null) {
            from.index to to.index
          } else {
            currentDragInfo.first to to.index
          }

        val safeFrom = (from.index - headerItems).coerceIn(0, mutableQueueWindows.lastIndex)
        val safeTo = (to.index - headerItems).coerceIn(0, mutableQueueWindows.lastIndex)

        mutableQueueWindows.move(safeFrom, safeTo)
      }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
      if (!reorderableState.isAnyItemDragging) {
        dragInfo?.let { (from, to) ->
          val safeFrom = (from - headerItems).coerceIn(0, queueWindows.lastIndex)
          val safeTo = (to - headerItems).coerceIn(0, queueWindows.lastIndex)

          if (!playerConnection.player.shuffleModeEnabled) {
            playerConnection.player.moveMediaItem(safeFrom, safeTo)
          } else {
            playerConnection.player.setShuffleOrder(
              DefaultShuffleOrder(
                queueWindows
                  .map { it.firstPeriodIndex }
                  .toMutableList()
                  .move(safeFrom, safeTo)
                  .toIntArray(),
                System.currentTimeMillis()
              )
            )
          }
          dragInfo = null
        }
      }
    }

    LaunchedEffect(queueWindows) {
      mutableQueueWindows.apply {
        clear()
        addAll(queueWindows)
      }
    }

    LaunchedEffect(mutableQueueWindows) {
      if (currentWindowIndex != -1) {
        lazyListState.scrollToItem(currentWindowIndex)
      }
    }

    val queueShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    val queueModifier =
      if (isLiquidGlass) {
        Modifier.fillMaxSize()
          .clip(queueShape)
          .liquidGlass(
            config = glassConfig,
            shape = queueShape,
            applyEdgeEffects = false
          )
          .border(
            width = 0.75.dp,
            brush =
              Brush.verticalGradient(
                colors =
                  listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent,
                  )
              ),
            shape = queueShape,
          )
      } else {
        Modifier.fillMaxSize()
          .clip(queueShape)
          .background(
            if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
          )
      }

    Column(
      modifier = queueModifier,
    ) {
      Box(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier =
            Modifier.size(width = 36.dp, height = 4.dp)
              .clip(CircleShape)
              .background(
                if (isLiquidGlass) Color.White.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
              )
        )
      }

      Column(
        modifier =
          Modifier.clickable(
              indication = null,
              interactionSource = remember { MutableInteractionSource() }
            ) {}
            .windowInsetsPadding(
              WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            ),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          AsyncImage(
            model = mediaMetadata?.thumbnailUrl,
            contentDescription = null,
            modifier =
              Modifier.size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .then(
                  if (isLiquidGlass)
                    Modifier.border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                  else Modifier
                )
          )

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = mediaMetadata?.title.orEmpty(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
              color = if (isLiquidGlass) Color.White else MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
              style = MaterialTheme.typography.bodyMedium,
              color =
                if (isLiquidGlass) Color.White.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          val isLiked = currentSong?.song?.liked == true
          val likeDescription =
            if (isLiked) stringResource(R.string.action_remove_like)
            else stringResource(R.string.action_like)
          TooltipBox(
            positionProvider =
              TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text(likeDescription) } },
            state = rememberTooltipState(),
          ) {
            if (isLiquidGlass) {
              Box(
                modifier =
                  Modifier.padding(end = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (isLiked) 0.25f else 0.12f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clickable(onClick = playerConnection::toggleLike),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter =
                    painterResource(
                      if (isLiked) R.drawable.favorite else R.drawable.favorite_border
                    ),
                  contentDescription = likeDescription,
                  modifier = Modifier.size(20.dp),
                  tint = if (isLiked) Color(0xFFFF4081) else Color.White.copy(alpha = 0.85f)
                )
              }
            } else {
              FilledTonalIconButton(
                onClick = playerConnection::toggleLike,
                modifier = Modifier.padding(end = 8.dp)
              ) {
                Icon(
                  painter =
                    painterResource(
                      if (isLiked) R.drawable.favorite else R.drawable.favorite_border
                    ),
                  contentDescription = likeDescription,
                  tint =
                    if (isLiked) MaterialTheme.colorScheme.error
                    else LocalContentColor.current
                )
              }
            }
          }

          val lockDescription =
            if (locked) stringResource(R.string.unlock_queue)
            else stringResource(R.string.lock_queue)
          TooltipBox(
            positionProvider =
              TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text(lockDescription) } },
            state = rememberTooltipState(),
          ) {
            if (isLiquidGlass) {
              Box(
                modifier =
                  Modifier.padding(end = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (locked) 0.25f else 0.12f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clickable { locked = !locked },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter =
                    painterResource(if (locked) R.drawable.lock else R.drawable.lock_open),
                  contentDescription = lockDescription,
                  modifier = Modifier.size(20.dp),
                  tint = if (locked) Color.White else Color.White.copy(alpha = 0.75f)
                )
              }
            } else {
              FilledTonalIconButton(
                onClick = { locked = !locked },
                modifier = Modifier.padding(end = 8.dp)
              ) {
                Icon(
                  painter =
                    painterResource(if (locked) R.drawable.lock else R.drawable.lock_open),
                  contentDescription = lockDescription,
                )
              }
            }
          }

          val moreDescription = stringResource(R.string.more_options)
          TooltipBox(
            positionProvider =
              TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text(moreDescription) } },
            state = rememberTooltipState(),
          ) {
            if (isLiquidGlass) {
              Box(
                modifier =
                  Modifier.size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clickable {
                      menuState.show {
                        PlayerMenu(
                          mediaMetadata = mediaMetadata,
                          navController = navController,
                          playerBottomSheetState = playerBottomSheetState,
                          onShowDetailsDialog = {
                            mediaMetadata?.id?.let {
                              bottomSheetPageState.show { ShowMediaInfo(it) }
                            }
                          },
                          onDismiss = menuState::dismiss
                        )
                      }
                    },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter = painterResource(R.drawable.more_vert),
                  contentDescription = moreDescription,
                  modifier = Modifier.size(20.dp),
                  tint = Color.White.copy(alpha = 0.85f)
                )
              }
            } else {
              FilledTonalIconButton(
                onClick = {
                  menuState.show {
                    PlayerMenu(
                      mediaMetadata = mediaMetadata,
                      navController = navController,
                      playerBottomSheetState = playerBottomSheetState,
                      onShowDetailsDialog = {
                        mediaMetadata?.id?.let {
                          bottomSheetPageState.show { ShowMediaInfo(it) }
                        }
                      },
                      onDismiss = menuState::dismiss
                    )
                  }
                }
              ) {
                Icon(
                  painter = painterResource(R.drawable.more_vert),
                  contentDescription = moreDescription,
                )
              }
            }
          }
        }

        if (isLiquidGlass) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            val shufflePillShape = RoundedCornerShape(24.dp)
            Box(
              modifier =
                Modifier.weight(1f)
                  .height(46.dp)
                  .clip(shufflePillShape)
                  .background(Color.White.copy(alpha = if (shuffleModeEnabled) 0.30f else 0.10f))
                  .border(
                    width = if (shuffleModeEnabled) 0.75.dp else 0.5.dp,
                    color = Color.White.copy(alpha = if (shuffleModeEnabled) 0.50f else 0.20f),
                    shape = shufflePillShape
                  )
                  .clickable(enabled = !isListenTogetherGuest) {
                    playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled
                  },
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  painter = painterResource(R.drawable.shuffle),
                  contentDescription = null,
                  modifier = Modifier.size(19.dp),
                  tint = if (shuffleModeEnabled) Color.White else Color.White.copy(alpha = 0.70f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                  text = stringResource(R.string.shuffle),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight =
                    if (shuffleModeEnabled) androidx.compose.ui.text.font.FontWeight.Bold
                    else androidx.compose.ui.text.font.FontWeight.Medium,
                  color = if (shuffleModeEnabled) Color.White else Color.White.copy(alpha = 0.75f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            val isRepeatActive = repeatMode != Player.REPEAT_MODE_OFF
            val repeatPillShape = RoundedCornerShape(24.dp)
            Box(
              modifier =
                Modifier.weight(1f)
                  .height(46.dp)
                  .clip(repeatPillShape)
                  .background(Color.White.copy(alpha = if (isRepeatActive) 0.30f else 0.10f))
                  .border(
                    width = if (isRepeatActive) 0.75.dp else 0.5.dp,
                    color = Color.White.copy(alpha = if (isRepeatActive) 0.50f else 0.20f),
                    shape = repeatPillShape
                  )
                  .clickable(enabled = !isListenTogetherGuest) {
                    playerConnection.player.toggleRepeatMode()
                  },
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  painter =
                    painterResource(
                      when (repeatMode) {
                        Player.REPEAT_MODE_ALL -> R.drawable.repeat
                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                        else -> R.drawable.repeat
                      }
                    ),
                  contentDescription = null,
                  modifier = Modifier.size(19.dp),
                  tint = if (isRepeatActive) Color.White else Color.White.copy(alpha = 0.70f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                  text = stringResource(R.string.repeat),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight =
                    if (isRepeatActive) androidx.compose.ui.text.font.FontWeight.Bold
                    else androidx.compose.ui.text.font.FontWeight.Medium,
                  color = if (isRepeatActive) Color.White else Color.White.copy(alpha = 0.75f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            val radioPillShape = RoundedCornerShape(24.dp)
            Box(
              modifier =
                Modifier.weight(1f)
                  .height(46.dp)
                  .clip(radioPillShape)
                  .background(Color.White.copy(alpha = 0.10f))
                  .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.20f),
                    shape = radioPillShape
                  )
                  .clickable(enabled = !isListenTogetherGuest) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.starting_radio),
                        Toast.LENGTH_SHORT
                      )
                      .show()
                    playerConnection.startRadioSeamlessly()
                  },
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  painter = painterResource(R.drawable.radio),
                  contentDescription = null,
                  modifier = Modifier.size(19.dp),
                  tint = Color.White.copy(alpha = 0.70f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                  text = stringResource(R.string.start_an_radio),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                  color = Color.White.copy(alpha = 0.75f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        } else {
          FlowRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
          ) {
            ToggleButton(
              checked = shuffleModeEnabled,
              onCheckedChange = { playerConnection.player.shuffleModeEnabled = it },
              enabled = !isListenTogetherGuest,
              shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
              colors =
                ToggleButtonDefaults.toggleButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
              modifier = Modifier.weight(1f).height(48.dp),
            ) {
              Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
              Text(
                text = stringResource(R.string.shuffle),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            ToggleButton(
              checked = repeatMode != Player.REPEAT_MODE_OFF,
              onCheckedChange = { playerConnection.player.toggleRepeatMode() },
              enabled = !isListenTogetherGuest,
              shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
              colors =
                ToggleButtonDefaults.toggleButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
              modifier = Modifier.weight(1f).height(48.dp),
            ) {
              Icon(
                painter =
                  painterResource(
                    when (repeatMode) {
                      Player.REPEAT_MODE_ALL -> R.drawable.repeat
                      Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                      else -> R.drawable.repeat
                    }
                  ),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
              Text(
                text = stringResource(R.string.repeat),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            ToggleButton(
              checked = false,
              onCheckedChange = {
                Toast.makeText(
                    context,
                    context.getString(R.string.starting_radio),
                    Toast.LENGTH_SHORT
                  )
                  .show()
                playerConnection.startRadioSeamlessly()
              },
              enabled = !isListenTogetherGuest,
              shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
              colors =
                ToggleButtonDefaults.toggleButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
              modifier = Modifier.weight(1f).height(48.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  painter = painterResource(R.drawable.radio),
                  contentDescription = null,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(
                  text = stringResource(R.string.start_an_radio),
                  style = MaterialTheme.typography.labelMedium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
              text = stringResource(R.string.continue_playing),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
              color = if (isLiquidGlass) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = stringResource(R.string.next_in_queue),
              style = MaterialTheme.typography.bodySmall,
              color =
                if (isLiquidGlass) Color.White.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          val badgeShape = RoundedCornerShape(12.dp)
          Box(
            modifier =
              Modifier.clip(badgeShape)
                .background(
                  if (isLiquidGlass) Color.White.copy(alpha = 0.12f)
                  else MaterialTheme.colorScheme.secondaryContainer
                )
                .then(
                  if (isLiquidGlass)
                    Modifier.border(0.5.dp, Color.White.copy(alpha = 0.22f), badgeShape)
                  else Modifier
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = pluralStringResource(R.plurals.n_song, queueWindows.size, queueWindows.size),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color =
                  if (isLiquidGlass) Color.White.copy(alpha = 0.85f)
                  else MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall,
                color =
                  if (isLiquidGlass) Color.White.copy(alpha = 0.5f)
                  else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
              )
              Text(
                text = makeTimeString(queueLength * 1000L),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color =
                  if (isLiquidGlass) Color.White.copy(alpha = 0.85f)
                  else MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        }

        AnimatedVisibility(
          visible = inSelectMode,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
        ) {
          val selectedSongs =
            remember(selection.toList(), mutableQueueWindows) {
              mutableQueueWindows
                .filter { it.mediaItem.mediaId in selection }
                .mapNotNull { it.mediaItem.metadata }
            }
          val selectedItems =
            remember(selection.toList(), mutableQueueWindows) {
              mutableQueueWindows.filter { it.mediaItem.mediaId in selection }
            }
          val count = selection.size
          val selShape = RoundedCornerShape(16.dp)
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .clip(selShape)
                .background(
                  if (isLiquidGlass) Color.White.copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.surfaceContainerHighest
                )
                .then(
                  if (isLiquidGlass)
                    Modifier.border(0.5.dp, Color.White.copy(alpha = 0.28f), selShape)
                  else Modifier
                )
                .padding(horizontal = 8.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onExitSelectionMode,
            ) {
              Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = null,
                tint = if (isLiquidGlass) Color.White else LocalContentColor.current
              )
            }
            Text(
              text = pluralStringResource(R.plurals.n_selected, count, count),
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.labelLarge,
              color = if (isLiquidGlass) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
              checked = count == mutableQueueWindows.size && count > 0,
              onCheckedChange = {
                if (count == mutableQueueWindows.size) {
                  selection.clear()
                } else {
                  selection.clear()
                  mutableQueueWindows.forEach { selection.add(it.mediaItem.mediaId) }
                }
              }
            )
            IconButton(
              enabled = count > 0,
              onClick = {
                menuState.show {
                  SelectionMediaMetadataMenu(
                    songSelection = selectedSongs,
                    onDismiss = menuState::dismiss,
                    clearAction = onExitSelectionMode,
                    currentItems = selectedItems,
                  )
                }
              },
            ) {
              Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = null,
                tint = if (isLiquidGlass) Color.White else LocalContentColor.current,
              )
            }
          }
        }
      }

      Box(modifier = Modifier.weight(1f)) {
        LazyColumn(
          state = lazyListState,
          contentPadding =
            WindowInsets.systemBars
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .add(
                WindowInsets(
                  top = 8.dp,
                  bottom = ListItemHeight + 8.dp,
                ),
              )
              .asPaddingValues(),
          modifier = Modifier.fillMaxSize().nestedScroll(state.preUpPostDownNestedScrollConnection)
        ) {
          item(key = "queue_top_spacer") {
            Spacer(
              modifier = Modifier.animateContentSize().height(if (inSelectMode) 48.dp else 0.dp),
            )
          }

          itemsIndexed(
            items = mutableQueueWindows,
            key = { _, item -> item.uid.toString() },
          ) { index, window ->
            ReorderableItem(
              state = reorderableState,
              key = window.uid.toString(),
            ) {
              val currentItem by rememberUpdatedState(window)
              val isActive = window.uid == currentPlayingUid
              val dismissBoxState =
                rememberSwipeToDismissBoxState(
                  positionalThreshold = { totalDistance -> totalDistance }
                )

              var processedDismiss by remember { mutableStateOf(false) }
              LaunchedEffect(dismissBoxState.currentValue) {
                val dv = dismissBoxState.currentValue
                if (
                  !processedDismiss &&
                    !isListenTogetherGuest &&
                    (dv == SwipeToDismissBoxValue.StartToEnd ||
                      dv == SwipeToDismissBoxValue.EndToStart)
                ) {
                  processedDismiss = true
                  playerConnection.player.removeMediaItem(currentItem.firstPeriodIndex)
                  dismissJob?.cancel()
                  dismissJob =
                    coroutineScope.launch {
                      val snackbarResult =
                        snackbarHostState.showSnackbar(
                          message =
                            context.getString(
                              R.string.removed_song_from_playlist,
                              currentItem.mediaItem.metadata?.title,
                            ),
                          actionLabel = context.getString(R.string.undo),
                          duration = SnackbarDuration.Short,
                        )
                      if (snackbarResult == SnackbarResult.ActionPerformed) {
                        playerConnection.player.addMediaItem(currentItem.mediaItem)
                        playerConnection.player.moveMediaItem(
                          mutableQueueWindows.size,
                          currentItem.firstPeriodIndex,
                        )
                      }
                    }
                }
                if (dv == SwipeToDismissBoxValue.Settled) {
                  processedDismiss = false
                }
              }

              val onCheckedChange: (Boolean) -> Unit = {
                if (it) {
                  selection.add(window.mediaItem.mediaId)
                } else {
                  selection.remove(window.mediaItem.mediaId)
                }
              }

              val itemShape = RoundedCornerShape(16.dp)
              val itemBgColor = Color.Transparent

              val content: @Composable () -> Unit = {
                Row(
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.animateItem(),
                ) {
                  MediaMetadataListItem(
                    mediaMetadata = window.mediaItem.metadata!!,
                    isSelected = false,
                    isActive = isActive,
                    isPlaying = isPlaying && isActive,
                    shape = itemShape,
                    color = itemBgColor,
                    trailingContent = {
                      if (inSelectMode) {
                        Checkbox(
                          checked = window.mediaItem.mediaId in selection,
                          onCheckedChange = onCheckedChange
                        )
                      } else {
                        if (!isListenTogetherGuest) {
                          IconButton(
                            onClick = {
                              menuState.show {
                                QueueMenu(
                                  mediaMetadata = window.mediaItem.metadata!!,
                                  navController = navController,
                                  playerBottomSheetState = playerBottomSheetState,
                                  onShowDetailsDialog = {
                                    window.mediaItem.mediaId.let {
                                      bottomSheetPageState.show { ShowMediaInfo(it) }
                                    }
                                  },
                                  onDismiss = menuState::dismiss,
                                )
                              }
                            }
                          ) {
                            Icon(
                              painter = painterResource(R.drawable.more_vert),
                              contentDescription = null,
                              tint =
                                if (isLiquidGlass) Color.White.copy(alpha = 0.75f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                          }
                        }
                        if (!locked && !isListenTogetherGuest) {
                          Icon(
                            painter = painterResource(R.drawable.drag_handle),
                            contentDescription = null,
                            modifier = Modifier.draggableHandle().padding(12.dp),
                            tint =
                              if (isLiquidGlass) Color.White.copy(alpha = 0.60f)
                              else MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }
                    },
                    modifier =
                      Modifier.fillMaxWidth()
                        .combinedClickable(
                          onClick = {
                            if (inSelectMode) {
                              onCheckedChange(window.mediaItem.mediaId !in selection)
                            } else if (!isListenTogetherGuest) {
                              if (index == currentWindowIndex) {
                                if (isCasting) {
                                  if (castIsPlaying) {
                                    castHandler?.pause()
                                  } else {
                                    castHandler?.play()
                                  }
                                } else {
                                  playerConnection.togglePlayPause()
                                }
                              } else {
                                if (isCasting) {
                                  val mediaId = window.mediaItem.mediaId
                                  val navigated =
                                    castHandler?.navigateToMediaIfInQueue(mediaId) ?: false
                                  if (!navigated) {
                                    playerConnection.player.seekToDefaultPosition(
                                      window.firstPeriodIndex
                                    )
                                  }
                                } else {
                                  playerConnection.player.seekToDefaultPosition(
                                    window.firstPeriodIndex,
                                  )
                                  playerConnection.player.playWhenReady = true
                                }
                              }
                            }
                          },
                          onLongClick = {
                            if (!inSelectMode) {
                              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                              inSelectMode = true
                              onCheckedChange(true)
                            }
                          },
                        ),
                  )
                }
              }

              if (locked) {
                content()
              } else {
                SwipeToDismissBox(
                  state = dismissBoxState,
                  backgroundContent = {
                    Spacer(Modifier.fillMaxSize())
                  },
                ) {
                  content()
                }
              }
            }
          }

          if (automix.isNotEmpty()) {
            item(key = "automix_divider") {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                  Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
              ) {
                Text(
                  text = stringResource(R.string.similar_content),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                  color =
                    if (isLiquidGlass) Color.White.copy(alpha = 0.9f)
                    else MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Box(
                  modifier =
                    Modifier.weight(1f)
                      .height(0.5.dp)
                      .background(
                        if (isLiquidGlass) Color.White.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                      )
                )
              }
            }

            itemsIndexed(
              items = automix,
              key = { _, it -> it.mediaId },
            ) { index, item ->
              val automixShape = listItemShape(index, automix.size)
              val automixBgColor =
                if (isLiquidGlass) Color.Transparent
                else MaterialTheme.colorScheme.surfaceContainer
              Row(
                horizontalArrangement = Arrangement.Center,
              ) {
                MediaMetadataListItem(
                  mediaMetadata = item.metadata!!,
                  shape = automixShape,
                  color = automixBgColor,
                  trailingContent = {
                    if (!isListenTogetherGuest) {
                      IconButton(
                        onClick = {
                          playerConnection.service.playNextAutomix(
                            item,
                            index,
                          )
                        },
                      ) {
                        Icon(
                          painter = painterResource(R.drawable.playlist_play),
                          contentDescription = null,
                          tint =
                            if (isLiquidGlass) Color.White.copy(alpha = 0.75f)
                            else LocalContentColor.current
                        )
                      }
                      IconButton(
                        onClick = {
                          playerConnection.service.addToQueueAutomix(
                            item,
                            index,
                          )
                        },
                      ) {
                        Icon(
                          painter = painterResource(R.drawable.queue_music),
                          contentDescription = null,
                          tint =
                            if (isLiquidGlass) Color.White.copy(alpha = 0.75f)
                            else LocalContentColor.current
                        )
                      }
                    }
                  },
                  modifier =
                    Modifier.fillMaxWidth()
                      .combinedClickable(
                        onClick = {},
                        onLongClick = {
                          menuState.show {
                            QueueMenu(
                              mediaMetadata = item.metadata!!,
                              navController = navController,
                              playerBottomSheetState = playerBottomSheetState,
                              onShowDetailsDialog = {
                                item.mediaId.let { bottomSheetPageState.show { ShowMediaInfo(it) } }
                              },
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

        SnackbarHost(
          hostState = snackbarHostState,
          modifier =
            Modifier.padding(
                bottom =
                  ListItemHeight +
                    WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
              )
              .align(Alignment.BottomCenter),
        )
      }
    }
  }

  if (showCommentSheet) {
    CommentSheet(videoId = mediaMetadata?.id ?: "", onDismiss = { showCommentSheet = false })
  }
}

@Composable
private fun PlayerQueueButton(
  icon: Int,
  onClick: () -> Unit,
  isActive: Boolean,
  enabled: Boolean = true,
  shape: RoundedCornerShape,
  modifier: Modifier = Modifier,
  text: String? = null,
  textButtonColor: Color,
  iconButtonColor: Color,
  iconSize: androidx.compose.ui.unit.Dp,
  textBackgroundColor: Color,
  playerBackground: PlayerBackgroundStyle,
  isLiquidGlass: Boolean = false,
) {
  val buttonModifier = Modifier.clip(shape).clickable(enabled = enabled, onClick = onClick)

  val alphaFactor = if (enabled) 1f else 0.35f

  val appliedModifier =
    if (isLiquidGlass) {
      if (isActive) {
        modifier
          .then(buttonModifier)
          .background(Color.White.copy(alpha = 0.32f))
          .border(0.75.dp, Color.White.copy(alpha = 0.50f), shape)
          .alpha(alphaFactor)
      } else {
        modifier
          .then(buttonModifier)
          .background(Color.White.copy(alpha = 0.12f))
          .border(0.5.dp, Color.White.copy(alpha = 0.22f), shape)
          .alpha(alphaFactor)
      }
    } else {
      if (isActive) {
        modifier.then(buttonModifier.background(textButtonColor)).alpha(alphaFactor)
      } else {
        modifier
          .then(
            buttonModifier.border(
              width = 1.dp,
              color = textButtonColor.copy(alpha = 0.3f),
              shape = shape
            )
          )
          .alpha(alphaFactor)
      }
    }

  Box(modifier = appliedModifier, contentAlignment = Alignment.Center) {
    if (text != null) {
      Text(
        text = text,
        color =
          if (isLiquidGlass) Color.White.copy(alpha = if (enabled) 1f else 0.6f)
          else iconButtonColor.copy(alpha = if (enabled) 1f else 0.6f),
        fontSize = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().basicMarquee()
      )
    } else {
      val baseTint =
        if (isLiquidGlass) {
          if (isActive) Color.White else Color.White.copy(alpha = 0.85f)
        } else {
          if (isActive) {
            iconButtonColor
          } else {
            when (playerBackground) {
              PlayerBackgroundStyle.BLUR,
              PlayerBackgroundStyle.GRADIENT,
              PlayerBackgroundStyle.GLOW_ANIMATED,
              PlayerBackgroundStyle.APPLE_MUSIC,
              PlayerBackgroundStyle.LIVE_MESH,
              PlayerBackgroundStyle.LIQUID_GLASS -> Color.White
              PlayerBackgroundStyle.DEFAULT ->
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            }
          }
        }
      val finalTint = if (enabled) baseTint else baseTint.copy(alpha = 0.5f)
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier.size(iconSize),
        tint = finalTint
      )
    }
  }
}
