# ModernUiTemplate

Two-module Android project:

- `gameui`: Android Library containing the Compose UI and public `GameMenu` API.
- `app`: Demo application that loads `gameui` and shows the button `شروع بازی`.

## Versions

- minSdk 21
- targetSdk 36
- compileSdk 36
- AGP 9.3.0
- Kotlin Compose plugin 2.4.20
- Gradle 9.5.0
- Compose BOM 2025.10.01
- Activity Compose 1.11.0
- Material3 1.4.0

## Build

This repository intentionally does not require `gradlew`; GitHub Actions installs Gradle 9.5.0 and runs the `gradle` command directly.

```bash
gradle :app:assembleDebug
gradle :gameui:assembleRelease
```

Outputs:

- APK: `app/build/outputs/apk/`
- AAR: `gameui/build/outputs/aar/`

The library entry point is:

```kotlin
GameMenu.show(activity)
```

The button calls:

```kotlin
GameMenu.onStartGameClicked()
```

and logs:

```text
I/GameMenu: START_GAME_CLICKED
```
