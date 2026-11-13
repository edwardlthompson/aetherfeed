//! Fetch feed XML for the desktop news list (avoids WebView CORS).

use std::time::Duration;

#[tauri::command]
pub async fn http_get_text(url: String) -> Result<String, String> {
    let trimmed = url.trim();
    if !(trimmed.starts_with("https://") || trimmed.starts_with("http://")) {
        return Err("only http(s) URLs".into());
    }
    let client = reqwest::Client::builder()
        .timeout(Duration::from_secs(20))
        .redirect(reqwest::redirect::Policy::limited(4))
        .user_agent(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 \
             (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 AetherFeed/1",
        )
        .build()
        .map_err(|e| e.to_string())?;
    let resp = client.get(trimmed).send().await.map_err(|e| e.to_string())?;
    if !resp.status().is_success() {
        return Err(format!("HTTP {}", resp.status().as_u16()));
    }
    resp.text().await.map_err(|e| e.to_string())
}
