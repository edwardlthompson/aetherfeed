mod feed_fetch;
mod import_file;
mod oauth;

use std::time::Duration;
use tauri::webview::PageLoadEvent;
use tauri::{AppHandle, Manager};

#[tauri::command]
fn unread_total() -> u32 {
    0
}

#[tauri::command]
fn playback_state() -> &'static str {
    "stopped"
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
        .invoke_handler(tauri::generate_handler![
            unread_total,
            playback_state,
            app_data_dir,
            oauth::oauth_wait,
            import_file::pick_import_file,
            import_file::read_text_path,
            import_file::library_read,
            import_file::library_write,
            import_file::seed_write,
            feed_fetch::feed_fetch
        ])
        .on_page_load(|webview, payload| {
            if payload.event() != PageLoadEvent::Finished {
                return;
            }
            let webview = webview.clone();
            std::thread::spawn(move || {
                std::thread::sleep(Duration::from_secs(2));
                let title_webview = webview.clone();
                let _ = webview.eval_with_callback(
                    r#"(function(){try{var n=JSON.parse(localStorage.getItem('af-imported-feeds')||'[]');return Array.isArray(n)?n.length:0;}catch(e){return 0;}})()"#,
                    move |result| {
                        let count = result.trim_matches('"').parse::<u64>().unwrap_or(0);
                        if let Some(window) = title_webview.app_handle().get_webview_window("main")
                        {
                            let title = format!("AetherFeed - {count} subscriptions");
                            let _ = window.set_title(&title);
                        }
                    },
                );
            });
        })
        .setup(|app| {
            if let Ok(dir) = app.path().app_data_dir() {
                let _ = std::fs::create_dir_all(&dir);
            }
            if let Some(window) = app.get_webview_window("main") {
                let _ = window.set_title("AetherFeed");
            }
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running AetherFeed");
}
