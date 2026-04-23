# KMP / Compose Multiplatform MVP

This project now has two app surfaces:

- `:app` is the current production Android app.
- `:composeApp` is the new cross-platform MVP shell for Android, iOS, and desktop.

## What is in the MVP shell

- Shared Compose UI in `composeApp/src/commonMain`.
- Android entry point in `composeApp/src/androidMain`.
- Desktop entry point in `composeApp/src/desktopMain`.
- iOS framework entry point in `composeApp/src/iosMain`.

The first MVP screen uses sample finance data on purpose. The next migration step is to move DTOs and repository contracts from `:app` into common code, then provide platform adapters for storage, network, auth, and background sync.

## Useful commands

```powershell
.\gradlew.bat :composeApp:desktopRun -PallowUnsupportedBuildJdk=true
.\gradlew.bat :composeApp:assembleDebug -PallowUnsupportedBuildJdk=true
.\gradlew.bat :app:assembleDebug -PallowUnsupportedBuildJdk=true
```

iOS builds must be finalized on macOS with Xcode. On Windows the shared iOS source set can be edited, but Apple binaries are not produced locally.

For the existing Android `:app` module, prefer JDK 17 or 21. JDK 26 can break KSP/Room processing.
Room was updated to `2.8.4` so it can run with Kotlin 2.x and KSP2.
