use std::time::Duration;

const MAX_BYTES: usize = 2_000_000;

#[tauri::command]
pub async fn feed_fetch(url: String) -> Result<String, String> {
    let parsed = reqwest::Url::parse(&url).map_err(|err| err.to_string())?;
    if parsed.scheme() != "http" && parsed.scheme() != "https" {
        return Err("unsupported scheme".into());
    }
    let client = reqwest::Client::builder()
        .user_agent("AetherFeed/0.1")
        .timeout(Duration::from_secs(15))
        .redirect(reqwest::redirect::Policy::limited(8))
        .build()
        .map_err(|err| err.to_string())?;
    let response = client.get(parsed).send().await.map_err(|err| err.to_string())?;
    if !response.status().is_success() {
        return Err(format!("HTTP {}", response.status().as_u16()));
    }
    let bytes = response.bytes().await.map_err(|err| err.to_string())?;
    if bytes.len() > MAX_BYTES {
        return Err("response too large".into());
    }
    Ok(String::from_utf8_lossy(&bytes).into_owned())
}
