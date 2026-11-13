use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::sync::Mutex;

static POSITION_MS: AtomicU64 = AtomicU64::new(0);
static PLAYING: AtomicBool = AtomicBool::new(false);
static PLAYBACK_SPEED: Mutex<f32> = Mutex::new(1.0);

/// Maps a signed delta onto position, saturating at zero (mirrors Android skip contract).
pub fn skip_position(current_ms: u64, delta: i32) -> u64 {
    let next = i64::try_from(current_ms)
        .unwrap_or(i64::MAX)
        .saturating_add(i64::from(delta));
    u64::try_from(next.max(0)).unwrap_or(0)
}

/// Absolute seek replaces position (PlaybackCommand.Seek).
pub fn seek_position(_current_ms: u64, position_ms: u64) -> u64 {
    position_ms
}

#[tauri::command]
pub fn playback_play() -> bool {
    PLAYING.store(true, Ordering::Relaxed);
    true
}

#[tauri::command]
pub fn playback_pause() -> bool {
    PLAYING.store(false, Ordering::Relaxed);
    false
}

#[tauri::command]
pub fn playback_seek(ms: u64) -> u64 {
    let current = POSITION_MS.load(Ordering::Relaxed);
    let next = seek_position(current, ms);
    POSITION_MS.store(next, Ordering::Relaxed);
    next
}

#[tauri::command]
pub fn playback_skip(delta: i32) -> u64 {
    let current = POSITION_MS.load(Ordering::Relaxed);
    let next = skip_position(current, delta);
    POSITION_MS.store(next, Ordering::Relaxed);
    next
}

#[tauri::command]
pub fn playback_set_speed(rate: f32) -> f32 {
    let stored = rate.clamp(0.25, 3.0);
    if let Ok(mut guard) = PLAYBACK_SPEED.lock() {
        *guard = stored;
    }
    stored
}

#[tauri::command]
pub fn playback_position() -> u64 {
    POSITION_MS.load(Ordering::Relaxed)
}

#[cfg(test)]
mod tests {
    use super::{seek_position, skip_position};

    #[test]
    fn seek_sets_absolute_position() {
        assert_eq!(seek_position(30_000, 12_500), 12_500);
        assert_eq!(seek_position(0, 0), 0);
    }

    #[test]
    fn skip_advances_and_clamps_at_zero() {
        assert_eq!(skip_position(10_000, 5_000), 15_000);
        assert_eq!(skip_position(3_000, -5_000), 0);
        assert_eq!(skip_position(0, -100), 0);
    }
}
