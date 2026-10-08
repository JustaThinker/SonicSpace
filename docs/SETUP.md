# SonicSpace Setup & Build Guide

This guide provides complete instructions for setting up the local development environment and building **SonicSpace** from source.

---

## 1. Prerequisites

Before building SonicSpace, ensure your development workstation meets the following requirements:

* **Android Studio**: Android Studio Ladybug (2024.2.1) or newer (Meerkat / Narwhal supported).
* **JDK**: **Java 21 (JDK 21)** is required. (Android Studio's embedded JBR or OpenJDK 21).
* **Android SDK**:
  * `compileSdk`: **35**
  * `targetSdk`: **35**
  * `minSdk`: **26** (Android 8.0 Oreo)
* **Git**: Installed and available in your shell.

---

## 2. Cloning the Repository

Clone the SonicSpace repository to your local machine:

```bash
git clone https://github.com/JustaThinker/SonicSpace.git
cd SonicSpace
```

---

## 3. Local Environment Configuration

### Android SDK Path (`local.properties`)

Create a `local.properties` file in the project root containing the absolute path to your Android SDK:

```bash
# On Linux / macOS
cp local.properties.template local.properties
```

Edit `local.properties` to specify your SDK path:

```properties
## Example paths:
# Windows:
sdk.dir=C:\\Users\\<username>\\AppData\\Local\\Android\\Sdk

# macOS:
# sdk.dir=/Users/<username>/Library/Android/sdk

# Linux:
# sdk.dir=/home/<username>/Android/Sdk
```

> **Note**: `local.properties` is personal to your machine and is ignored by `.gitignore`. Never commit this file.

---

## 4. Building the Project

### Command Line Build

To compile and assemble the debug APK:

**On Linux / macOS:**
```bash
./gradlew assembleUniversalGmsDebug
```

**On Windows (PowerShell):**
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleUniversalGmsDebug
```

Output APK will be located at:
```
app/build/outputs/apk/universalGms/debug/app-universalGms-debug.apk
```

### Android Studio Build

1. Launch Android Studio.
2. Select **Open** and choose the `SonicSpace` root directory.
3. Allow Gradle to synchronize dependencies.
4. Select the `app` run configuration with the `universalGmsDebug` build variant.
5. Click **Run** (`Shift + F10`) to deploy to a connected Android device or emulator.

---

## 5. Running Tests

Run the unit test suite:

```bash
# Run all unit tests across all modules
./gradlew test

# Run app unit tests only
./gradlew :app:testUniversalGmsDebugUnitTest
```

---

## 6. Optional Configurations

### Google Services (Firebase Analytics / Crashlytics)

SonicSpace includes stub-safe fallbacks for Google Services. If you wish to enable custom Firebase integration:
1. Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name `com.music.sonic`.
3. Download `google-services.json` and place it in the `app/` directory.

### AI Lyrics Translation

SonicSpace supports real-time AI-powered translation of synchronized lyrics via OpenRouter. You can configure this directly inside the app:
1. Obtain an API key from [OpenRouter](https://openrouter.ai/).
2. Open SonicSpace on your device.
3. Navigate to **Settings -> AI Settings**.
4. Set provider to **OpenRouter** and enter your API Key.