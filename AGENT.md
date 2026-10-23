# AGENT.md - SonicSpace

Context file for AI agents (Antigravity, Claude Code, etc.) working in this repo.
Keep this file up to date as the project evolves — it gives the agent full context
without re-scanning the codebase every session.

---

## Development Rules & Guidelines

> **Maintenance rule:** whenever you add or change a feature, structure, module, build config, or convention in this repo, **update this file in the same PR/commit**. Treat an out-of-date `AGENT.md` as a bug.

> **Dynamic Design Pattern rule:** Whenever you make ANY changes or updates to the UI, layout, or components, you **must automatically document** the new or updated design patterns directly in this `AGENT.md` file (and `docs/DESIGN.md` if applicable) during the same session.

> **Attribution rule:** if a feature is ported from, adapted from, or inspired by another open-source project (even partially — a UI pattern, an algorithm, a parsing approach, a whole file), you **must** add or update an entry in `README.md`'s **Credits / Special Thanks** section in the same commit with clear, specific attribution.

> **Upcoming Update rule:** whenever you add a new feature, fix a bug, or merge a PR, you **must** update the `upcomingupdate.json` file in the root directory. Add your changes to the appropriate array (`features` or `bug_fixes`) so that they are tracked for updates.

> **Do not push without explicit instruction.** Making code changes (editing files, committing locally) is fine whenever asked, but **never run `git push` — or otherwise publish changes to GitHub — unless the user explicitly tells you to push** in that message.

> **Pre-push checklist:**
> 1. Remove all temporary/scratch files (`.py`, `.sh`, `.log`, scratch scripts).
> 2. Verify compilation is clean (`./gradlew :app:compileGmsDebugKotlin`).
> 3. Write conventional commits (`feat(ui): ...`, `fix(playback): ...`).
> 4. Keep `AGENT.md` and documentation up to date.

> **Build variant:** Always use the GMS variant (`./gradlew assembleGmsDebug` or on Windows `.\gradlew.bat assembleGmsDebug`). Release outputs: `SonicSpace-gms.apk` (`:app:assembleGmsRelease`) and `SonicSpace-foss.apk` (`:app:assembleFossRelease`).

---

## What SonicSpace Is

**SonicSpace** is a native **Android** music streaming client (Kotlin 2.3 + Jetpack Compose) that connects to YouTube Music's catalog ad-free, featuring:
* Offline downloads and local media library playback.
* Real-time synchronized lyrics with multi-provider failover and AI translation.
* Ambient music recognition ("Sonic Find").
* Liquid Glass blur and expressive dynamic theming with SpatialFlow harmonic palette generation.
* Elevated dynamic navigation bar with scroll-to-hide ergonomics.
* Haptic feedback engine with customizable vibration intensity.
* Synced group listening ("Listen Together") and Discord Rich Presence.

Package / Namespace: `com.music.sonic` (Application ID: `com.music.sonic`).

---

## Tech Stack

* **Language**: Kotlin 2.3 (JVM target 21), Gradle Kotlin DSL (`.kts`).
* **UI**: Jetpack Compose (Material 3 Expressive, `material3`), `haze` for Liquid Glass blur, Coil 3 for image loading, Lottie for animations, Shimmer for loading placeholders.
* **Palette Engine**: AndroidX Palette with fine-tuned HSL harmonic seed extractor for soft, contrast-safe dark, light, and AMOLED modes.
* **Architecture**: MVVM — ViewModels (`viewmodels/`) + Compose screens (`ui/screens/`) + Repository pattern (`data/` / `db/`). Dagger Hilt (`dagger.hilt`) for dependency injection.
* **Persistence**: Room Database (`modules/core/`) and AndroidX DataStore Preferences (`utils/dataStore`).
* **Playback Engine**: AndroidX Media3 / ExoPlayer (`media3-session`, `media3-hls`, `media3-ui`, `media3-okhttp`) in `modules/playback/`.
* **Networking**: Ktor client (primary) + Retrofit, OkHttp, Jsoup.
* **Build System**: Gradle version catalog at `gradle/libs.versions.toml`, AGP 9.0.0, KSP for annotation processing.

---

## Multi-Module Architecture

The codebase is organized into clean, modular subprojects:

```
SonicSpace/
├── app/                  # Application UI, ViewModels, Hilt DI, Navigation
└── modules/
    ├── core/             # Shared data models, Room DB, DataStore, constants
    ├── playback/         # Media3/ExoPlayer audio service, queues, downloads
    ├── unison/           # Cross-cutting shared utilities and base abstractions
    ├── innertube/        # YouTube Music InnerTube API client
    ├── shazamkit/        # Sonic Find ambient audio recognition
    ├── artistvideo/      # Artist video playback
    ├── lyrics/           # Lyrics orchestration and AI translation
    ├── canvas/           # Canvas video background loop engine
    └── providers/        # External content source providers
        ├── lyrics/       # Lyrics providers (betterlyrics, kugou, lrclib, paxsenix, simp, youly)
        └── canvas/       # Canvas providers (applecanvas, soniccanvas)
```

---

## App Module Internal Structure

Path: `app/src/main/kotlin/com/music/sonic/`

