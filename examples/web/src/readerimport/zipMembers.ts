/** Read text members from a local ZIP (stored or deflate). */

async function inflateRaw(comp: Uint8Array): Promise<Uint8Array> {
  const copy = Uint8Array.from(comp);
  const stream = new Blob([copy]).stream().pipeThrough(new DecompressionStream("deflate-raw"));
  return new Uint8Array(await new Response(stream).arrayBuffer());
}

export async function zipTextMembers(buffer: ArrayBuffer): Promise<Record<string, string>> {
  const view = new DataView(buffer);
  const bytes = new Uint8Array(buffer);
  const decoder = new TextDecoder();
  const members: Record<string, string> = {};
  let offset = 0;
  while (offset + 30 <= bytes.length && view.getUint32(offset, true) === 0x04034b50) {
    const method = view.getUint16(offset + 8, true);
    const compSize = view.getUint32(offset + 18, true);
    const nameLen = view.getUint16(offset + 26, true);
    const extraLen = view.getUint16(offset + 28, true);
    const nameStart = offset + 30;
    const name = decoder.decode(bytes.subarray(nameStart, nameStart + nameLen));
    const dataStart = nameStart + nameLen + extraLen;
    const comp = bytes.subarray(dataStart, dataStart + compSize);
    offset = dataStart + compSize;
    if (name.endsWith("/") || !/\.(xml|opml|json)$/i.test(name)) continue;
    const data = method === 0 ? comp : method === 8 ? await inflateRaw(comp) : null;
    if (data) members[name] = decoder.decode(data);
  }
  return members;
}
