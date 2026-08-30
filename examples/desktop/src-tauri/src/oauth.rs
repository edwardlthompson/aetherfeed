use std::io::{Read, Write};
use std::net::TcpListener;
use std::thread;
use std::time::{Duration, Instant};

const OAUTH_TIMEOUT_SECS: u64 = 180;

#[tauri::command]
pub async fn oauth_wait(port: u16) -> Result<String, String> {
    tauri::async_runtime::spawn_blocking(move || wait_for_code(port))
        .await
        .map_err(|err| err.to_string())?
}

fn wait_for_code(port: u16) -> Result<String, String> {
    let listener = TcpListener::bind(("127.0.0.1", port)).map_err(|err| err.to_string())?;
    listener
        .set_nonblocking(true)
        .map_err(|err| err.to_string())?;
    let started = Instant::now();
    loop {
        match listener.accept() {
            Ok((mut stream, _)) => return read_code(&mut stream),
            Err(err) if err.kind() == std::io::ErrorKind::WouldBlock => {
                if started.elapsed() > Duration::from_secs(OAUTH_TIMEOUT_SECS) {
                    return Err("Drive sign-in timed out".to_string());
                }
                thread::sleep(Duration::from_millis(50));
            }
            Err(err) => return Err(err.to_string()),
        }
    }
}

fn read_code(stream: &mut std::net::TcpStream) -> Result<String, String> {
    let mut buf = [0u8; 4096];
    let read = stream.read(&mut buf).map_err(|err| err.to_string())?;
    let request = String::from_utf8_lossy(&buf[..read]);
    let line = request.lines().next().unwrap_or("");
    let path = line.split_whitespace().nth(1).unwrap_or("");
    let query = path.split_once('?').map(|(_, q)| q).unwrap_or("");
    let code = query
        .split('&')
        .filter_map(|pair| pair.split_once('='))
        .find(|(key, _)| *key == "code")
        .map(|(_, value)| urlencoding_decode(value))
        .ok_or_else(|| {
            query
                .split('&')
                .filter_map(|pair| pair.split_once('='))
                .find(|(key, _)| *key == "error")
                .map(|(_, value)| format!("Drive auth {value}"))
                .unwrap_or_else(|| "missing OAuth code".to_string())
        })?;
    let body = "<html><body>AetherFeed can close this window.</body></html>";
    let response = format!(
        "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: {}\r\n\r\n{body}",
        body.len()
    );
    let _ = stream.write_all(response.as_bytes());
    Ok(code)
}

fn urlencoding_decode(value: &str) -> String {
    let mut out = String::new();
    let bytes = value.as_bytes();
    let mut i = 0;
    while i < bytes.len() {
        if bytes[i] == b'%' && i + 2 < bytes.len() {
            if let Ok(byte) = u8::from_str_radix(&value[i + 1..i + 3], 16) {
                out.push(byte as char);
                i += 3;
                continue;
            }
        }
        out.push(bytes[i] as char);
        i += 1;
    }
    out
}
