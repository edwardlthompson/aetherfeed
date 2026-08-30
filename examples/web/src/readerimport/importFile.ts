/** Turn a user-selected File into a ReaderImportFile. */

import type { ReaderImportFile } from "@aetherfeed/domain";
import { zipTextMembers } from "./zipMembers";

export async function fileToReaderImport(file: File): Promise<ReaderImportFile> {
  const name = file.name;
  if (name.toLowerCase().endsWith(".zip")) {
    const members = await zipTextMembers(await file.arrayBuffer());
    return { name, members };
  }
  return { name, text: await file.text() };
}
