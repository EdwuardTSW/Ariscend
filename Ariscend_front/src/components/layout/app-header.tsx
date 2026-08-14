"use client";

import Link from "next/link";
import { Settings, UserRound } from "lucide-react";
import { useSelectedUser } from "@/contexts/selected-user-context";

export function AppHeader() {
  const { selectedUser } = useSelectedUser();

  return (
    <header className="sticky top-0 z-40 border-b border-white/[0.06] bg-black/95 md:bg-black/85 md:backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-[1200px] items-center justify-between px-4 md:h-20 md:px-8">
        <Link href="/" className="focus-ring rounded-md text-2xl font-bold tracking-[-0.04em] md:text-4xl">
          Ariscend
        </Link>
        <div className="flex items-center gap-2">
          <span className="hidden text-sm text-[#a9abad] sm:inline">{selectedUser?.name}</span>
          <Link href="/ajustes" className="focus-ring flex size-11 items-center justify-center rounded-full text-[#a9abad] transition hover:bg-white/[0.06] hover:text-white" aria-label="Ajustes">
            <Settings className="size-5" />
          </Link>
          <Link href="/seleccionar-usuario" className="focus-ring flex size-11 items-center justify-center rounded-full text-[#a9abad] transition hover:bg-white/[0.06] hover:text-white" aria-label="Cambiar usuario">
            <UserRound className="size-5" />
          </Link>
        </div>
      </div>
    </header>
  );
}