```
di/             # Hilt DI modules: AppModule, NetworkModule, SourcesModule (@SourcesHttpClient)
constants/      # Preference keys and constant definitions (DataStore keys, AudioQuality enum)
data/
  sources/      # Multi-source audio resolver: MusicSource, ResolvedStream, JioSaavnSource,
                #   AddonSource, SourceResolver — tries Addon → JioSaavn → YouTube
db/             # Room database entities and DAOs
eq/             # Graphic equalizer and audio effects
listentogether/ # Real-time synchronized listening feature
localmedia/     # On-device audio file indexing and playback
lyrics/         # Lyrics orchestration and UI integration
models/         # App-level UI state models
playback/       # Media3 foreground service glue and PlayerConnection
quicksettings/  # Android Quick Settings tiles
recognition/    # Sonic Find UI and recognition controller
spotify/        # Spotify API and fast library sync
ui/
  component/    # Reusable widgets (Header, Liquid Glass, Shimmer, Dialogs)
  player/       # BottomSheetPlayer and FullPlayer components
  screens/      # Top-level screen destinations (Home, Search, Library, Settings, etc.)
  theme/        # SpatialFlowTheme, typography, color extraction, HSL generator
utils/          # Android and Compose utilities (includes SettingsStore DataStore wrapper)
viewmodels/     # Feature ViewModels
```

### Multi-Source Audio Chain

`SourceResolver` is injected into `MusicService` and called **before** the YouTube fallback on every cache miss:

1. **Addon Server** (`AddonSource`) — user-configured HTTP endpoint; supports lossless FLAC/ALAC from Tidal, Qobuz, Deezer, Apple Music via community addons. Protocol: `GET /search?q=...&quality=...` → optional `GET /stream/{id}`.
2. **JioSaavn** (`JioSaavnSource`) — built-in, always-on 320 kbps MP3 via `saavn.dev` public API. Skipped when Data Saver is on or user disables it.
3. **YouTube Music** — last resort; called when `SourceResolver.resolve()` returns `null`.

**Quality tiers** (`AudioQuality` enum in `PreferenceKeys.kt`):
- `LOSSLESS` — Addon only → YouTube (no JioSaavn downgrade)
- `HIGH` — Addon → JioSaavn → YouTube
- `OPUS` — Addon → JioSaavn → YouTube Opus (default)
- `YOUTUBE_ONLY` — skip all external sources

Settings UI lives in `PlayerSettings.kt` under the **High Quality Sources** group.

---

## UI & Design Guidelines

1. **Liquid Glass Aesthetic**:
   * Uses `Modifier.liquidGlass` with high blur radius and translucent surface tinting for app bars and headers.
   * Main top app bar is 59 dp high (`AppBarHeight`).

5. **Onboarding Illustrations**:
   * Keep the Welcome page branded with SonicSpace's own launcher art; use the attributed SpatialFlow vectors for the remaining onboarding pages.

2. **Elevated Dynamic Navigation Bar**:
   * Configurable via Appearance settings: Standard Navbar (84 dp) or Dynamic Navbar (76 dp without labels, 88 dp with labels).
   * Elevated floating icons with smooth scale animation (1.22x) and enlarged icon targets (28–30 dp).
   * Smooth sliding selection chip indicator (`animatedOffsetPx`, `animatedWidthPx`) gliding seamlessly across tabs on screen changes with spring physics and liquid stretch/squash dynamics.
   * Extended background surface filling the bottom gap down to the edge of the screen, with content insets for gesture navigation.
   * Auto-hide on downward scroll gestures and auto-show on upward scroll.
   * Scoped strictly to root destinations (`Home`, `Search`, `Library`).

3. **Harmonic Album Color Palette**:
   * Generates subdued, non-harsh palettes for Dark, Light, and AMOLED black modes.
   * Background saturation is clamped to prevent piercing neon glare.

4. **Track Row Standardization**:
   * Unboxed track rows (`RecentTrackRow`) with 52 dp square cover art, 10 dp rounded corners, and clear typographic hierarchy.

5. **Compact Grouped Settings Options**:
   * `Material3SettingsGroup` groups all option rows in a single rounded card container (`20 dp` corners) per section.
   * Compact option row padding (`8–9 dp` vertical) and sleek inner horizontal dividers for dense, readable settings screens.

6. **About & Updates Screen**:
   * Keep app identity and Developer link first, App Updates next, and acknowledgements as the final section on the same scrollable page.
   * Keep update status/check, preferences, and version history together in a compact settings group; route legacy update destinations to this unified page.

7. **Listening Stats**:
   * The Stats destination owns its `Your Replay` app bar and uses a consistent 16 dp content gutter with independent app bar and player/navigation insets.

8. **Search Explore**:
   * Keep Search as one discovery destination with a direct search field; browse sections retain their upstream category headings and responsive artwork grid.
   * Start browse content directly with the catalog categories, using dynamic mood/genre artwork and direct browse navigation; do not add a parallel charts-provider tab or extra page header.

9. **Player Action Chips**:
   * Configurable via Player Settings ('Show Player Option Chips' toggle).
   * Renders horizontal action pills below player controls for Like, Lyrics, Download, Comments, and Share.

