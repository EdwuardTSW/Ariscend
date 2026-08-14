"use client";

import { useRouter } from "next/navigation";
import { LogOut, UserRound } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { useSelectedUser } from "@/contexts/selected-user-context";

export default function SettingsPage() {
  const { selectedUser, clearUser } = useSelectedUser();
  const router = useRouter();
  function changeUser() { clearUser(); router.push("/seleccionar-usuario"); }
  return <div className="mx-auto max-w-2xl"><div className="mb-8"><p className="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#8c8e91]">Preferencias locales</p><h1 className="text-3xl font-semibold tracking-[-0.04em] md:text-5xl">Ajustes</h1></div><ObsidianCard className="p-6"><div className="relative z-10 flex items-center gap-4"><span className="flex size-12 items-center justify-center rounded-full border border-white/10"><UserRound className="size-5" /></span><div className="min-w-0 flex-1"><p className="truncate text-lg font-semibold">{selectedUser?.name}</p><p className="truncate text-sm text-[#8c8e91]">{selectedUser?.email}</p></div></div><div className="relative z-10 mt-6 border-t border-white/[0.07] pt-5"><Button variant="secondary" onClick={changeUser}><LogOut className="size-4" /> Cambiar usuario</Button></div></ObsidianCard></div>;
}
