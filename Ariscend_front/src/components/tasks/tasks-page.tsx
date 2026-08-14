"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Check, Circle, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { tasksApi } from "@/services/tasks-api";
import type { Task } from "@/types/api";

export function TasksPage() {
  const { selectedUser } = useSelectedUser();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState("");
  const [priority, setPriority] = useState<Task["priority"]>("MEDIUM");

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    tasksApi.list(selectedUser.id)
      .then((result) => { if (active) setTasks(result); })
      .catch((error) => { if (active) toast.error(error instanceof Error ? error.message : "No se pudieron cargar los pendientes."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [selectedUser]);

  async function submit(event: FormEvent) {
    event.preventDefault(); if (!selectedUser) return;
    try {
      const task = await tasksApi.create(selectedUser.id, { title, priority });
      setTasks((current) => [task, ...current]); setTitle(""); setShowForm(false);
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo crear el pendiente."); }
  }

  async function toggle(task: Task) {
    if (!selectedUser) return;
    try {
      const updated = await tasksApi.setCompleted(selectedUser.id, task.id, !task.completed);
      setTasks((current) => current.map((item) => item.id === task.id ? updated : item));
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo actualizar."); }
  }

  async function remove(task: Task) {
    if (!selectedUser || !window.confirm(`¿Eliminar “${task.title}”?`)) return;
    try { await tasksApi.delete(selectedUser.id, task.id); setTasks((current) => current.filter((item) => item.id !== task.id)); }
    catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo eliminar."); }
  }

  if (loading) return <PageLoading label="Cargando pendientes" />;

  const priorityLabel = { LOW: "Baja", MEDIUM: "Media", HIGH: "Alta" };
  return (
    <div>
      <div className="mb-8 flex items-end justify-between gap-4"><div><p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Enfoque claro</p><h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Pendientes</h1></div><Button onClick={() => setShowForm((value) => !value)}><Plus className="size-4" /> Nuevo</Button></div>
      {showForm && <ObsidianCard className="mb-6 p-5"><form onSubmit={submit} className="grid gap-3 md:grid-cols-[1fr_180px_auto]"><input required maxLength={150} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="¿Qué necesitas hacer?" className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4" /><select value={priority} onChange={(event) => setPriority(event.target.value as Task["priority"])} className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4"><option value="LOW">Prioridad baja</option><option value="MEDIUM">Prioridad media</option><option value="HIGH">Prioridad alta</option></select><Button type="submit">Crear</Button></form></ObsidianCard>}
      {tasks.length === 0 ? <ObsidianCard className="p-10 text-center text-[#999b9e]">No tienes pendientes. Disfruta el espacio.</ObsidianCard> : <div className="space-y-3">{tasks.map((task) => <ObsidianCard key={task.id} className={`flex items-center gap-4 p-4 md:p-5 ${task.completed ? "opacity-55" : ""}`}><button onClick={() => void toggle(task)} className="focus-ring relative z-10 rounded-full">{task.completed ? <Check className="size-7 rounded-full bg-white p-1.5 text-black" /> : <Circle className="size-7 text-[#66686b]" />}</button><div className="relative z-10 min-w-0 flex-1"><h2 className={`truncate font-semibold ${task.completed ? "line-through" : ""}`}>{task.title}</h2><p className="mt-1 text-xs text-[#818386]">Prioridad {priorityLabel[task.priority]}{task.dueDate ? ` · ${task.dueDate}` : ""}</p></div><Button variant="ghost" size="icon" onClick={() => void remove(task)}><Trash2 className="size-4" /></Button></ObsidianCard>)}</div>}
    </div>
  );
}
