"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Check, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { PageError } from "@/components/feedback/page-error";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { habitsApi } from "@/services/habits-api";
import type { Habit } from "@/types/api";

export function HabitsPage() {
  const { selectedUser } = useSelectedUser();
  const [habits, setHabits] = useState<Habit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState("");
  const [category, setCategory] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function load(userId: number) {
    setError(null);
    try {
      setHabits(await habitsApi.list(userId));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar los hábitos.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    habitsApi.list(selectedUser.id)
      .then((result) => { if (active) setHabits(result); })
      .catch((requestError) => { if (active) setError(requestError instanceof Error ? requestError.message : "No se pudieron cargar los hábitos."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reloadKey, selectedUser]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!selectedUser) return;
    setSubmitting(true);
    try {
      await habitsApi.create(selectedUser.id, { name, category: category || undefined });
      setName(""); setCategory(""); setShowForm(false);
      await load(selectedUser.id);
      toast.success("Hábito creado.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "No se pudo crear el hábito.");
    } finally { setSubmitting(false); }
  }

  async function complete(habit: Habit) {
    if (!selectedUser || habit.completedToday) return;
    try {
      await habitsApi.complete(selectedUser.id, habit.id);
      setHabits((current) => current.map((item) => item.id === habit.id ? { ...item, completedToday: true } : item));
      toast.success("Hábito completado.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo completar."); }
  }

  async function deactivate(habit: Habit) {
    if (!selectedUser || !window.confirm(`¿Desactivar “${habit.name}”?`)) return;
    try {
      await habitsApi.deactivate(selectedUser.id, habit.id);
      setHabits((current) => current.filter((item) => item.id !== habit.id));
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo desactivar."); }
  }

  if (loading) return <PageLoading label="Cargando tus hábitos" />;
  if (error) return <PageError message={error} onRetry={() => { setError(null); setLoading(true); setReloadKey((value) => value + 1); }} />;

  return (
    <div>
      <div className="mb-8 flex items-end justify-between gap-4">
        <div><p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Constancia diaria</p><h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Hábitos</h1></div>
        <Button onClick={() => setShowForm((value) => !value)}><Plus className="size-4" /> Nuevo hábito</Button>
      </div>

      {showForm && (
        <ObsidianCard className="mb-6 p-5 md:p-6">
          <form onSubmit={submit} className="grid gap-3 md:grid-cols-[1fr_240px_auto]">
            <input aria-label="Nombre del hábito" required maxLength={120} value={name} onChange={(event) => setName(event.target.value)} placeholder="Nombre del hábito" className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4" />
            <input aria-label="Categoría del hábito" maxLength={80} value={category} onChange={(event) => setCategory(event.target.value)} placeholder="Categoría opcional" className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4" />
            <Button type="submit" disabled={submitting}>{submitting ? "Guardando..." : "Crear"}</Button>
          </form>
        </ObsidianCard>
      )}

      {habits.length === 0 ? (
        <ObsidianCard className="p-10 text-center text-[#999b9e]">Todavía no tienes hábitos activos.</ObsidianCard>
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {habits.map((habit) => (
            <ObsidianCard key={habit.id} className="flex items-center justify-between gap-4 p-5">
              <div className="relative z-10 min-w-0"><h2 className="truncate text-lg font-semibold">{habit.name}</h2><p className="mt-1 text-sm text-[#85878a]">{habit.category || "Sin categoría"} · {habit.targetDaysPerWeek} días/semana</p></div>
              <div className="relative z-10 flex gap-1">
                <Button size="icon" variant={habit.completedToday ? "primary" : "secondary"} disabled={habit.completedToday} onClick={() => void complete(habit)} aria-label={`Completar ${habit.name}`}><Check className="size-4" /></Button>
                <Button size="icon" variant="ghost" onClick={() => void deactivate(habit)} aria-label={`Desactivar ${habit.name}`}><Trash2 className="size-4" /></Button>
              </div>
            </ObsidianCard>
          ))}
        </div>
      )}
    </div>
  );
}
