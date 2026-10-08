# Contributing to SonicSpace

Thank you for your interest in contributing to **SonicSpace**! We welcome contributions from developers, designers, translators, and documentation writers of all skill levels.

This document outlines the contribution workflow, coding standards, and guidelines for submitting pull requests.

---

## 1. Getting Started

### Prerequisites
* JDK 21
* Android Studio (Ladybug / Meerkat or later)
* Android SDK 35
* Familiarity with Kotlin, Jetpack Compose, and Android development patterns

### Forking and Cloning
1. Fork the [SonicSpace repository](https://github.com/JustaThinker/SonicSpace) on GitHub.
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SonicSpace.git
   cd SonicSpace
   ```
3. Add the upstream remote:
   ```bash
   git remote add upstream https://github.com/JustaThinker/SonicSpace.git
   ```

---

## 2. Development Workflow

### Creating a Feature Branch
Always create a dedicated topic branch off of the `main` branch:
```bash
git checkout main
git pull upstream main
git checkout -b feature/my-new-feature
```

### Commit Guidelines
* Keep commits atomic and focused on a single change.
* Use clear, imperative commit messages:
  * `feat: add sleep timer quick setting tile`
  * `fix: prevent navbar text overlap in dynamic style`
  * `refactor: extract HSL color generation into Theme.kt`
  * `docs: update setup instructions for JDK 21`

---

## 3. Project Architecture & Code Organization

SonicSpace is structured into modular Gradle subprojects to keep code maintainable:

```
app/src/main/kotlin/com/music/sonic/
├── di/             # Hilt dependency injection modules and entry points
├── ui/             # Jetpack Compose UI
│   ├── component/  # Reusable UI widgets, buttons, glass surfaces
│   ├── player/     # BottomSheetPlayer and FullPlayer views
│   ├── screens/    # Top-level screen destinations (Home, Search, Library, Settings)
│   └── theme/      # Theme.kt, dynamic album palette generator, typography
├── utils/          # Compose and Android helper utilities
└── viewmodels/     # Architecture ViewModels binding domain models to UI states
```

Domain and playback logic resides in dedicated modules:
* `modules/core`: Domain entities, Room database, DataStore preferences.
* `modules/playback`: ExoPlayer service, download manager, audio processing.
* `modules/lyrics`: Synced lyrics parsing, provider orchestration, AI translation.
* `modules/innertube`: YouTube Music API integrations.

---

## 4. Coding Conventions

* **Language**: Idiomatic Kotlin using coroutines, flows, and structured concurrency.
* **UI**: 100% Jetpack Compose. Avoid XML layouts.
* **Compose State**: Hoist state when possible; use `rememberSaveable` or ViewModel `StateFlow` for persistent UI state.
* **Formatting**:
  * 2 or 4 spaces indentation (consistent with surrounding code).
  * Meaningful variable and function names.
  * No commented-out dead code.
* **Packages**: Lowercase under `com.music.sonic.*`.

---

## 5. Submitting a Pull Request

1. **Verify Compilation**: Ensure the project compiles cleanly without errors:
   ```bash
   ./gradlew assembleUniversalGmsDebug
   ```
2. **Run Tests**: Verify all unit tests pass:
   ```bash
   ./gradlew test
   ```
3. **Push to Your Fork**:
   ```bash
   git push origin feature/my-new-feature
   ```
4. **Open a PR**: Open a Pull Request against the `main` branch of `JustaThinker/SonicSpace`. Include a concise description of the changes and screenshots/screen recordings for visual modifications.
