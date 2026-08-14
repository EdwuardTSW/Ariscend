import { NoteEditor } from "@/components/notes/note-editor";
import { notFound } from "next/navigation";

export default async function NotePage({ params }: { params: Promise<{ noteId: string }> }) {
  const { noteId } = await params;
  const parsedNoteId = Number(noteId);
  if (!Number.isSafeInteger(parsedNoteId) || parsedNoteId < 1) notFound();
  return <NoteEditor noteId={parsedNoteId} />;
}
