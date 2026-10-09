@file:Suppress("DEPRECATION")

package com.music.sonic.ui.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import coil3.compose.AsyncImage
import com.music.sonic.R
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.datastore.preferences.core.edit
import com.music.sonic.constants.DarkModeKey
import com.music.sonic.constants.DynamicNavStyleKey
import com.music.sonic.constants.EnableHapticsKey
import com.music.sonic.constants.HasSeenOnboardingKey
import com.music.sonic.constants.HideNavLabelsKey
import com.music.sonic.constants.VibrationStrengthKey
import com.music.sonic.utils.HapticFeedbackService
import com.music.sonic.utils.dataStore
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 9 })
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val hasHaptics = remember { vibrator?.hasVibrator() == true }

    var isLoggedIn by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Connected User") }
    var userProfileUrl by remember { mutableStateOf<String?>(null) }

    var hapticsLevel by rememberSaveable { mutableFloatStateOf(prefs.getFloat("vibration_strength", 80f)) }
    var themeMode by rememberSaveable { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }
    var hideNavLabels by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_nav_labels", false)) }
    var dynamicNavStyle by rememberSaveable { mutableStateOf(prefs.getBoolean("dynamic_nav_style", false)) }

    val hapticsService = remember {
        HapticFeedbackService(
            context = context.applicationContext,
            isEnabled = true,
            strength = hapticsLevel
        )
    }

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
    val micPermission = Manifest.permission.RECORD_AUDIO

    var audioGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED) }
    var notifGranted by remember { mutableStateOf(notifPermission?.let { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED } ?: true) }
    var micGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, micPermission) == PackageManager.PERMISSION_GRANTED) }

    val allPermissionsGranted = audioGranted && notifGranted && micGranted

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        audioGranted = permissions[audioPermission] ?: audioGranted
        if (notifPermission != null) {
            notifGranted = permissions[notifPermission] ?: notifGranted
        }
        micGranted = permissions[micPermission] ?: micGranted
    }

    Scaffold(
        bottomBar = {
            val isNextEnabled = if (pagerState.currentPage == 4) allPermissionsGranted else true
            SetupBottomBar(
                pagerState = pagerState,
                isNextEnabled = isNextEnabled,
                onNextClicked = {
                    if (pagerState.currentPage == 4 && !allPermissionsGranted) {
                        val permsToRequest = mutableListOf<String>()
                        if (!audioGranted) permsToRequest.add(audioPermission)
                        if (!notifGranted && notifPermission != null) permsToRequest.add(notifPermission)
                        if (!micGranted) permsToRequest.add(micPermission)
                        if (permsToRequest.isNotEmpty()) {
                            permissionLauncher.launch(permsToRequest.toTypedArray())
                        }
                    } else if (isNextEnabled && pagerState.currentPage < pagerState.pageCount - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                            )
                        }
                    }
                },
                onBackClicked = {
                    if (pagerState.currentPage > 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage - 1,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                            )
                        }
                    }
                },
                onFinishClicked = {
                    prefs.edit {
                        putBoolean("has_seen_onboarding_v1", true)
                            .putFloat("vibration_strength", hapticsLevel)
                            .putString("theme_mode", themeMode)
                            .putBoolean("hide_nav_labels", hideNavLabels)
                            .putBoolean("dynamic_nav_style", dynamicNavStyle)
                    }
                    scope.launch {
                        context.dataStore.edit { data ->
                            data[HasSeenOnboardingKey] = true
                            data[HideNavLabelsKey] = hideNavLabels
                            data[DynamicNavStyleKey] = dynamicNavStyle
                            data[VibrationStrengthKey] = hapticsLevel
                            data[EnableHapticsKey] = (hapticsLevel > 0f)
                            val darkModeVal = when (themeMode.lowercase()) {
                                "dark" -> "ON"
                                "light" -> "OFF"
                                else -> "AUTO"
                            }
                            data[DarkModeKey] = darkModeVal
                        }
                    }
                    onComplete()
                }
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            val pageOffsetProvider = {
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> WelcomePage(pageOffsetProvider)
                    1 -> EcosystemPage(pageOffsetProvider)
                    2 -> FeatureListPage(pageOffsetProvider)
                    3 -> SignInPage(
                        pageOffsetProvider = pageOffsetProvider,
                        isLoggedIn = isLoggedIn,
                        userName = userName,
                        userProfileUrl = userProfileUrl,
                        onLoginSuccess = { isLoggedIn = true },
                        onGuestClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        }
                    )
                    4 -> PermissionsPage(
                        pageOffsetProvider = pageOffsetProvider,
                        audioGranted = audioGranted,
                        notifGranted = notifGranted,
                        micGranted = micGranted,
                        onRequestAudio = { permissionLauncher.launch(arrayOf(audioPermission)) },
                        onRequestNotif = { notifPermission?.let { permissionLauncher.launch(arrayOf(it)) } },
                        onRequestMic = { permissionLauncher.launch(arrayOf(micPermission)) }
                    )
                    5 -> ThemeSelectionPage(
                        pageOffsetProvider = pageOffsetProvider,
                        themeMode = themeMode,
                        onThemeChanged = {
                            themeMode = it
                            prefs.edit { putString("theme_mode", it) }
                            val darkModeVal = when (it.lowercase()) {
                                "dark" -> "ON"
                                "light" -> "OFF"
                                else -> "AUTO"
                            }
                            scope.launch { context.dataStore.edit { data -> data[DarkModeKey] = darkModeVal } }
                            hapticsService.performClick(force = true)
                        }
                    )
                    6 -> NavigationStylePage(
                        pageOffsetProvider = pageOffsetProvider,
                        hideNavLabels = hideNavLabels,
                        dynamicNavStyle = dynamicNavStyle,
                        onHideNavLabelsChanged = {
                            hideNavLabels = it
                            prefs.edit { putBoolean("hide_nav_labels", it) }
                            scope.launch { context.dataStore.edit { data -> data[HideNavLabelsKey] = it } }
                            hapticsService.performToggle(it, force = true)
                        },
                        onDynamicNavStyleChanged = {
                            dynamicNavStyle = it
                            prefs.edit { putBoolean("dynamic_nav_style", it) }
                            scope.launch { context.dataStore.edit { data -> data[DynamicNavStyleKey] = it } }
                            hapticsService.performToggle(it, force = true)
                        }
                    )
                    7 -> PreferencesPage(
                        pageOffsetProvider = pageOffsetProvider,
                        hapticsLevel = hapticsLevel,
                        onHapticsLevelChanged = {
                            hapticsLevel = it
                            prefs.edit { putFloat("vibration_strength", it) }
                            scope.launch {
                                context.dataStore.edit { data ->
                                    data[VibrationStrengthKey] = it
                                    data[EnableHapticsKey] = (it > 0f)
                                }
                            }
                            hapticsService.previewVibration(it)
                        }
                    )
                    8 -> FinishPage(
                        pageOffsetProvider = pageOffsetProvider,
                        isLoggedIn = isLoggedIn,
                        userProfileUrl = userProfileUrl
                    )
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ImmersivePageLayout(
    pageOffsetProvider: () -> Float,
    drawableRes: Int?,
    imageUrl: String? = null,
    imageScale: Float = 1f,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = if (screenWidth < 360.dp) 20.dp else if (screenWidth > 480.dp) 32.dp else 28.dp
    val verticalPadding = if (screenWidth < 360.dp) 16.dp else if (screenWidth > 480.dp) 24.dp else 20.dp

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationX = offset * 15f
                    rotationZ = offset * 2f
                    scaleX = imageScale + kotlin.math.abs(offset * 0.08f)
                    scaleY = imageScale + kotlin.math.abs(offset * 0.08f)
                }
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Background decoration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                )
            } else if (drawableRes != null) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = "Background decoration",
                    contentScale = ContentScale.Fit,
                    colorFilter = if (iconTint == Color.Unspecified) null else ColorFilter.tint(iconTint),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )
            } else {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxSize()) {}
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            content()
        }
    }
}

