import { apiRequest } from "@/services/api-client";
import type { Habit, HabitCompletion } from "@/types/api";

export const habitsApi = {
  list: (userId: number) =>
    apiRequest<Habit[]>(`/api/users/${userId}/habits`),
  completions: (userId: number, habitId: number) =>
    apiRequest<HabitCompletion[]>(
      `/api/users/${userId}/habits/${habitId}/completions`,
    ),
  allCompletions: (userId: number, signal?: AbortSignal) =>
    apiRequest<HabitCompletion[]>(`/api/users/${userId}/habits/completions`, { signal }),
  complete: (userId: number, habitId: number) =>
    apiRequest<HabitCompletion>(
      `/api/users/${userId}/habits/${habitId}/complete`,
      { method: "POST", body: "{}" },
    ),
  create: (
    userId: number,
    data: {
      name: string;
      description?: string;
      category?: string;
      frequency?: "DAILY" | "WEEKLY";
      targetDaysPerWeek?: number;
    },
  ) =>
    apiRequest<Habit>(`/api/users/${userId}/habits`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  deactivate: (userId: number, habitId: number) =>
    apiRequest<void>(`/api/users/${userId}/habits/${habitId}`, {
      method: "DELETE",
    }),
};
