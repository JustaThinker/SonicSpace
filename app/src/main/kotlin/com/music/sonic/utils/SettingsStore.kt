package com.music.sonic.utils

import android.content.Context
import com.music.sonic.constants.AddonEnabledKey
import com.music.sonic.constants.AddonServerUrlKey
import com.music.sonic.constants.DataSaverEnabledKey
import com.music.sonic.constants.JioSaavnEnabledKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin DataStore wrapper providing synchronous (blocking) reads of key settings
 * that are needed from non-Compose, non-coroutine contexts (e.g. MusicSource impls).
 *
 * All reads call [DataStore.get] which internally uses [runBlocking] on [Dispatchers.IO],
 * so callers should already be on a background thread (the sources run on [Dispatchers.IO]).
 */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** True when the addon server feature is enabled by the user. */
    val addonEnabled: Boolean
        get() = context.dataStore[AddonEnabledKey] ?: false

    /** Base URL of the user-configured addon server, or blank string if not set. */
    val addonServerUrl: String
        get() = context.dataStore[AddonServerUrlKey] ?: ""

    /** True when JioSaavn 320 kbps fallback is enabled (default true). */
    val jiosaavnEnabled: Boolean
        get() = context.dataStore[JioSaavnEnabledKey] ?: true

    /** True when Data Saver mode is active (disables all external network sources). */
    val dataSaverEnabled: Boolean
        get() = context.dataStore[DataSaverEnabledKey] ?: false
}
