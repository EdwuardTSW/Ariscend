"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Archive, FilePlus2, Pin, RefreshCw, Search } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { notesApi } from "@/services/notes-api";
import type { Note } from "@/types/api";

const dateFormatter = new Intl.DateTimeFormat("es-MX", {
  day: "numeric",
  month: "short",
  year: "numeric",
});

export function NotesList() {
  const { selectedUser } = useSelectedUser();
  const router = useRouter();
  const [notes, setNotes] = useState<Note[]>([]);
  const [query, setQuery] = useState("");
  const [archived, setArchived] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!selectedUser) return;
    const controller = new AbortController();
    const timer = window.setTimeout(async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await notesApi.list(selectedUser.id, { query, archived }, controller.signal);
        setNotes(result.content);
      } catch (requestError) {
        if (controller.signal.aborted) return;
        setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar las notas.");
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }, 220);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [archived, query, reloadKey, selectedUser]);

  async function createNote() {
    if (!selectedUser) return;
    setCreating(true);
    try {
      const note = await notesApi.create(selectedUser.id);
      router.push(`/notas/${note.id}`);
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo crear la nota.");
      setCreating(false);
    }
  }

  async function togglePin(note: Note) {
    if (!selectedUser) return;
    try {
      const updated = await notesApi.setPinned(selectedUser.id, note.id, !note.pinned);
      setNotes((current) => current.map((item) => (item.id === note.id ? updated : item)));
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo actualizar la nota.");
    }
  }

  return (
    <div>
      <section className="mb-7 flex flex-col gap-5 md:mb-9 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 font-[var(--font-geist)] text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Captura tus ideas</p>
          <h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Notas</h1>
          <p className="mt-2 text-[#a9abad]">Un espacio rápido para todo lo que no quieres olvidar.</p>
        </div>
        <Button onClick={() => void createNote()} disabled={creating}>
          <FilePlus2 className="size-4" /> {creating ? "Creando..." : "Nueva nota"}
        </Button>
      </section>

      <div className="mb-6 flex flex-col gap-3 sm:flex-row">
        <label className="relative flex-1">
          <Search className="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-[#77797c]" />
          <input
            aria-label="Buscar en tus notas"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Buscar en tus notas"
            className="focus-ring h-12 w-full rounded-full border border-white/10 bg-[#101010] pl-11 pr-4 text-sm placeholder:text-white/25"
          />
        </label>
        <Button variant={archived ? "primary" : "secondary"} onClick={() => setArchived((value) => !value)}>
          <Archive className="size-4" /> {archived ? "Archivadas" : "Ver archivo"}
        </Button>
      </div>

      {loading ? (
        <PageLoading label="Ordenando tus notas" />
      ) : error ? (
        <ObsidianCard className="flex min-h-52 flex-col items-center justify-center gap-4 p-7 text-center">
          <p className="text-[#b9b9bb]">{error}</p>
          <Button variant="secondary" onClick={() => setReloadKey((value) => value + 1)}><RefreshCw className="size-4" /> Reintentar</Button>
        </ObsidianCard>
      ) : notes.length === 0 ? (
        <ObsidianCard className="flex min-h-64 flex-col items-center justify-center p-8 text-center">
          <FilePlus2 className="mb-5 size-8 text-[#77797c]" />
          <h2 className="text-xl font-semibold">{archived ? "No tienes notas archivadas" : "Tu mente tiene espacio"}</h2>
          <p className="mt-2 max-w-md text-[#8f9194]">{archived ? "Las notas que archives aparecerán aquí." : "Crea una nota y empieza a escribir sin configurar nada más."}</p>
          {!archived && <Button className="mt-6" onClick={() => void createNote()}>Crear mi primera nota</Button>}
        </ObsidianCard>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {notes.map((note) => (
            <ObsidianCard key={note.id} className="group min-h-52 p-5 transition hover:-translate-y-0.5 hover:bg-[#171717]">
              <button
                onClick={() => void togglePin(note)}
                className={`focus-ring absolute right-4 top-4 z-20 flex size-11 items-center justify-center rounded-full transition ${note.pinned ? "bg-white text-black" : "text-[#a9abad] hover:bg-white/[0.06] hover:text-white"}`}
                aria-label={note.pinned ? `Desfijar ${note.title || "nota sin título"}` : `Fijar ${note.title || "nota sin título"}`}
                aria-pressed={note.pinned}
              >
                <Pin className="size-4" />
              </button>
              <Link href={`/notas/${note.id}`} className="focus-ring relative z-10 block h-full rounded-lg pr-12">
                <div className="mb-8">
                  <h2 className="line-clamp-2 text-xl font-semibold leading-7">{note.title?.trim() || "Nota sin título"}</h2>
                </div>
                <p className="line-clamp-4 whitespace-pre-wrap text-sm leading-6 text-[#9b9da0]">
                  {note.content.trim() || "Empieza a escribir..."}
                </p>
                <p className="absolute bottom-0 left-0 font-[var(--font-geist)] text-xs uppercase tracking-[0.08em] text-[#a9abad]">
                  {dateFormatter.format(new Date(note.updatedAt))}
                </p>
              </Link>
            </ObsidianCard>
          ))}
        </div>
      )}
    </div>
  );
}
