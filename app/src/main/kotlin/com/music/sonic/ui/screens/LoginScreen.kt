package com.music.sonic.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.music.innertube.YouTube
import com.music.sonic.LocalPlayerAwareWindowInsets
import com.music.sonic.R
import com.music.sonic.constants.AccountChannelHandleKey
import com.music.sonic.constants.AccountEmailKey
import com.music.sonic.constants.AccountNameKey
import com.music.sonic.constants.DataSyncIdKey
import com.music.sonic.constants.InnerTubeCookieKey
import com.music.sonic.constants.SavedAccountsKey
import com.music.sonic.constants.VisitorDataKey
import com.music.sonic.models.AccountData
import com.music.sonic.ui.component.IconButton
import com.music.sonic.ui.utils.backToMain
import com.music.sonic.utils.rememberPreference
import com.music.sonic.utils.reportException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun LoginScreen(
  navController: NavController,
) {
  val coroutineScope = rememberCoroutineScope()
  var visitorData by rememberPreference(VisitorDataKey, "")
  var dataSyncId by rememberPreference(DataSyncIdKey, "")
  var innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
  var accountName by rememberPreference(AccountNameKey, "")
  var accountEmail by rememberPreference(AccountEmailKey, "")
  var accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
  var savedAccountsJson by rememberPreference(SavedAccountsKey, "[]")
  var hasCompletedLogin by remember { mutableStateOf(false) }

  var webView: WebView? = null

  AndroidView(
    modifier = Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current).fillMaxSize(),
    factory = { webViewContext ->
      WebView(webViewContext).apply {
        webViewClient =
          object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
              loadUrl(
                "javascript:if(window.yt && window.yt.config_){ " +
                "  if(window.yt.config_.VISITOR_DATA) Android.onRetrieveVisitorData(window.yt.config_.VISITOR_DATA); " +
                "  if(window.yt.config_.DATASYNC_ID) Android.onRetrieveDataSyncId(window.yt.config_.DATASYNC_ID); " +
                "}"
              )

              if (url?.startsWith("https://music.youtube.com") == true && !hasCompletedLogin) {
                val cookie = CookieManager.getInstance().getCookie(url)
                if (cookie != null && ("SAPISID" in cookie || "HSID" in cookie)) {
                  hasCompletedLogin = true
                  innerTubeCookie = cookie

                  coroutineScope.launch {
                    delay(500)

                    YouTube.cookie = cookie
                    YouTube.dataSyncId = dataSyncId
                    YouTube.visitorData = visitorData

                    Timber.d("Login: YouTube object initialized, validating...")

                    YouTube.accountInfo()
                      .onSuccess {
                        accountName = it.name
                        accountEmail = it.email.orEmpty()
                        accountChannelHandle = it.channelHandle.orEmpty()

                        val newAccount =
                          AccountData(
                            name = it.name,
                            email = it.email.orEmpty(),
                            channelHandle = it.channelHandle.orEmpty(),
                            cookie = cookie,
                            visitorData = visitorData,
                            dataSyncId = dataSyncId,
                            avatarUrl = it.thumbnailUrl.orEmpty()
                          )
                        val accounts =
                          try {
                              Json.decodeFromString<List<AccountData>>(savedAccountsJson)
                            } catch (e: Exception) {
                              emptyList()
                            }
                            .toMutableList()
                        accounts.removeAll { acc -> acc.name == newAccount.name }
                        accounts.add(newAccount)
                        savedAccountsJson = Json.encodeToString(accounts)

                        Timber.d("Login: Successfully logged in as ${it.name}")

                        webView?.apply {
                          stopLoading()
                          clearHistory()
                          clearCache(true)
                          clearFormData()
                        }

                        if (navController.previousBackStackEntry != null) {
                          navController.popBackStack()
                        } else {
                          navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                          }
                        }
                      }
                      .onFailure {
                        Timber.e(it, "Login: Authentication validation failed")
                        hasCompletedLogin = false
                        reportException(it)
                      }
                  }
                }
              }
            }
          }
        settings.apply {
          javaScriptEnabled = true
          setSupportZoom(true)
          builtInZoomControls = true
          displayZoomControls = false
        }
        addJavascriptInterface(
          object {
            @JavascriptInterface
            fun onRetrieveVisitorData(newVisitorData: String?) {
              if (!newVisitorData.isNullOrBlank()) {
                coroutineScope.launch(Dispatchers.Main) {
                  visitorData = newVisitorData
                }
              }
            }

            @JavascriptInterface
            fun onRetrieveDataSyncId(newDataSyncId: String?) {
              if (!newDataSyncId.isNullOrBlank()) {
                val cleanSyncId = newDataSyncId.substringBefore("||")
                coroutineScope.launch(Dispatchers.Main) {
                  dataSyncId = cleanSyncId
                }
              }
            }
          },
          "Android"
        )
        webView = this

        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        loadUrl("https://accounts.google.com/ServiceLogin?continue=https%3A%2F%2Fmusic.youtube.com")
      }
    }
  )

  TopAppBar(
    title = { Text(stringResource(R.string.login)) },
    navigationIcon = {
      IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
      }
    }
  )

  BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }
}
