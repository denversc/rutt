use anyhow::Result;
use rutt_core::{Action, State};
use slint::{ModelRc, SharedString, VecModel};
use std::cell::RefCell;
use std::rc::Rc;

slint::include_modules!();

#[tokio::main]
async fn main() -> Result<()> {
    let app = AppWindow::new()?;

    // Initial Mock State
    let state = Rc::new(RefCell::new(State::DirectoryLoaded {
        path: "/mock/path".to_string(),
        items: rutt_core::get_mock_items(),
        selected_index: 0,
        scroll_offset: 0,
    }));

    update_ui_from_state(&app, &state.borrow(), true);

    let app_weak = app.as_weak();
    let state_clone = state.clone();

    app.on_key_pressed(move |key_text: SharedString| {
        let app = app_weak.unwrap();
        let action = match key_text.as_str() {
            "q" => Some(Action::Quit),
            "k" | "\u{f700}" => Some(Action::MoveUp), // Up Arrow
            "j" | "\u{f701}" => Some(Action::MoveDown), // Down Arrow
            "\n" | "l" | "\u{f703}" => Some(Action::Enter), // Right Arrow
            "\x08" | "h" | "\u{f702}" => Some(Action::Back), // Left Arrow
            _ => None,
        };

        if let Some(action) = action {
            if action == Action::Quit {
                let _ = slint::quit_event_loop();
                return;
            }

            let mut state_ref = state_clone.borrow_mut();
            let visible_count = (app.get_visible_count() as usize).max(1);
            handle_action(&mut state_ref, action, visible_count);
            update_ui_from_state(&app, &state_ref, false);
        }
    });

    app.run()?;

    Ok(())
}

fn handle_action(state: &mut State, action: Action, visible_height: usize) {
    if let State::DirectoryLoaded {
        items,
        selected_index,
        scroll_offset,
        ..
    } = state
    {
        match action {
            Action::MoveUp => {
                if *selected_index > 0 {
                    *selected_index -= 1;
                    if *selected_index < *scroll_offset {
                        *scroll_offset = *selected_index;
                    }
                }
            }
            Action::MoveDown => {
                if *selected_index < items.len() - 1 {
                    *selected_index += 1;
                    if *selected_index >= *scroll_offset + visible_height {
                        *scroll_offset = (*selected_index + 1).saturating_sub(visible_height);
                    }
                }
            }
            _ => {} // Handle others later
        }
    }
}

fn update_ui_from_state(app: &AppWindow, state: &State, update_items: bool) {
    if let State::DirectoryLoaded {
        items,
        selected_index,
        scroll_offset,
        ..
    } = state
    {
        if update_items {
            let ui_items: Vec<UiFileItem> = items
                .iter()
                .map(|item| UiFileItem {
                    name: item.name.clone().into(),
                    is_dir: item.is_dir,
                    size: item.size.to_string().into(),
                })
                .collect();
            let model = Rc::new(VecModel::from(ui_items));
            app.set_items(ModelRc::from(model));
        }
        app.set_selected_index(*selected_index as i32);
        app.set_scroll_offset(*scroll_offset as i32);
    }
}