@Composable
fun WelcomePage(pageOffsetProvider: () -> Float) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.ic_launcher_foreground,
        imageScale = 1.4f,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Welcome\nto SonicSpace.",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * 30f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Let's setup everything for you.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * 50f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun EcosystemPage(pageOffsetProvider: () -> Float) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.music_note,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Your\nComplete\nEcosystem.",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * -30f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Stream millions of tracks or play your local library, perfectly synced.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * -60f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun FeatureListPage(pageOffsetProvider: () -> Float) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.equalizer,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Stream",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 20f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                }
            )
            Text(
                text = "Discover",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 40f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                }
            )
            Text(
                text = "Sing Along.",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 60f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Karaoke lyrics, powerful search, and offline downloads all in one place.",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * 80f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionPage(
    pageOffsetProvider: () -> Float,
    themeMode: String,
    onThemeChanged: (String) -> Unit
) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.palette,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Style it\nyour way.",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * -30f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                Triple("system", "System Default", Icons.Default.Settings),
                Triple("dark", "Dark Mode", Icons.Default.DarkMode),
                Triple("light", "Light Mode", Icons.Default.LightMode)
            ).forEachIndexed { index, (mode, label, _) ->
                Card(
                    onClick = { onThemeChanged(mode) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(
                        width = if (themeMode == mode) 2.dp else 1.dp,
                        color = if (themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .graphicsLayer {
                            val offset = pageOffsetProvider()
                            translationY = offset * (50f + (index * 40f))
                            alpha = 1f - kotlin.math.abs(offset * 1.5f)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .then(
                                    if (themeMode != mode) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (themeMode == mode) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (themeMode == mode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NavigationStylePage(
    pageOffsetProvider: () -> Float,
    hideNavLabels: Boolean,
    dynamicNavStyle: Boolean,
    onHideNavLabelsChanged: (Boolean) -> Unit,
    onDynamicNavStyleChanged: (Boolean) -> Unit
) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.nav_bar,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Navigate\nSeamlessly.",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * -30f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 25f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            val previewNavHeight by animateDpAsState(
                targetValue = if (dynamicNavStyle) {
                    if (hideNavLabels) 76.dp else 88.dp
                } else {
                    84.dp
                },
                label = "previewNavHeight"
            )
            val previewIconSize by animateDpAsState(
                targetValue = if (dynamicNavStyle) {
                    if (hideNavLabels) 28.dp else 24.dp
                } else {
                    26.dp
                },
                label = "previewIconSize"
            )

            NavigationBar(
                modifier = Modifier.fillMaxWidth().height(previewNavHeight),
                containerColor = Color.Transparent,
                tonalElevation = if (dynamicNavStyle) 10.dp else 0.dp,
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp)
            ) {
                listOf("Home" to Icons.Default.Home, "Search" to Icons.Default.Search, "Library" to Icons.Default.LibraryMusic).forEachIndexed { index, item ->
                    val selected = index == 0
                    val isLabelVisible = !hideNavLabels
                    val labelComposable: (@Composable () -> Unit)? = if (isLabelVisible) {
                        @Composable {
                            Text(
                                item.first,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else null
                    NavigationBarItem(
                        selected = selected,
                        onClick = {},
                        icon = {
                            if (dynamicNavStyle) {
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (selected) 1.22f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "previewDynamicIconScale"
                                )
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        item.second,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                scaleX = animatedScale
                                                scaleY = animatedScale
                                            }
                                            .size(previewIconSize)
                                    )
                                }
                            } else {
                                Icon(item.second, contentDescription = null, modifier = Modifier.size(previewIconSize))
                            }
                        },
                        label = labelComposable,
                        alwaysShowLabel = if (dynamicNavStyle) false else isLabelVisible,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = if (dynamicNavStyle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            selectedIconColor = if (dynamicNavStyle) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 50f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hide Nav Labels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remove text labels from the bottom navigation bar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(checked = hideNavLabels, onCheckedChange = onHideNavLabelsChanged)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 100f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Navbar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compact height with bold, elevated icons.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(checked = dynamicNavStyle, onCheckedChange = onDynamicNavStyleChanged)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PreferencesPage(
    pageOffsetProvider: () -> Float,
    hapticsLevel: Float,
    onHapticsLevelChanged: (Float) -> Unit
) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.vibration,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Sensory\nExperience.",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * -30f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * 50f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Music Haptics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Adjust the intensity of beat-synced vibrations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = hapticsLevel,
                    onValueChange = onHapticsLevelChanged,
                    valueRange = 0f..100f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionsPage(
    pageOffsetProvider: () -> Float,
    audioGranted: Boolean,
    notifGranted: Boolean,
    micGranted: Boolean,
    onRequestAudio: () -> Unit,
    onRequestNotif: () -> Unit,
    onRequestMic: () -> Unit
) {
    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = R.drawable.storage,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Enable\nPermissions.",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * -30f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionCard(
            title = "Music Library",
            description = "Access local audio files for offline playback.",
            icon = Icons.Default.LibraryMusic,
            isGranted = audioGranted,
            pageOffsetProvider = pageOffsetProvider,
            offsetMultiplier = 200f,
            onRequest = onRequestAudio
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = "Playback Notifications",
                description = "Control music from your lock screen.",
                icon = Icons.Default.Notifications,
                isGranted = notifGranted,
                pageOffsetProvider = pageOffsetProvider,
                offsetMultiplier = 300f,
                onRequest = onRequestNotif
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        PermissionCard(
            title = "Audio Engine",
            description = "Required for immersive effects and synced lyrics.",
            icon = Icons.Default.Mic,
            isGranted = micGranted,
            pageOffsetProvider = pageOffsetProvider,
            offsetMultiplier = 400f,
            onRequest = onRequestMic
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    pageOffsetProvider: () -> Float,
    offsetMultiplier: Float,
    onRequest: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 0.dp, vertical = 6.dp)
            .graphicsLayer {
                val offset = pageOffsetProvider()
                translationY = offset * offsetMultiplier * 0.2f
                alpha = 1f - kotlin.math.abs(offset * 1.5f)
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        onClick = { if (!isGranted) onRequest() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = "Permission icon",
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = if (isGranted) 1.1f else 1f
                            scaleY = if (isGranted) 1.1f else 1f
                        },
                    tint = if (isGranted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isGranted) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Grant $title permission",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OnboardingLoginDialog(
    onDismissRequest: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(500.dp).padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Sign in to YouTube Music",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            android.webkit.WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                webViewClient = object : android.webkit.WebViewClient() {
                                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        val cookieManager = android.webkit.CookieManager.getInstance()
                                        val cookies = cookieManager.getCookie("https://music.youtube.com")
                                        if (cookies != null && cookies.contains("SAPISID") && cookies.contains("HSID")) {
                                            onLoginSuccess()
                                        }
                                    }
                                }
                                loadUrl("https://accounts.google.com/ServiceLogin?service=youtube&passive=true&continue=https://music.youtube.com/")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun SignInPage(
    pageOffsetProvider: () -> Float,
    isLoggedIn: Boolean,
    userName: String,
    userProfileUrl: String?,
    onLoginSuccess: () -> Unit,
    onGuestClick: () -> Unit
) {
    var showWebView by remember { mutableStateOf(false) }

    if (showWebView) {
        OnboardingLoginDialog(
            onDismissRequest = { showWebView = false },
            onLoginSuccess = {
                showWebView = false
                onLoginSuccess()
            }
        )
    }

    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = if (isLoggedIn && userProfileUrl != null) null else R.drawable.search,
        imageUrl = if (isLoggedIn) userProfileUrl else null,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Welcome,\n$userName",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    lineHeight = 48.sp,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.graphicsLayer {
                        val offset = pageOffsetProvider()
                        translationY = offset * -30f
                        alpha = 1f - kotlin.math.abs(offset * 1.5f)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(percent = 50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.graphicsLayer {
                        val offset = pageOffsetProvider()
                        translationY = offset * -20f
                        alpha = 1f - kotlin.math.abs(offset * 1.5f)
                    }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Login Successful", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(120.dp))
            } else {
                Text(
                    text = "Sign In &\nSync.",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    lineHeight = 48.sp,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.graphicsLayer {
                        val offset = pageOffsetProvider()
                        translationY = offset * -30f
                        alpha = 1f - kotlin.math.abs(offset * 1.5f)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Connect your account to sync playlists, liked songs, and preferences across all your devices.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                    lineHeight = 24.sp,
                    modifier = Modifier.graphicsLayer {
                        val offset = pageOffsetProvider()
                        translationY = offset * -20f
                        alpha = 1f - kotlin.math.abs(offset * 1.5f)
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { showWebView = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .graphicsLayer {
                            val offset = pageOffsetProvider()
                            translationY = offset * -10f
                            alpha = 1f - kotlin.math.abs(offset * 1.5f)
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "YouTube Music",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with YouTube Music",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onGuestClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .graphicsLayer {
                            val offset = pageOffsetProvider()
                            translationY = offset * -5f
                            alpha = 1f - kotlin.math.abs(offset * 1.5f)
                        },
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FinishPage(
    pageOffsetProvider: () -> Float,
    isLoggedIn: Boolean,
    userProfileUrl: String?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    ImmersivePageLayout(
        pageOffsetProvider = pageOffsetProvider,
        drawableRes = if (isLoggedIn && userProfileUrl != null) null else R.drawable.search,
        imageUrl = if (isLoggedIn) userProfileUrl else null,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * -30f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Setup Complete",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ready to\nFlow.",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                fontSize = 42.sp,
                lineHeight = 48.sp,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * -20f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your library is fully initialized and the engine is primed. It's time to immerse yourself in the ultimate auditory experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                lineHeight = 24.sp,
                modifier = Modifier.graphicsLayer {
                    val offset = pageOffsetProvider()
                    translationY = offset * -10f
                    alpha = 1f - kotlin.math.abs(offset * 1.5f)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupBottomBar(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    isNextEnabled: Boolean = true,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onFinishClicked: () -> Unit
) {
    val morphAnimationSpec = tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing)
    val rotationAnimationSpec = tween<Float>(durationMillis = 900, easing = FastOutSlowInEasing)

    val targetShapeValues = when (pagerState.currentPage % 3) {
        0 -> listOf(50f, 50f, 50f, 50f)
        1 -> listOf(26f, 26f, 26f, 26f)
        else -> listOf(18f, 50f, 18f, 50f)
    }

    val animatedTopStart by animateFloatAsState(targetShapeValues[0], morphAnimationSpec, label = "TopStart")
    val animatedTopEnd by animateFloatAsState(targetShapeValues[1], morphAnimationSpec, label = "TopEnd")
    val animatedBottomStart by animateFloatAsState(targetShapeValues[2], morphAnimationSpec, label = "BottomStart")
    val animatedBottomEnd by animateFloatAsState(targetShapeValues[3], morphAnimationSpec, label = "BottomEnd")

    val animatedRotation by animateFloatAsState(
        targetValue = pagerState.currentPage * 360f,
        animationSpec = rotationAnimationSpec,
        label = "Rotation"
    )

    val shape = RoundedCornerShape(
        topEnd = 38.dp,
        topStart = 38.dp,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    )

    Surface(
        modifier = modifier.shadow(elevation = 10.dp, shape = shape, clip = true),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = (pagerState.currentPage + 1f) / pagerState.pageCount,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label = "progress"
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = pagerState.currentPage,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "StepTextAnimation"
                ) { targetPage ->
                    if (targetPage == 0) {
                        Text(
                            text = "Let's Go!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBackClicked,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Step $targetPage of ${pagerState.pageCount - 1}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                            )
                        }
                    }
                }

                val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
                val fabAlpha by animateFloatAsState(if (isNextEnabled || isLastPage) 1f else 0.4f, label = "fab_alpha")

                AnimatedContent(
                    targetState = isLastPage,
                    label = "FabOrExtended",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith fadeOut(animationSpec = tween(90))
                    }
                ) { isFinish ->
                    if (isFinish) {
                        ExtendedFloatingActionButton(
                            onClick = onFinishClicked,
                            text = { Text("Start Listening", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Check, contentDescription = "Finish") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        )
                    } else {
                        FloatingActionButton(
                            onClick = { if (isNextEnabled) onNextClicked() },
                            shape = RoundedCornerShape(
                                topStartPercent = animatedTopStart.toInt(),
                                topEndPercent = animatedTopEnd.toInt(),
                                bottomStartPercent = animatedBottomStart.toInt(),
                                bottomEndPercent = animatedBottomEnd.toInt()
                            ),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .rotate(animatedRotation)
                                .graphicsLayer { alpha = fabAlpha }
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.rotate(-animatedRotation)
                            )
                        }
                    }
                }
            }
        }
    }
}
