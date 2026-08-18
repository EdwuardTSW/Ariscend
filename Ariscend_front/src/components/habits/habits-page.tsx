"use client";

import { useState, type FormEvent } from "react";
import { Check, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { PageError } from "@/components/feedback/page-error";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { useHabitProgress } from "@/contexts/habit-progress-context";
import { habitsApi } from "@/services/habits-api";
import type { Habit } from "@/types/api";

const defaultCategories = ["Salud", "Bienestar", "Productividad", "Estudio", "Finanzas", "Creatividad"];
const emojiOptions = ["💪", "🏃", "🧠", "📚", "💧", "🥗", "🧘", "💰"];

export function HabitsPage() {
  const { selectedUser } = useSelectedUser();
  const { habits, loading, error, refresh, completeHabit } = useHabitProgress();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState("");
  const [category, setCategory] = useState("");
  const [customCategory, setCustomCategory] = useState("");
  const [emoji, setEmoji] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!selectedUser) return;
    setSubmitting(true);
    try {
      const selectedCategory = category === "__custom" ? customCategory.trim() : category;
      await habitsApi.create(selectedUser.id, { name, category: selectedCategory || undefined, icon: emoji.trim() || undefined });
      setName(""); setCategory(""); setCustomCategory(""); setEmoji(""); setShowForm(false);
      await refresh();
      toast.success("Hábito creado.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "No se pudo crear el hábito.");
    } finally { setSubmitting(false); }
  }

  async function complete(habit: Habit) {
    if (!selectedUser || habit.completedToday) return;
    try {
      await completeHabit(habit);
      toast.success("Hábito completado.");
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo completar."); }
  }

  async function deactivate(habit: Habit) {
    if (!selectedUser || !window.confirm(`¿Desactivar “${habit.name}”?`)) return;
    try {
      await habitsApi.deactivate(selectedUser.id, habit.id);
      await refresh();
    } catch (error) { toast.error(error instanceof Error ? error.message : "No se pudo desactivar."); }
  }

  if (loading) return <PageLoading label="Cargando tus hábitos" />;
  if (error) return <PageError message={error} onRetry={() => void refresh()} />;

  const categories = [...new Set([...defaultCategories, ...habits.map((habit) => habit.category).filter((item): item is string => Boolean(item))])];

  return (
    <div>
      <div className="mb-8 flex items-end justify-between gap-4">
        <div><p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Constancia diaria</p><h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Hábitos</h1></div>
        <Button onClick={() => setShowForm((value) => !value)}><Plus className="size-4" /> Nuevo hábito</Button>
      </div>

      {showForm && (
        <ObsidianCard className="mb-6 p-5 md:p-6">
          <form onSubmit={submit} className="grid gap-4 md:grid-cols-[96px_1fr_240px_auto]">
            <div>
              <label className="text-xs text-[#8c8e91]" htmlFor="habit-emoji">Emoji</label>
              <input id="habit-emoji" aria-label="Emoji del hábito" maxLength={12} value={emoji} onChange={(event) => setEmoji(event.target.value)} placeholder="✨" className="focus-ring mt-1 h-11 w-full rounded-xl border border-white/10 bg-black px-3 text-center text-xl" />
            </div>
            <div>
              <label className="text-xs text-[#8c8e91]" htmlFor="habit-name">Hábito</label>
              <input id="habit-name" required maxLength={120} value={name} onChange={(event) => setName(event.target.value)} placeholder="Leer 20 minutos" className="focus-ring mt-1 h-11 w-full rounded-xl border border-white/10 bg-black px-4" />
            </div>
            <div>
              <label className="text-xs text-[#8c8e91]" htmlFor="habit-category">Categoría</label>
              <select id="habit-category" value={category} onChange={(event) => setCategory(event.target.value)} className="focus-ring mt-1 h-11 w-full rounded-xl border border-white/10 bg-black px-4">
                <option value="">Sin categoría</option>
                {categories.map((item) => <option key={item} value={item}>{item}</option>)}
                <option value="__custom">Nueva categoría...</option>
              </select>
            </div>
            <Button type="submit" disabled={submitting} className="self-end">{submitting ? "Guardando..." : "Crear"}</Button>
            {category === "__custom" && <input aria-label="Nueva categoría" required maxLength={80} value={customCategory} onChange={(event) => setCustomCategory(event.target.value)} placeholder="Nombre de la nueva categoría" className="focus-ring h-11 rounded-xl border border-white/10 bg-black px-4 md:col-start-3" />}
            <div className="flex flex-wrap gap-2 md:col-span-4">
              {emojiOptions.map((item) => <button key={item} type="button" aria-label={`Usar ${item}`} onClick={() => setEmoji(item)} className={`focus-ring flex size-10 items-center justify-center rounded-full border text-lg transition ${emoji === item ? "border-white bg-white text-black" : "border-white/10 bg-black hover:border-white/30"}`}>{item}</button>)}
            </div>
          </form>
        </ObsidianCard>
      )}

      {habits.length === 0 ? (
        <ObsidianCard className="p-10 text-center text-[#999b9e]">Todavía no tienes hábitos activos.</ObsidianCard>
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {habits.map((habit) => (
            <ObsidianCard key={habit.id} className={`flex items-center justify-between gap-4 p-5 transition-all duration-300 ${habit.completedToday ? "border-white/10 bg-[#080808] opacity-75" : "hover:border-white/20"}`}>
              <div className="relative z-10 flex min-w-0 items-center gap-3"><span className="flex size-11 shrink-0 items-center justify-center rounded-2xl border border-white/10 bg-black text-xl">{habit.icon || "✦"}</span><div className="min-w-0"><h2 className={`truncate text-lg font-semibold transition ${habit.completedToday ? "text-[#85878a] line-through" : ""}`}>{habit.name}</h2><p className="mt-1 text-sm text-[#85878a]">{habit.category || "Sin categoría"} · {habit.targetDaysPerWeek} días/semana</p></div></div>
              <div className="relative z-10 flex gap-1">
                <Button size="icon" variant={habit.completedToday ? "primary" : "secondary"} disabled={habit.completedToday} onClick={() => void complete(habit)} aria-label={`Completar ${habit.name}`} aria-pressed={habit.completedToday}><Check className={`size-4 ${habit.completedToday ? "animate-check-pop" : ""}`} /></Button>
                <Button size="icon" variant="ghost" onClick={() => void deactivate(habit)} aria-label={`Desactivar ${habit.name}`}><Trash2 className="size-4" /></Button>
              </div>
            </ObsidianCard>
          ))}
        </div>
      )}
    </div>
  );
}
