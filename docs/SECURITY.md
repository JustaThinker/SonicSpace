# SonicSpace Security Policy

## Supported Versions

Security updates are actively provided for the following releases of **SonicSpace**:

| Version | Status |
| :--- | :--- |
| 1.5.x | :white_check_mark: Supported |
| 1.4.x | :white_check_mark: Supported |
| < 1.4 | :x: Unsupported (Upgrade recommended) |

---

## Reporting a Vulnerability

The SonicSpace team takes application security and user safety seriously. If you discover a potential vulnerability, please report it responsibly:

1. **Do NOT open a public GitHub issue.** Public disclosure exposes users before a fix can be prepared.
2. Submit a private report via **[GitHub Security Advisories](https://github.com/JustaThinker/SonicSpace/security/advisories/new)**.
3. Include the following details in your advisory:
   * Description and scope of the security issue.
   * Specific steps or proof-of-concept to reproduce the behavior.
   * Potential impact on user devices, stored tokens, or privacy.
   * Suggested remediation or patches if available.

We will acknowledge receipt of your report within 48 hours and work with you on a timeline for verification and release.

---

## Security Practices for Developers

* **Sensitive Data**: Never commit secrets, signing keystores (`*.jks`, `*.keystore`), `local.properties`, or authentication tokens to version control.
* **Network Security**: All external API calls must use HTTPS / TLS 1.3 encryption.
* **Storage Isolation**: User authentication cookies and session headers must be stored in secure, app-private storage (`EncryptedSharedPreferences` or app-private DataStore).
* **Safe API Levels**: Android SDK calls that depend on newer platform features (e.g., `RenderEffect` on Android 12+) must always be guarded with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` checks.

---

## Privacy & Telemetry Commitment

* SonicSpace does not collect personal identifiers, sell user data, or include advertising SDKs.
* All playback history, cached tracks, and preferences remain local on the user's device unless explicitly synced with user-authorized providers.
