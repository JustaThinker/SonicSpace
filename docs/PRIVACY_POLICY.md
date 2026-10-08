# Privacy Policy for SonicSpace

**Last Updated: October 2026**

SonicSpace ("we", "our", or "the app") is committed to maintaining complete transparency regarding user privacy, data handling, and device permissions.

---

## 1. Zero Tracking & No Data Selling

* **No Advertising**: SonicSpace contains zero third-party advertising SDKs or tracking networks.
* **No Telemetry**: We do not collect, monitor, sell, or rent your listening habits, search history, or personal identifiers.
* **No Mandatory Accounts**: Using SonicSpace does not require registering an account with us.

---

## 2. Information Handled by the App

### Local Storage & Device Data
* **Media Playback**: SonicSpace requests local storage/audio permissions only to index and play local music files stored on your device.
* **Database & Preferences**: Your favorites, playlists, search history, settings, and downloaded tracks are stored entirely in your device's local Room database and Android DataStore.

### Optional External Account Connections
* **YouTube Music**: If you optionally sign in to YouTube Music via the app's web login to access personal playlists or favorites, your session cookies are stored securely on your device and sent exclusively to official YouTube endpoints.
* **Spotify Library Import**: When utilizing the Spotify import feature, public playlist metadata is parsed directly on your device without transmitting credentials to external third parties.
* **Discord Rich Presence**: If you enable Discord RPC in settings, track metadata (title, artist, elapsed time) is transmitted directly to your local Discord client.

---

## 3. Network Communications

All streaming requests, thumbnail images, lyrics, and metadata queries connect directly from your device to the respective content providers over HTTPS:
* **Audio Streams**: YouTube Music / Google endpoints.
* **Lyrics**: LRCLIB, KuGou, BetterLyrics, and connected lyrics APIs.
* **Updates**: GitHub Releases API for checking update availability.

---

## 4. Permissions Used

* `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`: Used exclusively to play local music files.
* `POST_NOTIFICATIONS`: Required on Android 13+ to display the system media playback notification.
* `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Required for continuous background playback without Android OS process termination.
* `RECORD_AUDIO` (Optional): Required only if using the Sonic Find ambient music recognition feature to sample ambient music. Audio is never stored or sent anywhere other than fingerprinting servers.

---

## 5. Contact & Questions

If you have questions about SonicSpace's privacy approach or source code, you can inspect the repository or reach out:

* **Repository**: [https://github.com/JustaThinker/SonicSpace](https://github.com/JustaThinker/SonicSpace)
* **Issues**: [https://github.com/JustaThinker/SonicSpace/issues](https://github.com/JustaThinker/SonicSpace/issues)
