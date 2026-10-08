package com.music.sonic.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.sonic.BuildConfig
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.echomusic.component.UpdateAvailableDialog
import com.music.sonic.echomusic.updater.ChangelogSection
import com.music.sonic.echomusic.updater.autoClearOldApks
import com.music.sonic.echomusic.updater.checkForUpdate
import com.music.sonic.echomusic.updater.getAutoUpdateCheckSetting
import com.music.sonic.echomusic.updater.getLastCheckedTime
import com.music.sonic.echomusic.updater.getUpdateAvailableState
import com.music.sonic.echomusic.updater.getUpdateNotificationsSetting
import com.music.sonic.echomusic.updater.saveAutoUpdateCheckSetting
import com.music.sonic.echomusic.updater.saveLastCheckedTime
import com.music.sonic.echomusic.updater.saveUpdateAvailableState
import com.music.sonic.echomusic.updater.saveUpdateNotificationsSetting
import com.music.sonic.ui.component.ChangelogItem
import com.music.sonic.ui.component.IconButton
import com.music.sonic.ui.component.Material3SettingsGroup
import com.music.sonic.ui.component.Material3SettingsItem
import com.music.sonic.ui.component.detachedItemShape
import com.music.sonic.ui.component.endItemShape
import com.music.sonic.ui.component.leadingItemShape
import com.music.sonic.ui.component.middleItemShape
import com.music.sonic.ui.utils.backToMain
import com.music.sonic.ui.utils.parseMarkdownToSections
import com.music.sonic.ui.utils.parseSimpleMarkdown
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSettings(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  highlightKey: String? = null
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  var autoUpdateEnabled by remember { mutableStateOf(getAutoUpdateCheckSetting(context)) }
  var updateNotificationsEnabled by remember { mutableStateOf(getUpdateNotificationsSetting(context)) }
  var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
  var isChecking by remember { mutableStateOf(false) }
  var lastCheckedTime by remember { mutableStateOf(getLastCheckedTime(context)) }

  var availableVersion by remember { mutableStateOf("") }
  var availableChangelog by remember { mutableStateOf<List<ChangelogSection>>(emptyList()) }
  var availableDescription by remember { mutableStateOf<String?>(null) }
  var availableApkUrl by remember { mutableStateOf<String?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }

  fun runCheck(showToastOnResult: Boolean = true) {
    if (isChecking) return
    isChecking = true
    coroutineScope.launch {
      checkForUpdate(
        context = context,
        onSuccess = { tag, isAvailable, changelog, size, date, description, imageUrl, apkUrl ->
          isChecking = false
          val nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a"))
          lastCheckedTime = nowTime
          saveLastCheckedTime(context, nowTime)
          isUpdateAvailable = isAvailable
          saveUpdateAvailableState(context, isAvailable)

          if (isAvailable) {
            availableVersion = tag
            availableChangelog = changelog
            availableDescription = description
            availableApkUrl = apkUrl
            showUpdateDialog = true
          } else {
            if (showToastOnResult) {
              Toast.makeText(
                context,
                context.getString(R.string.on_latest_version),
                Toast.LENGTH_SHORT
              ).show()
            }
          }
        },
        onError = {
          isChecking = false
          if (showToastOnResult) {
            Toast.makeText(
              context,
              context.getString(R.string.cant_check_updates),
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      )
    }
  }

  // Clean old apks on initial screen load
  LaunchedEffect(Unit) {
    autoClearOldApks(context)
  }

  if (showUpdateDialog && availableVersion.isNotBlank()) {
    UpdateAvailableDialog(
      version = availableVersion,
      changelog = availableChangelog,
      description = availableDescription,
      onDismiss = { showUpdateDialog = false }
    )
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surface,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.app_updates_title),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        navigationIcon = {
          IconButton(
            onClick = navController::navigateUp,
            onLongClick = navController::backToMain
          ) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        windowInsets = TopAppBarDefaults.windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        scrollBehavior = scrollBehavior
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(
          LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
        )
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp)
        .padding(
          top = innerPadding.calculateTopPadding() + 8.dp,
          bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 32.dp
        ),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Status Header Card
      UpdateStatusCard(
        isUpdateAvailable = isUpdateAvailable,
        availableVersion = availableVersion,
        isChecking = isChecking,
        lastCheckedTime = lastCheckedTime,
        onCheckClick = { runCheck(showToastOnResult = true) }
      )

      // Settings Group
      Material3SettingsGroup(
        scrollState = scrollState,
        title = stringResource(R.string.update_settings_title),
        items = listOf(
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.auto_update_check)),
            icon = painterResource(R.drawable.update),
            title = { Text(stringResource(R.string.auto_update_check)) },
            description = { Text(stringResource(R.string.auto_update_check_subtitle)) },
            trailingContent = {
              Switch(
                checked = autoUpdateEnabled,
                onCheckedChange = { enabled ->
                  autoUpdateEnabled = enabled
                  saveAutoUpdateCheckSetting(context, enabled)
                  if (!enabled) {
                    saveUpdateAvailableState(context, false)
                    isUpdateAvailable = false
                  }
                },
                thumbContent = {
                  Icon(
                    painter = painterResource(
                      id = if (autoUpdateEnabled) R.drawable.check else R.drawable.close
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = {
              autoUpdateEnabled = !autoUpdateEnabled
              saveAutoUpdateCheckSetting(context, autoUpdateEnabled)
              if (!autoUpdateEnabled) {
                saveUpdateAvailableState(context, false)
                isUpdateAvailable = false
              }
            }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.update_notifications)),
            icon = painterResource(R.drawable.notification),
            title = { Text(stringResource(R.string.update_notifications)) },
            description = { Text(stringResource(R.string.update_notifications_subtitle)) },
            trailingContent = {
              Switch(
                checked = updateNotificationsEnabled,
                onCheckedChange = { enabled ->
                  updateNotificationsEnabled = enabled
                  saveUpdateNotificationsSetting(context, enabled)
                },
                thumbContent = {
                  Icon(
                    painter = painterResource(
                      id = if (updateNotificationsEnabled) R.drawable.check else R.drawable.close
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                  )
                }
              )
            },
            onClick = {
              updateNotificationsEnabled = !updateNotificationsEnabled
              saveUpdateNotificationsSetting(context, updateNotificationsEnabled)
            }
          ),
          Material3SettingsItem(
            isHighlighted = (highlightKey == stringResource(R.string.changelog)),
            icon = painterResource(R.drawable.restore),
            title = { Text(stringResource(R.string.changelog)) },
            description = { Text(stringResource(R.string.view_version_history)) },
            trailingContent = {
              Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
              )
            },
            onClick = { navController.navigate("settings/changelog") }
          )
        )
      )
    }
  }
}

@Composable
private fun UpdateStatusCard(
  isUpdateAvailable: Boolean,
  availableVersion: String,
  isChecking: Boolean,
  lastCheckedTime: String,
  onCheckClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.98f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "cardScale"
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .graphicsLayer { scaleX = scale; scaleY = scale },
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
    elevation = CardDefaults.cardElevation(0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 24.dp, horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(
            if (isUpdateAvailable) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
          )
      ) {
        Icon(
          painter = painterResource(
            if (isUpdateAvailable) R.drawable.update else R.drawable.deployed_app_update
          ),
          contentDescription = null,
          tint = if (isUpdateAvailable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(42.dp)
        )
      }

      Text(
        text = if (isUpdateAvailable) "Update Available!" else "SonicSpace is up to date",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
          Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        if (isUpdateAvailable && availableVersion.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
          ) {
            Text(
              text = "New: $availableVersion",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      }

      if (lastCheckedTime.isNotBlank()) {
        Text(
          text = stringResource(R.string.last_checked, lastCheckedTime),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
        )
      }

      Spacer(Modifier.height(4.dp))

      Button(
        onClick = onCheckClick,
        enabled = !isChecking,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        if (isChecking) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(Modifier.width(8.dp))
          Text(stringResource(R.string.checking_for_updates), fontWeight = FontWeight.Bold)
        } else {
          Icon(
            painter = painterResource(R.drawable.update),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(Modifier.width(8.dp))
          Text(stringResource(R.string.check_for_update), fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
