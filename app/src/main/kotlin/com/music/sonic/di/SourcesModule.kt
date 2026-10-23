package com.music.sonic.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for the OkHttpClient used exclusively by the multi-source audio resolvers
 * (AddonSource, JioSaavnSource). Keeps it separate from MusicService's own client.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SourcesHttpClient

@Module
@InstallIn(SingletonComponent::class)
object SourcesModule {

    /**
     * A dedicated OkHttpClient for external audio source lookups.
     * Short timeouts are intentional: if an external source doesn't respond quickly,
     * we fall through to the next source rather than stalling playback.
     */
    @Provides
    @Singleton
    @SourcesHttpClient
    fun provideSourcesOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
}
