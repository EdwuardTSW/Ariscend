"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, Plus, RefreshCw, UserRound } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { useSelectedUser } from "@/contexts/selected-user-context";
import type { User } from "@/types/api";

export default function SelectUserPage() {
  const { users, loading, error, selectUser, createUser, refresh } = useSelectedUser();
  const router = useRouter();
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");

  function destination() {
    const next = new URLSearchParams(window.location.search).get("next");
    return next?.startsWith("/") && !next.startsWith("//") ? next : "/";
  }

  function choose(user: User) {
    selectUser(user);
    router.push(destination());
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    try {
      await createUser({ name, email });
      toast.success("Tu espacio está listo.");
      router.push(destination());
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo crear el usuario.");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <PageLoading label="Buscando tus espacios" />;

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-5xl flex-col justify-center px-4 py-12 md:px-8">
      <div className="animate-enter">
        <p className="mb-8 text-3xl font-bold tracking-[-0.05em] md:text-5xl">Ariscend</p>
        <div className="mb-9 max-w-2xl">
          <p className="mb-3 font-[var(--font-geist)] text-xs font-semibold uppercase tracking-[0.18em] text-[#8c8e91]">
            Selección local
          </p>
          <h1 className="text-3xl font-semibold tracking-[-0.035em] md:text-5xl">¿En qué espacio trabajarás hoy?</h1>
          <p className="mt-4 text-base leading-7 text-[#a9abad] md:text-lg">
            Esta selección reemplaza temporalmente el inicio de sesión mientras preparamos la autenticación real.
          </p>
        </div>

        {error && (
          <ObsidianCard className="mb-6 flex items-center justify-between gap-4 border-red-300/20 p-4 text-red-200">
            <span>{error}</span>
            <Button variant="secondary" size="small" onClick={() => void refresh()}>
              <RefreshCw className="size-4" /> Reintentar
            </Button>
          </ObsidianCard>
        )}

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {users.map((user, index) => (
            <button
              key={user.id}
              style={{ animationDelay: `${index * 50}ms` }}
              onClick={() => choose(user)}
              className="obsidian-card focus-ring group rounded-2xl p-5 text-left transition hover:-translate-y-0.5 hover:bg-[#181818]"
            >
              <div className="mb-7 flex items-center justify-between">
                <span className="flex size-11 items-center justify-center rounded-full border border-white/10 bg-white/[0.04]">
                  <UserRound className="size-5" />
                </span>
                <ArrowRight className="size-5 text-[#737579] transition group-hover:translate-x-1 group-hover:text-white" />
              </div>
              <p className="text-xl font-semibold">{user.name}</p>
              <p className="mt-1 truncate text-sm text-[#8c8e91]">{user.email}</p>
            </button>
          ))}

          {!error && (
            <button
              onClick={() => setShowForm(true)}
              className="focus-ring flex min-h-40 flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-white/15 text-[#a9abad] transition hover:border-white/35 hover:bg-white/[0.03] hover:text-white"
            >
              <Plus className="size-6" />
              <span className="font-semibold">Crear otro espacio</span>
            </button>
          )}
        </div>

        {(showForm || users.length === 0) && !error && (
          <ObsidianCard className="mt-6 max-w-xl p-6 md:p-8">
            <h2 className="text-xl font-semibold">Nuevo usuario</h2>
            <form onSubmit={submit} className="mt-6 space-y-4">
              <label className="block text-sm text-[#a9abad]">
                Nombre
                <input
                  required
                  maxLength={100}
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  className="focus-ring mt-2 h-12 w-full rounded-xl border border-white/10 bg-black px-4 text-white placeholder:text-white/25"
                  placeholder="Tu nombre"
                />
              </label>
              <label className="block text-sm text-[#a9abad]">
                Correo
                <input
                  required
                  type="email"
                  maxLength={254}
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  className="focus-ring mt-2 h-12 w-full rounded-xl border border-white/10 bg-black px-4 text-white placeholder:text-white/25"
                  placeholder="correo@ejemplo.com"
                />
              </label>
              <div className="flex justify-end gap-3 pt-2">
                {users.length > 0 && <Button type="button" variant="ghost" onClick={() => setShowForm(false)}>Cancelar</Button>}
                <Button type="submit" disabled={submitting}>{submitting ? "Creando..." : "Crear y continuar"}</Button>
              </div>
            </form>
          </ObsidianCard>
        )}
      </div>
    </main>
  );
}
