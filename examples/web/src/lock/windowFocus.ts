export async function listenWindowFocus(onFocus: (focused: boolean) => void): Promise<() => void> {
  try {
    const { getCurrentWindow } = await import("@tauri-apps/api/window");
    const unlisten = await getCurrentWindow().onFocusChanged((event) => {
      onFocus(Boolean(event.payload));
    });
    return () => {
      unlisten();
    };
  } catch {
    return () => undefined;
  }
}
