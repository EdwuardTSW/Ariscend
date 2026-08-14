import type { Habit, HabitCompletion } from "@/types/api";

export interface HabitHistory {
  habit: Habit;
  completions: HabitCompletion[];
}

function localDateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function calculateStreak(histories: HabitHistory[]) {
  const completedDates = new Set(
    histories.flatMap(({ completions }) => completions.map((item) => item.completedDate)),
  );
  const cursor = new Date();
  if (!completedDates.has(localDateKey(cursor))) cursor.setDate(cursor.getDate() - 1);

  let streak = 0;
  while (completedDates.has(localDateKey(cursor))) {
    streak += 1;
    cursor.setDate(cursor.getDate() - 1);
  }
  return streak;
}

export function calculateWeeklyConsistency(histories: HabitHistory[]) {
  const monday = new Date();
  const day = monday.getDay();
  monday.setHours(0, 0, 0, 0);
  monday.setDate(monday.getDate() - (day === 0 ? 6 : day - 1));
  const start = localDateKey(monday);

  const completions = histories.reduce(
    (total, history) =>
      total + history.completions.filter((item) => item.completedDate >= start).length,
    0,
  );
  const expected = histories.reduce(
    (total, history) => total + history.habit.targetDaysPerWeek,
    0,
  );
  return expected === 0 ? 0 : Math.min(100, Math.round((completions / expected) * 100));
}
