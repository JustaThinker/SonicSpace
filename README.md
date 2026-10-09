<div align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher_static_round.png" alt="SonicSpace Logo" width="108"/>

  <h1>SonicSpace</h1>

  <p><b>A modern, high-performance, ad-free Android music client built for focus, speed, and beautiful listening.</b></p>

  <p>
    <a href="https://github.com/JustaThinker/SonicSpace/releases/latest"><img src="https://img.shields.io/github/v/release/JustaThinker/SonicSpace?style=flat-square&color=blue" alt="Latest Release"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-green.svg?style=flat-square" alt="License"></a>
    <img src="https://img.shields.io/badge/Kotlin-2.3-purple.svg?style=flat-square" alt="Kotlin 2.3">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg?style=flat-square" alt="Compose">
  </p>
</div>

---

### Overview

**SonicSpace** is an open-source native Android music player crafted with Kotlin and Jetpack Compose. It connects seamlessly to the entire YouTube Music catalog without ads, tracks, or subscriptions, pairing fast streaming with offline caching, local audio playback, synchronized lyrics, and dynamic audio visualization.

Built on an expressive, high-contrast visual design, SonicSpace prioritizes pure listening pleasure over algorithmic clutter—giving you complete control over your library, audio processing, and aesthetic.

---

### Key Features

* **Zero Ads & Frictionless Playback** — Stream any track or playlist without ads or interruptions.
* **Offline Downloads & Local Library** — Download tracks, albums, and playlists for offline playback, or play high-resolution audio files stored on your device.
* **Expressive Dynamic Themes** — Harmonic Material 3 Expressive theming that dynamically adapts to album artwork, featuring true AMOLED pure black modes and fine-tuned saturation control.
* **Real-time Synchronized Lyrics** — Word-by-word synced lyrics powered by multiple providers (LRCLIB, KuGou, BetterLyrics, YouLy, Paxsenix) with integrated on-the-fly AI translation.
* **Listen Together** — Host and join real-time synchronized listening rooms with friends.
* **Smart Music Discovery** — Curated Daily Discover, Picked for You shelves, interactive genre chips, and forgotten favorites.
* **Audio Powerhouse** — Built on AndroidX Media3 (ExoPlayer), complete with 10-band graphic equalizer, bass boost, pitch control, sleep timers, and Discord Rich Presence.

---

### Tech Stack & Architecture

SonicSpace is structured into modular Gradle components to maximize build speed, separation of concerns, and testability:

* **Language**: Kotlin 2.3
* **UI**: Jetpack Compose & Material 3 Expressive Theme
* **Audio Engine**: AndroidX Media3 (ExoPlayer)
* **Dependency Injection**: Dagger Hilt
* **Database & Persistence**: Room Database & AndroidX DataStore
* **Networking**: Ktor & OkHttp
* **Image Loading**: Coil 3
* **Palette Engine**: AndroidX Palette with custom HSL harmonic color generator

---

### Repository Structure

```
SonicSpace/
├── app/                  # Main Android application (UI screens, ViewModels, DI, navigation)
├── modules/              # Modular feature and domain components
│   ├── core/             # Shared data models, Room database, DataStore, constants
│   ├── playback/         # Media3/ExoPlayer audio service, queue management, downloads
│   ├── unison/           # Cross-cutting shared utilities and base abstractions
│   ├── innertube/        # YouTube Music InnerTube API client
│   ├── shazamkit/        # Sonic Find ambient audio recognition engine
│   ├── artistvideo/      # Artist music video playback integration
│   ├── lyrics/           # Lyrics orchestration and AI translation pipelines
│   ├── canvas/           # Looping video canvas background playback
│   └── providers/        # External content source providers
│       ├── lyrics/       # Lyrics providers (betterlyrics, kugou, lrclib, paxsenix, simp, youly)
│       └── canvas/       # Canvas video loop providers
├── docs/                 # Detailed architectural, setup, and design documentation
├── scripts/              # Build, asset, and automation scripts
└── assets/               # Graphical assets and logos
```

---

### Documentation Hub

Detailed documentation is available in the [`docs/`](docs/) directory:

| Guide | Description |
| :--- | :--- |
| **[Setup & Build Guide](docs/SETUP.md)** | Step-by-step instructions for JDK 21, Android Studio, and building APKs |
| **[Design System](docs/DESIGN.md)** | UI guidelines, Liquid Glass blur, elevated navigation, and theme system |
| **[Contributing](docs/CONTRIBUTING.md)** | Pull request workflow, coding conventions, and repository standards |
| **[Code of Conduct](docs/CODE_OF_CONDUCT.md)** | Community standards and engagement pledges |
| **[Security Policy](docs/SECURITY.md)** | Vulnerability reporting and security considerations |
| **[Privacy Policy](docs/PRIVACY_POLICY.md)** | Data handling, analytics, and device permission policies |
| **[Release History](docs/RELEASE_INFO.md)** | Changelogs and version release milestones |

---

### Building SonicSpace

Prerequisites: **JDK 21** and Android SDK 35 installed.

```bash
# Clone the repository
git clone https://github.com/JustaThinker/SonicSpace.git
cd SonicSpace

# Build debug APK
./gradlew assembleUniversalGmsDebug

# Run unit tests
./gradlew test
```

*(On Windows, run `.\gradlew.bat` instead of `./gradlew`)*

---

### Credits & Special Thanks

SonicSpace is made possible thanks to the work of the open-source community and these projects:

* **[ViMusic](https://github.com/vFSFitvNM/ViMusic)** — For foundational Android music streaming patterns and InnerTube concepts.
* **EchoMusic** — For app updater infrastructure, canvas video loop providers, and upstream project foundations.
* **BitChord** — For responsive layout patterns, duotone category card designs, and desktop cross-platform paradigms.
* **SpatialFlow** — For the SpatialFlow dynamic harmonic HSL theme engine, palette color generator, and expressive vector system.
* **[SimpMusic](https://github.com/maxrave-dev/SimpMusic)** — For inspiration on lyrics providers, canvas video loops, and PipePipe integration.
* **[NewPipe](https://github.com/TeamNewPipe/NewPipe) & [PipePipe](https://github.com/maxrave-dev/PipePipeExtractor)** — For media stream extraction logic and parsing utilities.
* **[LRCLIB](https://lrclib.net)** — For open-source synchronized lyrics API services.
* **[KuGou](https://www.kugou.com/)** — For KuGou lyrics search and decoding integration.

---

### License & Disclaimer

SonicSpace is released under the [GNU General Public License v3.0](LICENSE). 

SonicSpace is an open-source client intended for personal listening that interfaces with publicly available endpoints. SonicSpace is not affiliated with, endorsed by, or associated with Google or YouTube. All media rights and trademarks belong to their respective copyright holders.
