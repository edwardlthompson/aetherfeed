use tauri::{AppHandle, Manager};

#[tauri::command]
fn unread_total() -> u32 {
    0
}

#[tauri::command]
fn app_data_dir(app: AppHandle) -> Result<String, String> {
    app.path()
        .app_data_dir()
        .map(|path| path.to_string_lossy().into_owned())
        .map_err(|err| err.to_string())
}

pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_notification::init())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .invoke_handler(tauri::generate_handler![unread_total, app_data_dir])
        .setup(|app| {
            if let Some(window) = app.get_webview_window("main") {
                let _ = window.set_title("AetherFeed");
            }
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running AetherFeed");
}
