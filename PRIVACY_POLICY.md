# Privacy Policy for FocusLock

**Last Updated:** November 10, 2026

FocusLock ("we", "our", or "the app") is committed to protecting your privacy. This Privacy Policy explains how information is handled when you use the FocusLock Android application.

## 1. Core Principle: 100% Offline & Private
FocusLock is an offline-first application. We do not require an account, we do not operate a remote tracking server, and **we do not collect, transmit, sell, or share your personal data with any third party**.

All information regarding your installed applications, focus sessions, screen time, and limits is stored exclusively on your device's local storage (SQLite Room Database and encrypted DataStore).

---

## 2. Permissions & How They Are Used

FocusLock requests specific Android permissions strictly to provide its core digital wellbeing and distraction-blocking functionality:

### A. Usage Access (`PACKAGE_USAGE_STATS`)
- **Purpose**: Used to query Android's `UsageStatsManager` locally on your device to display your daily screen time and calculate whether an app has exceeded a limit you configured.
- **Data Handling**: Usage statistics remain on your device and are never transmitted over the internet.

### B. Accessibility Service (`AccessibilityService` API)
- **Purpose**: FocusLock uses the Android `AccessibilityService` API strictly to observe foreground window transitions (`TYPE_WINDOW_STATE_CHANGED`). This enables FocusLock to detect when a distracting application you selected is opened during an active focus session or after exceeding a daily limit.
- **Prominent Disclosure**:
  - FocusLock **does not** collect, read, or log keystrokes, passwords, private messages, personal content, or sensitive data.
  - FocusLock **does not** modify any system settings without your explicit action.
  - FocusLock **does not** transmit any Accessibility data over the network.
  - The service only inspects the foreground application package name to determine if it is in your restricted list.

### C. Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)
- **Purpose**: Used to display the FocusLock blocking screen over restricted applications when opened during an active focus block.

### D. Foreground Service (`FOREGROUND_SERVICE_SPECIAL_USE`)
- **Purpose**: Displays an ongoing countdown notification while a focus session is in progress so the session is not terminated prematurely by the operating system.

---

## 3. Data Storage & Deletion
All app data is stored locally in the application's private sandbox on your device. You can permanently delete all stored data at any time by:
1. Clearing the app storage in **Android Settings $\rightarrow$ Apps $\rightarrow$ FocusLock $\rightarrow$ Storage $\rightarrow$ Clear Data**.
2. Uninstalling the application from your device.

---

## 4. Children's Privacy
FocusLock does not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children.

---

## 5. Contact Us
If you have any questions or concerns regarding this Privacy Policy, please reach out via GitHub:
- **Repository**: [https://github.com/webdeveloper-fresher32/Focus-Lock](https://github.com/webdeveloper-fresher32/Focus-Lock)

