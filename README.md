<div align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher_static_round.png" alt="SonicSpace Logo" width="108"/>

  <h1>SonicSpace</h1>

  <p><b>A minimalist, ad-free Android music client built for focus, speed, and beautiful listening.</b></p>

  <p>
    <a href="https://github.com/JustaThinker/SonicSpace/releases/latest"><img src="https://img.shields.io/github/v/release/JustaThinker/SonicSpace?style=flat-square&color=blue" alt="Latest Release"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-green.svg?style=flat-square" alt="License"></a>
  </p>
</div>

---

### Overview

**SonicSpace** is a native Android music app crafted in Kotlin and Jetpack Compose. It gives you access to the full YouTube Music catalog completely ad-free, paired with offline downloads, real-time synced lyrics.

Built around a clean, high-contrast visual identity, SonicSpace strips away algorithmic clutter in favor of smooth navigation, and deep sound customization.

---

### Highlights

* **Ad-Free & Offline** — Stream any track without ads, play local files, and download music for offline listening.
* **Minimalist UI** — High-contrast design language built with Jetpack Compose, dynamic Material You color schemes.
* **Youtube Sync** — Effortlessly import and synchronize your Youtube favorites with a single tap.
* **Listen Together** — Host real-time synced listening sessions with friends.

---

### Tech Stack

Built natively with **Kotlin 2.3** & **Jetpack Compose** using **Media3 (ExoPlayer)** for low-latency playback, **Hilt** for dependency injection, **Room** & **DataStore** for persistence, and **Ktor** for networking.

---

### Documentation

| Guide | Description |
| :--- | :--- |
| **[Setup & Build Guide](docs/SETUP.md)** | Prerequisites, local environment setup, build variants, and running the app |
| **[Design System](docs/DESIGN.md)** | Minimalist design language, Liquid Glass blur system, and UI patterns |
| **[Contributing](docs/CONTRIBUTING.md)** | Guidelines for PRs, coding conventions, and Git workflow |
| **[Security Policy](docs/SECURITY.md)** | Reporting vulnerabilities and security guidelines |
| **[All Documentation](docs/README.md)** | Complete documentation hub including Privacy Policy and Release History |

---

### Repository Structure

```
SonicSpace/
├── app/                  # Main Android application (UI, ViewModels, DI, navigation)
├── modules/              # Modular architecture components
│   ├── core/             # Shared domain models, Room database, DataStore, constants
│   ├── playback/         # Media3/ExoPlayer playback engine and audio pipeline
│   ├── unison/           # Cross-cutting shared utilities
│   ├── innertube/        # YouTube Music InnerTube API client
│   ├── shazamkit/        # Echo Find music recognition service
│   ├── artistvideo/      # Artist video playback service
│   ├── lyrics/           # Lyrics orchestration and AI translation
│   ├── canvas/           # Canvas video loop background engine
│   └── providers/        # External content source provider implementations
│       ├── lyrics/       # Providers (betterlyrics, kugou, lrclib, paxsenix, simp, youly)
│       └── canvas/       # Providers (applecanvas, echomusiccanvas)
├── docs/                 # Documentation hub and developer guides
├── scripts/              # Project maintenance and asset generation scripts
├── assets/               # Media badges and graphics
└── licenses/             # Open-source third-party licenses
```

---

### Credits

SonicSpace stands on the shoulders of the open-source community. Special thanks to these projects:

| Project | Contribution / Inspiration |
| :--- | :--- |
| **[BitChord](https://github.com/kushagrasinghx/BitChord)** | BitChord Design pattern, responsive feed design, and UI inspiration |
| **[Echo Music](https://github.com)** | Core application initializers, updater infrastructure, and player components |
| **[SimpMusic](https://github.com/maxrave-dev/SimpMusic)** | YouTube Music client architecture and synchronized lyrics implementation reference |
| **[Metrolist](https://github.com/vFS/Metrolist)** | Material You UI patterns, InnerTube playback queue handling, and audio engine inspiration |
| **[SpatialFlow](https://github.com)** | Vector drawables and custom icon set |

---

### License & Disclaimer

Released under the [GNU General Public License v3.0](LICENSE). SonicSpace is an open-source client intended for personal use that accesses publicly available media endpoints. All media rights belong to their respective owners.
