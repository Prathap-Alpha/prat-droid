# Prat-Droid

Personal productivity app for Prathap — sky-blue cinematic splash, his portrait, and a
handwritten welcome with an angry-raptor roar + Australian voice line.

## Features
- **Cinematic splash** (8s): portrait → "Welcome to AI CFO's Raptor" (handwritten) → white
  RAPTOR wordmark wipes in → claw drawn beneath → roar + Australian-male voice. Tap to skip.
- **Reminders** — type it, pick when, get a notification (works offline, survives reboot).
- **Day Plan** — today's reminders + one-tap add-calendar-event.
- **Schedule Meeting** — opens Thusa (chat-to-calendar scheduler).
- **Quick Send** — saved message templates → WhatsApp / SMS / email.
- **Add to Calendar** — writes to the phone's calendar account (syncs to Outlook).
- **Cast Screen** — opens the system screen-mirror picker.

## Install (Samsung S25 Ultra)
The easy, auto-updating way is **Obtainium**:
1. Install **Obtainium** from the Play Store (or its GitHub release).
2. Add app → paste this repo's URL → Obtainium tracks the Releases and installs updates.
   (Private repo: add a GitHub token in Obtainium settings once.)
Or one-off: open the latest **Release**, download `Prat-Droid.apk`, tap to install
(allow "install unknown apps" once).

## Cloud build (seamless updates)
Every push to `main` triggers **GitHub Actions** (`.github/workflows/build.yml`):
builds the debug APK on a Linux runner, bumps `versionCode` to the run number, and publishes
a **Release** with `Prat-Droid.apk`. Obtainium picks up each new release automatically.

## Tech / why this stack
- Kotlin 1.8.22 · Jetpack Compose (BOM 2023.06.01) · Material 3 · AGP 7.4.2 · Gradle 7.6.3
- compileSdk 35 (built with an aapt2 override; CI supplies its own), targetSdk 33, minSdk 26
- The stack is deliberately JDK-11-compatible: the local dev machine has AF_UNIX sockets
  disabled, which breaks JDK 17+'s NIO selector (and thus Gradle). JDK 11 uses a TCP self-pipe
  and works. CI (Linux) has no such limit. No API keys, no network calls of its own.
