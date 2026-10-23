package com.music.sonic.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.sonic.BuildConfig
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
import com.music.sonic.ui.component.Material3SettingsGroup
import com.music.sonic.ui.component.Material3SettingsItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateSection(
  navController: NavController,
  highlightKey: String? = null
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var autoUpdateEnabled by remember { mutableStateOf(getAutoUpdateCheckSetting(context)) }
  var updateNotificationsEnabled by remember {
    mutableStateOf(getUpdateNotificationsSetting(context))
  }
  var isUpdateAvailable by remember { mutableStateOf(getUpdateAvailableState(context)) }
  var isChecking by remember { mutableStateOf(false) }
  var lastCheckedTime by remember { mutableStateOf(getLastCheckedTime(context)) }

  var availableVersion by remember { mutableStateOf("") }
  var availableChangelog by remember { mutableStateOf<List<ChangelogSection>>(emptyList()) }
  var availableDescription by remember { mutableStateOf<String?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }

  fun runCheck(showToastOnResult: Boolean = true) {
    if (isChecking) return
    isChecking = true
    coroutineScope.launch {
      checkForUpdate(
        context = context,
        onSuccess = { tag, isAvailable, changelog, _, _, description, _, _ ->
          isChecking = false
          val nowTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a"))
          lastCheckedTime = nowTime
          saveLastCheckedTime(context, nowTime)
          isUpdateAvailable = isAvailable
          saveUpdateAvailableState(context, isAvailable)

          if (isAvailable) {
            availableVersion = tag
            availableChangelog = changelog
            availableDescription = description
            showUpdateDialog = true
          } else if (showToastOnResult) {
            Toast.makeText(
                context,
                context.getString(R.string.on_latest_version),
                Toast.LENGTH_SHORT
              )
              .show()
          }
        },
        onError = {
          isChecking = false
          if (showToastOnResult) {
            Toast.makeText(
                context,
                context.getString(R.string.cant_check_updates),
                Toast.LENGTH_SHORT
              )
              .show()
          }
        }
      )
    }
  }

  LaunchedEffect(Unit) { autoClearOldApks(context) }

  if (showUpdateDialog && availableVersion.isNotBlank()) {
    UpdateAvailableDialog(
      version = availableVersion,
      changelog = availableChangelog,
      description = availableDescription,
      onDismiss = { showUpdateDialog = false }
    )
  }

  Material3SettingsGroup(
    title = stringResource(R.string.app_updates_title),
    compact = true,
    items =
      listOf(
        Material3SettingsItem(
          icon = painterResource(R.drawable.update),
          title = {
            Text(if (isUpdateAvailable) "Update available" else "SonicSpace is up to date")
          },
          description = {
            Text(
              if (lastCheckedTime.isNotBlank()) {
                stringResource(R.string.last_checked, lastCheckedTime)
              } else {
                "Version ${BuildConfig.VERSION_NAME}"
              }
            )
          },
          trailingContent = {
            if (isChecking) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = stringResource(R.string.check_for_update),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
          },
          onClick = { runCheck(showToastOnResult = true) }
        ),
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
                  painter =
                    painterResource(
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
                  painter =
                    painterResource(
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
