# SonicSpace Documentation

Welcome to the official developer and contributor documentation hub for **SonicSpace**.

This repository contains comprehensive reference documentation covering architectural principles, development setup, design guidelines, contribution processes, and project policies.

---

## Documentation Index

| Guide | Description |
| :--- | :--- |
| **[Setup Guide](SETUP.md)** | Step-by-step instructions for local build setup, JDK 21 requirements, Android SDK configuration, and running the app. |
| **[Design System](DESIGN.md)** | Visual guidelines, Liquid Glass blur system, elevated dynamic navigation, typography, and Material 3 Expressive color theming. |
| **[Contributing Guidelines](CONTRIBUTING.md)** | Workflow for opening issues and pull requests, Kotlin code formatting, and review standards. |
| **[Code of Conduct](CODE_OF_CONDUCT.md)** | Community pledge and expectations for inclusive, respectful collaboration. |
| **[Security Policy](SECURITY.md)** | Guidelines for responsible vulnerability disclosure and security considerations. |
| **[Privacy Policy](PRIVACY_POLICY.md)** | Transparency on local storage, network requests, and zero-telemetry practices. |
| **[Release Information](RELEASE_INFO.md)** | Version changelogs, updates, and release history. |

---

## Architectural Principles

SonicSpace is structured as a modern multi-module Android project:

```
SonicSpace/
├── app/                  # Application layer: Compose UI, ViewModels, Navigation, DI
├── modules/              # Domain, Data, and Feature Modules
│   ├── core/             # Database (Room), DataStore preferences, domain models, constants
│   ├── playback/         # Media3 ExoPlayer engine, background audio service, cache, queues
│   ├── unison/           # Cross-cutting utilities, common helpers, extensions
│   ├── innertube/        # YouTube Music InnerTube client
│   ├── shazamkit/        # Sonic Find ambient audio recognition engine
│   ├── artistvideo/      # Artist video playback integrations
│   ├── lyrics/           # Multi-provider lyrics orchestration & AI translation
│   ├── canvas/           # Looping video canvas background playback
│   └── providers/        # External content source provider implementations
│       ├── lyrics/       # Lyrics providers (betterlyrics, kugou, lrclib, paxsenix, simp, youly)
│       └── canvas/       # Canvas providers (applecanvas, soniccanvas)
├── docs/                 # Documentation directory
├── scripts/              # Build scripts and automation helpers
└── assets/               # Visual media and badges
```

### Core Architecture Highlights

1. **Unidirectional Data Flow**: Screens observe UI state exposed as Kotlin `StateFlow` from ViewModels and trigger actions via standard events.
2. **Modular Independence**: Core business logic, playback services, and network clients are encapsulated in separate modules to ensure fast incremental build times and clear boundaries.
3. **Jetpack Compose Native**: The entire interface is built with Compose, avoiding legacy XML views.
4. **AndroidX Media3 Service**: Audio playback runs in a dedicated foreground service using MediaSession and ExoPlayer, handling audio focus, lockscreen controls, and system media notifications seamlessly.
5. **Dynamic Harmonic Theming**: Theme colors adapt to the active track's album art using an HSL-harmonic palette generator that guarantees contrast-safe, non-harsh palettes for Dark, Light, and AMOLED black modes.

For developer environment configuration, proceed to the **[Setup Guide](SETUP.md)**.
