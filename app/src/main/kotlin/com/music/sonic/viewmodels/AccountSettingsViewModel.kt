package com.music.sonic.viewmodels

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.music.sonic.App
import com.music.sonic.constants.AccountChannelHandleKey
import com.music.sonic.constants.AccountEmailKey
import com.music.sonic.constants.AccountNameKey
import com.music.sonic.constants.DataSyncIdKey
import com.music.sonic.constants.InnerTubeCookieKey
import com.music.sonic.constants.VisitorDataKey
import com.music.sonic.utils.SyncUtils
import com.music.sonic.utils.dataStore
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AccountSettingsViewModel
@Inject
constructor(
  private val syncUtils: SyncUtils,
) : ViewModel() {

  fun logoutAndClearSyncedContent(context: Context, onCookieChange: (String) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      syncUtils.clearAllSyncedContent()

      App.forgetAccount(context)

      onCookieChange("")
    }
  }

  fun logoutKeepData(context: Context, onCookieChange: (String) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      App.forgetAccount(context)
      withContext(Dispatchers.Main) { onCookieChange("") }
    }
  }

  fun saveTokenAndRestart(
    context: Context,
    cookie: String,
    visitorData: String,
    dataSyncId: String,
    accountName: String,
    accountEmail: String,
    accountChannelHandle: String,
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      context.dataStore.edit { settings ->
        settings[InnerTubeCookieKey] = cookie
        settings[VisitorDataKey] = visitorData
        settings[DataSyncIdKey] = dataSyncId
        settings[AccountNameKey] = accountName
        settings[AccountEmailKey] = accountEmail
        settings[AccountChannelHandleKey] = accountChannelHandle
      }
      withContext(Dispatchers.Main) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
      }
    }
  }
}
