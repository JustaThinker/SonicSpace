package com.music.sonic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.music.innertube.utils.parseCookieString
import com.music.sonic.BuildConfig
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.constants.AccountEmailKey
import com.music.sonic.constants.AccountNameKey
import com.music.sonic.constants.InnerTubeCookieKey
import com.music.sonic.echomusic.updater.getUpdateAvailableState
import com.music.sonic.ui.component.Material3SettingsGroup
import com.music.sonic.ui.component.Material3SettingsItem
import com.music.sonic.utils.rememberPreference

@Composable
private fun SettingsProfileHeaderCard(
  accountName: String,
  accountEmail: String,
  isLoggedIn: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(28.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ),
    elevation = CardDefaults.cardElevation(0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.AccountCircle,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isLoggedIn && accountName.isNotBlank()) accountName else "Guest User",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isLoggedIn && accountEmail.isNotBlank()) accountEmail
          else if (isLoggedIn) "Logged in with YouTube Music"
          else "Tap to sign in or switch account",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      ) {
        Text(
          text = if (isLoggedIn) "Account" else "Sign In",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  highlightKey: String? = null
) {
  var searchQuery by rememberSaveable { mutableStateOf("") }
  val searchLower = searchQuery.lowercase()
  val context = LocalContext.current

  val (accountName) = rememberPreference(AccountNameKey, defaultValue = "")
  val (accountEmail) = rememberPreference(AccountEmailKey, defaultValue = "")
  val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, defaultValue = "")
  val isLoggedIn = "SAPISID" in parseCookieString(innerTubeCookie)

  val scrollState = rememberScrollState()

  Column(
    Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
      )
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(bottom = 120.dp)
  ) {
    Spacer(
      Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
    )

    Text(
      text = stringResource(R.string.settings),
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 16.dp)
    )

    TextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text(stringResource(R.string.search)) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Rounded.Search,
          contentDescription = stringResource(R.string.search)
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear")
          }
        }
      },
      shape = RoundedCornerShape(28.dp),
      colors =
        TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
          disabledIndicatorColor = Color.Transparent
        ),
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )

    if (searchQuery.isBlank()) {
      SettingsProfileHeaderCard(
        accountName = accountName,
        accountEmail = accountEmail,
        isLoggedIn = isLoggedIn,
        onClick = { navController.navigate("settings/account") }
      )

      Spacer(modifier = Modifier.height(16.dp))

      Material3SettingsGroup(
        items = listOf(
          Material3SettingsItem(
            title = { Text(stringResource(R.string.appearance)) },
            description = { Text(stringResource(R.string.setting_desc_appearance)) },
            customIcon = {
              Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/appearance") }
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.player_and_audio)) },
            description = { Text(stringResource(R.string.setting_desc_player)) },
            customIcon = {
              Icon(
                imageVector = Icons.Rounded.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/player") }
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.content)) },
            description = { Text(stringResource(R.string.setting_desc_content)) },
            customIcon = {
              Icon(
                painter = painterResource(R.drawable.language),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/content") }
          )
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      Material3SettingsGroup(
        items = listOf(
          Material3SettingsItem(
            title = { Text(stringResource(R.string.storage)) },
            description = { Text(stringResource(R.string.setting_desc_storage)) },
            customIcon = {
              Icon(
                imageVector = Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/storage") }
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.backup_restore)) },
            description = { Text(stringResource(R.string.setting_desc_backup)) },
            customIcon = {
              Icon(
                painter = painterResource(R.drawable.restore),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/backup_restore") }
          ),
          Material3SettingsItem(
            title = { Text(stringResource(R.string.privacy)) },
            description = { Text(stringResource(R.string.setting_desc_privacy)) },
            customIcon = {
              Icon(
                painter = painterResource(R.drawable.security),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/privacy") }
          )
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      val isUpdateAvailable = remember { getUpdateAvailableState(context) }
      Material3SettingsGroup(
        items = listOf(
          Material3SettingsItem(
            title = { Text("About & updates") },
            description = {
              Text(
                if (isUpdateAvailable) stringResource(R.string.update_available)
                else "v${BuildConfig.VERSION_NAME} • ${stringResource(R.string.app_update_uptodate)}"
              )
            },
            customIcon = {
              Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            onClick = { navController.navigate("settings/about") }
          )
        )
      )
    } else {
      val subSettings = getAllSearchableSettings()
      val matchedSubSettings = subSettings.filter {
        it.title.lowercase().contains(searchLower) ||
          (it.description?.lowercase()?.contains(searchLower) == true)
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        matchedSubSettings.forEach { setting ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth().clickable {
              val encodedTitle = android.net.Uri.encode(setting.title)
              val finalRoute =
                if (setting.route.contains("?")) "${setting.route}&highlightKey=$encodedTitle"
                else "${setting.route}?highlightKey=$encodedTitle"
              navController.navigate(finalRoute)
            }
          ) {
            ListItem(
              headlineContent = { Text(setting.title, fontWeight = FontWeight.SemiBold) },
              supportingContent = {
                Text(
                  if (setting.description != null) "${setting.category} • ${setting.description}"
                  else setting.category
                )
              },
              colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
          }
        }
      }
    }
  }
}
