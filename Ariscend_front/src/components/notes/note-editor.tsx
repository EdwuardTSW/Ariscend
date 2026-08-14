"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Archive, ArrowLeft, Check, Pin, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { PageLoading } from "@/components/feedback/page-loading";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { notesApi } from "@/services/notes-api";
import type { Note } from "@/types/api";

export function NoteEditor({ noteId }: { noteId: number }) {
  const { selectedUser } = useSelectedUser();
  const router = useRouter();
  const [note, setNote] = useState<Note | null>(null);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [saveState, setSaveState] = useState<"idle" | "saving" | "saved" | "error">("idle");
  const lastSaved = useRef("");
  const saveController = useRef<AbortController | null>(null);
  const saveVersion = useRef(0);
  const titleRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    const controller = new AbortController();
    void notesApi.get(selectedUser.id, noteId, controller.signal)
      .then((result) => {
        if (!active) return;
        const initialTitle = result.title ?? "";
        setNote(result);
        setTitle(initialTitle);
        setContent(result.content);
        lastSaved.current = JSON.stringify({ title: initialTitle, content: result.content });
        window.setTimeout(() => titleRef.current?.focus(), 80);
      })
      .catch((requestError) => {
        if (controller.signal.aborted) return;
        toast.error(requestError instanceof Error ? requestError.message : "No se pudo abrir la nota.");
        router.replace("/notas");
      })
      .finally(() => active && setLoading(false));
    return () => { active = false; controller.abort(); saveController.current?.abort(); };
  }, [noteId, router, selectedUser]);

  const persist = useCallback(async (nextTitle: string, nextContent: string) => {
    if (!selectedUser || !note) return false;
    const snapshot = JSON.stringify({ title: nextTitle, content: nextContent });
    if (snapshot === lastSaved.current) return true;
    saveController.current?.abort();
    const controller = new AbortController();
    const version = ++saveVersion.current;
    saveController.current = controller;
    setSaveState("saving");
    try {
      const updated = await notesApi.update(selectedUser.id, note.id, {
        title: nextTitle.trim() ? nextTitle : null,
        content: nextContent,
      }, controller.signal);
      if (version !== saveVersion.current) return false;
      lastSaved.current = snapshot;
      setNote(updated);
      setSaveState("saved");
      return true;
    } catch (requestError) {
      if (controller.signal.aborted) return false;
      setSaveState("error");
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo guardar la nota.");
      return false;
    }
  }, [note, selectedUser]);

  useEffect(() => {
    if (!selectedUser || !note) return;
    const snapshot = JSON.stringify({ title, content });
    if (snapshot === lastSaved.current) return;
    const timer = window.setTimeout(() => void persist(title, content), 700);
    return () => window.clearTimeout(timer);
  }, [content, note, persist, selectedUser, title]);

  async function goBack() {
    if (await persist(title, content)) router.push("/notas");
  }

  async function togglePin() {
    if (!selectedUser || !note) return;
    try {
      setNote(await notesApi.setPinned(selectedUser.id, note.id, !note.pinned));
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo actualizar la nota.");
    }
  }

  async function archive() {
    if (!selectedUser || !note) return;
    try {
      if (!await persist(title, content)) return;
      await notesApi.setArchived(selectedUser.id, note.id, !note.archived);
      toast.success(note.archived ? "Nota restaurada." : "Nota archivada.");
      router.push("/notas");
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo archivar la nota.");
    }
  }

  async function remove() {
    if (!selectedUser || !note || !window.confirm("¿Eliminar esta nota definitivamente?")) return;
    try {
      await notesApi.delete(selectedUser.id, note.id);
      toast.success("Nota eliminada.");
      router.push("/notas");
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo eliminar la nota.");
    }
  }

  if (loading || !note) return <PageLoading label="Abriendo tu nota" />;

  return (
    <div className="mx-auto max-w-4xl">
      <div className="mb-6 flex items-center justify-between gap-3">
        <Button variant="ghost" size="small" onClick={() => void goBack()}><ArrowLeft className="size-4" /> Notas</Button>
        <div className="flex items-center gap-1">
          <span aria-live="polite" className={`mr-2 flex items-center gap-1.5 text-xs ${saveState === "error" ? "text-red-300" : "text-[#a9abad]"}`}>
            {saveState === "saving" ? "Guardando..." : saveState === "error" ? "Sin guardar" : <><Check className="size-3.5" /> Guardado</>}
          </span>
          <Button variant={note.pinned ? "primary" : "ghost"} size="icon" onClick={() => void togglePin()} aria-label={note.pinned ? "Desfijar nota" : "Fijar nota"} aria-pressed={note.pinned}><Pin className="size-4" /></Button>
          <Button variant="ghost" size="icon" onClick={() => void archive()} aria-label={note.archived ? "Restaurar nota" : "Archivar nota"}><Archive className="size-4" /></Button>
          <Button variant="ghost" size="icon" onClick={() => void remove()} aria-label="Eliminar nota" className="hover:text-red-200"><Trash2 className="size-4" /></Button>
        </div>
      </div>

      <article className="min-h-[65vh] rounded-3xl border border-white/[0.07] bg-[#0b0b0b] p-5 md:p-10">
        <input
          aria-label="Título de la nota"
          ref={titleRef}
          value={title}
          maxLength={150}
          onChange={(event) => setTitle(event.target.value)}
          placeholder="Nota sin título"
          className="w-full border-none bg-transparent text-3xl font-semibold tracking-[-0.04em] outline-none placeholder:text-white/20 md:text-5xl"
        />
        <textarea
          aria-label="Contenido de la nota"
          value={content}
          maxLength={50000}
          onChange={(event) => setContent(event.target.value)}
          placeholder="Empieza a escribir..."
          className="mt-8 min-h-[48vh] w-full resize-none border-none bg-transparent text-base leading-8 text-[#d0d0d2] outline-none placeholder:text-white/20 md:text-lg"
        />
      </article>
    </div>
  );
}
