# Rutt - Project Outline

## 1. Architectural Strategy: The "Core Engine" Approach
The biggest challenge of having both a TUI (`ratatui`) and a GUI (`slint`) is avoiding code duplication. Architect the application with a strict separation of concerns:
*   **Core Crate/Module:** This should be a completely UI-agnostic engine. It handles file system traversal, state management (current directory, selected files, clipboard), file operations (copy, move, delete), and the Vim-state machine (Normal, Visual, Command modes).
*   **Event Bus/Message Passing:** The Core should communicate with the UI via channels (e.g., `mpsc`). The UI sends user intents (e.g., `Action::MoveDown`, `Action::Yank`), and the Core emits state updates (e.g., `State::DirectoryLoaded`, `State::Error`).
*   **UI Crates/Modules:** Two thin wrappers—one for `slint` and one for `ratatui`—that solely translate key presses into Core actions and render the Core's current state.

## 2. Suggested Rust Crates (Ecosystem)
Leverage the excellent Rust ecosystem to avoid reinventing the wheel:
*   **Async Runtime:** `tokio`. Offload FS operations to background tasks to prevent UI freezing.
*   **File System Watching:** `notify`. Handle cross-platform FS events perfectly so the explorer updates instantly when files change externally.
*   **Configuration:** `serde` + `toml` (or `kdl`). Make Vim bindings, themes, and layouts customizable. Use the `directories` crate to reliably find the `~/.config/rutt` folder across macOS, Windows, and Linux.
*   **Fast Traversal:** `ignore` (from the `ripgrep` author) or `jwalk` for insanely fast directory reading, useful for fuzzy searching or folder size calculations.
*   **Clipboard:** `arboard` for cross-platform clipboard support (integrating yank/put with the system clipboard).

## 3. Potential Challenges
*   **Cross-Platform File System Quirks:** Windows handles file paths, hidden files, and permissions very differently than macOS/Linux. Use `std::path::PathBuf` strictly and handle OS-specific metadata using `std::os::unix::fs::MetadataExt` and its Windows equivalent.
*   **Image/File Previews:** 
    *   *GUI (Slint)*: Relatively straightforward to render images.
    *   *TUI (Ratatui)*: Harder. Look into libraries that support the Kitty graphics protocol or Sixel to render images in the terminal, or fallback to text/hex previews.
*   **Vim Keybinding State:** Implementing a Vim engine can get complex quickly (handling counts like `3j`, or compound commands like `d/foo<CR>`). Start simple with basic modes (Normal, Command) and add Visual mode later.

## 4. Inspiration
Existing TUI file managers in Rust to study for handling file I/O and caching:
*   **Yazi:** Currently the fastest and most popular async Rust TUI file manager.
*   **Xplr:** Very customizable, keyboard-centric Rust file explorer.
*   **Broot:** Great for tree-based navigation.

## 5. Next Steps
*   Structure `Cargo.toml` into a workspace or distinct modules right away (e.g., `rutt-core`, `rutt-tui`, `rutt-gui`).
*   Scaffold the core logic.
*   Set up the basic Slint/Ratatui "Hello World" windows.
