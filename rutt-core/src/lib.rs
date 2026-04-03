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
        scroll_offset: usize,
    },
    Error(String),
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct FileItem {
    pub name: String,
    pub is_dir: bool,
    pub size: u64,
}

pub fn get_mock_items() -> Vec<FileItem> {
    vec![
        FileItem {
            name: "Documents".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Downloads".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Projects".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Pictures".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Music".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Videos".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Library".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "System".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Users".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "Applications".to_string(),
            is_dir: true,
            size: 0,
        },
        FileItem {
            name: "config.toml".to_string(),
            is_dir: false,
            size: 1024,
        },
        FileItem {
            name: "notes.txt".to_string(),
            is_dir: false,
            size: 512,
        },
        FileItem {
            name: "photo.png".to_string(),
            is_dir: false,
            size: 2048576,
        },
        FileItem {
            name: "song.mp3".to_string(),
            is_dir: false,
            size: 5120000,
        },
        FileItem {
            name: "video.mp4".to_string(),
            is_dir: false,
            size: 102400000,
        },
        FileItem {
            name: "README.md".to_string(),
            is_dir: false,
            size: 2048,
        },
        FileItem {
            name: ".gitignore".to_string(),
            is_dir: false,
            size: 128,
        },
        FileItem {
            name: "Cargo.toml".to_string(),
            is_dir: false,
            size: 256,
        },
        FileItem {
            name: "main.rs".to_string(),
            is_dir: false,
            size: 4096,
        },
        FileItem {
            name: "lib.rs".to_string(),
            is_dir: false,
            size: 2048,
        },
        FileItem {
            name: "styles.css".to_string(),
            is_dir: false,
            size: 1024,
        },
        FileItem {
            name: "index.html".to_string(),
            is_dir: false,
            size: 512,
        },
        FileItem {
            name: "script.js".to_string(),
            is_dir: false,
            size: 2048,
        },
        FileItem {
            name: "archive.zip".to_string(),
            is_dir: false,
            size: 512000000,
        },
    ]
}
