mod http_fetch;
mod lan_hub;
mod playback;
mod sync;

use std::sync::atomic::{AtomicU32, Ordering};
use tauri::{AppHandle, Manager};

use http_fetch::http_get_text;
use lan_hub::{
    lan_hub_start, lan_hub_status, lan_hub_stop, lan_put_bearer, open_pool, seal_pool,
};
use playback::{
    playback_pause, playback_play, playback_position, playback_seek, playback_set_speed,
    playback_skip,
};
use sync::{sync_pull, sync_push};

static UNREAD_TOTAL: AtomicU32 = AtomicU32::new(0);

fn tray_tooltip(count: u32) -> String {
    format!("AetherFeed — {count} unread")
}

/// Taskbar/window title badge when count is non-zero (tray tooltip stays on [tray_tooltip]).
fn window_badge_title(count: u32) -> Option<String> {
    if count == 0 {
        None
    } else {
        Some(format!("AetherFeed ({count})"))
    }
}

fn apply_tray_tooltip(app: &AppHandle, count: u32) {
    if let Some(tray) = app.tray_by_id("main") {
        let _ = tray.set_tooltip(Some(&tray_tooltip(count)));
    }
}

/// Windows tray icons do not support numeric badges (`tray-icon` ignores `set_title` on Windows).
/// macOS may show a short title beside the icon. A true Windows taskbar overlay needs
/// `ITaskbarList3::SetOverlayIcon` against the main HWND — we mirror the count on the window
/// title when non-zero so the taskbar button still reflects unread load.
fn apply_tray_badge(app: &AppHandle, count: u32) {
    let badge = window_badge_title(count);
    if let Some(tray) = app.tray_by_id("main") {
        let _ = tray.set_title(badge.as_deref());
    }
    if let Some(window) = app.get_webview_window("main") {
        let title = badge.unwrap_or_else(|| "AetherFeed".to_string());
        let _ = window.set_title(&title);
    }
}

fn apply_unread_chrome(app: &AppHandle, count: u32) {
    apply_tray_tooltip(app, count);
    apply_tray_badge(app, count);
}

#[tauri::command]
fn unread_total() -> u32 {
    UNREAD_TOTAL.load(Ordering::Relaxed)
}

#[tauri::command]
fn set_unread_total(app: AppHandle, count: u32) -> u32 {
    UNREAD_TOTAL.store(count, Ordering::Relaxed);
    apply_unread_chrome(&app, count);
    count
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
            set_unread_total,
            app_data_dir,
            playback_play,
            playback_pause,
            playback_seek,
            playback_skip,
            playback_set_speed,
            playback_position,
            sync_pull,
            sync_push,
            lan_hub_status,
            lan_hub_start,
            lan_hub_stop,
            seal_pool,
            open_pool,
            lan_put_bearer,
            http_get_text,
        ])
        .setup(|app| {
            if let Some(window) = app.get_webview_window("main") {
                let _ = window.set_title("AetherFeed");
            }
            apply_unread_chrome(app.handle(), UNREAD_TOTAL.load(Ordering::Relaxed));
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running AetherFeed");
}

#[cfg(test)]
mod tests {
    use super::{sync::SYNC_BLOB_NAME, tray_tooltip, window_badge_title};

    #[test]
    fn sync_blob_name_is_opaque_enc() {
        assert!(SYNC_BLOB_NAME.ends_with(".enc"));
    }

    #[test]
    fn tray_tooltip_matches_android_widget_contract() {
        assert_eq!(tray_tooltip(0), "AetherFeed — 0 unread");
        assert_eq!(tray_tooltip(3), "AetherFeed — 3 unread");
    }

    #[test]
    fn window_badge_title_only_when_nonzero() {
        assert_eq!(window_badge_title(0), None);
        assert_eq!(window_badge_title(5), Some("AetherFeed (5)".to_string()));
    }
}
