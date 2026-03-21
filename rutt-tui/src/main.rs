use anyhow::Result;
use crossterm::{
    event::{self, Event, KeyCode, KeyEventKind},
    execute,
    terminal::{disable_raw_mode, enable_raw_mode, EnterAlternateScreen, LeaveAlternateScreen},
};
use ratatui::{
    backend::CrosstermBackend,
    layout::{Alignment, Constraint, Layout},
    widgets::{Block, Borders, Paragraph},
    Frame, Terminal,
};
use std::io;

use rutt_core::{Action, FileItem, State};

#[tokio::main]
async fn main() -> Result<()> {
    // Setup terminal
    enable_raw_mode()?;
    let mut stdout = io::stdout();
    execute!(stdout, EnterAlternateScreen)?;
    let backend = CrosstermBackend::new(stdout);
    let mut terminal = Terminal::new(backend)?;

    // Initial Mock State
    let mut state = State::DirectoryLoaded {
        path: "/mock/path".to_string(),
        items: vec![
            FileItem { name: "Documents".to_string(), is_dir: true, size: 0 },
            FileItem { name: "Downloads".to_string(), is_dir: true, size: 0 },
            FileItem { name: "config.toml".to_string(), is_dir: false, size: 1024 },
            FileItem { name: "notes.txt".to_string(), is_dir: false, size: 512 },
        ],
        selected_index: 0,
    };

    // Run the app loop
    let res = run_app(&mut terminal, &mut state).await;

    // Restore terminal
    disable_raw_mode()?;
    execute!(terminal.backend_mut(), LeaveAlternateScreen)?;
    terminal.show_cursor()?;

    if let Err(err) = res {
        println!("{err:?}");
    }

    Ok(())
}

async fn run_app<B: ratatui::backend::Backend>(
    terminal: &mut Terminal<B>,
    state: &mut State,
) -> Result<()> {
    loop {
        terminal.draw(|f| ui(f, state))?;

        if event::poll(std::time::Duration::from_millis(16))? {
            if let Event::Key(key) = event::read()? {
                if key.kind == KeyEventKind::Press {
                    let action = match key.code {
                        KeyCode::Char('q') => Some(Action::Quit),
                        KeyCode::Char('k') | KeyCode::Up => Some(Action::MoveUp),
                        KeyCode::Char('j') | KeyCode::Down => Some(Action::MoveDown),
                        KeyCode::Enter | KeyCode::Char('l') | KeyCode::Right => Some(Action::Enter),
                        KeyCode::Backspace | KeyCode::Char('h') | KeyCode::Left => Some(Action::Back),
                        _ => None,
                    };

                    if let Some(action) = action {
                        if action == Action::Quit {
                            return Ok(());
                        }
                        handle_action(state, action);
                    }
                }
            }
        }
    }
}

fn handle_action(state: &mut State, action: Action) {
    if let State::DirectoryLoaded { items, selected_index, .. } = state {
        match action {
            Action::MoveUp => {
                if *selected_index > 0 {
                    *selected_index -= 1;
                }
            }
            Action::MoveDown => {
                if *selected_index < items.len() - 1 {
                    *selected_index += 1;
                }
            }
            _ => {} // Handle others later
        }
    }
}

fn ui(f: &mut Frame, state: &State) {
    let chunks = Layout::default()
        .direction(ratatui::layout::Direction::Vertical)
        .constraints([
            Constraint::Length(3),
            Constraint::Min(0),
            Constraint::Length(3),
        ])
        .split(f.area());

    let title_text = match state {
        State::DirectoryLoaded { path, .. } => format!("Rutt TUI - {path}"),
        _ => "Rutt TUI".to_string(),
    };

    let title = Paragraph::new(title_text)
        .alignment(Alignment::Center)
        .block(Block::default().borders(Borders::ALL));

    f.render_widget(title, chunks[0]);

    match state {
        State::DirectoryLoaded { items, selected_index, .. } => {
            let list_items: Vec<String> = items
                .iter()
                .enumerate()
                .map(|(i, item)| {
                    let prefix = if i == *selected_index { "> " } else { "  " };
                    let icon = if item.is_dir { "📁" } else { "📄" };
                    format!("{}{} {}", prefix, icon, item.name)
                })
                .collect();

            let content = Paragraph::new(list_items.join("\n"))
                .block(Block::default().borders(Borders::ALL));
            f.render_widget(content, chunks[1]);
        }
        State::Loading => {
            let content = Paragraph::new("Loading...")
                .alignment(Alignment::Center)
                .block(Block::default().borders(Borders::ALL));
            f.render_widget(content, chunks[1]);
        }
        State::Error(err) => {
            let content = Paragraph::new(format!("Error: {err}"))
                .alignment(Alignment::Center)
                .block(Block::default().borders(Borders::ALL));
            f.render_widget(content, chunks[1]);
        }
    }

    let footer = Paragraph::new("Status: Ready | 'q' to quit | 'j/k' to move")
        .alignment(Alignment::Left)
        .block(Block::default().borders(Borders::ALL));

    f.render_widget(footer, chunks[2]);
}
