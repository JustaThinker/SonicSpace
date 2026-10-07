<div align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher_static_round.png" alt="SonicSpace Logo" width="120"/>

  <h1>SonicSpace</h1>

  <p><b>A modern, ad-free Android music client built with Jetpack Compose, featuring real-time synced lyrics, Spotify import, offline playback, and a clean, minimal aesthetic.</b></p>

  <p>
    <a href="https://github.com/JustaThinker/SonicSpace/releases/latest"><img src="https://img.shields.io/github/v/release/JustaThinker/SonicSpace?style=flat-square&color=blue" alt="Latest Release"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-green.svg?style=flat-square" alt="License"></a>
  </p>
</div>

---

## About SonicSpace

**SonicSpace** is a native Android music application written in Kotlin and Jetpack Compose. It streams the full YouTube Music catalog completely ad-free, packed with power-user features including offline downloads, multi-source synchronized lyrics with AI translation, one-tap Spotify playlist import & fast sync, real-time group listening ("Listen Together"), and ambient music recognition ("Echo Find").

Designed from the ground up around a minimal, high-contrast design language with optional **Liquid Glass** translucent blur effects.

---

## Features

### Streaming & Playback
- **Ad-Free Streaming** — Stream high-quality audio without interruptions or ads.
- **Offline Downloads** — Download songs, albums, and playlists to your device for offline listening.
- **Background & Audio-Only Mode** — Seamless background playback when the screen is off or while using other apps.
- **Local Media Support** — Play local audio files stored directly on your device alongside online streams.
- **Crossfade & Gapless** — Smooth audio transitions between consecutive tracks.
- **Data Saver Mode** — Reduce data consumption automatically on limited networks.

### Design & UI
- **Minimalist Aesthetic** — High-contrast, stark typography, monochrome accents, and clean geometry.
- **Liquid Glass Effects** — Frosted glass translucent top bars and floating navigation toolbars.
- **Dynamic Color Themes** — Dynamic Material You color schemes with custom seed colors and Pure Black dark mode.
- **Fluid Animations** — GPU-accelerated Compose layout transitions and spring physics.

### Search & Discovery
- **Duotone Explore Grid** — Responsive mood & genre grid with duotone color palettes and dynamic artwork loading.
- **Smart Recommendations** — Personalized Daily Discover, Picked for You, Forgotten Favorites, and Community Playlists.
- **Echo Find** — Identify music playing in your surrounding environment using microphone listening.
- **Charts & Top Picks** — Browse global top charts, new releases, and trending mixes.

### Lyrics & Sync
- **Synchronized Lyrics** — Real-time synced lyrics with word-by-word highlight support.
- **Multi-Source Engine** — Aggregates lyrics from LRCLIB, KuGou, BetterLyrics, YouLyPlus, and Paxsenix.
- **AI Lyrics Translation** — On-the-fly translation of lyrics into your preferred language.

### Integrations
- **Spotify Fast Sync** — One-tap import and fast synchronization of your Spotify playlists.
- **Listen Together** — Real-time synchronized listening rooms with friends.
- **Discord Rich Presence** — Showcase your current track on Discord.
- **Google Cast** — Cast playback to Chromecast and Smart TVs.

---

## Tech Stack

- **Language**: Kotlin 2.3.10 (JVM Target 21)
- **UI Framework**: Jetpack Compose, Material 3, Adaptive Layouts (`androidx.compose.material3.adaptive`)
- **Playback Engine**: Media3 / ExoPlayer
- **Dependency Injection**: Hilt
- **Persistence**: Room Database, DataStore Preferences
- **Networking**: Ktor Client, OkHttp, Retrofit
- **Image Loading**: Coil 3
- **Animations**: Lottie Compose, Shimmer

---

## License

SonicSpace is open-source software licensed under the [GNU General Public License v3.0](LICENSE).

---

## Legal Disclaimer

SonicSpace is an open-source, non-commercial client application created for educational and personal use. It does not host, upload, or store any media content on its own servers. All content is accessed directly from publicly available web endpoints. All trademarks and media belong to their respective owners.
