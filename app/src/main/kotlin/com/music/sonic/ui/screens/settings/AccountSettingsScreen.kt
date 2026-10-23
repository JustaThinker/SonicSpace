package com.music.sonic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.innertube.YouTube
import com.music.innertube.utils.parseCookieString
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.constants.*
import com.music.sonic.constants.SavedAccountsKey
import com.music.sonic.models.AccountData
import com.music.sonic.ui.component.*
import com.music.sonic.ui.utils.backToMain
import com.music.sonic.utils.rememberPreference
import com.music.sonic.viewmodels.AccountSettingsViewModel
import com.music.sonic.viewmodels.HomeViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountSettingsScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  highlightKey: String? = null
) {
  val scrollState = androidx.compose.foundation.rememberScrollState()

  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current

  val (accountNamePref, _) = rememberPreference(AccountNameKey, "")
  val (accountEmail, _) = rememberPreference(AccountEmailKey, "")
  val (accountChannelHandle, _) = rememberPreference(AccountChannelHandleKey, "")
  val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
  val (visitorData, _) = rememberPreference(VisitorDataKey, "")
  val (dataSyncId, _) = rememberPreference(DataSyncIdKey, "")
  val (savedAccountsJson, onSavedAccountsJsonChange) = rememberPreference(SavedAccountsKey, "[]")
  val savedAccounts =
    remember(savedAccountsJson) {
      try {
        Json.decodeFromString<List<AccountData>>(savedAccountsJson)
      } catch (e: Exception) {
        emptyList<AccountData>()
      }
    }

  val (listenBrainzEnabled, onListenBrainzEnabledChange) =
    rememberPreference(ListenBrainzEnabledKey, false)
  val (listenBrainzToken, onListenBrainzTokenChange) = rememberPreference(ListenBrainzTokenKey, "")

  val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
  val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)
  val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, true)

  val homeViewModel: HomeViewModel = hiltViewModel()
  val accountSettingsViewModel: AccountSettingsViewModel = hiltViewModel()
  val accountName by homeViewModel.accountName.collectAsState()
  val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()

  val displayName =
    when {
      accountName.isNotBlank() && accountName != "Guest" -> accountName
      accountNamePref.isNotBlank() -> accountNamePref
      isLoggedIn -> stringResource(R.string.account)
      else -> stringResource(R.string.login)
    }

  var showToken by remember { mutableStateOf(false) }
  var showTokenEditor by remember { mutableStateOf(false) }
  var showLogoutDialog by remember { mutableStateOf(false) }
  var showListenBrainzTokenEditor by remember { mutableStateOf(false) }
  var showAccountSwitcher by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.account)) },
        navigationIcon = {
          IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        scrollBehavior = scrollBehavior
      )
    }
  ) { padding ->
    Column(
      modifier =
        Modifier.padding(padding)
          .windowInsetsPadding(
            LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
          )
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      LaunchedEffect(isLoggedIn, innerTubeCookie, accountImageUrl, accountName) {
        val nameToUse = if (accountNamePref.isNotBlank()) accountNamePref else accountName
        if (isLoggedIn && nameToUse.isNotBlank() && nameToUse != "Guest") {
          val mutableAccounts = savedAccounts.toMutableList()
          val index = mutableAccounts.indexOfFirst { it.cookie == innerTubeCookie }
          if (index == -1) {
            mutableAccounts.add(
              AccountData(
                name = nameToUse,
                email = accountEmail,
                channelHandle = accountChannelHandle,
                cookie = innerTubeCookie,
                visitorData = visitorData,
                dataSyncId = dataSyncId,
                avatarUrl = accountImageUrl ?: ""
              )
            )
            onSavedAccountsJsonChange(Json.encodeToString(mutableAccounts))
          } else if (
            mutableAccounts[index].name.isBlank() || mutableAccounts[index].avatarUrl.isBlank()
          ) {
            mutableAccounts[index] =
              mutableAccounts[index].copy(
                name = nameToUse,
                avatarUrl = accountImageUrl ?: mutableAccounts[index].avatarUrl
              )
            onSavedAccountsJsonChange(Json.encodeToString(mutableAccounts))
          }
        }
      }

      AccountSummaryCard(
        isLoggedIn = isLoggedIn,
        accountName = displayName,
        accountEmail = accountEmail,
        accountHandle = accountChannelHandle,
        accountImageUrl = accountImageUrl,
        accountSwitcherEnabled = isLoggedIn || savedAccounts.isNotEmpty(),
        onPrimaryAction = {
          if (isLoggedIn) {
            navController.navigate("account")
          } else {
            navController.navigate("login")
          }
        },
        onSecondaryAction = {
          if (isLoggedIn) {
            showLogoutDialog = true
          } else {
            showTokenEditor = true
          }
        },
        onOpenAccountSwitcher = { showAccountSwitcher = true }
      )

      Spacer(modifier = Modifier.height(10.dp))

      if (isLoggedIn) {
        Material3SettingsGroup(
          scrollState = scrollState,
          title = stringResource(R.string.settings_section_player_content),
          items =
            listOf(
              Material3SettingsItem(
                isHighlighted = (highlightKey == stringResource(R.string.more_content)),
                icon = painterResource(R.drawable.add_circle),
                title = { Text(stringResource(R.string.more_content)) },
                description = { Text(stringResource(R.string.more_content_desc)) },
                trailingContent = {
                  Switch(
                    checked = useLoginForBrowse,
                    onCheckedChange = {
                      YouTube.useLoginForBrowse = it
                      onUseLoginForBrowseChange(it)
                    },
                    thumbContent = {
                      Icon(
                        painter =
                          painterResource(
                            id = if (useLoginForBrowse) R.drawable.check else R.drawable.close
                          ),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                      )
                    }
                  )
                },
                onClick = {
                  val newValue = !useLoginForBrowse
                  YouTube.useLoginForBrowse = newValue
                  onUseLoginForBrowseChange(newValue)
                }
              ),
              Material3SettingsItem(
                isHighlighted = (highlightKey == stringResource(R.string.yt_sync)),
                icon = painterResource(R.drawable.cached),
                title = { Text(stringResource(R.string.yt_sync)) },
                description = { Text(stringResource(R.string.yt_sync_desc)) },
                trailingContent = {
                  Switch(
                    checked = ytmSync,
                    onCheckedChange = onYtmSyncChange,
                    thumbContent = {
                      Icon(
                        painter =
                          painterResource(id = if (ytmSync) R.drawable.check else R.drawable.close),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                      )
                    }
                  )
                },
                onClick = { onYtmSyncChange(!ytmSync) }
              )
            )
        )

        Spacer(modifier = Modifier.height(10.dp))
      }

      Material3SettingsGroup(
        scrollState = scrollState,
        title = stringResource(R.string.integrations),
        items =
          listOf(
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.discord_integration)),
              icon = painterResource(R.drawable.discord),
              title = { Text(stringResource(R.string.discord_integration)) },
              description = { Text(stringResource(R.string.discord_integration_desc)) },
              onClick = { navController.navigate("settings/discord") }
            ),
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.spotify_account)),
              icon = painterResource(R.drawable.ic_spotify),
              title = { Text(stringResource(R.string.spotify_account)) },
              description = { Text(stringResource(R.string.spotify_import_desc)) },
              onClick = { navController.navigate("settings/spotify_import") }
            ),
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.lastfm_integration)),
              icon = painterResource(R.drawable.ic_lastfm),
              title = { Text(stringResource(R.string.lastfm_integration)) },
              description = { Text(stringResource(R.string.lastfm_integration_desc)) },
              onClick = { navController.navigate("settings/lastfm") }
            ),
            Material3SettingsItem(
              isHighlighted = (highlightKey == stringResource(R.string.listenbrainz_scrobbling_desc)),
              icon = painterResource(R.drawable.token),
              title = { Text("ListenBrainz") },
              description = {
                Text(
                  if (listenBrainzToken.isNotBlank()) "Token configured"
                  else stringResource(R.string.listenbrainz_scrobbling_desc)
                )
              },
              trailingContent = {
                Switch(
                  checked = listenBrainzEnabled,
                  onCheckedChange = onListenBrainzEnabledChange,
                  thumbContent = {
                    Icon(
                      painter =
                        painterResource(id = if (listenBrainzEnabled) R.drawable.check else R.drawable.close),
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                  }
                )
              },
              onClick = { showListenBrainzTokenEditor = true }
            ),

          )
      )
    }

    if (showTokenEditor) {
      val text =
        """
                ***INNERTUBE COOKIE*** =$innerTubeCookie
                ***VISITOR DATA*** =$visitorData
                ***DATASYNC ID*** =$dataSyncId
                ***ACCOUNT NAME*** =$accountNamePref
                ***ACCOUNT EMAIL*** =$accountEmail
                ***ACCOUNT CHANNEL HANDLE*** =$accountChannelHandle
            """
          .trimIndent()

      TextFieldDialog(
        initialTextFieldValue = TextFieldValue(text),
        onDone = { data ->
          var cookie = ""
          var visitorDataValue = ""
          var dataSyncIdValue = ""
          var accountNameValue = ""
          var accountEmailValue = ""
          var accountChannelHandleValue = ""

          data.split("\n").forEach {
            when {
              it.startsWith("***INNERTUBE COOKIE*** =") -> cookie = it.substringAfter("=")
              it.startsWith("***VISITOR DATA*** =") -> visitorDataValue = it.substringAfter("=")
              it.startsWith("***DATASYNC ID*** =") -> dataSyncIdValue = it.substringAfter("=")
              it.startsWith("***ACCOUNT NAME*** =") -> accountNameValue = it.substringAfter("=")
              it.startsWith("***ACCOUNT EMAIL*** =") -> accountEmailValue = it.substringAfter("=")
              it.startsWith("***ACCOUNT CHANNEL HANDLE*** =") ->
                accountChannelHandleValue = it.substringAfter("=")
            }
          }
          accountSettingsViewModel.saveTokenAndRestart(
            context = context,
            cookie = cookie,
            visitorData = visitorDataValue,
            dataSyncId = dataSyncIdValue,
            accountName = accountNameValue,
            accountEmail = accountEmailValue,
            accountChannelHandle = accountChannelHandleValue,
          )
        },
        onDismiss = { showTokenEditor = false },
        singleLine = false,
        maxLines = 8,
        isInputValid = { fullText ->
          val cookieLine = fullText.lines().find { it.startsWith("***INNERTUBE COOKIE*** =") }
          val cookieValue = cookieLine?.substringAfter("***INNERTUBE COOKIE*** =")?.trim() ?: ""
          cookieValue.isNotEmpty() && "SAPISID" in parseCookieString(cookieValue)
        },
        extraContent = { InfoLabel(text = stringResource(R.string.token_adv_login_description)) }
      )
    }
    if (showLogoutDialog) {
      DefaultDialog(
        onDismiss = { showLogoutDialog = false },
        title = { Text(stringResource(R.string.logout_dialog_title)) },
        buttons = {
          Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                accountSettingsViewModel.logoutKeepData(context, onInnerTubeCookieChange)
                showLogoutDialog = false
                navController.navigateUp()
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(stringResource(R.string.logout_keep_data))
            }

            FilledTonalButton(
              onClick = {
                accountSettingsViewModel.logoutAndClearSyncedContent(
                  context,
                  onInnerTubeCookieChange
                )
                showLogoutDialog = false
              },
              modifier = Modifier.fillMaxWidth(),
              colors =
                ButtonDefaults.filledTonalButtonColors(
                  contentColor = MaterialTheme.colorScheme.error
                )
            ) {
              Text(stringResource(R.string.logout_clear_data))
            }

            TextButton(onClick = { showLogoutDialog = false }, modifier = Modifier.fillMaxWidth()) {
              Text(stringResource(android.R.string.cancel))
            }
          }
        }
      ) {
        Text(
          text = stringResource(R.string.logout_dialog_message),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }
    }

    if (showListenBrainzTokenEditor) {
      TextFieldDialog(
        initialTextFieldValue = TextFieldValue(listenBrainzToken),
        onDone = { data ->
          onListenBrainzTokenChange(data)
          showListenBrainzTokenEditor = false
        },
        onDismiss = { showListenBrainzTokenEditor = false },
        singleLine = true,
        maxLines = 1,
        isInputValid = { it.isNotEmpty() },
        extraContent = {
          InfoLabel(text = stringResource(R.string.listenbrainz_scrobbling_description))
        }
      )
    }

    if (showAccountSwitcher) {
      AccountSwitcherSheet(
        savedAccounts = savedAccounts,
        activeCookie = innerTubeCookie,
        onSwitchAccount = { account ->
          accountSettingsViewModel.saveTokenAndRestart(
            context = context,
            cookie = account.cookie,
            visitorData = account.visitorData,
            dataSyncId = account.dataSyncId,
            accountName = account.name,
            accountEmail = account.email,
            accountChannelHandle = account.channelHandle
          )
        },
        onRemoveAccount = { account ->
          val mutableAccounts = savedAccounts.toMutableList()
          mutableAccounts.remove(account)
          onSavedAccountsJsonChange(Json.encodeToString(mutableAccounts))
        },
        onAddAnotherAccount = {
          showAccountSwitcher = false
          navController.navigate("login")
        },
        onDismiss = { showAccountSwitcher = false }
      )
    }

    Spacer(Modifier.height(100.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
      )
    )
  }
}

