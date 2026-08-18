"use client";

import * as Dialog from "@radix-ui/react-dialog";
import { createContext, useContext, useEffect, useState } from "react";
import { Flame, Sparkles, X } from "lucide-react";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { calculateStreak, calculateWeeklyConsistency, type HabitHistory } from "@/lib/dashboard-metrics";
import { habitsApi } from "@/services/habits-api";
import type { Habit } from "@/types/api";

interface HabitProgressValue {
  habits: Habit[];
  histories: HabitHistory[];
  loading: boolean;
  error: string | null;
  streak: number;
  consistency: number;
  refresh: () => Promise<void>;
  completeHabit: (habit: Habit) => Promise<boolean>;
}

const HabitProgressContext = createContext<HabitProgressValue | null>(null);

export function HabitProgressProvider({ children }: { children: React.ReactNode }) {
  const { selectedUser } = useSelectedUser();
  const [habits, setHabits] = useState<Habit[]>([]);
  const [histories, setHistories] = useState<HabitHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [celebrating, setCelebrating] = useState(false);

  async function refresh() {
    if (!selectedUser) return;
    setError(null);
    try {
      const [activeHabits, completions] = await Promise.all([
        habitsApi.list(selectedUser.id),
        habitsApi.allCompletions(selectedUser.id),
      ]);
      setHabits(activeHabits);
      setHistories(activeHabits.map((habit) => ({
        habit,
        completions: completions.filter((completion) => completion.habitId === habit.id),
      })));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "No se pudo cargar tu progreso.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    const userId = selectedUser.id;
    Promise.all([habitsApi.list(userId), habitsApi.allCompletions(userId)])
      .then(([activeHabits, completions]) => {
        if (!active) return;
        setHabits(activeHabits);
        setHistories(activeHabits.map((habit) => ({
          habit,
          completions: completions.filter((completion) => completion.habitId === habit.id),
        })));
      })
      .catch((requestError) => {
        if (active) setError(requestError instanceof Error ? requestError.message : "No se pudo cargar tu progreso.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [selectedUser]);

  async function completeHabit(habit: Habit) {
    if (!selectedUser || habit.completedToday) return false;
    const completion = await habitsApi.complete(selectedUser.id, habit.id);
    const completesDay = habits.length > 0 && habits.every((item) => item.id === habit.id || item.completedToday);
    setHabits((current) => current.map((item) => item.id === habit.id ? { ...item, completedToday: true } : item));
    setHistories((current) => current.map((history) => history.habit.id === habit.id
      ? {
          ...history,
          habit: { ...history.habit, completedToday: true },
          completions: [completion, ...history.completions],
        }
      : history));
    if (completesDay) setCelebrating(true);
    return completesDay;
  }

  const value: HabitProgressValue = {
    habits,
    histories,
    loading,
    error,
    streak: calculateStreak(histories),
    consistency: calculateWeeklyConsistency(histories),
    refresh,
    completeHabit,
  };

  return (
    <HabitProgressContext.Provider value={value}>
      {children}
      <Dialog.Root open={celebrating} onOpenChange={setCelebrating}>
        <Dialog.Portal>
          <Dialog.Overlay className="fixed inset-0 z-[70] bg-black/85 backdrop-blur-sm" />
          <Dialog.Content className="celebration-panel fixed inset-0 z-[80] m-auto h-fit max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-md overflow-y-auto rounded-[2rem] border border-white/15 bg-[#111111] p-7 text-center outline-none md:p-9">
            <Dialog.Close aria-label="Cerrar celebración" className="focus-ring absolute right-4 top-4 flex size-11 items-center justify-center rounded-full text-[#8c8e91] transition hover:bg-white/[0.07] hover:text-white"><X className="size-5" /></Dialog.Close>
            <div className="mx-auto flex size-20 items-center justify-center rounded-full border border-white/15 bg-white text-black">
              <Flame className="celebration-flame size-9" fill="currentColor" />
            </div>
            <Dialog.Title className="mt-7 text-3xl font-semibold tracking-[-0.04em]">Misión cumplida.</Dialog.Title>
            <Dialog.Description className="mt-4 text-base leading-7 text-[#b5b7b9]">
              Hoy no negociaste con tu objetivo. Cumpliste cada hábito, encendiste tu racha y avanzaste. Sigue así: la disciplina ya está hablando por ti.
            </Dialog.Description>
            <div className="mt-7 flex items-center justify-center gap-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]"><Sparkles className="size-4" /> Día conquistado</div>
          </Dialog.Content>
        </Dialog.Portal>
      </Dialog.Root>
    </HabitProgressContext.Provider>
  );
}

export function useHabitProgress() {
  const context = useContext(HabitProgressContext);
  if (!context) throw new Error("useHabitProgress debe usarse dentro de HabitProgressProvider.");
  return context;
}
