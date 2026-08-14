"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { usersApi } from "@/services/users-api";
import type { User } from "@/types/api";

const STORAGE_KEY = "ariscend.selectedUserId";
const STORED_USER_KEY = "ariscend.selectedUser";

function readStoredUser(): User | null {
  try {
    const value = window.localStorage.getItem(STORED_USER_KEY);
    if (!value) return null;
    const user = JSON.parse(value) as Partial<User>;
    return typeof user.id === "number" && typeof user.name === "string" && typeof user.email === "string"
      ? user as User
      : null;
  } catch {
    return null;
  }
}

interface SelectedUserContextValue {
  users: User[];
  selectedUser: User | null;
  loading: boolean;
  error: string | null;
  selectUser: (user: User) => void;
  createUser: (data: { name: string; email: string }) => Promise<User>;
  clearUser: () => void;
  refresh: () => Promise<void>;
}

const SelectedUserContext = createContext<SelectedUserContextValue | null>(null);

export function SelectedUserProvider({ children }: { children: React.ReactNode }) {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      const result = await usersApi.list();
      setUsers(result);
      const storedId = window.localStorage.getItem(STORAGE_KEY);
      const storedUser = result.find((user) => String(user.id) === storedId) ?? null;
      setSelectedUser(storedUser);
      if (storedUser) window.localStorage.setItem(STORED_USER_KEY, JSON.stringify(storedUser));
      if (storedId && !storedUser) {
        window.localStorage.removeItem(STORAGE_KEY);
        window.localStorage.removeItem(STORED_USER_KEY);
      }
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "No se pudo conectar con el backend.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const cachedUser = readStoredUser();
    let active = true;
    if (cachedUser) {
      queueMicrotask(() => {
        if (!active) return;
        setSelectedUser(cachedUser);
        setLoading(false);
      });
    }
    const controller = new AbortController();
    usersApi.list(controller.signal)
      .then((result) => {
        if (!active) return;
        setUsers(result);
        const storedId = window.localStorage.getItem(STORAGE_KEY);
        const storedUser = result.find((user) => String(user.id) === storedId) ?? null;
        setSelectedUser(storedUser);
        if (storedUser) window.localStorage.setItem(STORED_USER_KEY, JSON.stringify(storedUser));
        if (storedId && !storedUser) {
          window.localStorage.removeItem(STORAGE_KEY);
          window.localStorage.removeItem(STORED_USER_KEY);
        }
      })
      .catch((requestError) => {
        if (active && !cachedUser) setError(requestError instanceof Error ? requestError.message : "No se pudo conectar con el backend.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; controller.abort(); };
  }, []);

  function selectUser(user: User) {
    window.localStorage.setItem(STORAGE_KEY, String(user.id));
    window.localStorage.setItem(STORED_USER_KEY, JSON.stringify(user));
    setSelectedUser(user);
  }

  async function createUser(data: { name: string; email: string }) {
    const user = await usersApi.create(data);
    setUsers((current) => [...current, user]);
    selectUser(user);
    return user;
  }

  function clearUser() {
    window.localStorage.removeItem(STORAGE_KEY);
    window.localStorage.removeItem(STORED_USER_KEY);
    setSelectedUser(null);
  }

  return (
    <SelectedUserContext.Provider
      value={{ users, selectedUser, loading, error, selectUser, createUser, clearUser, refresh }}
    >
      {children}
    </SelectedUserContext.Provider>
  );
}

export function useSelectedUser() {
  const context = useContext(SelectedUserContext);
  if (!context) throw new Error("useSelectedUser debe usarse dentro de SelectedUserProvider.");
  return context;
}
