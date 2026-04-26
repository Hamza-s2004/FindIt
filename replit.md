# FindIt — Lost & Found Android App

## Overview

FindIt is a **native Android application** written in Kotlin and built with Gradle.
It is a lost-and-found app that lets users report, search, and manage lost or
discovered items.

This repository was imported into Replit. Because Android apps require an
emulator or a physical device to run, the actual app cannot execute inside the
Replit web preview. To run the app, open the project in Android Studio and run
it on an emulator or a connected Android device.

## Tech stack

- Language: Kotlin
- Build system: Gradle (Kotlin DSL), Android Gradle Plugin 9.0.1
- Android: compileSdk 36, targetSdk 36, minSdk 24
- UI libraries: AndroidX AppCompat, Material Components, ConstraintLayout, RecyclerView
- Package: `com.example.findit`

## Project layout

```
FindIt/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/findit/
│       │   ├── MainActivity.kt
│       │   ├── HomeFragment.kt
│       │   ├── SearchFragment.kt
│       │   ├── PostFragment.kt
│       │   ├── NotificationsFragment.kt
│       │   └── ProfileFragment.kt
│       └── res/  (layouts, drawables, values, mipmaps)
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── site/
    └── index.html   ← Replit preview landing page (not part of the Android app)
```

## Replit setup

Since this is an Android project with no web frontend or backend, the Replit
workspace serves a small static landing page on port 5000 that documents the
project. This satisfies the workspace requirement of having a running workflow
and gives anyone opening the Repl a quick overview.

- Runtime: Python 3.11 (only used to serve the static landing page)
- Workflow: `Start application` → `python -m http.server 5000 --bind 0.0.0.0 --directory site`
- Port: 5000 (webview)

The `site/` folder is purely informational and is not part of the Android app.

## How to run the Android app locally

1. Install [Android Studio](https://developer.android.com/studio).
2. Open this folder as an existing Gradle project.
3. Let Gradle sync, then run on an emulator or a connected device.

From the command line (with the Android SDK installed):

```
./gradlew assembleDebug
```
