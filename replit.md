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
- Build system: Gradle 8.9 (Kotlin DSL), Android Gradle Plugin 8.7.3, Kotlin 2.0.21
- Android: compileSdk 35, targetSdk 35, minSdk 24
- UI: AndroidX AppCompat, Material Components, ConstraintLayout, RecyclerView
- Data: SQLiteOpenHelper (no Room), Retrofit + Gson, OkHttp logging interceptor
- Concurrency: Kotlin Coroutines (Dispatchers.IO for all DB / network work)
- Package: `com.example.findit`

## Architecture

Clean separation of concerns following the assignment brief:

```
com.example.findit
├── MainActivity                       (single Activity host)
├── data/
│   ├── api/
│   │   ├── ApiService                 (Retrofit interface — F1)
│   │   └── RetrofitClient             (singleton Retrofit + OkHttp)
│   ├── model/
│   │   ├── Item                       (domain model + DB row)
│   │   ├── Category                   (parent table model — FK target)
│   │   └── ApiPost                    (Gson DTO)
│   ├── db/
│   │   └── DatabaseHelper             (SQLiteOpenHelper — F2: 2 tables + FK)
│   └── repository/
│       └── ItemRepository             (F1, F3, F4, F5 — single source of truth)
├── ui/
│   ├── adapters/
│   │   └── ItemAdapter                (RecyclerView adapter — supports two row layouts)
│   └── fragments/
│       ├── HomeFragment               (offline-first list, filter tabs)
│       ├── PostFragment               (Create — F3)
│       ├── EditItemFragment           (Update — F3)
│       ├── DetailFragment             (Read + entry to Update/Delete — F3)
│       ├── SearchFragment             (collects filters)
│       ├── SearchResultsFragment      (dynamic LIKE/ORDER BY query — F5)
│       ├── ProfileFragment
│       └── NotificationsFragment
└── utils/
    └── Result                         (sealed wrapper for outcomes)
```

## Logic Map (assignment requirement)

| Feature | File | Function |
| --- | --- | --- |
| F1 — REST API call | `data/repository/ItemRepository.kt` | `fetchApiData()` |
| F2 — SQLite schema (2 tables, FK) | `data/db/DatabaseHelper.kt` | `onCreate()` |
| F3 — CRUD: Create | `data/repository/ItemRepository.kt` | `insertItem()` |
| F3 — CRUD: Read | `data/repository/ItemRepository.kt` | `getAllItems()`, `getItemById()`, `getItemsByType()` |
| F3 — CRUD: Update | `data/repository/ItemRepository.kt` | `updateItem()` |
| F3 — CRUD: Delete | `data/repository/ItemRepository.kt` | `deleteItem()` |
| F4 — API → SQLite cache (Option A) | `data/repository/ItemRepository.kt` | `syncApiToDb()` (called from `HomeFragment.refreshFromApi()`) |
| F5 — Dynamic search/sort | `data/repository/ItemRepository.kt` | `searchItems(query, type, categoryId, fromDate, toDate, sort)` |

## Threading

Every public function on `ItemRepository` is `suspend` and wraps its body in
`withContext(Dispatchers.IO)`. Fragments launch coroutines on
`viewLifecycleOwner.lifecycleScope` (the Main dispatcher), so UI updates always
happen on the main thread while DB and network work runs off it.

## Database schema

```sql
CREATE TABLE categories (
  id    INTEGER PRIMARY KEY AUTOINCREMENT,
  name  TEXT NOT NULL UNIQUE,
  emoji TEXT NOT NULL DEFAULT ''
);

CREATE TABLE items (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  title        TEXT NOT NULL,
  description  TEXT NOT NULL DEFAULT '',
  type         TEXT NOT NULL DEFAULT 'Lost',     -- 'Lost' | 'Found'
  category_id  INTEGER NOT NULL,                 -- FK -> categories.id
  location     TEXT NOT NULL DEFAULT '',
  date         TEXT NOT NULL DEFAULT '',
  contact      TEXT NOT NULL DEFAULT '',
  source       TEXT NOT NULL DEFAULT 'LOCAL',    -- 'LOCAL' | 'API'
  remote_id    INTEGER,
  created_at   INTEGER NOT NULL,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);
```

Foreign keys are enforced (`db.setForeignKeyConstraintsEnabled(true)` in
`onConfigure`). Categories are seeded on `onCreate`.

## Data flow (F4 — Option A: offline-first)

1. App starts → `HomeFragment.loadFromDb()` reads cached items from SQLite.
2. In parallel, `HomeFragment.refreshFromApi()` calls
   `ItemRepository.syncApiToDb()` which fetches `/posts` from
   `jsonplaceholder.typicode.com`, maps each post into an `Item`, and upserts
   it into SQLite (matched by `remote_id`).
3. When the sync completes, the list is reloaded from SQLite so UI shows the
   merged dataset (locally created + API-cached).

If the network call fails, a `Toast` is shown and the local cache is left
untouched.

## Replit setup

Android apps cannot execute inside the Replit web preview, so the Repl serves a
small static landing page on port 5000 that documents the project.

- Runtime: Python 3.11 (only used to serve the static landing page)
- Workflow: `Start application` → `python -m http.server 5000 --bind 0.0.0.0 --directory site`
- Port: 5000 (webview)

The `site/` folder is purely informational and is not part of the Android app.

## How to run the Android app locally

1. Install [Android Studio](https://developer.android.com/studio).
2. Open this folder as an existing Gradle project.
3. Let Gradle sync (this will pull Retrofit, Gson, OkHttp, Coroutines).
4. Run on an emulator or a connected device.

From the command line (with the Android SDK installed):

```
./gradlew assembleDebug
./gradlew assembleRelease   # signed with release.keystore at repo root
```

## Building APKs inside this Repl

The Repl is fully provisioned to build the project headlessly:

- JDK 19 (GraalVM 22.3) installed via the language module
- Android SDK at `~/android-sdk` with platform-tools, platforms;android-35
  + android-36, build-tools;34.0.0 + 35.0.0 + 36.0.0
- `local.properties` at the repo root points Gradle at that SDK
- `release.keystore` at the repo root (RSA 2048, alias `release`,
  storePassword `123456`, keyPassword `123456`) signs the release build via
  the `signingConfigs.release` block in `app/build.gradle.kts`

To rebuild from a shell in the Repl:

```
export ANDROID_HOME=$HOME/android-sdk
./gradlew --no-daemon assembleDebug assembleRelease
```

Outputs:

- `app/build/outputs/apk/debug/app-debug.apk`     (signed with the Android debug cert)
- `app/build/outputs/apk/release/app-release.apk` (signed with `release.keystore`, v2 scheme)

A first build takes ~1.5 minutes. Because Gradle daemons are killed when an
ad-hoc bash session exits in the Replit container, run long builds either
inside a workflow or with `setsid`/`nohup` and a redirected log file.

## Required permissions

Declared in `AndroidManifest.xml`:

- `android.permission.INTERNET` — Retrofit / OkHttp
- `android.permission.ACCESS_NETWORK_STATE`
