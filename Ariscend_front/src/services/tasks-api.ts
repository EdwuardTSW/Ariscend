import { apiRequest } from "@/services/api-client";
import type { Task } from "@/types/api";

interface TaskInput {
  title: string;
  description?: string | null;
  dueDate?: string | null;
  priority: "LOW" | "MEDIUM" | "HIGH";
}

export const tasksApi = {
  list: (userId: number) => apiRequest<Task[]>(`/api/users/${userId}/tasks`),
  create: (userId: number, data: TaskInput) =>
    apiRequest<Task>(`/api/users/${userId}/tasks`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  setCompleted: (userId: number, taskId: number, completed: boolean) =>
    apiRequest<Task>(`/api/users/${userId}/tasks/${taskId}/completed`, {
      method: "PATCH",
      body: JSON.stringify({ completed }),
    }),
  delete: (userId: number, taskId: number) =>
    apiRequest<void>(`/api/users/${userId}/tasks/${taskId}`, { method: "DELETE" }),
};
