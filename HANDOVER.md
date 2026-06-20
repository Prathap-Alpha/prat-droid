# Prat-Droid — Handover (for Google AI Studio / Gemini)

## 0. The one-line truth
The repo today is a **phone app**. It does **not** appear on the Ford Ranger Raptor's
Android Auto screen. To put anything on that screen you must build a **separate Android Auto
app using the Car App Library** (templated UI, no root). That part is NOT built yet — build it.

## 1. Where the code is
- GitHub (public): https://github.com/Prathap-Alpha/prat-droid
- Clone: `git clone https://github.com/Prathap-Alpha/prat-droid`
- Latest APK: https://github.com/Prathap-Alpha/prat-droid/releases/latest/download/Prat-Droid.apk
- CI: `.github/workflows/build.yml` builds on every push and publishes a GitHub Release.

## 2. Current state (the phone app)
- Package: `bw.alphadirect.pratdroid`. Jetpack Compose.
- Stack: AGP 7.4.2 · Gradle 7.6.3 · Kotlin 1.8.22 · Compose BOM 2023.06.01 · compileSdk 35 · targetSdk 35 · minSdk 26.
- Features: animated splash (photo, "RAPTOR" wordmark, claw, roar + en-AU TTS), reminders+notifications, notes, day plan, quick-send, calendar intent, MediaProjection screen recording, device status, flashlight. 10 Robolectric unit tests.
- Source layout: `app/src/main/java/bw/alphadirect/pratdroid/{ui,data,reminders,cast,util}`.

## 3. The actual goal
Show "Prat" on the Raptor's Android Auto display. Hard facts:
- Android Auto only projects **Car App Library** apps in vetted categories (navigation, messaging,
  media, POI). It will NOT run arbitrary phone UI / video on a moving screen.
- **Excluded on purpose (do NOT implement):** rooting the phone, editing GMS `phenotype.db`,
  or bypassing AA speed-lock/tap-limit. That defeats driver-safety controls. Build the legitimate
  Car App Library app instead.

## 4. Build-environment gotchas (will block you if ignored)
- The dev Windows machine has **AF_UNIX sockets disabled** → JDK 17/21 NIO `Selector.open()` throws
  `Unable to establish loopback connection` → Gradle cannot run. **Use JDK 11 locally** (TCP self-pipe).
  GitHub's Linux CI is unaffected (JDK 17 fine).
- AGP 7.4.2's bundled aapt2 cannot read the `android-35` platform. Pass a newer aapt2:
  `-Pandroid.aapt2FromMavenOverride=<SDK>/build-tools/35.0.0/aapt2(.exe)`.
- Local debug build (Windows, JDK 11):
  ```
  set JAVA_HOME=C:\Users\PrathapAsus\work\jdk11\jdk-11.0.31+11
  gradlew.bat assembleDebug -Pandroid.aapt2FromMavenOverride=C:/Users/PrathapAsus/AppData/Local/Android/Sdk/build-tools/36.1.0/aapt2.exe --no-daemon
  ```

## 5. Build the Android Auto app — exact steps

### 5.1 Gradle (app/build.gradle.kts)
```kotlin
implementation("androidx.car.app:app:1.4.0")
```

### 5.2 res/xml/automotive_app_desc.xml
```xml
<automotiveApp>
    <uses name="template" />
</automotiveApp>
```

### 5.3 AndroidManifest.xml (inside <application>)
```xml
<meta-data
    android:name="com.google.android.gms.car.application"
    android:resource="@xml/automotive_app_desc" />
<meta-data
    android:name="androidx.car.app.minCarApiLevel"
    android:value="1" />

<service
    android:name=".car.PratCarAppService"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.car.app.CarAppService" />
        <category android:name="androidx.car.app.category.POI" />
    </intent-filter>
</service>
```
(Category determines where AA surfaces it; POI/MESSAGING/NAVIGATION are the templated options.
 Play Store publishing needs Google review — for personal use, developer-mode sideload works.)

### 5.4 app/src/main/java/bw/alphadirect/pratdroid/car/PratCarAppService.kt
```kotlin
package bw.alphadirect.pratdroid.car

import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class PratCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator =
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        else
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = MainCarScreen(carContext)
    }
}
```

### 5.5 app/src/main/java/bw/alphadirect/pratdroid/car/MainCarScreen.kt
```kotlin
package bw.alphadirect.pratdroid.car

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class MainCarScreen(carContext: CarContext) : Screen(carContext) {
    private val templates = listOf(
        "Running 10 min late, on my way.",
        "In a meeting, will call you back shortly.",
        "Approved. Please proceed."
    )

    override fun onGetTemplate(): Template {
        val list = ItemList.Builder().apply {
            templates.forEach { msg ->
                addItem(
                    Row.Builder()
                        .setTitle(msg)
                        .setOnClickListener {
                            // Car-safe: confirm only. Actual send must be handed to the phone
                            // (e.g. via a foreground action) — never free-type while driving.
                            CarToast.makeText(carContext, "Ready: $msg", CarToast.LENGTH_SHORT).show()
                        }
                        .build()
                )
            }
        }.build()

        return ListTemplate.Builder()
            .setSingleList(list)
            .setTitle("Prat — Quick Send")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
```

## 6. Run it on the Raptor (no Play Store)
1. On the phone, open **Android Auto settings** → scroll to **Version**, tap it ~10× → **Developer settings** unlocks.
2. In Developer settings enable **Unknown sources** (and "Add new cars to Android Auto" if present).
3. Install the debug APK on the phone, connect to the Raptor by USB → the templated app appears in the AA app launcher.
4. To test on a desktop without the car: install **Desktop Head Unit (DHU)** from the SDK Manager
   (SDK Tools → "Android Auto Desktop Head Unit emulator") and run `desktop-head-unit`.

## 7. Constraints to respect (these are why the full app can't be on the screen)
- Templates only: lists/panes/grid/navigation/messaging. No custom Compose, no video, capped list length while moving.
- The phone app and the car app share the same APK/package but are different entry points — the Compose UI stays phone-only; the Car App Library service is the car-only surface.
