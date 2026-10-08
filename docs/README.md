# SonicSpace Documentation

Welcome to the SonicSpace developer and contributor documentation hub. Here you'll find comprehensive guides covering installation, architecture, UI design standards, contribution processes, and project policies.

---

## Documentation Index

| Document | Description |
| :--- | :--- |
| **[Setup Guide](SETUP.md)** | Step-by-step instructions for setting up the local build environment, prerequisites (JDK 21, Android SDK), build variants, and running the app. |
| **[Design System & Guidelines](DESIGN.md)** | Core visual philosophy, Liquid Glass blur system, typography, component rules, and UI patterns for SonicSpace. |
| **[Contributing Guidelines](CONTRIBUTING.md)** | Code conventions, Kotlin/Compose best practices, Git workflow, pull request checklist, and commit guidelines. |
| **[Code of Conduct](CODE_OF_CONDUCT.md)** | Community standards and expectations under the Contributor Covenant. |
| **[Security Policy](SECURITY.md)** | Security vulnerability reporting guidelines, supported versions, and best practices. |
| **[Privacy Policy](PRIVACY_POLICY.md)** | Information regarding user privacy, local storage, analytics, and data handling. |
| **[Release History](RELEASE_INFO.md)** | Historical version release notes, changelogs, and notable milestone updates. |

---

## Architectural Overview

SonicSpace is organized as a modular Android application:

```
SonicSpace/
├── app/                  # Main Android application (UI, ViewModels, DI, orchestration)
├── modules/              # Modular architecture components
│   ├── core/             # Shared data models, Room database, DataStore, constants
│   ├── playback/         # Media3 / ExoPlayer playback engine, audio pipeline, queues
│   ├── unison/           # Cross-cutting shared utilities and common base classes
│   ├── innertube/        # YouTube Music InnerTube API client
│   ├── shazamkit/        # Music recognition service ("Echo Find")
│   ├── artistvideo/      # Artist video features
│   ├── lyrics/           # Lyrics orchestration and AI translation engine
│   ├── canvas/           # Video canvas background loop engine
│   └── providers/        # Content provider modules
│       ├── lyrics/       # Lyrics providers (betterlyrics, kugou, lrclib, paxsenix, simp, youly)
│       └── canvas/       # Canvas providers (applecanvas, echomusiccanvas)
├── docs/                 # Project documentation and guides
└── scripts/              # Build, asset, and maintenance scripts
```

For guidelines on coding style and contributing new features, please refer to [Contributing Guidelines](CONTRIBUTING.md).
