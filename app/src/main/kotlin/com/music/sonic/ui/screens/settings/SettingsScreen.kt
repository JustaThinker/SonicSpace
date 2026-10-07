package com.music.sonic.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.ui.component.getSettingsSegmentedShape

@Composable
private fun SettingsCategoryItem(
  title: String,
  subtitle: String,
  icon: @Composable () -> Unit,
  onClick: () -> Unit
) {
  ListItem(
    headlineContent = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
    },
    supportingContent = {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    leadingContent = {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        icon()
      }
    },
    trailingContent = {
      Icon(
        painter = painterResource(R.drawable.navigate_next),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(22.dp)
      )
    },
    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    modifier = Modifier.clickable { onClick() }
  )
}

@Composable
private fun SettingsGroupCard(items: List<@Composable () -> Unit>) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    items.forEachIndexed { index, item ->
      val shape = getSettingsSegmentedShape(index = index, count = items.size)
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
      ) {
        item()
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

  val accountText = stringResource(R.string.account)
  val appearanceText = stringResource(R.string.appearance)
  val playerText = stringResource(R.string.player_and_audio)
  val contentText = stringResource(R.string.content)
  val privacyText = stringResource(R.string.privacy)
  val storageText = stringResource(R.string.storage)
  val backupText = stringResource(R.string.backup_restore)
  val aboutText = stringResource(R.string.about)

  val scrollState = rememberScrollState()
  Column(
    Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
      )
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp)
      .padding(bottom = 120.dp)
  ) {
    Spacer(
      Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
    )
    Text(
      text = stringResource(R.string.settings),
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 18.dp)
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
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    if (searchQuery.isBlank()) {
      SettingsGroupCard(
        items = listOf(
          {
            SettingsCategoryItem(
              title = accountText,
              subtitle = stringResource(R.string.setting_desc_account),
              icon = {
                Icon(
                  imageVector = Icons.Rounded.AccountCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/account") }
            )
          },
          {
            SettingsCategoryItem(
              title = appearanceText,
              subtitle = stringResource(R.string.setting_desc_appearance),
              icon = {
                Icon(
                  imageVector = Icons.Rounded.Palette,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/appearance") }
            )
          },
          {
            SettingsCategoryItem(
              title = playerText,
              subtitle = stringResource(R.string.setting_desc_player),
              icon = {
                Icon(
                  imageVector = Icons.Rounded.PlayCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/player") }
            )
          },
          {
            SettingsCategoryItem(
              title = contentText,
              subtitle = stringResource(R.string.setting_desc_content),
              icon = {
                Icon(
                  painter = painterResource(R.drawable.language),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/content") }
            )
          },
          {
            SettingsCategoryItem(
              title = storageText,
              subtitle = stringResource(R.string.setting_desc_storage),
              icon = {
                Icon(
                  imageVector = Icons.Rounded.LibraryMusic,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/storage") }
            )
          },
          {
            SettingsCategoryItem(
              title = backupText,
              subtitle = stringResource(R.string.setting_desc_backup),
              icon = {
                Icon(
                  painter = painterResource(R.drawable.restore),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/backup_restore") }
            )
          },
          {
            SettingsCategoryItem(
              title = privacyText,
              subtitle = stringResource(R.string.setting_desc_privacy),
              icon = {
                Icon(
                  painter = painterResource(R.drawable.security),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/privacy") }
            )
          },
          {
            SettingsCategoryItem(
              title = aboutText,
              subtitle = stringResource(R.string.setting_desc_about),
              icon = {
                Icon(
                  imageVector = Icons.Rounded.Info,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(26.dp)
                )
              },
              onClick = { navController.navigate("settings/about") }
            )
          }
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
