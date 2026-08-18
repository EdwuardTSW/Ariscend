"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Check, FilePlus2, Flame, RefreshCw, TrendingUp } from "lucide-react";
import { toast } from "sonner";
import { ProgressRing } from "@/components/dashboard/progress-ring";
import { PageLoading } from "@/components/feedback/page-loading";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { useHabitProgress } from "@/contexts/habit-progress-context";
import { notesApi } from "@/services/notes-api";
import type { Habit } from "@/types/api";

export function Dashboard() {
  const { selectedUser } = useSelectedUser();
  const { habits, loading, error, streak, consistency, refresh, completeHabit } = useHabitProgress();
  const router = useRouter();
  const [creatingNote, setCreatingNote] = useState(false);
  const [completingId, setCompletingId] = useState<number | null>(null);
  const [greeting, setGreeting] = useState("Hola");

  useEffect(() => {
    function updateGreeting() {
      const hour = new Date().getHours();
      setGreeting(hour < 12 ? "Buenos días" : hour < 19 ? "Buenas tardes" : "Buenas noches");
    }
    updateGreeting();
    const timer = window.setInterval(updateGreeting, 60_000);
    return () => window.clearInterval(timer);
  }, []);

  async function handleCompleteHabit(habit: Habit) {
    if (!selectedUser || habit.completedToday) return;
    setCompletingId(habit.id);
    try {
      const completedDay = await completeHabit(habit);
      if (!completedDay) toast.success(`${habit.name} completado.`);
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo completar el hábito.");
    } finally {
      setCompletingId(null);
    }
  }

  async function createQuickNote() {
    if (!selectedUser) return;
    setCreatingNote(true);
    try {
      const note = await notesApi.create(selectedUser.id);
      router.push(`/notas/${note.id}`);
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo crear la nota.");
      setCreatingNote(false);
    }
  }

  if (loading) return <PageLoading label="Calculando tu progreso" />;

  if (error) {
    return (
      <ObsidianCard className="flex min-h-64 flex-col items-center justify-center gap-5 p-8 text-center">
        <p className="max-w-md text-[#c8c8ca]">{error}</p>
        <Button variant="secondary" onClick={() => void refresh()}><RefreshCw className="size-4" /> Reintentar</Button>
      </ObsidianCard>
    );
  }

  const completed = habits.filter((habit) => habit.completedToday).length;
  const pending = habits.length - completed;
  const progress = habits.length === 0 ? 0 : Math.round((completed / habits.length) * 100);
  const firstName = selectedUser?.name.trim().split(/\s+/)[0] ?? "";

  return (
    <div>
      <section className="animate-enter mb-7 flex items-end justify-between gap-4 md:mb-8">
        <div>
          <h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">¡{greeting}, {firstName}!</h1>
          <p className="mt-2 text-[#a9abad] md:text-lg">Resumen de tu progreso general y acciones importantes.</p>
        </div>
        <Button onClick={() => void createQuickNote()} disabled={creatingNote} className="hidden md:inline-flex">
          <FilePlus2 className="size-4" /> {creatingNote ? "Creando..." : "Nota rápida"}
        </Button>
      </section>

      <section className="grid gap-5 lg:grid-cols-[1fr_255px]">
        <ObsidianCard className="p-6 md:p-8">
          <div className="relative z-10 flex flex-col items-center gap-7 md:flex-row md:gap-12">
            <ProgressRing value={progress} />
            <div className="w-full flex-1">
              <h2 className="text-2xl font-semibold tracking-[-0.025em]">{progress >= 75 ? "Rendimiento óptimo" : progress >= 40 ? "Buen progreso" : "Un paso a la vez"}</h2>
              <p className="mt-3 max-w-xl leading-7 text-[#a9abad]">
                {habits.length === 0
                  ? "Crea tu primer hábito y empieza a construir una rutina sostenible."
                  : "Cada acción cuenta. Mantén el ritmo para acercarte a tus objetivos semanales."}
              </p>
              <div className="mt-7 grid grid-cols-2 gap-3">
                <div className="rounded-xl border border-white/[0.07] bg-white/[0.025] p-4">
                  <p className="font-[var(--font-geist)] text-[11px] font-semibold uppercase tracking-[0.12em] text-[#8c8e91]">Hábitos activos</p>
                  <p className="mt-2 font-[var(--font-geist)] text-3xl font-semibold">{habits.length}</p>
                </div>
                <div className="rounded-xl border border-white/[0.07] bg-white/[0.025] p-4">
                  <p className="font-[var(--font-geist)] text-[11px] font-semibold uppercase tracking-[0.12em] text-[#8c8e91]">Por completar</p>
                  <p className="mt-2 font-[var(--font-geist)] text-3xl font-semibold">{pending}</p>
                </div>
              </div>
            </div>
          </div>
        </ObsidianCard>

        <div className="grid grid-cols-2 gap-4 lg:grid-cols-1">
          <ObsidianCard className="p-5 lg:p-6">
            <div className="relative z-10 flex h-full flex-col justify-between gap-5">
              <div className="flex items-center justify-between text-[#a9abad]"><span className="text-xs font-semibold uppercase tracking-[0.12em]">Racha actual</span><Flame className="size-5" /></div>
              <p className="text-3xl font-semibold tracking-[-0.04em] md:text-4xl">{streak} {streak === 1 ? "día" : "días"}</p>
            </div>
          </ObsidianCard>
          <ObsidianCard className="p-5 lg:p-6">
            <div className="relative z-10 flex h-full flex-col justify-between gap-5">
              <div className="flex items-center justify-between text-[#a9abad]"><span className="text-xs font-semibold uppercase tracking-[0.12em]">Consistencia</span><TrendingUp className="size-5" /></div>
              <div><p className="text-3xl font-semibold tracking-[-0.04em] md:text-4xl">{consistency}%</p><div className="mt-3 h-1.5 overflow-hidden rounded-full bg-white/10"><div className="h-full rounded-full bg-white transition-all" style={{ width: `${consistency}%` }} /></div></div>
            </div>
          </ObsidianCard>
        </div>
      </section>

      <section className="mt-7">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold md:text-2xl">Acciones rápidas</h2>
          <Button onClick={() => void createQuickNote()} disabled={creatingNote} variant="secondary" size="small" className="md:hidden">
            <FilePlus2 className="size-4" /> Nota
          </Button>
        </div>
        {habits.length === 0 ? (
          <ObsidianCard className="p-6 text-[#a9abad]">Todavía no tienes hábitos activos.</ObsidianCard>
        ) : (
          <div className="grid gap-3 md:grid-cols-3">
            {habits.slice(0, 3).map((habit, index) => (
              <button
                key={habit.id}
                style={{ animationDelay: `${index * 60}ms` }}
                 onClick={() => void handleCompleteHabit(habit)}
                disabled={habit.completedToday || completingId === habit.id}
                className={`obsidian-card focus-ring group flex min-h-24 items-center justify-between rounded-2xl p-5 text-left transition-all duration-300 ${habit.completedToday ? "border-white/10 bg-[#080808] text-[#85878a]" : "hover:bg-[#181818]"}`}
              >
                <span className="relative z-10 flex min-w-0 items-center gap-3"><span className="text-xl">{habit.icon || "✦"}</span><span className={habit.completedToday ? "truncate line-through" : "truncate font-semibold"}>{habit.name}</span></span>
                <span className={`relative z-10 flex size-9 items-center justify-center rounded-full border transition ${habit.completedToday ? "border-white bg-white text-black" : "border-white/15 text-[#8c8e91] group-hover:border-white group-hover:text-white"}`}>
                  <Check className={`size-4 ${habit.completedToday ? "animate-check-pop" : ""}`} />
                </span>
              </button>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
