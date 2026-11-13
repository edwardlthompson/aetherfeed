//! Home-LAN encrypted pool hub (desktop host).

use aetherfeed_crypto::{put_bearer, random_salt, seal_envelope};
use base64::{engine::general_purpose::STANDARD as B64, Engine as _};
use serde::{Deserialize, Serialize};
use std::net::{IpAddr, Ipv4Addr, SocketAddr};
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::{Mutex, OnceLock};
use tauri::AppHandle;
use tauri::Manager;

pub const DEFAULT_PORT: u16 = 8787;

#[derive(Clone, Serialize, Deserialize)]
pub struct HubMeta {
    pub etag: String,
    #[serde(rename = "authSaltB64")]
    pub auth_salt_b64: String,
    pub port: u16,
}

#[derive(Clone, Serialize)]
pub struct HubStatus {
    pub running: bool,
    pub port: u16,
    pub etag: String,
    #[serde(rename = "authSaltB64")]
    pub auth_salt_b64: String,
    #[serde(rename = "lanUrls")]
    pub lan_urls: Vec<String>,
}

struct HubState {
    running: AtomicBool,
    port: Mutex<u16>,
    passphrase: Mutex<String>,
    auth_salt_b64: Mutex<String>,
    etag: Mutex<String>,
    blob: Mutex<Option<String>>,
    data_dir: Mutex<Option<PathBuf>>,
    runtime: Mutex<Option<tokio::runtime::Runtime>>,
}

fn hub() -> &'static HubState {
    static HUB: OnceLock<HubState> = OnceLock::new();
    HUB.get_or_init(|| HubState {
        running: AtomicBool::new(false),
        port: Mutex::new(DEFAULT_PORT),
        passphrase: Mutex::new(String::new()),
        auth_salt_b64: Mutex::new(String::new()),
        etag: Mutex::new("0".into()),
        blob: Mutex::new(None),
        data_dir: Mutex::new(None),
        runtime: Mutex::new(None),
    })
}

fn is_private_ip(ip: IpAddr) -> bool {
    match ip {
        IpAddr::V4(v4) => {
            v4.is_private() || v4.is_loopback() || v4.is_link_local() || v4.is_unspecified()
        }
        IpAddr::V6(v6) => v6.is_loopback() || v6.is_unique_local() || v6.is_unicast_link_local(),
    }
}

fn lan_urls(port: u16) -> Vec<String> {
    let mut urls = vec![format!("http://127.0.0.1:{port}")];
    if let Ok(ifaces) = local_ip_address::list_afinet_netifas() {
        for (_name, ip) in ifaces {
            if let IpAddr::V4(v4) = ip {
                if v4.is_private() && !v4.is_loopback() {
                    urls.push(format!("http://{v4}:{port}"));
                }
            }
        }
    }
    urls
}

