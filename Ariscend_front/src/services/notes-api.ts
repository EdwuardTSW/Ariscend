import { apiRequest, toQueryString } from "@/services/api-client";
import type { Note, PagedResponse } from "@/types/api";

export const notesApi = {
  list: (
    userId: number,
    filters: { query?: string; pinned?: boolean; archived?: boolean; page?: number; size?: number } = {},
  ) =>
    apiRequest<PagedResponse<Note>>(
      `/api/users/${userId}/notes${toQueryString({ archived: false, page: 0, size: 30, ...filters })}`,
    ),
  get: (userId: number, noteId: number) =>
    apiRequest<Note>(`/api/users/${userId}/notes/${noteId}`),
  create: (userId: number, data: { title?: string; content?: string } = {}) =>
    apiRequest<Note>(`/api/users/${userId}/notes`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  update: (
    userId: number,
    noteId: number,
    data: { title: string | null; content: string },
  ) =>
    apiRequest<Note>(`/api/users/${userId}/notes/${noteId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  setPinned: (userId: number, noteId: number, pinned: boolean) =>
    apiRequest<Note>(`/api/users/${userId}/notes/${noteId}/pinned`, {
      method: "PATCH",
      body: JSON.stringify({ pinned }),
    }),
  setArchived: (userId: number, noteId: number, archived: boolean) =>
    apiRequest<Note>(`/api/users/${userId}/notes/${noteId}/archived`, {
      method: "PATCH",
      body: JSON.stringify({ archived }),
    }),
  delete: (userId: number, noteId: number) =>
    apiRequest<void>(`/api/users/${userId}/notes/${noteId}`, {
      method: "DELETE",
    }),
};
