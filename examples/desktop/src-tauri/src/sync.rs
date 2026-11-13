use std::collections::HashMap;
use std::sync::{Mutex, OnceLock};

pub const SYNC_BLOB_NAME: &str = "aetherfeed-sync.enc";

fn blob_store() -> &'static Mutex<HashMap<String, Vec<u8>>> {
    static STORE: OnceLock<Mutex<HashMap<String, Vec<u8>>>> = OnceLock::new();
    STORE.get_or_init(|| Mutex::new(HashMap::new()))
}

fn memory_key(endpoint: &str) -> String {
    format!("memory://{endpoint}")
}

fn is_memory_endpoint(endpoint: &str) -> bool {
    endpoint.starts_with("memory://") || endpoint.trim().is_empty()
}

pub fn webdav_pull(endpoint: String) -> Result<Option<Vec<u8>>, String> {
    if !is_memory_endpoint(&endpoint) {
        return Err("HTTP WebDAV requires a configured desktop build; use memory:// for tests".into());
    }
    let key = memory_key(&endpoint);
    let guard = blob_store()
        .lock()
        .map_err(|_| "webdav store lock poisoned".to_string())?;
    Ok(guard.get(&key).cloned())
}

pub fn webdav_push(endpoint: String, blob: Vec<u8>) -> Result<(), String> {
    if blob.is_empty() {
        return Err("sync blob must not be empty".into());
    }
    if !is_memory_endpoint(&endpoint) {
        return Err("HTTP WebDAV requires a configured desktop build; use memory:// for tests".into());
    }
    let key = memory_key(&endpoint);
    let mut guard = blob_store()
        .lock()
        .map_err(|_| "webdav store lock poisoned".to_string())?;
    guard.insert(key, blob);
    Ok(())
}

#[tauri::command]
pub fn sync_pull(endpoint: String) -> Result<Option<Vec<u8>>, String> {
    webdav_pull(endpoint)
}

#[tauri::command]
pub fn sync_push(endpoint: String, blob: Vec<u8>) -> Result<(), String> {
    webdav_push(endpoint, blob)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn push_then_pull_round_trip_in_memory() {
        let endpoint = "memory://dav.example.test/sync".to_string();
        assert!(webdav_pull(endpoint.clone()).unwrap().is_none());
        let blob = vec![1u8, 2, 3];
        webdav_push(endpoint.clone(), blob.clone()).unwrap();
        assert_eq!(webdav_pull(endpoint).unwrap(), Some(blob));
    }
}
