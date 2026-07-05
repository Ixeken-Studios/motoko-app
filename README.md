# Motoko - Personal Finance Manager

An Android app for personal finance management built with Kotlin and Jetpack Compose. Track your income, expenses, and subscriptions, and manage multiple accounts and wallets with absolute privacy.

[<img src="https://img.shields.io/badge/Download-Latest_Release-6A994E?style=for-the-badge&logo=android&logoColor=white" height="45">](https://github.com/Ixeken-Studios/motoko-app/releases/latest)

![Motoko Feature Graphic](/fastlane/metadata/android/en-US/images/featureGraphic.png)


## Key Features

### Dashboard
- Track income, expenses, and total balance at a glance.
- Quick summary of recent transactions.

### History
- Full transaction ledger with search, category filtering, and time filters.
- Grouped view of transactions by date (Today, Yesterday, specific dates).
- Swipe-to-delete gesture on standard transactions with undo capability.

### Subscriptions
- Track monthly and annual services.
- Automatic calculation of upcoming billing period expenses (monthly/annual totals).
- Detailed subscription cards with customized categories.

### Category & Wallet Management
- Create, edit, and delete custom categories.
- Manage multiple wallets (ex: Cash, Debit Card, Savings) to control where your money is stored.
- Separate finances across isolated custom accounts (ex: Personal, Work, Joint).

### Settings & Privacy
- **100% Local Privacy:** All financial records, receipts, and accounts stay exclusively on your device.
- Optional biometric authentication (App Lock) using system fingerprint, PIN, or pattern.
- Configurable text size (5 options) and main typography.
- Ergonomic navigation bar alignment selector (left, center, right).
- Import and export complete backups (JSON files) with backward compatibility.
- Optional check for updates linked directly to GitHub Releases.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3, Lucide Icons |
| Architecture | MVVM (Model-View-ViewModel), Clean UI architecture |
| DI | Hilt 2.57 |
| Database | Room 2.6.1 (SQLite) |
| Storage | Preferences DataStore 1.1.1 |
| Build | AGP 8.7.3, KSP, Gradle |

<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Android/android2.svg">&nbsp;&nbsp;
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Kotlin/kotlin2.svg">&nbsp;&nbsp;
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">&nbsp;&nbsp;
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/SQLite/sqlite2.svg">&nbsp;&nbsp;
</p>

## Requirements

To build and run Motoko locally, make sure you have:
- **Android Studio Koala** (2024.1.1) or newer.
- **JDK 17** or newer (Android Studio comes with an embedded JetBrains Runtime JBR which is fully compatible).
- **Android SDK Platform 35** installed via Android Studio SDK Manager.

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Ixeken-Studios/motoko-app.git
cd motoko-app
```

### 2. Build the application

- **Debug Build (Recommended for testing):**
  - *Linux/macOS:* `./gradlew assembleDebug`
  - *Windows:* `.\gradlew.bat assembleDebug`

- **Release Build (Requires local signing configuration in `local.properties`):**
  - *Linux/macOS:* `./gradlew assembleRelease --no-configuration-cache`
  - *Windows:* `.\gradlew.bat assembleRelease --no-configuration-cache`

The compiled APKs will be located in: `app/build/outputs/apk/`

## Acknowledgements

- **[Lucide Icons](https://lucide.dev/)** - For the beautiful, clean, and modern open-source vector icon library.

## License
<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/LicenceMIT/licencemit2.svg">
</p>
This project is licensed under the MIT License - see the license file for details.