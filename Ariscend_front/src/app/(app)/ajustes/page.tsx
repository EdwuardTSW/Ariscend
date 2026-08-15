"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { KeyRound, LogOut, UserRound } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { useAuth } from "@/contexts/auth-context";
import { authApi } from "@/services/auth-api";

const passwordInputClass = "focus-ring mt-2 h-12 w-full rounded-xl border border-white/10 bg-black/70 px-4 text-white placeholder:text-white/25";

export default function SettingsPage() {
  const { selectedUser } = useSelectedUser();
  const { logout } = useAuth();
  const router = useRouter();
  const [loggingOut, setLoggingOut] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  async function signOut() {
    setLoggingOut(true);
    try {
      await logout();
      router.replace("/login");
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo cerrar la sesión.");
      router.replace("/login");
    } finally {
      setLoggingOut(false);
    }
  }

  async function changePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (newPassword.length < 12 || newPassword.length > 72) {
      toast.error("La nueva contraseña debe tener entre 12 y 72 caracteres.");
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error("La confirmación no coincide con la nueva contraseña.");
      return;
    }

    setChangingPassword(true);
    try {
      await authApi.changePassword({ currentPassword, newPassword });
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      toast.success("Contraseña actualizada.");
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo cambiar la contraseña.");
    } finally {
      setChangingPassword(false);
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <div className="mb-8">
        <p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Cuenta y preferencias</p>
        <h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Ajustes</h1>
      </div>

      <div className="space-y-5">
        <ObsidianCard className="p-6">
          <div className="relative z-10 flex items-center gap-4">
            <span className="flex size-12 items-center justify-center rounded-full border border-white/10"><UserRound className="size-5" /></span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-lg font-semibold">{selectedUser?.name}</p>
              <p className="truncate text-sm text-[#8c8e91]">{selectedUser?.email}</p>
            </div>
          </div>
        </ObsidianCard>

        <ObsidianCard className="p-6">
          <div className="relative z-10">
            <div className="flex items-start gap-4">
              <span className="flex size-12 shrink-0 items-center justify-center rounded-2xl border border-white/10"><KeyRound className="size-5" /></span>
              <div>
                <h2 className="text-lg font-semibold">Cambiar contraseña</h2>
                <p className="mt-1 text-sm leading-6 text-[#8c8e91]">Usa una contraseña única de entre 12 y 72 caracteres.</p>
              </div>
            </div>

            <form onSubmit={changePassword} className="mt-6 space-y-4">
              <label className="block text-sm font-medium text-[#c7c8ca]">Contraseña actual
                <input required type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} className={passwordInputClass} />
              </label>
              <label className="block text-sm font-medium text-[#c7c8ca]">Nueva contraseña
                <input required type="password" autoComplete="new-password" minLength={12} maxLength={72} value={newPassword} onChange={(event) => setNewPassword(event.target.value)} className={passwordInputClass} />
              </label>
              <label className="block text-sm font-medium text-[#c7c8ca]">Confirmar nueva contraseña
                <input required type="password" autoComplete="new-password" minLength={12} maxLength={72} value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} className={passwordInputClass} />
              </label>
              <Button type="submit" disabled={changingPassword}>{changingPassword ? "Actualizando..." : "Actualizar contraseña"}</Button>
            </form>
          </div>
        </ObsidianCard>

        <ObsidianCard className="p-6">
          <div className="relative z-10">
            <h2 className="text-lg font-semibold">Sesión</h2>
            <p className="mt-1 text-sm text-[#8c8e91]">Cierra tu sesión en este dispositivo.</p>
            <Button variant="secondary" onClick={() => void signOut()} disabled={loggingOut} className="mt-5"><LogOut className="size-4" /> {loggingOut ? "Cerrando sesión..." : "Cerrar sesión"}</Button>
          </div>
        </ObsidianCard>
      </div>
    </div>
  );
}
