import { describe, expect, it } from "vitest";
import { zipTextMembers } from "./zipMembers";

function storedZip(name: string, body: string): ArrayBuffer {
  const encoder = new TextEncoder();
  const nameBytes = encoder.encode(name);
  const data = encoder.encode(body);
  const out = new Uint8Array(30 + nameBytes.length + data.length);
  const view = new DataView(out.buffer);
  view.setUint32(0, 0x04034b50, true);
  view.setUint16(8, 0, true);
  view.setUint32(18, data.length, true);
  view.setUint32(22, data.length, true);
  view.setUint16(26, nameBytes.length, true);
  out.set(nameBytes, 30);
  out.set(data, 30 + nameBytes.length);
  return out.buffer;
}

describe("zip text members", () => {
  it("reads stored XML members and ignores other files", async () => {
    const members = await zipTextMembers(
      storedZip(
        "Takeout/Reader/subscriptions.xml",
        "<opml><outline xmlUrl='https://example.invalid/rss.xml'/></opml>",
      ),
    );
    expect(members["Takeout/Reader/subscriptions.xml"]).toContain("example.invalid");
  });
});
