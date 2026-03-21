use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum Action {
    Quit,
    MoveUp,
    MoveDown,
    Enter,
    Back,
    // Add more actions as needed
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum State {
    Loading,
    DirectoryLoaded {
        path: String,
        items: Vec<FileItem>,
        selected_index: usize,
    },
    Error(String),
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct FileItem {
    pub name: String,
    pub is_dir: bool,
    pub size: u64,
}
