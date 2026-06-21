# Question for Google — Car App Library (POI) app not visible on a real head unit

**Summary:** A correctly-declared Car App Library app does not appear on a physical
Android Auto head unit when sideloaded, even with developer mode + "Unknown sources" on.
What is the supported path to run/test it on a real car before Play publication?

## Environment
- App type: custom Android **for Cars App Library** app
- Library: `androidx.car.app:app:1.4.0`
- Build: AGP 7.4.2, Kotlin 1.8.22, compileSdk 35, targetSdk 35, minSdk 26
- Phone: Samsung Galaxy S25 Ultra, Android 15
- Connection: **wireless** Android Auto
- Head unit: Ford Ranger Raptor (2024), **SYNC 4/4A**, 12" portrait screen
- Android Auto: developer mode unlocked, **Unknown sources enabled**

## What we built
`CarAppService` + `Session` + two `Screen`s using **templates** (not phone UI):
`MessageTemplate` (a startup/welcome screen) and `ListTemplate` (a menu).

AndroidManifest.xml (inside `<application>`):
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
res/xml/automotive_app_desc.xml:
```xml
<automotiveApp>
    <uses name="template" />
</automotiveApp>
```
We confirmed all of the above are present in the **built APK** manifest (`aapt dump xmltree`).

## Problem
- The APK installs and runs fine as a normal phone app.
- On the real head unit (wireless AA), the app does **not** appear in the Android Auto app
  launcher, and does **not** appear under **Android Auto → Customize launcher**.
- Developer mode + Unknown sources are on. We also force-stopped Android Auto, reconnected,
  and rebooted the phone. No change — no sign Android Auto recognizes the app at all.

## What we already found
Google's support documentation indicates the **"Unknown sources"** developer setting applies to
**media, messaging, and parked apps — not** apps built with the Android for Cars App Library.

## Questions
1. Is there any supported way to run a Car App Library app on a **physical head unit** before
   Google Play publication, or is the **Desktop Head Unit (DHU)** the only pre-publication test path?
2. Does the **`POI`** category render on consumer Android Auto / Ford SYNC, or does it require
   Google approval / allowlisting before it will appear on a real car?
3. Is there any manifest, category, or developer-settings step we are missing that would make a
   sideloaded Car App Library app visible on a physical head unit?

## Where this was posted
- Google Issue Tracker — component "Android Auto" / "Android for Cars App Library"
- Stack Overflow tags: `android-auto`, `android-app-library`
- Android Auto Developers Google Group
