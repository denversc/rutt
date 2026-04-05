# Code Review: `rust` branch vs `remotes/origin/dev`

## Summary of Change's Intent
The changes centralize action handling and scrolling logic into `rutt-core`, implement visual themes for both GUI (Apple-like) and TUI (Catppuccin), and add support for directory scrolling in both interfaces.

## Detailed Feedback

**1. `rutt-gui/src/main.rs` [HIGH]**
The `update_items` boolean flag optimization in `update_ui_from_state` is fragile. While it works for `MoveUp`/`MoveDown` inside the event loop, it will fail to update the UI when directory navigation (`Action::Enter`/`Action::Back`) is implemented, as those actions will change the items list but the call currently hardcodes `false` for `update_items`.
*Suggestion:* Change the logic to determine `update_items` dynamically, for example, by checking if the directory path or the underlying items list has changed.

**2. `rutt-gui/src/main.rs` [MEDIUM]**
`update_ui_from_state` only handles the `DirectoryLoaded` state. If the application enters an `Error` or `Loading` state, the GUI will continue to display the previous directory's items with no visual indication of the error or loading status.

**3. `rutt-gui/ui/appwindow.slint` [HIGH]**
Using `event.text` to capture arrow keys is unreliable in Slint. For non-text keys like `UpArrow` or `DownArrow`, `event.text` is typically empty. This means the arrow key functionality currently works in the TUI but will likely be broken in the GUI.
*Suggestion:* Explicitly handle `event.key` for special keys:
```slint
key-pressed(event) => {
    if (event.text != "") {
        root.key_pressed(event.text);
    } else if (event.key == Key.UpArrow) {
        root.key_pressed("k");
    } else if (event.key == Key.DownArrow) {
        root.key_pressed("j");
    }
    accept
}
```

**4. `rutt-gui/ui/appwindow.slint` [MEDIUM]**
Manually setting `listview.viewport-y` when `scroll_offset` changes might conflict with the `ListView`'s internal scrolling state, especially if the user interacts with the mouse or if Slint's internal layout engine tries to manage the scroll position.

**5. `rutt-tui/src/main.rs` [LOW]**
`UI_CHROME_HEIGHT` is a magic number. While it currently matches the layout (header=3, footer=3, borders=2), it makes the layout fragile if constraints or padding are adjusted in the future.
