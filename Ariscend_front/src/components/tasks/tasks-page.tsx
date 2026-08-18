"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Archive, Check, ChevronDown, Circle, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { PageError } from "@/components/feedback/page-error";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { tasksApi } from "@/services/tasks-api";
import type { Task } from "@/types/api";

export function TasksPage() {
  const { selectedUser } = useSelectedUser();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState("");
  const [priority, setPriority] = useState<Task["priority"]>("MEDIUM");
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [archiveOpen, setArchiveOpen] = useState(true);

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    tasksApi.list(selectedUser.id)
      .then((result) => { if (active) setTasks(result); })
      .catch((requestError) => { if (active) setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar los pendientes."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reloadKey, selectedUser]);

  async function submit(event: FormEvent) {
    event.preventDefault(); if (!selectedUser) return;
    try {
      const task = await tasksApi.create(selectedUser.id, { title, priority });
      setTasks((current) => [task, ...current]); setTitle(""); setShowForm(false);
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo crear el pendiente."); }
  }

  async function toggle(task: Task) {
    if (!selectedUser) return;
    setUpdatingId(task.id);
    try {
      const updated = await tasksApi.setCompleted(selectedUser.id, task.id, !task.completed);
      setTasks((current) => current.map((item) => item.id === task.id ? updated : item));
      if (!task.completed) setArchiveOpen(true);
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo actualizar."); }
    finally { setUpdatingId(null); }
  }

  async function remove(task: Task) {
    if (!selectedUser || !window.confirm(`¿Eliminar “${task.title}”?`)) return;
    try { await tasksApi.delete(selectedUser.id, task.id); setTasks((current) => current.filter((item) => item.id !== task.id)); }
    catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo eliminar."); }
  }

  if (loading) return <PageLoading label="Cargando pendientes" />;
  if (error) return <PageError message={error} onRetry={() => { setError(null); setLoading(true); setReloadKey((value) => value + 1); }} />;

  const priorityLabel = { LOW: "Baja", MEDIUM: "Media", HIGH: "Alta" };
  const pendingTasks = tasks.filter((task) => !task.completed);
  const archivedTasks = tasks.filter((task) => task.completed);

  function taskCard(task: Task, index: number) {
    return (
      <ObsidianCard key={task.id} style={{ animationDelay: `${index * 45}ms` }} className={`animate-enter flex items-center gap-4 p-4 transition-all duration-300 md:p-5 ${task.completed ? "border-white/[0.06] bg-[#080808] opacity-70" : "hover:border-white/20"}`}>
        <button aria-label={task.completed ? `Marcar ${task.title} como pendiente` : `Completar ${task.title}`} aria-pressed={task.completed} disabled={updatingId === task.id} onClick={() => void toggle(task)} className="focus-ring relative z-10 flex size-11 items-center justify-center rounded-full transition disabled:opacity-50">
          {task.completed ? <Check className="animate-check-pop size-7 rounded-full bg-white p-1.5 text-black" /> : <Circle className="size-7 text-[#a9abad] transition hover:scale-110 hover:text-white" />}
        </button>
        <div className="relative z-10 min-w-0 flex-1"><h2 className={`truncate font-semibold transition ${task.completed ? "text-[#77797c] line-through" : ""}`}>{task.title}</h2><p className="mt-1 text-xs text-[#85878a]">Prioridad {priorityLabel[task.priority]}{task.dueDate ? ` · ${task.dueDate}` : ""}</p></div>
        <Button aria-label={`Eliminar ${task.title}`} variant="ghost" size="icon" onClick={() => void remove(task)}><Trash2 className="size-4" /></Button>
      </ObsidianCard>
    );
  }

  return (
    <div>
      <div className="mb-8 flex items-end justify-between gap-4"><div><p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Enfoque claro</p><h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Pendientes</h1></div><Button onClick={() => setShowForm((value) => !value)}><Plus className="size-4" /> Nuevo</Button></div>
      {showForm && <ObsidianCard className="mb-6 p-5"><form onSubmit={submit} className="grid gap-3 md:grid-cols-[1fr_180px_auto]"><input aria-label="Título del pendiente" required maxLength={150} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="¿Qué necesitas hacer?" className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4" /><select aria-label="Prioridad" value={priority} onChange={(event) => setPriority(event.target.value as Task["priority"])} className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4"><option value="LOW">Prioridad baja</option><option value="MEDIUM">Prioridad media</option><option value="HIGH">Prioridad alta</option></select><Button type="submit">Crear</Button></form></ObsidianCard>}
      {tasks.length === 0 ? <ObsidianCard className="p-10 text-center text-[#999b9e]">No tienes pendientes. Disfruta el espacio.</ObsidianCard> : (
        <div className="space-y-8">
          <section aria-labelledby="pending-heading">
            <div className="mb-3 flex items-center justify-between"><h2 id="pending-heading" className="text-sm font-semibold uppercase tracking-[0.13em] text-[#8c8e91]">Por hacer</h2><span className="rounded-full border border-white/10 px-2.5 py-1 text-xs text-[#a9abad]">{pendingTasks.length}</span></div>
            {pendingTasks.length === 0 ? <ObsidianCard className="p-7 text-center text-sm text-[#85878a]">Todo despejado. Tus tareas terminadas están en Archivados.</ObsidianCard> : <div className="space-y-3">{pendingTasks.map(taskCard)}</div>}
          </section>
          {archivedTasks.length > 0 && (
            <section aria-labelledby="archive-heading">
              <button type="button" aria-expanded={archiveOpen} onClick={() => setArchiveOpen((value) => !value)} className="focus-ring mb-3 flex min-h-11 w-full items-center justify-between rounded-xl px-1 text-left">
                <span className="flex items-center gap-2"><Archive className="size-4 text-[#8c8e91]" /><span id="archive-heading" className="text-sm font-semibold uppercase tracking-[0.13em] text-[#8c8e91]">Archivados</span><span className="rounded-full border border-white/10 px-2 py-0.5 text-xs text-[#85878a]">{archivedTasks.length}</span></span>
                <ChevronDown className={`size-4 text-[#8c8e91] transition ${archiveOpen ? "rotate-180" : ""}`} />
              </button>
              {archiveOpen && <div className="space-y-3">{archivedTasks.map(taskCard)}</div>}
            </section>
          )}
        </div>
      )}
    </div>
  );
}
