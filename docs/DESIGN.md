# SonicSpace Design System & UI Guidelines

SonicSpace features a distinct, modern visual identity that combines high-contrast minimalist typography with translucent **Liquid Glass** blur effects, responsive gestures, and dynamic harmonic color theming.

This guide outlines the core design language, components, and patterns used throughout SonicSpace.

---

## 1. Core Visual Principles

### Minimalist & High-Contrast
* Interfaces prioritize typography, clean album art, and content density over algorithmic fluff or distracting gradients.
* High-contrast text pairings (`titleMedium` in bold paired with subdued `bodySmall` in greyish tones) make song rows and metadata instantly readable.

### Liquid Glass Blur & Translucency
* Surfaces in SonicSpace take advantage of frosted glass blurs:
  * Top app bars utilize `Modifier.liquidGlass` over scrolling backdrops with a streamlined 59 dp height (`AppBarHeight`).
  * Modal bottom sheets and floating toolbars use soft translucent containers (`surfaceContainerLow.copy(alpha = 0.6f)` or `surfaceVariant.copy(alpha = 0.3f)`) rather than heavy opaque layers.

### Dynamic Harmonic Color Theming
* Theme colors are derived from the active track's artwork using a fine-tuned HSL harmonic algorithm:
  * **Color Saturation Limiting**: Raw vibrant seeds are tastefully calmed into a 0.16–0.38 saturation range to eliminate harsh neon glare while preserving rich warmth.
  * **Monochrome Guarding**: True grayscale album covers automatically fallback to elegant neutral tones without ugly hue artifacts.
  * **AMOLED Pure Black**: When enabled, backgrounds and surface containers render as `#000000` for maximum power efficiency and OLED contrast.
  * **Balanced Light Theme**: High-contrast contrast levels ensure text and controls remain crisp and never washed out or faded in bright environments.

---

## 2. Navigation Architecture

### Elevated Dynamic Navigation Bar
SonicSpace features two navigation styles configurable in **Settings -> Appearance**:

1. **Standard Navbar**:
   * Fixed 84 dp height with standard Material 3 label and indicator styling.
2. **Dynamic Navbar Mode**:
   * Height: 76 dp (labels hidden) or 88 dp (with labels enabled).
   * **Elevated Floating Icons**: Each navigation tab is rendered on an elevated pill container with 2–6 dp shadow and tonal elevation, floating slightly when active.
   * **Scroll-to-Hide Ergonomics**: Automatically scrolls out of view on continuous downward feeds (Home, Library, Search) and glides smoothly back into place on upward gestures.
   * **Root Scoping**: Only visible on primary top-level tabs (Home, Search, Library), keeping sub-destinations (Albums, Artists, Playlists, Settings) completely immersive.

---

## 3. Component Guidelines

### Track Rows (`RecentTrackRow`)
* Track list rows (History, Albums, Playlists) follow an unboxed layout:
  * Cover art: 52 dp square with 10 dp rounded corners.
  * Typography: Bold title with secondary artist information.
  * Trailing actions (menu button, download indicator) remain cleanly aligned on the right.

### Feeds & Recommendation Shelves
* **Daily Discover**: Always pinned to the top of the Home feed.
* **Picked for You**: Displayed as a horizontal multi-page compact shelf with square-cropped cover art.
* **Explore Grid**: Duotone image cards with responsive column counts and dynamic artwork loading.

### Haptic Feedback
* Tactile sensations accompany key user actions (tab clicks, seekbar adjustments, playback buttons).
* Adjustable intensity slider (0–100%) available under Appearance settings.

---

## 4. Typography System

SonicSpace relies on `MaterialTheme.typography` styled with Google Sans / Sans-Serif font families:
* **Display Titles**: ExtraBold / Black weight with tight negative letter-spacing for punchy headers.
* **Section Headers**: `titleMedium` / `titleLarge` in SemiBold.
* **Metadata & Captions**: `bodySmall` / `labelSmall` with subtle transparency (`0.75f - 0.85f`) to emphasize primary song titles.
