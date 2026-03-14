# Rutt - GUI File Manager

## Project Overview
Rutt is a Kotlin Multiplatform Compose desktop application intended to be a GUI file manager replacement for the macOS built-in "Finder" application.

## Key Features & UI Conventions
- **Vim-like Navigation**: The application uses `h`, `j`, `k`, `l` for primary navigation.
  - `j`: Move selection down the list.
  - `k`: Move selection up the list.
  - `h`: Navigate to the parent directory.
  - `l`: Navigate into the currently selected directory.
- **Editable Path Bar**: The top of the UI contains an editable text field displaying the current directory path. Pressing `ENTER` after modifying the path navigates to the specified directory.
- **List Ordering**: Directories are listed first, followed by files. The list is sorted alphabetically within those groups.

## Technical Details
- **UI Framework**: Compose Multiplatform (Desktop/JVM target).
- **Core Files**:
  - `composeApp/src/jvmMain/kotlin/app/rutt/rutt/App.kt`: Contains the main UI logic, state management, and key event handling.
- **Build System**: Gradle.
  - `composeApp/build.gradle.kts`: Configuration for the compose app.
  - `gradle.properties`: Global gradle configuration.

## Known Issues & Workarounds
- **Skiko Native Access Warnings**: To suppress warnings regarding `org.jetbrains.skiko.Library` calling `java.lang.System::load` in the JVM, the `--enable-native-access=ALL-UNNAMED` JVM argument is required. This is currently configured in:
  1. `gradle.properties` (for `org.gradle.jvmargs` and `kotlin.daemon.jvmargs`).
  2. `composeApp/build.gradle.kts` (for the `application` block's `jvmArgs`).

## Development Guidelines
- Maintain the vim-like keyboard navigation paradigm for all new features where applicable.
- Ensure that the UI remains keyboard-friendly and focus is managed correctly (e.g., returning focus to the file list after path entry).
