use std::fs;
use std::path::PathBuf;
use tauri::{AppHandle, Manager};

#[derive(serde::Serialize)]
pub struct ImportFilePayload {
    pub name: String,
    pub text: String,
}

fn library_path(app: &AppHandle) -> Result<PathBuf, String> {
    let dir = app.path().app_data_dir().map_err(|err| err.to_string())?;
    fs::create_dir_all(&dir).map_err(|err| err.to_string())?;
    Ok(dir.join("imported-feeds.json"))
}

#[tauri::command]
pub fn read_text_path(path: String) -> Result<String, String> {
    fs::read_to_string(path).map_err(|err| err.to_string())
}

#[tauri::command]
pub fn library_read(app: AppHandle) -> Result<String, String> {
    let path = library_path(&app)?;
    if !path.exists() {
        return Ok("[]".to_string());
    }
    fs::read_to_string(path).map_err(|err| err.to_string())
}

#[tauri::command]
pub fn library_write(app: AppHandle, json: String) -> Result<(), String> {
    fs::write(library_path(&app)?, json).map_err(|err| err.to_string())
}

#[tauri::command]
pub fn seed_write(app: AppHandle, json: String) -> Result<(), String> {
    let dir = app.path().app_data_dir().map_err(|err| err.to_string())?;
    fs::create_dir_all(&dir).map_err(|err| err.to_string())?;
    fs::write(dir.join("seed-library.json"), json).map_err(|err| err.to_string())
}

#[tauri::command]
pub async fn pick_import_file() -> Result<Option<ImportFilePayload>, String> {
    tauri::async_runtime::spawn_blocking(read_picked_file)
        .await
        .map_err(|err| err.to_string())?
}

fn read_picked_file() -> Result<Option<ImportFilePayload>, String> {
    let path: PathBuf = match rfd::FileDialog::new()
        .add_filter("Reader export", &["opml", "xml", "json"])
        .pick_file()
    {
        Some(path) => path,
        None => return Ok(None),
    };
    let name = path
        .file_name()
        .map(|value| value.to_string_lossy().into_owned())
        .unwrap_or_else(|| "export.opml".to_string());
    let text = fs::read_to_string(&path).map_err(|err| err.to_string())?;
    Ok(Some(ImportFilePayload { name, text }))
}
