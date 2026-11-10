# FocusLock 🛡️ — Anti-Distraction Screen Time Blocker (Android)

[![Download APK](https://img.shields.io/badge/Download-FocusLock%20v1.0.0%20APK-10B981?style=for-the-badge&logo=android&logoColor=white)](https://github.com/webdeveloper-fresher32/Focus-Lock/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-purple.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20Architecture-orange.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

**FocusLock** is an offline-first, distraction-locking Android application engineered to help developers, students, and job seekers protect deep work blocks for interview preparation, DSA practice, System Design study, and coding sprints.

Unlike passive screen-time monitors that merely observe and generate reports, FocusLock **actively enforces focus sessions** by intercepting restricted apps (Instagram, YouTube, Free Fire, Reddit, etc.) with high friction and intentional barriers against impulse bypasses.

---

## ✨ Key Features

- ⏱️ **Flexible Focus Durations**:
  - **Quick Presets**: Jump straight into `25m`, `30m`, `45m`, `1 hr`, `1.5 hrs`, `2 hrs`, or `3 hrs` blocks.
  - **Manual Custom Steppers**: Accurately set any duration by hours and minutes with quick `+15m`, `+30m`, `+1 hr`, and `+2 hrs` adjustments.
- 🚫 **Real-Time App Interception & Blocking**:
  - Instantly detects and deflects launches of restricted entertainment and social apps via Android's `AccessibilityService`.
  - Displays a dedicated `BlockOverlayActivity` encouraging you to return to your goals.
- 🔒 **Multiple Strictness Modes**:
  - **Normal**: Soft blocking with informational warnings.
  - **Strict**: Disables casual cancellation and pause controls.
  - **Maximum**: Complete lockdown. Emergency exit requires an unskippable 60-second countdown and typing an accountability pledge.
- ⏳ **Persistent Target-Epoch Timer**:
  - Focus sessions store persistent target timestamps in Room SQLite.
  - Countdowns survive app closure, process death, background killer termination, and device reboots (`BOOT_COMPLETED`).
- 📊 **Daily App Usage & Deflection Analytics**:
  - Integrates with Android's `UsageStatsManager` for foreground app tracking.
  - Visual breakdown of daily screen time, category usage, and focus hours calculated 100% locally on device.
- 🔔 **Foreground Countdown Notification**:
  - Persistent ongoing system notification keeps active focus status and remaining time visible in the status bar.
- 🛡️ **Fail-Safe Whitelisting**:
  - Critical system capabilities (emergency dialer 911/112, phone calls, system settings) are whitelisted and never blocked.

---

## 🏗️ Architecture & Technology Stack

FocusLock follows modern Android development practices and Clean Architecture guidelines:

```
app/
 ├── core/
 │    ├── navigation/         # Navigation Compose destinations & routes
 │    ├── theme/              # Material 3 dark-first palette & typography
 │    └── utils/              # Time formatting, permission helpers
 ├── data/
 │    ├── local/
 │    │    ├── dao/           # Room DAOs (AppDao, SessionDao, DailyUsageDao)
 │    │    ├── entity/        # Database entities & schema
 │    │    └── AppDatabase.kt # Room database instance
 │    └── repository/         # Data repository implementations
 ├── domain/
 │    ├── model/              # Domain models (FocusSession, AppInfo, StrictnessLevel)
 │    └── repository/         # Repository contracts
 ├── presentation/
 │    ├── blocker/            # Full-screen block overlay UI
 │    ├── dashboard/          # Daily screen time & quick-start focus cards
 │    ├── focus/              # Session setup, custom duration picker, active session view
 │    ├── apps/               # Installed app discovery, categories & daily limits
 │    ├── permissions/        # Permission onboarding flow
 │    └── stats/              # Local analytics & session history
 └── service/
      ├── FocusLockAccessibilityService.kt # Real-time app launch detector
      ├── FocusSessionService.kt           # Foreground timer service
      └── BootReceiver.kt                  # Device reboot restoration receiver
```

### Tech Stack Details:
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material 3 (Dark Theme First)
- **Database**: Room 2.6+ (SQLite with KSP code generation)
- **Preferences**: Jetpack DataStore (Preferences)
- **Concurrency**: Kotlin Coroutines & `StateFlow`
- **System APIs**:
  - `UsageStatsManager` for screen time tracking
  - `AccessibilityService` for window state detection
  - `PackageManager` for local application discovery
  - `SYSTEM_ALERT_WINDOW` for prompt overlay interception

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Iguana | Jellyfish | Koala** or newer
- **JDK 17** or **JDK 21**
- Android SDK with `compileSdk = 34` and `minSdk = 26` (Android 8.0 Oreo or higher)

### Setup & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/webdeveloper-fresher32/Focus-Lock.git
   cd Focus-Lock
   ```
2. **Open in Android Studio**:
   - Open Android Studio, click **Open**, and navigate to the project directory.
   - Let Gradle sync completely.
3. **Build & Install via CLI**:
   ```bash
   ./gradlew :app:assembleDebug
   ./gradlew :app:installDebug
   ```

---

## 🔐 Required Android Permissions

Upon first launch, FocusLock guides you through enabling:
1. **Usage Access**: Permits `UsageStatsManager` to track foreground screen time locally.
2. **Accessibility Service**: Enables `FocusLock Protection Service` to detect when a restricted app is launched.
3. **Display Over Other Apps**: Allows the focus blocker overlay to appear over unauthorized apps.
4. **Ignore Battery Optimizations**: Prevents Android background task killers from stopping your active focus session.

> **Privacy Guarantee**: FocusLock operates **100% offline**. No network calls are made, no telemetry is gathered, and no personal data ever leaves your device.

---

## 📄 License

```
Copyright 2024 Ganesh Pirikirala

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
