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
    style::{Color, Style, Stylize},
    text::{Line, Span},
    Frame, Terminal,
};
use std::io;

use rutt_core::{Action, State};

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
        items: rutt_core::get_mock_items(),
        selected_index: 0,
        scroll_offset: 0,
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
        let mut visible_height = 0;
        terminal.draw(|f| {
            visible_height = (f.area().height as usize).saturating_sub(6 + 2); // 3 (header) + 3 (footer) + 2 (borders)
            ui(f, state);
        })?;

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
                        handle_action(state, action, visible_height);
                    }
                }
            }
        }
    }
}

fn handle_action(state: &mut State, action: Action, visible_height: usize) {
    if let State::DirectoryLoaded { items, selected_index, scroll_offset, .. } = state {
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
        State::DirectoryLoaded { path, .. } => format!(" Rutt TUI - {path} "),
        _ => " Rutt TUI ".to_string(),
    };

    let title = Paragraph::new(title_text)
        .alignment(Alignment::Center)
        .style(Style::default().bg(Color::Rgb(24, 24, 37)).fg(Color::Rgb(203, 166, 247)).bold()) // Mantle bg, Mauve fg
        .block(Block::default().borders(Borders::ALL).border_style(Style::default().fg(Color::Rgb(69, 71, 90)))); // Surface1 border

    f.render_widget(title, chunks[0]);

    match state {
        State::DirectoryLoaded { items, selected_index, scroll_offset, .. } => {
            let list_items: Vec<Line> = items
                .iter()
                .enumerate()
                .skip(*scroll_offset)
                .take((chunks[1].height as usize).saturating_sub(2)) // visible height within borders
                .map(|(i, item)| {
                    let is_selected = i == *selected_index;
                    let prefix = if is_selected { "> " } else { "  " };
                    let icon = if item.is_dir { "📁 " } else { "📄 " };
                    
                    let mut spans = vec![
                        Span::from(prefix).fg(Color::Rgb(245, 194, 231)).bold(), // Pink
                        Span::from(icon),
                    ];
                    
                    let item_style = if item.is_dir {
                        Style::default().fg(Color::Rgb(137, 180, 250)) // Blue
                    } else {
                        Style::default().fg(Color::Rgb(205, 214, 244)) // Text
                    };
                    
                    let item_style = if is_selected {
                        item_style.bg(Color::Rgb(49, 50, 68)).bold() // Surface0 bg
                    } else {
                        item_style
                    };
                    
                    spans.push(Span::from(item.name.clone()).style(item_style));
                    
                    Line::from(spans)
                })
                .collect();

            let content = Paragraph::new(list_items)
                .block(Block::default().borders(Borders::ALL).border_style(Style::default().fg(Color::Rgb(69, 71, 90)))); // Surface1 border
            f.render_widget(content, chunks[1]);
        }
        State::Loading => {
            let content = Paragraph::new("Loading...")
                .alignment(Alignment::Center)
                .style(Style::default().fg(Color::Rgb(205, 214, 244))) // Text
                .block(Block::default().borders(Borders::ALL).border_style(Style::default().fg(Color::Rgb(69, 71, 90)))); // Surface1 border
            f.render_widget(content, chunks[1]);
        }
        State::Error(err) => {
            let content = Paragraph::new(format!("Error: {err}"))
                .alignment(Alignment::Center)
                .style(Style::default().fg(Color::Rgb(243, 139, 168))) // Red
                .block(Block::default().borders(Borders::ALL).border_style(Style::default().fg(Color::Rgb(69, 71, 90)))); // Surface1 border
            f.render_widget(content, chunks[1]);
        }
    }

    let footer = Paragraph::new(" Status: Ready | 'q' to quit | 'j/k' to move ")
        .alignment(Alignment::Left)
        .style(Style::default().bg(Color::Rgb(24, 24, 37)).fg(Color::Rgb(108, 112, 134))) // Mantle bg, Overlay0 fg
        .block(Block::default().borders(Borders::ALL).border_style(Style::default().fg(Color::Rgb(69, 71, 90)))); // Surface1 border

    f.render_widget(footer, chunks[2]);
}
