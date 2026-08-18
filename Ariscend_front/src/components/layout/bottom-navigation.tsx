"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { CircleCheckBig, FilePlus2, FileText, House, ListChecks, LoaderCircle, WalletCards } from "lucide-react";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { notesApi } from "@/services/notes-api";

const items = [
  { href: "/", label: "Inicio", icon: House },
  { href: "/habitos", label: "Hábitos", icon: CircleCheckBig },
  { href: "/pendientes", label: "Pendientes", icon: ListChecks },
  { href: "/finanzas", label: "Finanzas", icon: WalletCards },
  { href: "/notas", label: "Notas", icon: FileText },
];

export function BottomNavigation() {
  const pathname = usePathname();
  const router = useRouter();
  const { selectedUser } = useSelectedUser();
  const [creatingNote, setCreatingNote] = useState(false);

  async function createNote() {
    if (!selectedUser || creatingNote) return;
    setCreatingNote(true);
    try {
      const note = await notesApi.create(selectedUser.id);
      router.push(`/notas/${note.id}`);
      setCreatingNote(false);
    } catch (requestError) {
      toast.error(requestError instanceof Error ? requestError.message : "No se pudo crear la nota.");
      setCreatingNote(false);
    }
  }

  return (
    <nav aria-label="Navegación principal" className="glass-panel fixed bottom-[calc(1rem+env(safe-area-inset-bottom))] left-1/2 z-50 flex w-[calc(100%-32px)] max-w-[560px] -translate-x-1/2 items-center justify-around rounded-[2rem] px-2 pb-2.5 pt-8 md:bottom-6 md:w-auto md:gap-7 md:rounded-full md:px-9 md:py-2.5">
      <button type="button" aria-label="Crear nueva nota" aria-busy={creatingNote} disabled={creatingNote} onClick={() => void createNote()} className="focus-ring absolute left-1/2 top-0 flex size-14 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full border-4 border-black bg-white text-black transition hover:scale-105 disabled:opacity-70 md:hidden">
        {creatingNote ? <LoaderCircle className="size-5 animate-spin" /> : <FilePlus2 className="size-6" />}
      </button>
      {items.map((item) => {
        const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
        const Icon = item.icon;
        return (
          <Link
            key={item.href}
            href={item.href}
            aria-current={active ? "page" : undefined}
            prefetch={item.href === "/finanzas" ? false : undefined}
            className={cn(
              "focus-ring relative flex min-w-12 flex-col items-center gap-1 rounded-lg px-1.5 py-0.5 text-[10px] font-medium transition md:min-w-16 md:text-[11px]",
              active ? "text-white" : "text-[#a9abad] hover:text-white",
            )}
          >
            <Icon className="size-5 md:size-6" strokeWidth={active ? 2.4 : 1.8} />
            <span>{item.label}</span>
            {active && <span className="absolute -bottom-1.5 size-1 rounded-full bg-white" />}
          </Link>
        );
      })}
    </nav>
  );
}
