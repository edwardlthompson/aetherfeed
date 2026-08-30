import { DRIVE_FEEDS_NAME } from "@aetherfeed/domain";

const FILES = "https://www.googleapis.com/drive/v3/files";
const UPLOAD = "https://www.googleapis.com/upload/drive/v3/files";

function asBlob(bytes: Uint8Array): Blob {
  const copy = new Uint8Array(bytes.byteLength);
  copy.set(bytes);
  return new Blob([copy]);
}

async function driveJson<T>(token: string, url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...init,
    headers: { Authorization: `Bearer ${token}`, ...(init?.headers ?? {}) },
  });
  if (!res.ok) throw new Error(`Drive API ${res.status}`);
  return (await res.json()) as T;
}

export async function findFeedsFileId(token: string): Promise<string | null> {
  const q = encodeURIComponent(`name = '${DRIVE_FEEDS_NAME}'`);
  const data = await driveJson<{ files?: { id: string }[] }>(
    token,
    `${FILES}?spaces=appDataFolder&fields=files(id,name)&q=${q}`,
  );
  return data.files?.[0]?.id ?? null;
}

export async function pullFeedsBlob(token: string): Promise<Uint8Array | null> {
  const id = await findFeedsFileId(token);
  if (!id) return null;
  const res = await fetch(`${FILES}/${id}?alt=media`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Drive download ${res.status}`);
  return new Uint8Array(await res.arrayBuffer());
}

export async function pushFeedsBlob(token: string, blob: Uint8Array): Promise<void> {
  if (blob.length === 0) throw new Error("empty sync blob");
  const existing = await findFeedsFileId(token);
  if (existing) {
    const res = await fetch(`${UPLOAD}/${existing}?uploadType=media`, {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/octet-stream",
      },
      body: asBlob(blob),
    });
    if (!res.ok) throw new Error(`Drive update ${res.status}`);
    return;
  }
  const boundary = "aetherfeed";
  const meta = JSON.stringify({ name: DRIVE_FEEDS_NAME, parents: ["appDataFolder"] });
  const head = `--${boundary}\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${meta}\r\n--${boundary}\r\nContent-Type: application/octet-stream\r\n\r\n`;
  const tail = `\r\n--${boundary}--`;
  const body = new Uint8Array(head.length + blob.length + tail.length);
  body.set(new TextEncoder().encode(head), 0);
  body.set(blob, head.length);
  body.set(new TextEncoder().encode(tail), head.length + blob.length);
  const res = await fetch(`${UPLOAD}?uploadType=multipart`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": `multipart/related; boundary=${boundary}`,
    },
    body: asBlob(body),
  });
  if (!res.ok) throw new Error(`Drive create ${res.status}`);
}