fn persist(state: &HubState) -> Result<(), String> {
    let dir = state
        .data_dir
        .lock()
        .map_err(|_| "hub lock".to_string())?
        .clone()
        .ok_or_else(|| "hub data dir unset".to_string())?;
    std::fs::create_dir_all(&dir).map_err(|e| e.to_string())?;
    let meta = HubMeta {
        etag: state.etag.lock().map_err(|_| "etag lock")?.clone(),
        auth_salt_b64: state
            .auth_salt_b64
            .lock()
            .map_err(|_| "salt lock")?
            .clone(),
        port: *state.port.lock().map_err(|_| "port lock")?,
    };
    let meta_path = dir.join("lan-hub-meta.json");
    std::fs::write(
        &meta_path,
        serde_json::to_string_pretty(&meta).map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    let blob = state.blob.lock().map_err(|_| "blob lock")?.clone();
    let blob_path = dir.join("lan-pool.enc.json");
    match blob {
        Some(body) => std::fs::write(blob_path, body).map_err(|e| e.to_string())?,
        None => {
            let _ = std::fs::remove_file(blob_path);
        }
    }
    Ok(())
}

fn load_from_disk(dir: &Path) -> Result<(), String> {
    let state = hub();
    *state.data_dir.lock().map_err(|_| "dir lock")? = Some(dir.to_path_buf());
    let meta_path = dir.join("lan-hub-meta.json");
    if meta_path.is_file() {
        let raw = std::fs::read_to_string(&meta_path).map_err(|e| e.to_string())?;
        let meta: HubMeta = serde_json::from_str(&raw).map_err(|e| e.to_string())?;
        *state.etag.lock().map_err(|_| "etag lock")? = meta.etag;
        *state.auth_salt_b64.lock().map_err(|_| "salt lock")? = meta.auth_salt_b64;
        *state.port.lock().map_err(|_| "port lock")? = meta.port;
    }
    let blob_path = dir.join("lan-pool.enc.json");
    if blob_path.is_file() {
        let raw = std::fs::read_to_string(blob_path).map_err(|e| e.to_string())?;
        *state.blob.lock().map_err(|_| "blob lock")? = Some(raw);
    }
    Ok(())
}

fn ensure_auth_salt(state: &HubState) -> Result<String, String> {
    let mut salt = state.auth_salt_b64.lock().map_err(|_| "salt lock")?;
    if salt.is_empty() {
        *salt = B64.encode(random_salt());
    }
    Ok(salt.clone())
}

async fn handle(
    method: &str,
    path: &str,
    peer: SocketAddr,
    headers: &[(String, String)],
    body: Option<String>,
) -> (u16, Vec<(String, String)>, String) {
    if !is_private_ip(peer.ip()) {
        return (403, vec![], "forbidden: non-private peer".into());
    }
    let state = hub();
    match (method, path) {
        ("OPTIONS", _) => (204, vec![], "".into()),
        ("GET", "/health") => (200, vec![], "ok".into()),
        ("GET", "/v1/meta") => {
            let meta = HubMeta {
                etag: state.etag.lock().ok().map(|g| g.clone()).unwrap_or_else(|| "0".into()),
                auth_salt_b64: state
                    .auth_salt_b64
                    .lock()
                    .ok()
                    .map(|g| g.clone())
                    .unwrap_or_default(),
                port: state.port.lock().ok().map(|g| *g).unwrap_or(DEFAULT_PORT),
            };
            (
                200,
                vec![("content-type".into(), "application/json".into())],
                serde_json::to_string(&meta).unwrap_or_else(|_| "{}".into()),
            )
        }
        ("GET", "/v1/pool") => {
            let etag = state.etag.lock().ok().map(|g| g.clone()).unwrap_or_else(|| "0".into());
            let salt = state
                .auth_salt_b64
                .lock()
                .ok()
                .map(|g| g.clone())
                .unwrap_or_default();
            let blob = state.blob.lock().ok().and_then(|g| g.clone());
            match blob {
                Some(body) => (
                    200,
                    vec![
                        ("content-type".into(), "application/json".into()),
                        ("etag".into(), etag),
                        ("x-auth-salt-b64".into(), salt),
                    ],
                    body,
                ),
                None => (
                    404,
                    vec![("etag".into(), etag), ("x-auth-salt-b64".into(), salt)],
                    "".into(),
                ),
            }
        }
        ("PUT", "/v1/pool") => {
            let etag = state.etag.lock().ok().map(|g| g.clone()).unwrap_or_else(|| "0".into());
            let if_match = headers
                .iter()
                .find(|(k, _)| k.eq_ignore_ascii_case("if-match"))
                .map(|(_, v)| v.as_str())
                .unwrap_or("");
            if if_match != etag {
                return (412, vec![("etag".into(), etag)], "precondition failed".into());
            }
            let auth = headers
                .iter()
                .find(|(k, _)| k.eq_ignore_ascii_case("authorization"))
                .map(|(_, v)| v.as_str())
                .unwrap_or("");
            let bearer = auth.strip_prefix("Bearer ").unwrap_or("");
            let passphrase = state.passphrase.lock().ok().map(|g| g.clone()).unwrap_or_default();
            let salt = state
                .auth_salt_b64
                .lock()
                .ok()
                .map(|g| g.clone())
                .unwrap_or_default();
            let ok = put_bearer(&passphrase, &salt, &etag)
                .map(|expected| expected == bearer)
                .unwrap_or(false);
            if !ok {
                return (401, vec![], "unauthorized".into());
            }
            let body = match body {
                Some(b) if !b.is_empty() => b,
                _ => return (400, vec![], "empty body".into()),
            };
            let next_etag = (etag.parse::<u64>().unwrap_or(0) + 1).to_string();
            if let Ok(mut g) = state.blob.lock() {
                *g = Some(body);
            }
            if let Ok(mut g) = state.etag.lock() {
                *g = next_etag.clone();
            }
            let _ = persist(state);
            (
                200,
                vec![("etag".into(), next_etag.clone())],
                next_etag,
            )
        }
        _ => (404, vec![], "not found".into()),
    }
}

fn spawn_server(port: u16) -> Result<(), String> {
    let state = hub();
    if state.running.load(Ordering::SeqCst) {
        return Ok(());
    }
    let rt = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .map_err(|e| e.to_string())?;
    let addr = SocketAddr::new(IpAddr::V4(Ipv4Addr::UNSPECIFIED), port);
    state.running.store(true, Ordering::SeqCst);
    rt.spawn(async move {
        let listener = match tokio::net::TcpListener::bind(addr).await {
            Ok(l) => l,
            Err(e) => {
                eprintln!("lan hub bind failed: {e}");
                hub().running.store(false, Ordering::SeqCst);
                return;
            }
        };
        loop {
            let (stream, peer) = match listener.accept().await {
                Ok(v) => v,
                Err(_) => continue,
            };
            tokio::spawn(async move {
                if let Err(err) = serve_conn(stream, peer).await {
                    eprintln!("lan hub conn: {err}");
                }
            });
        }
    });
    *state.runtime.lock().map_err(|_| "runtime lock")? = Some(rt);
    Ok(())
}

async fn serve_conn(stream: tokio::net::TcpStream, peer: SocketAddr) -> Result<(), String> {
    use tokio::io::{AsyncReadExt, AsyncWriteExt};
    let mut buf = vec![0u8; 65_536];
    let mut stream = stream;
    let n = stream.read(&mut buf).await.map_err(|e| e.to_string())?;
    if n == 0 {
        return Ok(());
    }
    let req = String::from_utf8_lossy(&buf[..n]).into_owned();
    let mut lines = req.split("\r\n");
    let request_line = lines.next().unwrap_or("");
    let mut parts = request_line.split_whitespace();
    let method = parts.next().unwrap_or("GET").to_string();
    let path = parts.next().unwrap_or("/").to_string();
    let mut headers = Vec::new();
    let mut content_length = 0usize;
    for line in lines.by_ref() {
        if line.is_empty() {
            break;
        }
        if let Some((k, v)) = line.split_once(':') {
            let key = k.trim().to_string();
            let val = v.trim().to_string();
            if key.eq_ignore_ascii_case("content-length") {
                content_length = val.parse().unwrap_or(0);
            }
            headers.push((key, val));
        }
    }
    let header_end = req.find("\r\n\r\n").map(|i| i + 4).unwrap_or(n);
    let mut body = String::new();
    if content_length > 0 {
        if n > header_end {
            body.push_str(&req[header_end..]);
        }
        while body.len() < content_length {
            let m = stream.read(&mut buf).await.map_err(|e| e.to_string())?;
            if m == 0 {
                break;
            }
            body.push_str(&String::from_utf8_lossy(&buf[..m]));
        }
        if body.len() > content_length {
            body.truncate(content_length);
        }
    }
    let (status, resp_headers, resp_body) = handle(
        &method,
        &path,
        peer,
        &headers,
        if body.is_empty() { None } else { Some(body) },
    )
    .await;
    let reason = match status {
        200 => "OK",
        400 => "Bad Request",
        401 => "Unauthorized",
        403 => "Forbidden",
        404 => "Not Found",
        412 => "Precondition Failed",
        _ => "Error",
    };
    // CORS so tablet/laptop browsers on the same LAN can sync against this hub.
    let mut out = format!(
        "HTTP/1.1 {status} {reason}\r\ncontent-length: {}\r\nconnection: close\r\n\
Access-Control-Allow-Origin: *\r\n\
Access-Control-Allow-Methods: GET, PUT, OPTIONS\r\n\
Access-Control-Allow-Headers: Content-Type, Authorization, If-Match, X-Auth-Salt-B64\r\n\
Access-Control-Expose-Headers: etag, ETag, X-Auth-Salt-B64\r\n",
        resp_body.len()
    );
    for (k, v) in resp_headers {
        out.push_str(&format!("{k}: {v}\r\n"));
    }
    out.push_str("\r\n");
    out.push_str(&resp_body);
    stream.write_all(out.as_bytes()).await.map_err(|e| e.to_string())?;
    Ok(())
}

#[tauri::command]
pub fn lan_hub_status(app: AppHandle) -> Result<HubStatus, String> {
    let dir = app
        .path()
        .app_data_dir()
        .map_err(|e| e.to_string())?;
    let _ = load_from_disk(&dir);
    let state = hub();
    let port = *state.port.lock().map_err(|_| "port lock")?;
    Ok(HubStatus {
        running: state.running.load(Ordering::SeqCst),
        port,
        etag: state.etag.lock().map_err(|_| "etag lock")?.clone(),
        auth_salt_b64: state.auth_salt_b64.lock().map_err(|_| "salt lock")?.clone(),
        lan_urls: lan_urls(port),
    })
}

#[tauri::command]
pub fn lan_hub_start(app: AppHandle, passphrase: String, port: Option<u16>) -> Result<HubStatus, String> {
    if passphrase.trim().is_empty() {
        return Err("passphrase must not be empty".into());
    }
    let dir = app.path().app_data_dir().map_err(|e| e.to_string())?;
    let _ = load_from_disk(&dir);
    let state = hub();
    *state.data_dir.lock().map_err(|_| "dir lock")? = Some(dir);
    *state.passphrase.lock().map_err(|_| "pass lock")? = passphrase;
    if let Some(p) = port {
        *state.port.lock().map_err(|_| "port lock")? = p;
    }
    let _ = ensure_auth_salt(state)?;
    let bind_port = *state.port.lock().map_err(|_| "port lock")?;
    // Seed empty encrypted blob if missing so clients can open after first PUT.
    if state.blob.lock().map_err(|_| "blob lock")?.is_none() {
        let empty = seal_envelope(
            state.passphrase.lock().map_err(|_| "pass lock")?.as_str(),
            br#"{"version":2,"updatedAt":0,"subs":[],"reads":[],"stars":[],"playback":[]}"#,
        )?;
        *state.blob.lock().map_err(|_| "blob lock")? = Some(empty);
        *state.etag.lock().map_err(|_| "etag lock")? = "1".into();
    }
    persist(state)?;
    spawn_server(bind_port)?;
    // Give the accept loop a moment to mark running.
    std::thread::sleep(std::time::Duration::from_millis(50));
    lan_hub_status(app)
}

#[tauri::command]
pub fn lan_hub_stop() -> Result<HubStatus, String> {
    let state = hub();
    state.running.store(false, Ordering::SeqCst);
    // Drop runtime to abort accept loop.
    let _ = state.runtime.lock().map_err(|_| "runtime lock")?.take();
    Ok(HubStatus {
        running: false,
        port: *state.port.lock().map_err(|_| "port lock")?,
        etag: state.etag.lock().map_err(|_| "etag lock")?.clone(),
        auth_salt_b64: state.auth_salt_b64.lock().map_err(|_| "salt lock")?.clone(),
        lan_urls: lan_urls(*state.port.lock().map_err(|_| "port lock")?),
    })
}

#[tauri::command]
pub fn seal_pool(passphrase: String, plaintext: String) -> Result<String, String> {
    seal_envelope(&passphrase, plaintext.as_bytes())
}

#[tauri::command]
pub fn open_pool(passphrase: String, envelope_json: String) -> Result<String, String> {
    let bytes = aetherfeed_crypto::open_envelope(&passphrase, &envelope_json)?;
    String::from_utf8(bytes).map_err(|e| e.to_string())
}

#[tauri::command]
pub fn lan_put_bearer(passphrase: String, auth_salt_b64: String, etag: String) -> Result<String, String> {
    put_bearer(&passphrase, &auth_salt_b64, &etag)
}

/// Test/smoke helper: start hub in-memory without Tauri AppHandle.
pub fn boot_ephemeral(passphrase: &str, port: u16) -> Result<HubStatus, String> {
    if passphrase.trim().is_empty() {
        return Err("passphrase must not be empty".into());
    }
    let state = hub();
    *state.passphrase.lock().map_err(|_| "pass lock")? = passphrase.to_string();
    *state.port.lock().map_err(|_| "port lock")? = port;
    let _ = ensure_auth_salt(state)?;
    if state.blob.lock().map_err(|_| "blob lock")?.is_none() {
        let empty = seal_envelope(
            passphrase,
            br#"{"version":2,"updatedAt":0,"subs":[],"reads":[],"stars":[],"playback":[]}"#,
        )?;
        *state.blob.lock().map_err(|_| "blob lock")? = Some(empty);
        *state.etag.lock().map_err(|_| "etag lock")? = "1".into();
    }
    spawn_server(port)?;
    std::thread::sleep(std::time::Duration::from_millis(80));
    let bind_port = *state.port.lock().map_err(|_| "port lock")?;
    Ok(HubStatus {
        running: state.running.load(Ordering::SeqCst),
        port: bind_port,
        etag: state.etag.lock().map_err(|_| "etag lock")?.clone(),
        auth_salt_b64: state.auth_salt_b64.lock().map_err(|_| "salt lock")?.clone(),
        lan_urls: lan_urls(bind_port),
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use aetherfeed_crypto::{open_envelope, put_bearer, seal_envelope};
    use std::io::{Read, Write};
    use std::net::TcpStream;

    #[test]
    fn private_ipv4_ok() {
        assert!(is_private_ip(IpAddr::V4(Ipv4Addr::new(192, 168, 1, 10))));
        assert!(!is_private_ip(IpAddr::V4(Ipv4Addr::new(8, 8, 8, 8))));
    }

    fn http_exchange(port: u16, req: &str) -> (u16, String, String) {
        let mut stream =
            TcpStream::connect(("127.0.0.1", port)).expect("connect hub");
        stream
            .set_read_timeout(Some(std::time::Duration::from_secs(2)))
            .ok();
        stream.write_all(req.as_bytes()).expect("write");
        let mut buf = Vec::new();
        let _ = stream.read_to_end(&mut buf);
        let raw = String::from_utf8_lossy(&buf);
        let status = raw
            .lines()
            .next()
            .and_then(|l| l.split_whitespace().nth(1))
            .and_then(|s| s.parse().ok())
            .unwrap_or(0);
        let header_end = raw.find("\r\n\r\n").map(|i| i + 4).unwrap_or(raw.len());
        let headers = raw[..header_end].to_string();
        let body = raw[header_end..].to_string();
        (status, headers, body)
    }

    #[test]
    fn hub_http_get_put_roundtrip() {
        let port = 18787u16;
        let pass = "smoke-passphrase";
        let _ = lan_hub_stop();
        // Clear prior blob so this test owns etag seed.
        if let Ok(mut g) = hub().blob.lock() {
            *g = None;
        }
        if let Ok(mut g) = hub().etag.lock() {
            *g = "0".into();
        }
        if let Ok(mut g) = hub().auth_salt_b64.lock() {
            *g = String::new();
        }
        let status = boot_ephemeral(pass, port).expect("boot");
        assert!(status.running, "hub should be running");
        let (health, _, body) = http_exchange(port, "GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assert_eq!(health, 200);
        assert_eq!(body, "ok");

        let (meta_code, _, meta_body) =
            http_exchange(port, "GET /v1/meta HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assert_eq!(meta_code, 200);
        let meta: HubMeta = serde_json::from_str(&meta_body).expect("meta json");
        assert!(!meta.auth_salt_b64.is_empty());

        let (get_code, get_headers, get_body) =
            http_exchange(port, "GET /v1/pool HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assert_eq!(get_code, 200);
        assert!(get_headers.to_lowercase().contains("x-auth-salt-b64"));
        let plain = open_envelope(pass, &get_body).expect("open seed");
        assert!(String::from_utf8_lossy(&plain).contains("\"version\":2"));

        let etag = get_headers
            .lines()
            .find(|l| l.to_lowercase().starts_with("etag:"))
            .map(|l| l.split_once(':').unwrap().1.trim().to_string())
            .unwrap_or_else(|| meta.etag.clone());
        let next_doc = r#"{"version":2,"updatedAt":9,"subs":[{"url":"https://smoke.example/rss","title":"Smoke","kind":"news","tag":"","updatedAt":9}],"reads":[],"stars":[],"playback":[]}"#;
        let envelope = seal_envelope(pass, next_doc.as_bytes()).expect("seal");
        let bearer = put_bearer(pass, &meta.auth_salt_b64, &etag).expect("bearer");
        let put_req = format!(
            "PUT /v1/pool HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nIf-Match: {etag}\r\nAuthorization: Bearer {bearer}\r\nContent-Length: {}\r\n\r\n{envelope}",
            envelope.len()
        );
        let (put_code, _, _) = http_exchange(port, &put_req);
        assert_eq!(put_code, 200, "put should succeed");

        let (get2, _, body2) =
            http_exchange(port, "GET /v1/pool HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assert_eq!(get2, 200);
        let plain2 = open_envelope(pass, &body2).expect("open after put");
        assert!(String::from_utf8_lossy(&plain2).contains("smoke.example"));

        let _ = lan_hub_stop();
    }
}