private val AvatarSize = 72.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountSummaryCard(
  isLoggedIn: Boolean,
  accountName: String,
  accountEmail: String,
  accountHandle: String,
  accountImageUrl: String?,
  accountSwitcherEnabled: Boolean,
  onPrimaryAction: () -> Unit,
  onSecondaryAction: () -> Unit,
  onOpenAccountSwitcher: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.extraLarge,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Box(contentAlignment = Alignment.BottomEnd) {
          Surface(
            modifier = Modifier.size(AvatarSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            if (isLoggedIn && !accountImageUrl.isNullOrBlank()) {
              AsyncImage(
                model = accountImageUrl,
                contentDescription = null,
                modifier =
                  Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
              )
            } else {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  painter =
                    painterResource(
                      if (isLoggedIn) R.drawable.account else R.drawable.login,
                    ),
                  contentDescription = null,
                  modifier = Modifier.size(32.dp),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }

          if (isLoggedIn) {
            Surface(
              modifier = Modifier.size(24.dp),
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  painter = painterResource(R.drawable.check),
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onPrimary,
                )
              }
            }
          }
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = accountName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (accountHandle.isNotBlank()) {
            Text(
              text = accountHandle,
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          if (accountEmail.isNotBlank() || !isLoggedIn) {
            Text(
              text = accountEmail.ifBlank { stringResource(R.string.not_logged_in) },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SplitButtonLayout(
          leadingButton = {
            SplitButtonDefaults.LeadingButton(
              onClick = onPrimaryAction,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
              Icon(
                painter =
                  painterResource(
                    if (isLoggedIn) R.drawable.account else R.drawable.login,
                  ),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isLoggedIn) stringResource(R.string.account) else stringResource(R.string.login),
              )
            }
          },
          trailingButton = {
            SplitButtonDefaults.TrailingButton(
              checked = false,
              onCheckedChange = { onOpenAccountSwitcher() },
              enabled = accountSwitcherEnabled,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
              Icon(
                painter = painterResource(R.drawable.expand_more),
                contentDescription = stringResource(R.string.saved_accounts),
                modifier = Modifier.size(20.dp),
              )
            }
          },
        )

        TextButton(
          onClick = onSecondaryAction,
          colors =
            ButtonDefaults.textButtonColors(
              contentColor =
                if (isLoggedIn) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.primary
                },
            ),
        ) {
          Text(
            text = if (isLoggedIn) stringResource(R.string.action_logout) else stringResource(R.string.advanced_login),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSwitcherSheet(
  savedAccounts: List<AccountData>,
  activeCookie: String,
  onSwitchAccount: (AccountData) -> Unit,
  onRemoveAccount: (AccountData) -> Unit,
  onAddAnotherAccount: () -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss
  ) {
    Text(
      text = stringResource(R.string.saved_accounts),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )

    LazyColumn(
      modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
      contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (savedAccounts.isEmpty()) {
        item {
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = stringResource(R.string.no_saved_accounts),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(savedAccounts, key = { it.cookie }) { account ->
          val isActive = account.cookie == activeCookie
          Surface(
            onClick = {
              if (!isActive) {
                onSwitchAccount(account)
              }
              onDismiss()
            },
            shape = MaterialTheme.shapes.medium,
            color =
              if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              else MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (account.avatarUrl.isNotBlank()) {
                AsyncImage(
                  model = account.avatarUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.size(40.dp).clip(CircleShape)
                )
              } else {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                  modifier = Modifier.size(40.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      painter = painterResource(R.drawable.account),
                      contentDescription = null,
                      modifier = Modifier.size(22.dp),
                      tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = account.name.ifBlank { "Account" },
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                  color =
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (account.channelHandle.isNotBlank()) {
                  Text(
                    text = account.channelHandle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                } else if (account.email.isNotBlank()) {
                  Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              if (isActive) {
                Icon(
                  painter = painterResource(R.drawable.check),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
              } else {
                IconButton(onClick = { onRemoveAccount(account) }) {
                  Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = stringResource(R.string.remove_account),
                    tint = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
          onClick = onAddAnotherAccount,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            painter = painterResource(R.drawable.add),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.add_another_account))
        }
      }
    }
  }
}
