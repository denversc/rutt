# Rutt - GUI File Manager

## Project Overview

Rutt is a Kotlin Multiplatform Compose desktop application intended to be a GUI file manager replacement for the macOS built-in "Finder" application.

## Key Features & UI Conventions

- **Vim-like Navigation**: The application uses extensive keyboard shortcuts for navigation and file operations.
  - `j` / `k`: Move selection down/up the list.
  - `h` / `l`: Navigate to the parent directory / into the currently selected directory.
  - `g` / `G`: Jump to the top / bottom of the list.
  - `H` / `M` / `L`: Jump to the High (top), Middle, or Low (bottom) of the *currently visible* viewport.
  - `Ctrl-F` (or `Shift-J`): Page Down.
  - `Ctrl-B` (or `Shift-K`): Page Up.
  - `R` (Shift-R): Refresh the current directory listing from disk.
- **File Operations**:
  - `i`: Inline rename (cursor at the beginning).
  - `a`: Inline rename (cursor at the end).
  - `D` (Shift-D): Inline rename (clears the current name first).
  - While renaming: `ENTER` to confirm, `ESC` to cancel.
- **Editable Path Bar**: The top of the UI contains an editable text field displaying the current directory path. Pressing `ENTER` after modifying the path navigates to the specified directory.
- **List Ordering**: Directories are listed first, followed by files. The list is sorted alphabetically within those groups.

## Technical Details

- **UI Framework**: Compose Multiplatform (Desktop/JVM target).
- **Core Files**:
  - `composeApp/src/jvmMain/kotlin/App.kt`: Contains the main UI logic, state management, and key event handling.
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
- **Key Interception**: When adding new navigation keys, always check if an edit operation is in progress (`editingIndex != null`) to avoid intercepting text input meant for renaming.

